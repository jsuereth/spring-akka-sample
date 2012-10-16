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
import javax.annotation.PreDestroy
import akka.routing.RoundRobinRouter
import collection.JavaConverters._
import akka.util.Timeout
import akka.util.duration._

@Service
@Singleton
class AkkaSearchBean extends SearchService  {
  /** The entity manager for this bean. */
  @(PersistenceContext @annotation.target.setter)
  var em: EntityManager = null
  
  /** The ActorSystem that runs under this bean. */
  val system =  ActorSystem("search-service")
  
  /** Returns the current search service front-end actor. */
  def searchActor: ActorRef = system actorFor (system / "search-service-frontend")
  
  @PostConstruct
  def makeSearchActor = {
    // Startup....
    def getHotels = {
      val hotels = em.createQuery("select h from Hotel h").getResultList.asInstanceOf[java.util.List[Hotel]].asScala
      hotels foreach em.detach
      hotels
    }
    // Now feed data into Akka Search service.
    val searchProps = Props(new SingleActorSearch(getHotels))
    val router = RoundRobinRouter(nrOfInstances=5)
    
    // Create the search actor
    system.actorOf(searchProps withRouter router , "search-service-frontend")
  }
  
  @PreDestroy
  def shutdown(): Unit = system.shutdown()
  
  override def findHotels(criteria: SearchCriteria): java.util.List[Hotel] = {
    implicit val timeout = Timeout(5 seconds)
    val response = (searchActor ? HotelQuery(criteria)).mapTo[HotelResponse]
    Await.result(response, akka.util.Duration.Inf).hotels.asJava
  }
}

