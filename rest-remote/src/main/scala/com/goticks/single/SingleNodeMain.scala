package com.goticks.single

import akka.actor.{ActorSystem, Props}
import com.goticks.{BoxOffice, RestApi, Startup}
import com.typesafe.config.ConfigFactory

object SingleNodeMain extends App with Startup {
  val config = ConfigFactory.load("singlenode")
  implicit val system = ActorSystem("singlenode", config)

  val api = new RestApi() {
    implicit val requestTimeout = configuredRequestTimeout(config)
    implicit val executionContext = system.dispatcher
    def createBoxOffice = system.actorOf(BoxOffice.props, BoxOffice.name)
  }
  startup(api.routes)
}
