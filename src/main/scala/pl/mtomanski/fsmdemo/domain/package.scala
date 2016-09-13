package pl.mtomanski.fsmdemo

package object domain {
  type Id = String

  final case class Origin(name: String)

  final case class Destination(name: String)
}
