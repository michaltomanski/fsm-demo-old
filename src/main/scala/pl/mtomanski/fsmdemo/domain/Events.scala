package pl.mtomanski.fsmdemo.domain


sealed trait TicketMachineEvent

case class TicketMachineCreated(id: Id, origin: Origin) extends TicketMachineEvent

case class DestinationsFetched(destinations: Seq[Destination]) extends TicketMachineEvent

