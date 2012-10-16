package org.springframework.samples.travel
package search

import akka.actor.Actor

/** Note:  This is the *same* as a topic node in the scatter gather algorithm. */
class SingleActorSearch(hotels: Seq[Hotel]) extends Actor {
    
  val index: Map[String, Hotel] =
    (hotels map (hotel => makeSearchableString(hotel) -> hotel))(collection.breakOut)
  
  def receive: Receive = {
    case HotelQuery(search) if search.getSearchString == "timeout" => // Do nothing
    case HotelQuery(search) if search.getSearchString == "fail" => sys.error("O NOES - FAKE FAILUREZ")
    case HotelQuery(search)  => sender ! findHotels(search)
  }
  
  private def makeSearchableString(h: Hotel) =
    (h.getAddress + " " + h.getName + " " + h.getCity + " " + h.getState + " " + h.getZip).toLowerCase
  
  private def findHotels(search: SearchCriteria): HotelResponse = {
    val matched = for {
      (s, hotel) <- index
      if s contains search.getSearchString.toLowerCase
    } yield hotel
    HotelResponse(matched.toSeq)
  }
  
  
  override def preRestart(error: Throwable, msg: Option[Any]): Unit =
    println("[][][] restarting after exception on message: " + msg + " [][][]")
  
}