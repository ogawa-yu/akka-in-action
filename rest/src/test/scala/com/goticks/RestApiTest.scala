package com.goticks

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.goticks.BoxOffice.{Event, Events}
import com.goticks.TicketSeller.{Ticket, Tickets}
import org.scalatest.{Matchers, WordSpec}

class RestApiTest extends WordSpec
  with Matchers
  with ScalatestRouteTest
  with SprayJsonSupport
  with EventMarshalling {

  import scala.concurrent.duration._
  val testRoutes = new RestApi(system, Timeout(3 second)).routes

  "The RestApi" should {
    "eventRoute for Get Event" in {
      Get("/events/testEventA") ~> testRoutes ~> check {
        status == StatusCodes.NotFound
      }
    }
    "eventRoute for DELETE Event" in {
      Delete("/events/testEventA") ~> testRoutes ~> check {
        status == StatusCodes.NotFound
      }
    }
    "eventRoute for POST Event" in {
      Post("/events/testEventA", eventDescriptionFormat.write(EventDescription(2))) ~> testRoutes ~> check {
        status == StatusCodes.OK
        responseAs[Event] shouldEqual Event("testEventA", 2)
      }
      Post("/events/testEventA", eventDescriptionFormat.write(EventDescription(2))) ~> testRoutes ~> check {
        status == StatusCodes.BadRequest
        responseAs[Error] shouldEqual Error("testEventA event exists already.")
      }
      Get("/events/testEventA/") ~> testRoutes ~> check {
        status == StatusCodes.OK
        responseAs[Event] shouldEqual Event("testEventA", 2)
      }
      Delete("/events/testEventA/") ~> testRoutes ~> check {
        status == StatusCodes.OK
        responseAs[Event] shouldEqual Event("testEventA", 2)
      }
      Get("/events/testEventA") ~> testRoutes ~> check {
        status == StatusCodes.NotFound
      }
    }
    "eventsRoute for GET Event" in {
      Post("/events/testEventA", eventDescriptionFormat.write(EventDescription(2))) ~> testRoutes
      Post("/events/testEventB", eventDescriptionFormat.write(EventDescription(5))) ~> testRoutes
      Post("/events/testEventC", eventDescriptionFormat.write(EventDescription(10))) ~> testRoutes
      Get("/events") ~> testRoutes ~> check {
        status == StatusCodes.OK
        responseAs[Events] shouldEqual
          Events(Vector(Event("testEventA", 2), Event ("testEventB", 5), Event("testEventC", 10)))
      }
    }
    "ticketRoute for POST Event" in {
      Post("/events/testEventA", eventDescriptionFormat.write(EventDescription(2))) ~> testRoutes
      Post("/events/testEventB", eventDescriptionFormat.write(EventDescription(5))) ~> testRoutes
      Post("/events/testEventC", eventDescriptionFormat.write(EventDescription(10))) ~> testRoutes
      Post("/events/testEventA/tickets", ticketRequestFormat.write(TicketRequest(1))) ~> testRoutes ~> check {
        status == StatusCodes.OK
        responseAs[Tickets] shouldEqual
          Tickets("testEventA", Vector(Ticket(1)))
      }
    }
  }
}
