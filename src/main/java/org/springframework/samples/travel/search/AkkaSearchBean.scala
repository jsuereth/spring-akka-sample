package org.springframework.samples.travel
package search

import org.springframework.stereotype.Service
import javax.inject.{Inject,Singleton}
import javax.persistence.{EntityManager, PersistenceContext}
import akka.actor._
import scala.collection.JavaConverters._
import javax.annotation.PostConstruct

import akka.pattern.ask
import akka.dispatch.Await


@Service
@Singleton
class AkkaSearchBean extends SearchService  {
  
  @(PersistenceContext @annotation.target.setter)
  var em: EntityManager = null
  
  val system =  ActorSystem("search-service")
  
  def searchActor: ActorRef = system actorFor "search-service-frontend"
  
  @PostConstruct
  def makeSearchActor = {
    // Startup....
    val hotels = em.createQuery("select h from Hotel h").getResultList.asInstanceOf[java.util.List[Hotel]].asScala
    hotels foreach em.detach
    hotels foreach println
    // Now feed data into Akka Search service.
    val searchService = system.actorOf(Props[SingleActorSearch], "search-service-frontend")
    searchService ! IndexHotels(hotels)
  }
  
  override def findHotels(criteria: SearchCriteria): java.util.List[Hotel] = {
    import collection.JavaConverters._
    import akka.util.Timeout
    import akka.util.duration._
    implicit val timeout = Timeout(5 seconds)
    Await.result((searchActor ? HotelQuery(criteria)).mapTo[Seq[Hotel]], akka.util.Duration.Inf).asJava
  }
}

