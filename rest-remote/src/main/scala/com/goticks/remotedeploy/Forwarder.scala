package com.goticks.remotedeploy

import akka.actor.{Actor, ActorLogging, ActorRef, Props, ReceiveTimeout, Terminated}
import akka.util.Timeout
import com.goticks.BoxOffice

import scala.concurrent.duration._

object Forwarder {
  def props(implicit timeout: Timeout) = {
    Props(new Forwarder)
  }
  def name = "forwarder"
}

class Forwarder(implicit timeout: Timeout)
  extends Actor
  with ActorLogging {
  context.setReceiveTimeout(3 second)

  deployAndWatch()

  def deployAndWatch(): Unit = {
    val actor = context.actorOf(BoxOffice.props, BoxOffice.name)
    context.watch(actor)
    log.info("switching to maybe active state")
    context.become(maybeActive(actor))
    context.setReceiveTimeout(Duration.Undefined)
  }

  override def receive: Receive = deploying

  def deploying(): Receive = {
    case ReceiveTimeout =>
      deployAndWatch()

    case msg: Any =>
      log.error(s"Ignoring message $msg, remote actor is not ready yet.")
  }

  def maybeActive(actor: ActorRef): Receive = {
    case Terminated(actorRef) =>
      log.info("Actor $actorRef terminated.")
      log.info("switching to deploying state")
      context.become(deploying())
      context.setReceiveTimeout(3 second)
      deployAndWatch()

    case msg: Any => actor forward msg
  }
}
