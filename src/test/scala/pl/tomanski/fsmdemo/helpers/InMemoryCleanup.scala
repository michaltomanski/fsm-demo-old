package pl.tomanski.fsmdemo.helpers

import akka.actor.ActorSystem
import akka.persistence.inmemory.extension.{ InMemoryJournalStorage, InMemorySnapshotStorage, StorageExtension }
import akka.testkit.TestProbe
import org.scalatest.{ BeforeAndAfterEach, Suite }

/*
 * https://github.com/dnvriend/akka-persistence-inmemory#clearing-journal-and-snapshot-messages
 */
trait InMemoryCleanup extends BeforeAndAfterEach { _: Suite =>

  implicit def system: ActorSystem

  override protected def beforeEach(): Unit = {
    val tp = TestProbe()
    tp.send(StorageExtension(system).journalStorage, InMemoryJournalStorage.ClearJournal)
    tp.expectMsg(akka.actor.Status.Success(""))
    tp.send(StorageExtension(system).snapshotStorage, InMemorySnapshotStorage.ClearSnapshots)
    tp.expectMsg(akka.actor.Status.Success(""))
    super.beforeEach()
  }
}