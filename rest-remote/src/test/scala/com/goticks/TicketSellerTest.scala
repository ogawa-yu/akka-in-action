package com.goticks

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import com.goticks.BoxOffice.Event
import com.goticks.TicketSeller._
import org.scalatest.{MustMatchers, WordSpecLike}

class TicketSellerTest extends TestKit(ActorSystem("testssytem"))
  with WordSpecLike
  with MustMatchers
  with ImplicitSender
  with StopSystemAfterAll {

  def makeTickets = (1 to 10).map(Ticket.apply).toVector

  "The TicketSeller" must {
    "add ticket when a Add(Ticket(1)) is sent to it" in {
      val seller = system.actorOf(TicketSeller.props("test add event"), "seller_actor_Add")

      seller ! Add(Vector(Ticket(1)))
      expectNoMsg()
    }
    "buy ticket when a Buy(2) if the actor has two tickets, is sent to it" in {
      import akka.pattern.ask

      import scala.concurrent.duration._

      val seller = system.actorOf(TicketSeller.props("test buy event"), "seller_actor_Buy")
      implicit val timeout = Timeout(3 seconds)
      seller.ask(Add(makeTickets))

      seller ! Buy(2)
      expectMsg(Tickets("test buy event", Vector(Ticket(1), Ticket(2))))
    }
    "buy ticket failed when a Buy(2) if the actor has not two tickets, is sent to it" in {
      import akka.pattern.ask
      import scala.concurrent.duration._

      val props = TicketSeller.props("test buy event failed")
      val seller = system.actorOf(props, "seller_actor_Buy_FailCase")
      implicit val timeout = Timeout(3 seconds)
      seller.ask(Add(Vector(Ticket(1))))

      seller ! Buy(2)
      expectMsg(Tickets("test buy event failed", Vector.empty[Ticket]))
    }
    "get event when a GetEvent() is sent to it" in {
      val seller = system.actorOf(TicketSeller.props("test getEvent"), "seller_actor_GetEvent")

      seller ! GetEvent
      expectMsg(Some(Event("test getEvent", 0)))
    }
    "cancel event when a Cancel() is sent to it" in {
      import akka.pattern.ask
      import scala.concurrent.duration._

      val seller = system.actorOf(TicketSeller.props("test cancel"), "seller_actor_Cancel")
      implicit val timeout = Timeout(3 seconds)
      seller.ask(Add(makeTickets))

      seller ! Cancel
      expectMsg(Some(Event("test cancel", 10)))

      seller ! Buy(10)
      expectNoMsg()
    }
  }
}
