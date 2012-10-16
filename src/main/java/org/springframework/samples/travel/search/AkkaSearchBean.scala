package org.springframework.samples.travel
package search

import org.springframework.stereotype.Service
import javax.inject.Inject

@Service
class AkkaSearchBean extends SearchService  {
  override def findHotels(criteria: SearchCriteria): java.util.List[Hotel] = {
    println("ZOMG SEARCH: " + criteria)
    null
  }
  
}