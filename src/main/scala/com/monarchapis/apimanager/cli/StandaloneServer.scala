/*
 * Copyright (C) 2015 CapTech Ventures, Inc.
 * (http://www.captechconsulting.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.monarchapis.apimanager.cli

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import java.security.AccessControlException
import java.util.EnumSet
import java.util.Properties

import scala.util.control.Breaks.break
import scala.util.control.Breaks.breakable

import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.eclipse.jetty.ajp.Ajp13SocketConnector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.glassfish.jersey.servlet.ServletContainer
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.request.RequestContextListener
import org.springframework.web.util.IntrospectorCleanupListener
import org.springframework.web.util.Log4jConfigListener

import com.monarchapis.apimanager.servlet.ApiFilter
import com.monarchapis.apimanager.servlet.CorsFilter
import com.monarchapis.apimanager.servlet.ServerContextListener
import com.monarchapis.apimanager.servlet.SystemPropertiesListener

import grizzled.slf4j.Logging
import javax.servlet.DispatcherType

object StandaloneServer {
  private val VERSION = "0.8.1"
  private val SHUTDOWN_COMMAND = "SHUTDOWN"

  def main(args: Array[String]) {
    val server = new StandaloneServer
    server.run
  }
}

class StandaloneServer extends Runnable with Logging {
  import StandaloneServer._

  private var server: Server = null
  private var mainThread: Thread = null
  private var shutdownHook: ServerShutdownHook = null

  private var home: String = null
  private var config: String = null
  private var settings: Properties = null

  private var awaitSocket: ServerSocket = null
  private var stopAwait = false

  def run {
    start
  }

  def start {
    println(s"Starting API Manager...")

    load

    server = new Server()

    val context = new ServletContextHandler(server, "/", ServletContextHandler.NO_SESSIONS)
    server.setHandler(context)

    if ((new File("src/main/webapp")).exists) {
      context.setResourceBase("src/main/webapp")
    } else {
      context.setResourceBase(home + "/webapp")
    }

    context.setDisplayName("Monarch API Manager")
    context.setInitParameter("webAppRootKey", "api-manager.root")
    context.setInitParameter("contextConfigLocation", "file:${monarch.config}/application-context.xml")
    context.setInitParameter("log4jConfigLocation", "file:${monarch.config}/log4j.properties")

    context.addEventListener(new SystemPropertiesListener)
    context.addEventListener(new Log4jConfigListener)
    context.addEventListener(new IntrospectorCleanupListener)
    context.addEventListener(new ContextLoaderListener)
    context.addEventListener(new RequestContextListener)
    context.addEventListener(new ServerContextListener)

    context.addFilter(classOf[CorsFilter], s"/*", EnumSet.of(DispatcherType.REQUEST))

    addRestApi(context, "open", false)
    addRestApi(context, "management")
    addRestApi(context, "service")
    addRestApi(context, "analytics")
    addRestApi(context, "command")

    context.addServlet(classOf[DefaultServlet], "/*")

    val httpEnabled = getSetting("server.http.enabled", "false").toBoolean
    val httpsEnabled = getSetting("server.https.enabled", "false").toBoolean
    val ajpEnabled = getSetting("server.ajp.enabled", "false").toBoolean

    if (httpEnabled) {
      val port = getSetting("server.http.port", "8000").toInt

      val connector = new SelectChannelConnector
      connector.setPort(port)
      server.addConnector(connector)

      println(s"HTTP listening on $port")
    }

    if (httpsEnabled) {
      val port = getSetting("server.https.port", "8443").toInt

      val keyStorePath = {
        val path = getSetting("server.keyStore.path", null)

        if (path == null) {
          println("Error: keyStorePath is not set")
          sys.exit(-1)
        }

        if (path.startsWith("/")) path else s"$config/$path"
      }

      val keyStorePassword = getSetting("server.keyStore.password", null)

      val sslConnector = new SslSelectChannelConnector()
      sslConnector.setPort(port)
      sslConnector.setMaxIdleTime(30000)

      val cf = sslConnector.getSslContextFactory()

      cf.setKeyStorePath(keyStorePath)

      if (keyStorePassword != null) {
        cf.setKeyStorePassword(keyStorePassword)
      }

      cf.setExcludeCipherSuites(
        "SSL_RSA_WITH_DES_CBC_SHA",
        "SSL_DHE_RSA_WITH_DES_CBC_SHA",
        "SSL_DHE_DSS_WITH_DES_CBC_SHA",
        "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
        "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
        "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
        "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA")

      server.addConnector(sslConnector);

      println(s"HTTPS listening on $port")
    }

    if (ajpEnabled) {
      val port = getSetting("server.ajp.port", "8009").toInt
      val ajpConnector = new Ajp13SocketConnector
      ajpConnector.setPort(port.toInt)
      server.addConnector(ajpConnector)

      println(s"AJP listening on $port")
    }

    server.start

    mainThread = Thread.currentThread

    if (shutdownHook == null) {
      shutdownHook = new ServerShutdownHook
    }

    Runtime.getRuntime.addShutdownHook(shutdownHook)

    await

    server.stop
    server.destroy
  }

  def load {
    home = System.getProperty("monarch.home")

    if (home == null) {
      println("Error: The Java system property \"monarch.home\" must be defined")
      sys.exit(-1)
    }

    config = System.getProperty("monarch.config")

    if (config == null) {
      config = home + "/conf"
      System.setProperty("monarch.config", config)
    }

    val input = new FileInputStream(s"$config/standalone.properties");
    settings = new Properties
    settings.load(input)
    IOUtils.closeQuietly(input)
  }

  def await {
    val port = getSetting("server.http.shutdownPort", "8001").toInt

    // Set up a server socket to wait on
    try {
      awaitSocket = new ServerSocket(port, 1, InetAddress.getLoopbackAddress)
    } catch {
      case e: IOException => {
        error(s"Could not create listening socket for shutdown on port $port", e)
        return
      }
    }

    try {
      // Loop waiting for a connection and a valid command
      var continue = true

      while (!stopAwait) {
        breakable {
          var serverSocket = awaitSocket

          if (serverSocket == null) {
            break
          }

          // Wait for the next connection
          var socket: Socket = null
          val command = new StringBuilder

          try {
            var stream: InputStream = null
            val acceptStartTime = System.currentTimeMillis

            try {
              socket = serverSocket.accept
              socket.setSoTimeout(10 * 1000) // Ten seconds
              stream = socket.getInputStream
            } catch {
              case ste: SocketTimeoutException => {
                break
              }
              case ace: AccessControlException => {
                break
              }
              case e: IOException => {
                if (stopAwait) {
                  // Wait was aborted with socket.close
                  break
                }

                error("StandaloneServer.await: accept", e);
                stopAwait = true
                break
              }
            }

            // Read a set of characters from the socket
            var expected = SHUTDOWN_COMMAND.length * 2 // Cut off to avoid DoS attack

            breakable {
              while (expected > 0) {
                val ch = try {
                  stream.read
                } catch {
                  case e: IOException => {
                    warn("StandaloneServer.await: read", e);
                    -1
                  }
                }

                if (ch < 32) { // Control character or EOF terminates loop
                  break
                }

                command.append(ch.toChar)
                expected = expected - 1
              }
            }
          } finally {
            // Close the socket now that we are done with it
            try {
              if (socket != null) {
                socket.close
              }
            } catch {
              case e: IOException => {
                // Ignore
              }
            }
          }

          // Match against our command string
          val shutdownMatch = command.toString().equals(SHUTDOWN_COMMAND)

          if (shutdownMatch) {
            stopAwait = true
            break
          }
        }
      }
    } finally {
      val serverSocket = awaitSocket;
      awaitSocket = null;

      // Close the server socket and return
      if (serverSocket != null) {
        try {
          serverSocket.close
        } catch {
          case e: IOException => {
            // Ignore
          }
        }
      }
    }
  }

  def stop {
    println(s"Stopping API Manager...")

    if (shutdownHook != null) {
      Runtime.getRuntime.removeShutdownHook(shutdownHook);
      shutdownHook = null
    }

    stopAwait = true
  }

  def stopServer {
    load

    // Stop the existing server
    val port = getSetting("server.http.shutdownPort", "8001").toInt

    try {
      val socket = new Socket("localhost", port)
      val stream = socket.getOutputStream

      val shutdown = "SHUTDOWN"

      for (i <- 0 until shutdown.length) {
        stream.write(shutdown.charAt(i))
      }

      stream.flush();
    } catch {
      case ce: Exception => {
        System.exit(1)
      }
    }
  }

  def version {
    println(s"Monarch API Manager version $VERSION")
  }

  private class ServerShutdownHook extends Thread {
    override def run {
      stopAwait = true
    }
  }

  private def addRestApi(context: ServletContextHandler, name: String, secure: Boolean = true): Unit = {
    if (secure) {
      context.addFilter(classOf[ApiFilter], s"/$name/*", EnumSet.of(DispatcherType.REQUEST))
    }

    val apiHolder: ServletHolder = new ServletHolder(classOf[ServletContainer])
    apiHolder.setInitParameter(
      "jersey.config.server.provider.packages",
      s"com.monarchapis.apimanager.rest.common,com.monarchapis.apimanager.rest.$name")
    apiHolder.setInitOrder(1)
    context.addServlet(apiHolder, s"/$name/*")
  }

  def getSetting(key: String, default: String) = {
    val value = if (settings != null) {
      StringUtils.trimToNull(settings.getProperty(key))
    } else {
      null
    }

    if (value != null) value else default
  }
}