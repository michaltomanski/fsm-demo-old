package pl.mtomanski.fsmdemo

package object domain {
  type Id = String

  final case class Origin(id: Id, name: String)

  final case class Destination(id: Id, name: String)

  final case class Connection(id: Id, destination: Destination, departure: String)
}
