package uk.ac.newcastle.enterprisemiddleware.hotel;

import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * This Repository class provides data access operations for Hotel entities.
 * It uses JPA (Java Persistence API) for interacting with the database.
 *
 * @author [Your Name]
 */
@ApplicationScoped
public class HotelRepository {

    @PersistenceContext
    EntityManager em;

    /**
     * Returns a List of all persisted Hotel entities.
     *
     * @return List of Hotel entities
     */
    public List<Hotel> findAll() {
        TypedQuery<Hotel> query = em.createQuery("SELECT h FROM Hotel h", Hotel.class);
        return query.getResultList();
    }

    /**
     * Returns a single Hotel entity, specified by an id.
     *
     * @param id The id field of the Hotel to be returned
     * @return The Hotel with the specified id
     */
    public Hotel findById(Long id) {
        return em.find(Hotel.class, id);
    }

    /**
     * Writes the provided Hotel entity to the application database.
     *
     * @param hotel The Hotel entity to be written to the database
     * @return The Hotel entity that has been successfully written to the application database
     */
    public Hotel create(Hotel hotel) {
        em.persist(hotel);
        return hotel;
    }

    /**
     * Updates an existing Hotel entity in the application database with the provided Hotel entity.
     *
     * @param hotel The Hotel entity to be passed as an update to the application database
     * @return The Hotel entity that has been successfully updated in the application database
     */
    public Hotel update(Hotel hotel) {
        return em.merge(hotel);
    }

    /**
     * Deletes the provided Hotel entity from the application database if found there.
     *
     * @param hotel The Hotel entity to be removed from the application database
     * @return The Hotel entity that has been successfully removed from the application database; or null
     */
    public Hotel delete(Hotel hotel) {
        Hotel deletedHotel = findById(hotel.getId());
        if (deletedHotel != null) {
            em.remove(deletedHotel);
        }
        return deletedHotel;
    }

    /**
     * Returns a List of all persisted Hotel entities with the specified location.
     *
     * @param location The location field of the Hotel entities to be returned
     * @return List of Hotel entities with the specified location
     */
    public List<Hotel> findAllByLocation(String location) {
        TypedQuery<Hotel> query = em.createQuery("SELECT h FROM Hotel h WHERE h.location = :location", Hotel.class);
        query.setParameter("location", location);
        return query.getResultList();
    }

    /**
     * Returns a single Hotel entity, specified by a phone number.
     *
     * @param phoneNumber The phone number field of the Hotel to be returned
     * @return The Hotel with the specified phone number
     */
    public Hotel findByPhoneNumber(String phoneNumber) {
        TypedQuery<Hotel> query = em.createNamedQuery(Hotel.FIND_BY_PHONE, Hotel.class).setParameter("phoneNumber", phoneNumber);;
        query.setParameter("phoneNumber", phoneNumber);
        return query.getSingleResult();
    }

    /**
     * Returns a List of all persisted Hotel entities with the specified postal code.
     *
     * @param postalCode The postal code field of the Hotel entities to be returned
     * @return List of Hotel entities with the specified postal code
     */
    public List<Hotel> findAllByPostalCode(String postalCode) {
        TypedQuery<Hotel> query = em.createNamedQuery(Hotel.FIND_BY_POSTALCODE, Hotel.class).setParameter("postalCode", postalCode);
        query.setParameter("postalCode", postalCode);
        return query.getResultList();
    }


    /**
     * Returns a list of Hotel objects, specified by a String name.
     *
     * @param name The name field of the Customers to be returned
     * @return The Customers with the specified name
     */
    List<Hotel> findAllByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Hotel> criteria = cb.createQuery(Hotel.class);
        Root<Hotel> hotel = criteria.from(Hotel.class);
        criteria.select(hotel).where(cb.equal(hotel.get("name"), name));
        return em.createQuery(criteria).getResultList();
    }
}
