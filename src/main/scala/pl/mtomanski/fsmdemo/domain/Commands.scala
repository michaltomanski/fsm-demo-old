package pl.mtomanski.fsmdemo.domain

sealed trait TicketMachineCommand

case class CreateTicketMachine(origin: Origin)