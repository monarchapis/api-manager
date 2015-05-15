package com.monarchapis.apimanager.service.loadbalancing

import java.security.SecureRandom

import scala.collection.JavaConversions._
import scala.collection.mutable.Buffer
import scala.collection.mutable.HashMap
import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTime

import com.ecwid.consul.v1.ConsistencyMode
import com.ecwid.consul.v1.ConsulClient
import com.ecwid.consul.v1.QueryParams
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._

import grizzled.slf4j.Logging
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

class ConsulLoadBalancer(client: ConsulClient) extends LoadBalancer with Runnable with Logging {
  var index: Option[Long] = None
  var running = false
  var targets: Map[String, ServiceRegistration] = Map.empty[String, ServiceRegistration]
  var thread: Thread = null

  val counterMap = HashMap[String, RoundRobinCounter]()
  val tagPartitionsCache = HashMap[String, TagPartitions]()

  val random = new SecureRandom

  @PostConstruct
  def startPolling {
    if (!running) {
      running = true
      var thread = new Thread(this)
      thread.setDaemon(true)
      thread.start
      this.thread = thread;
    }
  }

  @PreDestroy
  def stopPolling {
    running = false
  }

  def getTarget(service: Service): Option[String] = {
    targets.get(service.name) match {
      case Some(registration) => {
        val target = if (service.requestWeights.size == 0) {
          val counter = registration.getCounter("default")
          val instances = registration.instances
          if (instances.size > 0) {
            Some(instances(counter.next(instances.size)))
          } else {
            None
          }
        } else {
          val tagPartitions = getTagPartitions(service, registration)
          val tag = getRandomWeightedTag(service)

          val resolvedTag = if (tagPartitions.contains(tag)) tag else "default"
          val buffer = tagPartitions(resolvedTag)

          if (buffer.size > 0) {
            val counter = registration.getCounter(resolvedTag)
            Some(buffer(counter.next(buffer.size)))
          } else {
            val buffer = tagPartitions("default")

            if (buffer.size > 0) {
              val counter = registration.getCounter("default")
              Some(buffer(counter.next(buffer.size)))
            } else {
              None
            }
          }
        }

        debug(s"Returning target $target")

        target match {
          case Some(target) => Some(target.host + ':' + target.port)
          case None => None
        }
      }
      case None => None
    }
  }

  protected def getTagPartitions(service: Service, registration: ServiceRegistration): HashMap[String, Buffer[ServiceInstance]] = {
    tagPartitionsCache.get(service.name) match {
      case Some(entry) => {
        if (service.modifiedDate.isEqual(entry.lastUpdated)) {
          return entry.tagPartitions
        }
      }
      case None =>
    }

    val keySet = service.requestWeights.keySet
    val tagPartitions = HashMap[String, scala.collection.mutable.Buffer[ServiceInstance]](
      "default" -> scala.collection.mutable.Buffer[ServiceInstance]())

    for (instance <- registration.instances) {
      var found = false

      breakable {
        for (tag <- instance.tags) {
          if (keySet.contains(tag)) {
            val list = tagPartitions.get(tag) match {
              case Some(list) => list
              case None => {
                val list = scala.collection.mutable.Buffer[ServiceInstance]()
                tagPartitions += tag -> list
                list
              }
            }

            list += instance
            found = true
            break
          }
        }
      }

      if (!found) {
        tagPartitions("default") += instance
      }
    }

    tagPartitionsCache += service.name -> TagPartitions(service.modifiedDate, tagPartitions)

    tagPartitions
  }

  protected def getRandomWeightedTag(service: Service) = {
    val sum = service.requestWeights.values.foldLeft(0)(_ + _)
    val tagNext = random.nextInt(sum)
    var start = 0
    service.requestWeights.find(weight => {
      var end = start + weight._2
      val result = start <= tagNext && tagNext < end
      start = end
      result
    }).get._1
  }

  def run {
    while (running) {
      try {
        val queryParams = index match {
          case Some(index) => new QueryParams(60000 * 5, index)
          case _ => new QueryParams(ConsistencyMode.DEFAULT)
        }

        if (index.isEmpty) {
          info("Querying services from the Consul catalog")
        }

        val response = client.getCatalogServices(queryParams)

        val previousIndex = index.getOrElse(-1)
        index = Some(response.getConsulIndex)

        val services = response.getValue();

        if (services != null && previousIndex != response.getConsulIndex) {
          if (previousIndex != -1) {
            info("Detected update in the Consul catalog")
          }

          val targetBuilder = Map.newBuilder[String, ServiceRegistration]

          services.keySet.foreach(serviceName => {
            debug(s"Getting endpoints for $serviceName")

            val builder = List.newBuilder[ServiceInstance]
            val response = client.getCatalogService(serviceName, new QueryParams(ConsistencyMode.DEFAULT))
            val serviceCatalog = response.getValue

            serviceCatalog.foreach(service => {
              val address = if (StringUtils.isNotBlank(service.getServiceAddress)) {
                service.getServiceAddress
              } else {
                service.getAddress
              }

              val instance = ServiceInstance(
                id = service.getServiceId(),
                host = address,
                port = service.getServicePort,
                tags = if (service.getServiceTags() != null) {
                  service.getServiceTags().toSet
                } else {
                  Set.empty[String]
                })
              debug(s"Found $instance")
              builder += instance
            })

            targetBuilder += serviceName -> ServiceRegistration(builder.result)
          })

          targets = targetBuilder.result
          tagPartitionsCache.clear
        }
      } catch {
        case e: Exception => {
          warn("Error encountered listening for catalog changes", e);
          Thread.sleep(60000)
        }
      }
    }
  }
}

case class TagPartitions(
  val lastUpdated: DateTime,
  val tagPartitions: HashMap[String, Buffer[ServiceInstance]])

case class ServiceRegistration(
  val instances: List[ServiceInstance],
  val counters: HashMap[String, RoundRobinCounter] = HashMap()) {

  def getCounter(tag: String) = {
    counters.get(tag) match {
      case Some(counter) => counter
      case None => {
        val counter = new RoundRobinCounter
        counters += tag -> counter

        counter
      }
    }
  }
}

case class ServiceInstance(
  val id: String,
  val host: String,
  val port: Int,
  val tags: Set[String]) {
  override def toString = {
    val tagString = if (!tags.isEmpty) {
      val sb = new StringBuffer

      for (tag <- tags) {
        if (sb.length > 0) {
          sb.append(", ")
        }
        sb.append(tag)
      }

      " [" + sb.toString + "]"
    } else { "" }

    s"$id @ $host:$port$tagString"
  }
}

class RoundRobinCounter {
  private var counter: Int = 0

  def next(upperBoundary: Int) = {
    var i = counter % upperBoundary
    counter += 1
    i
  }
}