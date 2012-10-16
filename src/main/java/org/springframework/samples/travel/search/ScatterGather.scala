package org.springframework.samples.travel
package search

import akka.actor.{Actor, ActorRef, Props}

/** The "head" actor for splitting data by country.
 * 
 * Sub-categories are also "heads" by city.
 */
class CountryCategoryActor(hotels: Seq[Hotel]) extends CategoryActor(hotels) {
  protected def category(hotel: Hotel): String = 
    Option(hotel) map(_.getCountry) getOrElse ""
  protected def newChild(hotels: Seq[Hotel]): Actor =
    new CityCategoryActor(hotels)
}
/**
 * The "head" actor for splitting data by city.
 * 
 * Sub-categories are full indexes by city.
 */
class CityCategoryActor(hotels: Seq[Hotel]) extends CategoryActor(hotels) {
  protected def category(hotel: Hotel): String = 
    Option(hotel) map(_.getCity) getOrElse ""
  protected def newChild(hotels: Seq[Hotel]): Actor =
    new SingleActorSearch(hotels)
}

/** This represents a category in the scatter gather model.
 * This class can be extended to define whether it has topic nodes or
 * other categories.
 * 
 * The `category` and `newChild` methods are overridable to generate
 * specific hierarchy members of this tree.
 */
abstract class CategoryActor(hotels: Seq[Hotel]) extends Actor {
  val children = for {
    (cat, hotels) <- hotels groupBy category
    actorProps = Props(newChild(hotels))
  } yield context.actorOf(actorProps, scrubActorName(cat))
  
  
  def receive: Receive = {
    case query: HotelQuery =>
      // Create interceptor.
      val listener = sender
      val gathererProps = Props(new Gatherer(listener, children.size))
      val gatherer = context.actorOf(gathererProps)
      // Scatter to our children
      for(child <- children) {
        child.tell(query, gatherer)
      }
  }
  
  private def scrubActorName(name: String) =
    name.replaceAll("[^-\\w:@&=+,.!~*'$_;]", "-")
  
  // TODO - Configurable and mutliple splits
  protected def category(hotel: Hotel): String
  protected def newChild(hotels: Seq[Hotel]): Actor
}



/** This actor is responsible for listening to responses from topic nodes
 * and gathering all the results before forwarding on.
 */
class Gatherer(listener: ActorRef, numNodes: Long) extends Actor {
  var responses: Seq[HotelResponse] = Seq.empty
    
  def receive: Receive = {
    case response: HotelResponse =>
      responses = responses :+ response
      if(responses.size >= numNodes) joinResponses()
  }
  
  def joinResponses():  Unit = {
    // TODO - Sort appropriately.
    listener ! HotelResponse(responses flatMap (_.hotels))
    // We're done as an actor now.
    context stop self
  }
  
}