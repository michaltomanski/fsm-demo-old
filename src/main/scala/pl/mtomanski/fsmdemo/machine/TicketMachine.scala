package pl.mtomanski.fsmdemo.machine

import akka.actor.{ActorRef, Props}
import akka.persistence.fsm.PersistentFSM
import pl.mtomanski.fsmdemo.actors.RouteActor.{DestinationsForOrigin, FetchRoutes}
import pl.mtomanski.fsmdemo.domain._

import scala.reflect._

class TicketMachine(routeActor: ActorRef) extends PersistentFSM[TicketMachineState, TicketMachineContext, TicketMachineEvent] {

	override def applyEvent(domainEvent: TicketMachineEvent, currentData: TicketMachineContext): TicketMachineContext = {
		domainEvent match {
			case TicketMachineCreated(id, origin) =>
				ContextWithOrigin(id, origin)
			case DestinationsFetched(destinations) =>
				currentData match {
					case ContextWithOrigin(id, origin) =>
						ContextWithDestinations(id, origin, destinations)
					case _ => ???
				}
		}
	}

	override def persistenceId: String = "TicketMachine"

	override def domainEventClassTag: ClassTag[TicketMachineEvent] = classTag[TicketMachineEvent]

	startWith(Idle, Empty)

	when(Idle) {
		case Event(CreateTicketMachine(origin), _) =>
			println("DD")
			routeActor ! FetchRoutes(origin)
			// todo generate id
			goto(FetchingDestinations) applying TicketMachineCreated("id", origin)
	}

	when(FetchingDestinations) {
		case Event(DestinationsForOrigin(_, destinations), data: ContextWithOrigin) =>
			goto(WaitingForDestinationSelection) applying DestinationsFetched(destinations)
	}

	when(WaitingForDestinationSelection) {
		case _ => ???
	}

	whenUnhandled {
		case Event(event, data) =>
			println(s"Unhandled event $event while data is $data in state $stateName")
			stay()
	}

	override def onRecoveryCompleted(): Unit = {
		println("Recovery completed")
	}
}

object TicketMachine {
	def props(routeActor: ActorRef): Props = Props(new TicketMachine(routeActor))
}