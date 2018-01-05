package com.goticks

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.goticks.BoxOffice._
import com.goticks.TicketSeller.{Ticket, Tickets}
import org.scalatest.{MustMatchers, WordSpecLike}

class BoxOfficeTest extends TestKit(ActorSystem("testsystem"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

  import scala.concurrent.duration._
  implicit val timeout = Timeout(3 seconds)
  "The BoxOffice Actor" must {
    "create event when a CreateEvent(\"test_createEvent\", 10) if \"test_createEvent\" has not published is sent to it" in {
      val testActor = system.actorOf(BoxOffice.props)

      testActor ! CreateEvent("test_createEvent", 10)

      expectMsg(EventCreated(Event("test_createEvent", 10)))
    }
    "create event when a CreateEvent(\"test_createEvent\", 10) if \"test_createEvent\" has published is sent to it" in {
      val testActor = system.actorOf(BoxOffice.props)

      testActor ! CreateEvent("test_createEvent", 10)
      testActor ! CreateEvent("test_createEvent", 10)

      expectMsg(EventCreated(Event("test_createEvent", 10)))
      expectMsg(EventExists)
    }
    "get event when a GetEvent() is sent to it" in {
      val testActor = system.actorOf(BoxOffice.props)

      testActor ! GetEvent("test_notFound")
      testActor ! CreateEvent("test_ticketA", 10)
      testActor ! GetEvent("test_ticketA")

      expectMsg(None)
      expectMsg(EventCreated(Event("test_ticketA", 10)))
      expectMsg(Some(Event("test_ticketA", 10)))
    }
    "get events when a GetEvents() is sent to it" in {
      val testActor = system.actorOf(BoxOffice.props)

      testActor ! GetEvents
      expectMsg(Events())

      testActor ! CreateEvent("ticketA", 10)
      testActor ! CreateEvent("ticketB", 3)
      testActor ! CreateEvent("ticketC", 6)
      testActor ! GetEvents

      expectMsg(EventCreated(Event("ticketA", 10)))
      expectMsg(EventCreated(Event("ticketB", 3)))
      expectMsg(EventCreated(Event("ticketC", 6)))
      expectMsg(Events(Vector(Event("ticketA", 10), Event("ticketB", 3), Event("ticketC", 6))))
    }
    "get tickets when a GetTiket() is sent to it" in {
      val testActor = system.actorOf(BoxOffice.props)

      testActor ! GetTicket("ticketA", 3)
      testActor ! CreateEvent("ticketA", 10)
      testActor ! GetTicket("ticketA", 10)
      testActor ! GetTicket("ticketA", 1)

      expectMsg(Tickets("ticketA"))
      expectMsg(EventCreated(Event("ticketA", 10)))
      expectMsg(Tickets("ticketA", (1 to 10).map(i => Ticket(i)).toVector))
      expectMsg(Tickets("ticketA"))
    }
    "cancel event when CancelEvent() is sent to it" in {
      val testActor = system.actorOf(BoxOffice.props)

      testActor ! CancelEvent("notFound")
      testActor ! CreateEvent("ticketA", 10)
      testActor ! CancelEvent("ticketA")

      expectMsg(None)
      expectMsg(EventCreated(Event("ticketA", 10)))
      expectMsg(Some(Event("ticketA", 10)))
    }
  }

}
