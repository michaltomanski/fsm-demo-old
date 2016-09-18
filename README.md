# FSM-DEMO
This repo contains a project used for presenting Akka persistent FSM. 

It shows a very basic and simplified implementation of a train ticket machine process manager.

The customer, when going to the train station, is presented with a list of the soonest train departures. She can choose one of the connections to buy. After the connection is chosen, a reservation for the ticket is being made. After the payment is made, the process ends with printing the ticket. When no payment is noted after two minutes, the machine goes back to the refreshed connections list, canceling the reservation.

Author: Michal Tomanski

# Requirements
Sbt is required in order to run tests.

# Usage
Run `sbt test` to run tests. 


