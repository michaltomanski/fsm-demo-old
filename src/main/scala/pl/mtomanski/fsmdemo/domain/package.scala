package pl.mtomanski.fsmdemo

package object domain {
  type Id = String

  type TicketNumber = String

  final case class Origin(id: Id, name: String)

  final case class Destination(id: Id, name: String)

  final case class Connection(id: Id, origin: Origin, destination: Destination, departure: String)
}
