package com.jgoticks;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.japi.Option;
import akka.pattern.Patterns;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jgoticks.TicketSeller.*;

public class TicketSellerTest extends TestKit {
    private static ActorSystem system_ = ActorSystem.create();

    public TicketSellerTest() {
        super(system_);
    }

    @AfterClass
    public static void teardownClass() throws Exception {
        system_.terminate();
    }

    private List<Ticket> makeTickets(int count) {
        return IntStream.range(0, count).mapToObj(Ticket::new).collect(Collectors.toList());
    }

    @Test
    public void test_add() {
        ActorRef testActor = getSystem().actorOf(TicketSeller.props("event test_add"), "test_add");

        testActor.tell(new Add(makeTickets(3)), getRef());
        expectNoMsg();
    }

    @Test
    public void test_buy() {
        ActorRef testActor = getSystem().actorOf(TicketSeller.props("event test_buy"), "test_buy");
        Patterns.ask(testActor, new Add(makeTickets(3)), 3000);

        testActor.tell(new Buy(1), getRef());
        expectMsg(Tickets.of("event test_buy", makeTickets(1)));

        testActor.tell(new Buy(3), getRef());
        expectMsg(Tickets.of("event test_buy", Collections.emptyList()));
    }

    @Test
    public void test_getEvent() {
        ActorRef testActor = getSystem().actorOf(TicketSeller.props("event test_getEvent"), "test_getEvent");
        Patterns.ask(testActor, new Add(makeTickets(5)), 3000);

        testActor.tell(new GetEvent(), getRef());
        expectMsg(Option.some(new BoxOffice.Event("event test_getEvent", 5)));
    }

    @Test
    public void test_cancel() {
        ActorRef testActor = getSystem().actorOf(TicketSeller.props("event test_cancel"), "test_cancel");
        testActor.tell(new Add(makeTickets(1000)),  getRef());

        testActor.tell(new Cancel(), getRef());
        expectMsg(Option.some(new BoxOffice.Event("event test_cancel",1000)));

        testActor.tell(new Buy(1000),  getRef());
        expectNoMsg();
    }
}
