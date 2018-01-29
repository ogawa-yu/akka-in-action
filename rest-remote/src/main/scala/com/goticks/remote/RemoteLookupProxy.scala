package com.goticks.remote

import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Identify, Terminated}

import scala.concurrent.duration._

class RemoteLookupProxy(path: String)
  extends Actor
  with ActorLogging {

  context.setReceiveTimeout(3 second)
  sendIdentifyRequest()

  def sendIdentifyRequest(): Unit = {
    log.info("send identify request=>{}", path)
    val selection = context.actorSelection(path)
    selection ! Identify(path)
  }

  def receive = identify

  def identify: Receive = {
    case ActorIdentity(path, Some(actor)) =>
      context.setReceiveTimeout(Duration.Undefined)
      log.info("switching active state")
      context.become(active(actor))
      context.watch(actor)
  }

  def active(actorRef: ActorRef): Receive = {
    case Terminated(actor) =>
      log.info(s"Actor $actor terminated.")
      log.info("switching to identify state")
      context.become(identify)
      context.setReceiveTimeout(3 second)
      sendIdentifyRequest()
    case msg: Any => actorRef forward msg
  }
}
