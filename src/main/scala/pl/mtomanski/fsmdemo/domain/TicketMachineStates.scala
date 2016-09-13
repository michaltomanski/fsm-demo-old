package pl.mtomanski.fsmdemo.domain

import akka.persistence.fsm.PersistentFSM.FSMState

sealed trait TicketMachineState extends FSMState

case object Idle extends TicketMachineState {
  override def identifier: String = "Idle"
}

case object FetchingDestinations extends TicketMachineState {
  override def identifier: String = "FetchingDestinations"
}

case object WaitingForDestinationSelection extends TicketMachineState {
  override def identifier: String = "WaitingForDestinationSelection"
}

case object WaitingForConnections extends TicketMachineState {
  override def identifier: String = "WaitingForConnections"
}

case object WaitingForConnectionSelection extends TicketMachineState {
  override def identifier: String = "WaitingForConnectionSelection"
}

case object WaitingForConfirmation extends TicketMachineState {
  override def identifier: String = "WaitingForConfirmation"
}

case object WaitingForPayment extends TicketMachineState {
  override def identifier: String = "WaitingForPayment"
}

case object PrintingTickets extends TicketMachineState {
  override def identifier: String = "PrintingTickets"
}