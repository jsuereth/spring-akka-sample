package org.springframework.samples.travel
package search

import org.springframework.stereotype.Service
import javax.inject.{Inject,Singleton}
import javax.persistence.{EntityManager, PersistenceContext}
import akka.actor._
import scala.collection.JavaConverters._
import javax.annotation.PostConstruct

@Service
@Singleton
class AkkaSearchBean extends SearchService  {
  
  @(PersistenceContext @annotation.target.setter)
  var em: EntityManager = null
  
  val system =  ActorSystem("search-service")
  
  @PostConstruct
  def makeSearchActor = {
    // Startup....
    val hotels = em.createQuery("select h from Hotel h").getResultList.asInstanceOf[java.util.List[Hotel]].asScala
    hotels foreach em.detach
    hotels foreach println
    // Now feed data into Akka Search service.
    null
  }
  
  override def findHotels(criteria: SearchCriteria): java.util.List[Hotel] = {
    println("ZOMG SEARCH: " + criteria)
    null
  }
}