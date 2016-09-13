import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen, Matchers, WordSpecLike}
import pl.mtomanski.fsmdemo.actors.RouteActor.FetchRoutes
import pl.mtomanski.fsmdemo.domain.{CreateTicketMachine, Origin}
import pl.mtomanski.fsmdemo.machine.TicketMachine

import scala.concurrent.duration._

class TicketMachineSpec extends TestKit(ActorSystem("test-actor-system"))
  with WordSpecLike
  with Matchers
  with GivenWhenThen
  with BeforeAndAfterAll
  with ImplicitSender {


  "TicketMachine" should {
    "Ask Route Actor for destinations when it's being created" in new Fixture {
      Given("Ticket machine in Idle state")

      When("Creating ticket machine")
      ticketMachine ! CreateTicketMachine(origin)

      Then("Route Actor should be asked for possible destinations from origin")
      routeActor.expectMsg(obj = FetchRoutes(origin), max = 10.seconds)
    }
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  trait Fixture {
    val routeActor = TestProbe("route-actor")
    val ticketMachine = system.actorOf(TicketMachine.props(routeActor.ref))

    val origin = Origin("Krakow")
  }
}
