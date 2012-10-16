package org.springframework.samples.travel;

import java.util.List;

/**
 * This service is responsible for fast/scalable search index of hotels.
 */
public interface SearchService {
	public  List<Hotel> findHotels(SearchCriteria criteria);
}