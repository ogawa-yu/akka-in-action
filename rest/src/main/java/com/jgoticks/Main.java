package com.jgoticks;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.Duration;
import scala.concurrent.Await;
import com.jgoticks.BoxOffice.*;

public class Main {
    /*
    public static void main(String args[]) {
        ActorSystem system = ActorSystem.create("main", ConfigFactory.load("goticks"));
        ActorRef boxOffice = system.actorOf(BoxOffice.props(), BoxOffice.name());

        boxOffice.tell(new Initialize(), ActorRef.noSenser());
        boxOffice.tell(new Order(), ActorRef.noSender());
    }*/
}
