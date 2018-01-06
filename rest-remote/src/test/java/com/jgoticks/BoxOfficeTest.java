package com.jgoticks;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Option;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import akka.util.Timeout;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jgoticks.BoxOffice.*;

@SuppressWarnings("unchecked")
public class BoxOfficeTest extends TestKit {
    private static ActorSystem system_ = ActorSystem.create();
    private static TestActorRef<BoxOffice> testActor_;
    private static Timeout timeout_;


    public BoxOfficeTest() {
        super(system_);
    }

    @BeforeClass
    public static void setupClass() throws Exception {
        testActor_ = TestActorRef.create(system_, BoxOffice.props(), BoxOffice.name());
        FiniteDuration delay = FiniteDuration.create(5, "second");
        timeout_ = new Timeout(delay);
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        system_.terminate();
    }

    private List<TicketSeller.Ticket> makeTicket(int count) {
        return IntStream.rangeClosed(1, count).mapToObj(TicketSeller.Ticket::new).collect(Collectors.toList());
    }

    @Test
    public void test_createEvent() {
        ActorRef testActor = getSystem().actorOf(BoxOffice.props(), "test_createEvent");

        testActor.tell(new CreateEvent("event_CreateEvent", 10), getRef());
        expectMsg(new EventCreated(new Event("event_CreateEvent", 10)));

        testActor.tell(new CreateEvent("event_CreateEvent", 10), getRef());
        expectMsg(new EventExists());

        testActor.tell(new CreateEvent("event_another", 2), getRef());
        expectMsg(new EventCreated(new Event("event_another", 2)));
    }

    @Test
    public void test_getEvent() {
        ActorRef testActor = getSystem().actorOf(BoxOffice.props(), "test_getEvent");

        testActor.tell(new GetEvent("not_found_event"), getRef());
        expectMsg(Option.none());

        testActor.tell(new CreateEvent("event_TicketA", 10), getRef());
        expectMsg(new EventCreated(new Event("event_TicketA", 10)));

        testActor.tell(new GetEvent("event_TicketA"), getRef());
        expectMsg(Option.some(new Event("event_TicketA", 10)));
    }

    @Test
    public void test_getEvents() {
        ActorRef testActor = getSystem().actorOf(BoxOffice.props(), "test_getEvents");

        testActor.tell(new GetEvents(), getRef());
        expectMsg(Events.of(Collections.emptyList()));

        testActor.tell(new CreateEvent("TicketA", 10), getRef());
        testActor.tell(new CreateEvent("TicketB", 10), getRef());
        testActor.tell(new CreateEvent("TicketC", 10), getRef());

        expectMsg(new EventCreated(new Event("TicketA", 10)));
        expectMsg(new EventCreated(new Event("TicketB", 10)));
        expectMsg(new EventCreated(new Event("TicketC", 10)));

        testActor.tell(new GetEvents(), getRef());
        expectMsg(timeout_.duration(),
                Events.of(
                Arrays.asList(new Event("TicketA", 10),
                            new Event("TicketB", 10),
                            new Event("TicketC", 10))));
    }

    @Test
    public void test_getTicket() {
        ActorRef testActor = getSystem().actorOf(BoxOffice.props(), "test_getTicket");

        testActor.tell(new GetTicket("ticketA", 10), getRef());
        expectMsg(TicketSeller.Tickets.of("ticketA", Collections.emptyList()));

        testActor.tell(new CreateEvent("ticketA", 10), getRef());
        expectMsg(new EventCreated(new Event("ticketA", 10)));

        testActor.tell(new GetTicket("ticketB", 10), getRef());
        expectMsg(TicketSeller.Tickets.of("ticketB", Collections.emptyList()));

        testActor.tell(new GetTicket("ticketA", 9), getRef());
        expectMsg(TicketSeller.Tickets.of("ticketA", makeTicket(9)));

        testActor.tell(new GetTicket("ticketA", 2), getRef());
        expectMsg(TicketSeller.Tickets.of("ticketA", Collections.emptyList()));
    }

    @Test
    public void test_cancelEvent() {
        ActorRef testActor = getSystem().actorOf(BoxOffice.props(), "test_cancelEvent");

        testActor.tell(new CancelEvent("notFound"), getRef());
        testActor.tell(new CreateEvent("ticketA", 10), getRef());
        testActor.tell(new CancelEvent("ticketA"), getRef());

        expectMsg(Option.none());
        expectMsg(new EventCreated(new Event("ticketA", 10)));
        expectMsg(new Option.Some(new Event("ticketA", 10)));
   }
}
