package org.springframework.samples.travel
package search

import akka.actor.Actor


class SingleActorSearch(hotels: Seq[Hotel]) extends Actor {
  
  val index: Map[String, Hotel] =
    (hotels map (hotel => uglySearchString(hotel) -> hotel))(collection.breakOut)
  
  def receive: Receive = {
    case HotelQuery(search)  => sender ! findHotels(search)
  }
  
  private def uglySearchString(h: Hotel) =
    (h.getAddress + " " + h.getName + " " + h.getCity + " " + h.getState + " " + h.getZip).toLowerCase
  
  private def findHotels(search: SearchCriteria): HotelResponse = {
    val matched = for {
      (s, hotel) <- index
      if s contains search.getSearchString.toLowerCase
    } yield hotel
    HotelResponse(matched.toSeq)
  }
}