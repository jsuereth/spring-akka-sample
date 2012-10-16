package org.springframework.samples.travel
package search

import akka.actor.Actor


class SingleActorSearch extends Actor {
  var index = null
  
  def receive: Receive = {
    case IndexHotels(hotels) => addHotels(hotels)
    case HotelQuery(search)  => sender ! findHotels(search)
  }
  
  private def findHotels(search: SearchCriteria): HotelResponse = 
    HotelResponse(Seq.empty)
  
  private def addHotels(hotels: Seq[Hotel]): Unit = {
    
  }
}