package org.springframework.samples.travel;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * A JPA-based implementation of the Booking Service. Delegates to a JPA entity
 * manager to issue data access calls against the backing repository. The
 * EntityManager reference is provided by the managing container (Spring)
 * automatically.
 */
@Service("bookingService")
@Repository
public class JpaBookingService implements BookingService {

	private EntityManager em;
	private SearchService search;

	@PersistenceContext
	public void setEntityManager(EntityManager em) {
		this.em = em;
	}
	
	@Inject
	public void setAkkaSearchService(SearchService search) {
		this.search = search;
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	public List<Booking> findBookings(String username) {
		if (username != null) {
			return em
					.createQuery(
							"select b from Booking b where b.user.username = :username order by b.checkinDate")
					.setParameter("username", username).getResultList();
		} else {
			return null;
		}
	}

	@Transactional(readOnly = true)
	public List<Hotel> findHotels(SearchCriteria criteria) {
		return search.findHotels(criteria);
	}

	@Transactional(readOnly = true)
	public Hotel findHotelById(Long id) {
		return em.find(Hotel.class, id);
	}

	@Transactional(readOnly = true)
	public Booking createBooking(Long hotelId, String username) {
		Hotel hotel = em.find(Hotel.class, hotelId);
		User user = findUser(username);
		Booking booking = new Booking(hotel, user);
		em.persist(booking);
		return booking;
	}

	@Transactional
	public void cancelBooking(Long id) {
		Booking booking = em.find(Booking.class, id);
		if (booking != null) {
			em.remove(booking);
		}
	}

	// helpers

	private String getSearchPattern(SearchCriteria criteria) {
		if (StringUtils.hasText(criteria.getSearchString())) {
			return "'%"
					+ criteria.getSearchString().toLowerCase()
							.replace('*', '%') + "%'";
		} else {
			return "'%'";
		}
	}

	private User findUser(String username) {
		return (User) em.createQuery(
				"select u from User u where u.username = :username")
				.setParameter("username", username).getSingleResult();
	}

}