package com.goticks

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.Logging
import com.typesafe.config.ConfigFactory

object FrontendMain extends App with Startup {
  val config = ConfigFactory.load("frontend")
  implicit val system = ActorSystem("frontend", config)

  val api = new RestApi() {
    implicit val executionContext = system.dispatcher
    implicit val requestTimeout = configuredRequestTimeout(config)
    val log = Logging(system.eventStream, "frontend")

    def createPath(): String = {
      val backendConf = config.getConfig("backend")
      val host = backendConf.getString("host")
      val port = backendConf.getInt("port")
      val protocol = backendConf.getString("protocol")
      val systemName = backendConf.getString("system")
      val actorName = backendConf.getString("actor")
      s"$protocol://$systemName@$host:$port/$actorName"
    }

    def createBoxOffice: ActorRef = {
      val path = createPath()
      system.actorOf(Props(new RemoteLookupProxy(path)), "lookupBoxOffice")
    }
  }
  startup(api.routes)
}
