package pl.mtomanski.fsmdemo.actors

import akka.actor.{Actor, Props}
import pl.mtomanski.fsmdemo.actors.ReservationActor.MakeReservation

class ReservationActor extends Actor {
  override def receive: Receive = {
    case MakeReservation(connectionId) => println(s"Reservation made for connection $connectionId")
  }
}

object ReservationActor {
  def props(): Props = Props(new ReservationActor)

  case class MakeReservation(connectionId: String)
  case class CancelReservation(connectionId: String)
}
