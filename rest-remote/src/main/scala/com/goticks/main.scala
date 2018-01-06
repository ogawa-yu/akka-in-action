package com.goticks

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object main extends App
  with RequestTimeout {
  
  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val port = config.getInt("http.port")
  val timeout = config.getString("akka.http.server.request-timeout")

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val server = new RestServer()
  server.start(host, port, timeout)
}
