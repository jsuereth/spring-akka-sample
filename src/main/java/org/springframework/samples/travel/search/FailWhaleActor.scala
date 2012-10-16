package org.springframework.samples.travel
package search

import akka.actor.{Actor, ActorRef, Props}
import akka.actor.ReceiveTimeout
import akka.util.Timeout
import akka.util.duration._

/** This message is passed from the query interceptor -> query cache. */
case object QueryOk
case object QueryFail
case object BecomeOk
case object BecomeBad

class FailWhaleException(msg: String) extends Exception(msg)

/** This actor can put a query cache in the front of a query service. */
class FailWhaleActor(service: ActorRef) extends Actor {
  
  // Construct external failure detector.
  val failDetector = context.actorOf(Props(new FailDetector(self)), "failure-detector")
  
  // Start in a good state
  def receive = passing
  
  lazy val passing: Receive = {
    case query: HotelQuery =>
      //TODO: Create interceptor to time things.
      val listener = sender
      val interceptor = context.actorOf(Props(new FailureInterceptor(failDetector, listener)))
      service.tell(query, interceptor)
    case BecomeBad =>
      println("[][][] Fail Whale -> Becoming BAAAAAAAAAAAD [][][]")
      context become failing
  }
  
  lazy val failing: Receive = {
    case query: HotelQuery =>
      sender ! new FailWhaleException("Search Service is unavailable")
    case BecomeOk =>
      println("[][][] Fail Whale -> Becoming OK [][][]")
      context become passing
  }
}

/** Intercepts query responses and if they take longer than 1 second, fail
 * the query and notify the fail whale.
 */
class FailureInterceptor(detector: ActorRef, listener: ActorRef) extends Actor {
  def receive: Receive = {
    case ex: Exception =>
      detector ! QueryFail
      listener ! ex
      context stop self
    case response: HotelResponse =>
      listener ! response
      detector ! QueryOk
      context stop self
    case ReceiveTimeout =>
      listener ! new FailWhaleException("Search Service is unavailable")
      detector ! QueryFail
      context stop self
  }
  
  context setReceiveTimeout (1 second)
}

/** A simple detector of whether or not we're in a failure state. */
class FailDetector(whale:  ActorRef) extends Actor {
  private var badQueryCount = 0
  
  def receive: Receive = {
    case QueryOk =>   
      if(badQueryCount > 0) badQueryCount -= 1
    case QueryFail => 
      badQueryCount += 1
      if(badQueryCount > 3) {
        // Over our limit.   Flip to fail whale and wait 30 seconds.
        whale ! BecomeBad
        context setReceiveTimeout (30 seconds)
        badQueryCount = 0
      }
    case ReceiveTimeout =>
      // We waited long enough, flip the whale.
      whale ! BecomeOk
  }
}

