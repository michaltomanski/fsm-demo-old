import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, Matchers, WordSpecLike}
import pl.mtomanski.fsmdemo.actors.ConnectionActor.{FetchSoonestConnections, SoonestConnectionsForOrigin}
import pl.mtomanski.fsmdemo.actors.GatewayActor.SoonestConnections
import pl.mtomanski.fsmdemo.actors.ReservationActor.{CancelReservation, MakeReservation}
import pl.mtomanski.fsmdemo.domain._
import pl.mtomanski.fsmdemo.machine.TicketMachine

import scala.concurrent.duration._

class TicketMachineSpec extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with Matchers
  with GivenWhenThen
  with BeforeAndAfterAll
  with ImplicitSender {


  "TicketMachine" should {
    "Ask Connection Actor for soonest connections when it's being created" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)

      Then("Connection Actor should be asked for the soonest connections from origin")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
    }

    "Inform gateway actor once soonest connections are retrieved" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)
      And("Connection actor responds with soonest connections")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
      connectionActor.reply(SoonestConnectionsForOrigin(origin, connections))

      Then("Gateway actor should receive the connections")
      gatewayActor.expectMsg(SoonestConnections(connections))
    }

    "Ask Reservation actor to make a reservation for selected connection" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)
      And("Connection actor responds with soonest connections")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
      connectionActor.reply(SoonestConnectionsForOrigin(origin, connections))
      And("Connection is selected")
      ticketMachine ! SelectConnection(connection1)

      Then("Reservation actor should be asked to make a reservation")
      reservationActor.expectMsg(MakeReservation(connection1.id))
    }

    "Ask Reservation actor to cancel the reservation when timeout occurs" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)
      And("Connection actor responds with soonest connections")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
      connectionActor.reply(SoonestConnectionsForOrigin(origin, connections))
      And("Connection is selected")
      ticketMachine ! SelectConnection(connection1)
      And("Reservation actor receives the make reservation command")
      reservationActor.expectMsg(MakeReservation(connection1.id))
      And("Reservation timeout passes")

      Then("Reservation actor should receive the cancel reservation command")
      reservationActor.expectMsg(10.seconds, CancelReservation(connection1.id)) // todo timeout from test props
    }

  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  trait Fixture {
    val gatewayActor = TestProbe("gateway-actor")
    val connectionActor = TestProbe("connection-actor")
    val reservationActor = TestProbe("reservation-actor")
    val ticketMachine = system.actorOf(TicketMachine.props(gatewayActor.ref, connectionActor.ref, reservationActor.ref))

    val originId = UUID.randomUUID().toString
    val origin = Origin(originId, "Krakow")

    val destination1 = Destination(UUID.randomUUID().toString, "Wroclaw")
    val destination2 = Destination(UUID.randomUUID().toString, "Warsaw")
    val connection1 = Connection(UUID.randomUUID().toString, destination1, "18:15")
    val connection2 = Connection(UUID.randomUUID().toString, destination1, "18:30")
    val connections = Seq(connection1, connection2)
  }
}
