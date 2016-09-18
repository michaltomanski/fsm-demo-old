package pl.mtomanski.fsmdemo.domain

sealed trait TicketMachineCommand

case class CreateTicketMachine(origin: Origin)

case class SelectConnection(connection: Connection)

case class PaymentSuccessfull(ticketMachineId: Id, paymentId: Id)