name := """akka-sample-persistent-fsm"""

version := "1.0"

scalaVersion := "2.11.8"

mainClass := Some("PersistentFSMExample")

fork := true

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % "2.4.10",
	"com.typesafe.akka" %% "akka-persistence" % "2.4.10",
	"org.iq80.leveldb" % "leveldb" % "0.7",
	"org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
	"org.scalatest" %% "scalatest" % "3.0.0" % "test",
	"com.typesafe.akka" % "akka-testkit_2.11" % "2.4.10",
	"com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.8"
)

fork in run := true