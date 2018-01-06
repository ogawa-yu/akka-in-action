package com.goticks

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.scalatest.{MustMatchers, WordSpecLike}

class RestServerTest extends WordSpecLike
  with MustMatchers
  with ScalatestRouteTest {
  "The RestServer" must {
    "When start called, HTTP service is started." in {
      val host = "0.0.0.0"
      val port = 5000
      val timeout = "3 sec"

      implicit val system = ActorSystem()
      implicit val materializer = ActorMaterializer()

      val server = new RestServer()
      server.start(host, port, timeout)

      server.stop()
    }
  }
}
