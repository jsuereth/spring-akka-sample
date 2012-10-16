package org.springframework.samples.travel
package search

import akka.actor.Actor


class SingleActorSearch extends Actor {
  var index: Seq[Hotel] = Seq.empty
  
  def receive: Receive = {
    case IndexHotels(hotels) => addHotels(hotels)
    case HotelQuery(search)  => sender ! findHotels(search)
  }
  
  private def findHotels(search: SearchCriteria): HotelResponse = 
    HotelResponse(index)
  
  private def addHotels(hotels: Seq[Hotel]): Unit = {
    index = hotels
  }
}