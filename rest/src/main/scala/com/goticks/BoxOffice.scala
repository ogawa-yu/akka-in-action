package com.goticks

import akka.actor._
import akka.util.Timeout

import scala.concurrent.Future

object BoxOffice {
  def props(implicit timeout: Timeout) = Props(new BoxOffice)
  def name = "boxOffice"

  case class CreateEvent(name: String, tickets: Int)
  case class GetEvent(name: String)
  case object GetEvents
  case class GetTicket(name: String, tickets: Int)
  case class CancelEvent(name: String)

  case class Event(name: String, tickets: Int)
  case class Events(events: Vector[Event] = Vector.empty[Event])

  sealed trait EventResponse
  case class EventCreated(event: Event) extends EventResponse
  case object EventExists extends EventResponse
}

class BoxOffice(implicit timeout: Timeout) extends Actor {
  import BoxOffice._
  def createTicketSeller(name: String) =
    context.actorOf(TicketSeller.props(name), name)

  def receive = {
    case CreateEvent(name, tickets) =>
      def create() = {
        val eventTickets = createTicketSeller(name)
        val newTickets = (1 to tickets).map { ticketId =>
          TicketSeller.Ticket(ticketId)
        }.toVector
        eventTickets ! TicketSeller.Add(newTickets)
        sender ! EventCreated(Event(name, tickets))
      }
      def exists(child: ActorRef) = sender ! EventExists
      context.child(name).fold(create())(exists)

    case GetEvent(event) =>
      def notFound() = sender ! None
      def getEvent(child: ActorRef) = child forward TicketSeller.GetEvent
      context.child(event).fold(notFound())(getEvent)

    case GetEvents =>
      import akka.pattern.{ask, pipe}
      implicit val ec = context.system.dispatcher

      def getEvents = context.children.map { child =>
        self.ask(GetEvent(child.path.name)).mapTo[Option[Event]]
      }
      def convertToEvents(f: Future[Iterable[Option[Event]]]) =
        f.map(_.flatten).map(l=> Events(l.toVector))

      pipe(convertToEvents(Future.sequence(getEvents))) to sender()

    case GetTicket(name, tickets) =>
      def notFound() = sender ! TicketSeller.Tickets(name)
      def getTicket(child: ActorRef) = child forward TicketSeller.Buy(tickets)
      context.child(name).fold(notFound())(getTicket)

    case CancelEvent(name) =>
      def notFound() = sender ! None
      def cancelEvent(child: ActorRef) = child forward TicketSeller.Cancel
      context.child(name).fold(notFound())(cancelEvent)
  }
}
