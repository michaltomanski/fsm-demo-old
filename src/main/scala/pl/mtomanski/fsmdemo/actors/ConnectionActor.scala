package pl.mtomanski.fsmdemo.actors

import java.util.UUID

import akka.actor.{Actor, Props}
import pl.mtomanski.fsmdemo.actors.ConnectionActor.{FetchSoonestConnections, SoonestConnectionsForOrigin}
import pl.mtomanski.fsmdemo.domain.{Connection, Destination, Origin}

class ConnectionActor extends Actor {

  override def receive = {
    case FetchSoonestConnections(origin) =>
      sender() ! getSoonestConnections(origin)
  }

  private def getSoonestConnections(origin: Origin) = {
    SoonestConnectionsForOrigin(origin, ConnectionActor.connections)
  }
}

object ConnectionActor {

  def props(): Props = Props(new ConnectionActor)

  case class FetchSoonestConnections(origin: Origin)

  case class SoonestConnectionsForOrigin(origin: Origin, connections: Seq[Connection])

  // Mocked
  val destination1 = Destination(UUID.randomUUID().toString, "Wroclaw")
  val destination2 = Destination(UUID.randomUUID().toString, "Warsaw")
  val connection1 = Connection(UUID.randomUUID().toString, destination1, "18:15")
  val connection2 = Connection(UUID.randomUUID().toString, destination1, "18:30")
  val connections = Seq(connection1, connection2)
}
