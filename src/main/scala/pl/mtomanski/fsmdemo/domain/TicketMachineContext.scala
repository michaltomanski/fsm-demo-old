package pl.mtomanski.fsmdemo.domain

sealed trait TicketMachineContext

case object Empty extends TicketMachineContext

final case class ContextWithOrigin(id: Id, origin: Origin) extends TicketMachineContext

final case class ContextWithDestinations(id: Id, origin: Origin, availableDestinations: Seq[Destination]) extends TicketMachineContext

final case class ContextWithDestinationSelected(id: Id, origin: Origin, availableDestinations: Seq[Destination], destination: Destination) extends TicketMachineContext