package com.jgoticks;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.dispatch.Futures;
import akka.event.DiagnosticLoggingAdapter;
import akka.event.Logging;
import akka.japi.Option;
import akka.pattern.Patterns;
import akka.util.Timeout;
import lombok.Data;
import lombok.Value;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static com.jgoticks.TicketSeller.Add;
import static com.jgoticks.TicketSeller.Ticket;

class BoxOffice extends AbstractActor {
    public static Props props() {
        return Props.create(BoxOffice.class, BoxOffice::new);
    }

    public static String name() {
        return ACTOR_NAME;
    }

    private static final String ACTOR_NAME = "boxOffice";
    private static final Timeout TIMEOUT_SEC = new Timeout(FiniteDuration.create(5, "second"));
    private DiagnosticLoggingAdapter log = Logging.getLogger(this);

    private ActorRef createTicketSeller(String event) {
        return getContext().actorOf(TicketSeller.props(event), event);
    }

    private BoxOffice() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CreateEvent.class, msg ->
                    fold(msg.getName(),
                            empty -> msg.create(createTicketSeller(msg.getName()), getSender(), getSelf()),
                            child -> getSender().tell(new EventExists(), getSelf())))
                .match(GetEvent.class, msg ->
                    fold(msg.getName(),
                            empty -> getSender().tell(msg.none(), getSelf()),
                            child -> child.forward(new TicketSeller.GetEvent(), getContext())))
                .match(GetEvents.class, msg -> {
                    final ExecutionContext ec = getContext().getSystem().dispatcher();
                    final Iterable<Future<Object>> fs = () -> StreamSupport
                            .stream(getContext().getChildren().spliterator(), false)
                            .map(actor -> Patterns.ask(getSelf(), new GetEvent(actor.path().name()), TIMEOUT_SEC))
                            .iterator();
                    final Future<Events> result = Futures.future(() ->
                            convertToEvents(Await.result(Futures.sequence(fs, ec), TIMEOUT_SEC.duration())), ec);
                    Patterns.pipe(result, ec).to(getSender());
                })
                .match(GetTicket.class, msg ->
                    fold(msg.getEvent(),
                            empty -> getSender().tell(msg.emptyTicket(), getSelf()),
                            child -> child.forward(new TicketSeller.Buy(msg.getTickets()), getContext())))
                .match(CancelEvent.class, msg ->
                    fold(msg.getName(),
                            empty -> getSender().tell(msg.none(), getSelf()),
                            child -> child.forward(new TicketSeller.Cancel(), getContext())))
                .matchAny(o -> log.info("received unknown message."))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static Events convertToEvents(Iterable<Object> result) {
        return new Events(StreamSupport.stream(result.spliterator(), false)
                .map(f ->  ((Option<Event>)f).get())
                .sorted(Comparator.comparing(Event::getName))
                .collect(Collectors.toList()));
    }

    private void fold(String name, Consumer<ActorRef> isEmpty, Consumer<ActorRef> f) {
        Optional<ActorRef> child = getContext().findChild(name);
        if (!child.isPresent()) {
            isEmpty.accept(child.orElse(ActorRef.noSender()));
        } else {
            f.accept(child.get());
        }
    }

    static @Data class Event {
        private final String name;
        private final int tickets;
    }

    static @Value(staticConstructor = "of") class Events {
        private List<Event> events;
    }

    static @Data class CreateEvent {
        final private String name;
        final private int tickets;

        void create(ActorRef seller, ActorRef sender, ActorRef receiver) {
            List<Ticket> newTickets =
                    IntStream.rangeClosed(1, tickets)
                            .mapToObj(Ticket::new)
                            .collect(Collectors.toList());
            seller.tell(new Add(newTickets), receiver);
            sender.tell(new EventCreated(new Event(name, tickets)), receiver);
        }

    }

    static @Data class GetEvent {
        private final String name;

        Option<Event> none() {
            return Option.none();
        }
    }

    static @Data class GetEvents {}

    static @Data class GetTicket {
        private final String event;
        private final int tickets;

        private TicketSeller.Tickets emptyTicket() {
            return TicketSeller.Tickets.of(event, Collections.emptyList());
        }
    }

    static @Data class CancelEvent {
        private final String name;

        Option<Event> none() {
            return Option.none();
        }
    }

    interface EventResponse {}
    static @Data class EventCreated implements EventResponse {
        private final Event event;
    }

    static @Data class EventExists implements EventResponse {}
}
