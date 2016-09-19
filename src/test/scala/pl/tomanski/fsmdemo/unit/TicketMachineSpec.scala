package pl.tomanski.fsmdemo.unit

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, Matchers, WordSpecLike}
import pl.mtomanski.fsmdemo.actors.ConnectionActor.{FetchSoonestConnections, SoonestConnectionsFromOrigin}
import pl.mtomanski.fsmdemo.actors.GatewayActor.{PaymentFailed, SoonestConnections}
import pl.mtomanski.fsmdemo.actors.PrintoutActor.{PrintOutFinished, PrintOutTicket}
import pl.mtomanski.fsmdemo.actors.ReservationActor.{CancelReservation, MakeReservation}
import pl.mtomanski.fsmdemo.domain._
import pl.mtomanski.fsmdemo.machine.TicketMachine
import pl.tomanski.fsmdemo.helpers.InMemoryCleanup

import scala.concurrent.duration._

class TicketMachineSpec extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with Matchers
  with GivenWhenThen
  with BeforeAndAfterAll
  with ImplicitSender
  with InMemoryCleanup {


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
      connectionActor.reply(SoonestConnectionsFromOrigin(connections))

      Then("Gateway actor should receive the connections")
      gatewayActor.expectMsg(SoonestConnections(connections))
    }

    "Ask Reservation actor to make a reservation for selected connection" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)
      And("Connection actor responds with soonest connections")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
      connectionActor.reply(SoonestConnectionsFromOrigin(connections))
      And("Connection is selected")
      ticketMachine ! SelectConnection(connection1)

      Then("Reservation actor should be asked to make a reservation")
      reservationActor.expectMsg(MakeReservation(connection1))
    }

    "When timeout occurs, cancel the reservation and inform gateway" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)
      And("Connection actor responds with soonest connections")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
      connectionActor.reply(SoonestConnectionsFromOrigin(connections))
      gatewayActor.expectMsg(SoonestConnections(connections))
      And("Connection is selected")
      ticketMachine ! SelectConnection(connection1)
      And("Reservation actor receives the make reservation command")
      reservationActor.expectMsg(MakeReservation(connection1))
      And("Reservation timeout passes")

      Then("Reservation actor should receive the cancel reservation command")
      reservationActor.expectMsg(10.seconds, CancelReservation(connection1)) // todo timeout from test props
      And("Gateway actor should receive PaymentFailed message")
      gatewayActor.expectMsg(10.seconds, PaymentFailed(connection1)) // todo timeout from test props
    }

    "Respond to gateway actor once the payment is made" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)
      And("Connection actor responds with soonest connections")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
      connectionActor.reply(SoonestConnectionsFromOrigin(connections))
      And("Connection is selected")
      ticketMachine ! SelectConnection(connection1)
      And("Reservation is made")
      reservationActor.expectMsg(MakeReservation(connection1))
      And("Successful payment is returned")
      ticketMachine ! PaymentSuccessful(paymentId)

      Then("PrintOut Actor should receive print out ticket command")
      printOutActor.expectMsg(PrintOutTicket(connection1))
    }

    "Notify gateway actor when printing out finishes" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)
      And("Connection actor responds with soonest connections")
      connectionActor.expectMsg(FetchSoonestConnections(origin))
      connectionActor.reply(SoonestConnectionsFromOrigin(connections))
      gatewayActor.expectMsg(SoonestConnections(connections))
      And("Connection is selected")
      ticketMachine ! SelectConnection(connection1)
      And("Reservation is made")
      reservationActor.expectMsg(MakeReservation(connection1))
      And("Successful payment is returned")
      ticketMachine ! PaymentSuccessful(paymentId)

      And("Printing out the ticket succeeded")
      printOutActor.expectMsg(PrintOutTicket(connection1))
      printOutActor.reply(PrintOutFinished(ticketNumber, connection1))

      Then("Gateway actor should be notified about the printout")
      gatewayActor.expectMsg(PrintOutFinished(ticketNumber, connection1))
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
    val printOutActor = TestProbe("printout-actor")
    val ticketMachine = system.actorOf(TicketMachine.props(gatewayActor.ref, connectionActor.ref, reservationActor.ref, printOutActor.ref))

    val originId = UUID.randomUUID().toString
    val origin = Origin(originId, "Krakow")

    val destination1 = Destination(UUID.randomUUID().toString, "Wroclaw")
    val destination2 = Destination(UUID.randomUUID().toString, "Warsaw")
    val departure1 = "18:15"
    val departure2 = "18:30"
    val connection1 = Connection("1", origin, destination1, departure1)
    val connection2 = Connection("2", origin, destination2, departure2)
    val connections = Seq(connection1, connection2)

    val paymentId = UUID.randomUUID().toString
    val ticketNumber = UUID.randomUUID().toString
  }
}
