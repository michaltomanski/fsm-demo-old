package pl.mtomanski.fsmdemo.machine

import akka.actor.{ActorRef, Props}
import akka.persistence.fsm.PersistentFSM
import com.typesafe.config.ConfigFactory
import pl.mtomanski.fsmdemo.actors.ConnectionActor.{FetchSoonestConnections, SoonestConnectionsForOrigin}
import pl.mtomanski.fsmdemo.actors.GatewayActor.SoonestConnections
import pl.mtomanski.fsmdemo.actors.ReservationActor.{CancelReservation, MakeReservation}
import pl.mtomanski.fsmdemo.domain.{ConnectionSelected, _}

import scala.concurrent.duration._
import scala.reflect._

class TicketMachine(gatewayActor: ActorRef, connectionActor: ActorRef, reservationActor: ActorRef) extends PersistentFSM[TicketMachineState, TicketMachineContext, TicketMachineEvent] {

	//val reservationTimeout = ConfigFactory.load().getString("reservation-timeout")
	val reservationTimeout = 2.seconds // todo props

	override def applyEvent(domainEvent: TicketMachineEvent, currentData: TicketMachineContext): TicketMachineContext = {
		domainEvent match {
			case TicketMachineCreated(id, origin) =>
				ContextWithOrigin(id, origin)
			case SoonestConnectionsFetched(connections) =>
				currentData match {
					case ContextWithOrigin(id, origin) =>
						ContextWithConnections(id, origin, connections)
					case _ => ???
				}
			case ConnectionSelected(selectedConnection) =>
				currentData match {
					case ContextWithConnections(id, origin, connections) =>
						ContextWithSelectedConnection(id, origin, selectedConnection)
				}
			case PaymentMade(paymentId) =>
				currentData match {
					case ContextWithSelectedConnection(id, origin, selectedConnection) =>
						ContextWithPayment(id, origin, selectedConnection, paymentId)
				}
			case ReservationTimeoutOccurred =>
				currentData match {
					case ContextWithSelectedConnection(id, origin, _) =>
						ContextWithOrigin(id, origin)
				}
		}
	}

	override def persistenceId: String = "TicketMachine"

	override def domainEventClassTag: ClassTag[TicketMachineEvent] = classTag[TicketMachineEvent]

	startWith(Idle, Empty)

	when(Idle) {
		case Event(CreateTicketMachine(origin), _) =>
			val id = TicketMachineIdGenerator.generate
			goto(FetchingSoonestConnections) applying TicketMachineCreated(id, origin)
	}

	when(FetchingSoonestConnections) {
		case Event(SoonestConnectionsForOrigin(_, connections), data: ContextWithOrigin) =>
			goto(WaitingForConnectionSelection) applying SoonestConnectionsFetched(connections)
	}

	when(WaitingForConnectionSelection) {
		case Event(SelectConnection(connection), data: ContextWithConnections) =>
			goto(WaitingForPayment) applying ConnectionSelected(connection)
	}

	when(WaitingForPayment, reservationTimeout) {
		case Event(PaymentSuccessfull(_, paymentId), data: ContextWithSelectedConnection) =>
			goto(PrintingTickets) applying PaymentMade(paymentId)
		case Event(StateTimeout, _) =>
			goto(FetchingSoonestConnections) applying ReservationTimeoutOccurred
	}

	when(PrintingTickets) {
		case _ => ???
	}

	whenUnhandled {
		case Event(event, data) =>
			println(s"Unhandled event $event while data is $data in state $stateName")
			stay()
	}

	onTransition {
		case Idle -> FetchingSoonestConnections =>
			nextStateData match {
				case ContextWithOrigin(id, origin) => connectionActor ! FetchSoonestConnections(origin)
			}
		case FetchingSoonestConnections -> WaitingForConnectionSelection =>
			nextStateData match {
				case ContextWithConnections(_, _, connections) => gatewayActor ! SoonestConnections(connections)
			}
		case WaitingForConnectionSelection -> WaitingForPayment =>
			nextStateData match {
				case ContextWithSelectedConnection(_, _, selectedConnection) =>
					reservationActor ! MakeReservation(selectedConnection.id)
			}
		case WaitingForPayment -> FetchingSoonestConnections =>
			stateData match {
				case ContextWithSelectedConnection(_, origin, selectedConnection) =>
					reservationActor ! CancelReservation(selectedConnection.id)
			}
	}

	override def onRecoveryCompleted(): Unit = {
		println("Recovery completed")
	}
}

object TicketMachine {
	def props(gatewayActor: ActorRef, connectionActor: ActorRef, reservationActor: ActorRef): Props = Props(new TicketMachine(gatewayActor, connectionActor, reservationActor))
}