package com.monarchapis.apimanager.service.loadbalancing

import scala.beans.BeanProperty
import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.service._
import grizzled.slf4j.Logging
import java.security.SecureRandom
import com.monarchapis.apimanager.security.AuthenticationRequest

class UpstreamLoadBalancer extends LoadBalancer with Logging {
  @BeanProperty var prefix = ""
  @BeanProperty var suffix = ""

  private val random = new SecureRandom

  def getTarget(service: Service, request: AuthenticationRequest, claims: Map[String, Any]): Option[String] = {
    if (service.requestWeights.size == 0) {
      Some(prefix + service.name + suffix)
    } else {
      Some(getRandomWeightedTag(service))
    }
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
}