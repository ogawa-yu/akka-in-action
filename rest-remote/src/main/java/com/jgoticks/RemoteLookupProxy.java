package com.jgoticks;

import akka.actor.AbstractActor;
import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Identify;
import akka.actor.Terminated;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.util.Timeout;
import scala.concurrent.duration.FiniteDuration;

public class RemoteLookupProxy extends AbstractActor {
    private String path_;
    private LoggingAdapter log = Logging.getLogger(this);

    public RemoteLookupProxy(String path) {
        path_ = path;
        getContext().setReceiveTimeout(FiniteDuration.create(3, "second"));
        sendIdentifyRequest();
    }

    private void sendIdentifyRequest() {
        ActorSelection selection = getContext().actorSelection(path_);
        selection.tell(new Identify(path_), getSelf());
    }

    @Override
    public Receive createReceive() {
        return identify();
    }

    private Receive identify() {
        return receiveBuilder()
                .match(ActorIdentity.class, msg -> {
                    msg.getActorRef().ifPresent(ref -> {
                        getContext().setReceiveTimeout(FiniteDuration.create(3, "s"));
                        log.info("switching to active state");
                        getContext().become(active(ref));
                        getContext().watch(ref);
                    });
                })
                .build();
    }

    private Receive active(ActorRef actor) {
        return receiveBuilder()
                .match(Terminated.class, msg -> {
                    log.info("actor " + msg.getActor() + " terminated.");
                    getContext().become(identify());
                    log.info("switching to identify state");
                    getContext().setReceiveTimeout(FiniteDuration.create(3, "second"));
                    sendIdentifyRequest();
                })
                .matchAny(msg -> actor.forward(msg, getContext()))
                .build();
    }
}
