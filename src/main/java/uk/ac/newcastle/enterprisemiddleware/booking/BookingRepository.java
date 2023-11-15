package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.logging.Logger;

@RequestScoped
public class BookingRepository {
    @Inject
    @Named("logger")
    Logger log;
    @Inject
    EntityManager em;
    public List<Booking> findAll() {
        return em.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();
    }

    public Booking findById(Long id) {
        return em.find(Booking.class, id);
    }

    public void create(Booking booking) {
        em.persist(booking);
    }

    public void update(Long id, Booking booking) {
        Booking existingBooking = em.find(Booking.class, id);
        if (existingBooking != null) {
            existingBooking.setCustomer(booking.getCustomer());
            existingBooking.setHotelId(booking.getHotelId());
            existingBooking.setCheckInDate(booking.getCheckInDate());
            existingBooking.setCheckOutDate(booking.getCheckOutDate());
        }
    }

    public void delete(Long id) {
        Booking booking = em.find(Booking.class, id);
        if (booking != null) {
            em.remove(booking);
        }
    }

    public List<Booking> findByCustomerId(long customerId) {
        TypedQuery<Booking> query = em.createNamedQuery(Booking.FIND_BY_CUSTOMERID, Booking.class).setParameter("customerId", customerId);
        return query.getResultList();
    }
}
