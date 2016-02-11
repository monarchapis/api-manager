package com.monarchapis.apimanager.service.loadbalancing

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import com.monarchapis.apimanager.model._
import com.monarchapis.apimanager.security.AuthenticationRequest
import com.monarchapis.apimanager.service._

import grizzled.slf4j.Logging

class LoadBalancerChain(loadBalancers: java.util.List[LoadBalancer]) extends LoadBalancer with Logging {
  val list = loadBalancers.asScala.toList

  def getTarget(service: Service, request: AuthenticationRequest, claims: Map[String, Any]): Option[String] = {
    for (lb <- list) {
      val target = lb.getTarget(service, request, claims)

      if (target.isDefined) {
        debug(s"Using target ${target.get}")
        return target
      }
    }

    return None
  }
}