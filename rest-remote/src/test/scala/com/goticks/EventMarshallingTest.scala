package com.goticks

import com.goticks.BoxOffice.{Event, Events}
import com.goticks.TicketSeller.{Ticket, Tickets}
import org.scalatest.{Matchers, WordSpec}
import spray.json.{JsArray, JsNumber, JsObject, JsString, JsValue}

class EventMarshallingTest extends WordSpec with Matchers with EventMarshalling {
  "The eventDescriptionFormat" must {
    "json format is" in {
        eventDescriptionFormat.write(EventDescription(4)) shouldEqual
          JsObject.apply(("tickets" -> JsNumber(4)))
    }
  }
  "The ticketRequestFormat" must {
    "json format is" in {
      ticketRequestFormat.write(TicketRequest(2)) shouldEqual
        JsObject.apply("tickets" -> JsNumber(2))
    }
  }
  "The eventFormat" must {
    "json format is" in {
      eventFormat.write(Event("eventA", 5)) shouldEqual
        JsObject.apply("name" -> JsString("eventA"), "tickets" -> JsNumber(5))
    }
  }
  "The eventsFormat" must {
    "json format is" in {
      eventsFormat.write(Events((1 to 3).map(i => Event("event" + i, i)).toVector)) shouldEqual
        JsObject.apply("events" -> JsArray(
          JsObject.apply("name" -> JsString("event1"), "tickets" -> JsNumber(1)),
          JsObject.apply("name" -> JsString("event2"), "tickets" -> JsNumber(2)),
          JsObject.apply("name" -> JsString("event3"), "tickets" -> JsNumber(3))
        ))
    }
  }
  "The ticketFormat" must {
    "json format is" in {
      ticketFormat.write(Ticket(1)) shouldEqual
        JsObject.apply("id" -> JsNumber(1))
    }
  }
  "The ticketsFormat" must {
    "json format is" in {
      ticketsFormat.write(Tickets("eventA", (1 to 3).map(i => Ticket(i)).toVector)) shouldEqual
        JsObject(
          "event" -> JsString("eventA"),
          "tickets" -> JsArray(
            JsObject.apply("id" -> JsNumber(1)),
            JsObject.apply("id" -> JsNumber(2)),
            JsObject.apply("id" -> JsNumber(3))
        ))
    }
  }
}
