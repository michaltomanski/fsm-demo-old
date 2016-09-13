package pl.mtomanski.fsmdemo.actors

import akka.actor.{Actor, Props}
import pl.mtomanski.fsmdemo.actors.RouteActor.{DestinationsForOrigin, FetchRoutes}
import pl.mtomanski.fsmdemo.domain.{Destination, Origin}

class RouteActor extends Actor {

  override def receive = {
    case FetchRoutes(origin) =>
      sender() ! getDestinations(origin)
  }

  private def getDestinations(origin: Origin) = {
    DestinationsForOrigin(origin, RouteActor.destinations)
  }
}

object RouteActor {

  def props(): Props = Props(new RouteActor)

  case class FetchRoutes(origin: Origin)

  case class DestinationsForOrigin(origin: Origin, destinations: Seq[Destination])

  // Mocked
  val destinations = Seq(Destination("Wroclaw"), Destination("Warsaw"))

}
