package org.springframework.samples.travel
package search

import akka.actor.ActorRef

sealed trait SearchMessages

case class IndexHotels(hotels: Seq[Hotel]) extends SearchMessages
case class HotelQuery(criteria: SearchCriteria) extends SearchMessages
//case class HotelScatterQuery(criteria: SearchCriteria, listener: ActorRef) extends SearchMessages
case class HotelResponse(hotels: Seq[Hotel]) extends SearchMessages
