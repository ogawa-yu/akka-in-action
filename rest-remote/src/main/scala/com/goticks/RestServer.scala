package com.goticks

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout

import scala.concurrent.Future
import scala.util.{Failure, Success}

class RestServer(implicit val system: ActorSystem,
                 implicit val materializer: ActorMaterializer)
  extends RequestTimeout {
  implicit val ec = system.dispatcher
  val log = Logging(system.eventStream, "goticks")

  def start(address: String, port: Int, timeout: String) = {
    val api = new RestApi(system, requestTimeout(timeout)).routes
    val bindingFuture: Future[ServerBinding] =
      Http().bindAndHandle(api, address, port)

    bindingFuture.map { serverBinging =>
      log.info(s"RestApi bound to ${serverBinging.localAddress} ")
    }.onComplete {
      case Success(_) =>
        log.info("Success to bind to {}:{}", address, port)
      case Failure(ex) =>
        log.error(ex, "Failed to bind to {}:{}", address, port)
        system.terminate()
    }
  }

  def stop() = {
    val f = Http().shutdownAllConnectionPools()
    f.onComplete {
      case Success(_) =>
        log.info(s"HTTP connection closed.")
      case Failure(ex) =>
        log.error(ex, "HTTP connection closed failed.")
    }
  }
}

trait RequestTimeout {
  import scala.concurrent.duration._
  def requestTimeout(timeout: String): Timeout = {
    val d = Duration(timeout)
    FiniteDuration(d.length, d.unit)
  }
}
