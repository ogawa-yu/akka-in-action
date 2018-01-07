package com.goticks

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Future

trait Startup extends RequestTimeout {
  def startup(api: Route)(implicit system: ActorSystem) = {
    val host = system.settings.config.getString("http.host")
    val port = system.settings.config.getInt("http.port")
    startHttpServer(api, host, port)
  }

  def startHttpServer(api: Route, host: String, port: Int)(implicit system: ActorSystem) = {
    implicit val ec = system.dispatcher
    implicit val materializer = ActorMaterializer()
    val bindingFuture: Future[Http.ServerBinding] =
      Http().bindAndHandle(api, host, port)
    val log = Logging(system.eventStream, "go-ticks")
    bindingFuture.map { serverBinding =>
      log.info(s"RestApi bound to ${serverBinding.localAddress}")
    }.onFailure {
      case ex: Exception =>
        log.error(ex, "Failed to bind to {}:{}", host, port)
        system.terminate()
    }
  }
}
