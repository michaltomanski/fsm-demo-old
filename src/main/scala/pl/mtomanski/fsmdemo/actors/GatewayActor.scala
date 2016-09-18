package pl.mtomanski.fsmdemo.actors

import akka.actor.{Actor, Props}
import pl.mtomanski.fsmdemo.actors.GatewayActor.SoonestConnections
import pl.mtomanski.fsmdemo.domain.Connection

class GatewayActor extends Actor {

  override def receive = {
    case SoonestConnections(connections) => println(s"Soonest connections received: $connections")
  }

}

object GatewayActor {

  def props(): Props = Props(new GatewayActor)

  case class SoonestConnections(connections: Seq[Connection])
}
