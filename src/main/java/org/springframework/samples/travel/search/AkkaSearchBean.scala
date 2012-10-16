package org.springframework.samples.travel
package search

import org.springframework.stereotype.Service
import javax.inject.Inject
import javax.persistence.{EntityManager, PersistenceContext}

import akka.actor._

@Service
class AkkaSearchBean(@PersistenceContext em: EntityManager) extends SearchService  {
  
  val system =  ActorSystem("search-service")
  
  
  override def findHotels(criteria: SearchCriteria): java.util.List[Hotel] = {
    println("ZOMG SEARCH: " + criteria)
    null
  }
  
}