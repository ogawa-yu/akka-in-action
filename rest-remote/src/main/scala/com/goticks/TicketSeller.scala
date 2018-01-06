package com.goticks

import akka.actor.{Actor, PoisonPill, Props}

object TicketSeller {
  def props(event: String) = Props(new TicketSeller(event))

  case class Ticket(id: Int)
  case class Tickets(event: String, tickets: Vector[Ticket] = Vector.empty[Ticket])
  case class Add(tickets: Vector[Ticket])
  case class Buy(tickets: Int)
  case object GetEvent
  case object Cancel
}

class TicketSeller(event: String) extends Actor {
  import TicketSeller._

  var tickets = Vector.empty[Ticket]

  override def receive() = {
    case Add(newTickets) => tickets = tickets ++ newTickets
    case Buy(numberOfTickets) =>
      val entries = tickets.take(numberOfTickets)
      if (entries.size >= numberOfTickets) {
        sender() ! Tickets(event, entries)
        tickets = tickets.drop(numberOfTickets)
      } else sender() ! Tickets(event)
    case GetEvent => sender() ! Some(BoxOffice.Event(event, tickets.size))
    case Cancel =>
      self ! PoisonPill
      sender() ! Some(BoxOffice.Event(event, tickets.size))
  }
}
