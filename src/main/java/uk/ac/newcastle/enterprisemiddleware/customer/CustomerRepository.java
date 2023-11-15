package uk.ac.newcastle.enterprisemiddleware.customer;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is a Repository class and connects the Service/Control layer (see {@link CustomerService}) with the
 * Domain/Entity Object (see {@link Customer}).
 *
 * There are no access modifiers on the methods making them 'package' scope. They should only be accessed by a
 * Service/Control object.
 *
 * @author Joshua Wilson
 * @see Customer
 * @see javax.persistence.EntityManager
 */
@RequestScoped
public class CustomerRepository {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    EntityManager em;

    /**
     * Returns a List of all persisted {@link Customer} objects, sorted alphabetically by name.
     *
     * @return List of Customer objects
     */
    List<Customer> findAllOrderedByName() {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_ALL, Customer.class);
        return query.getResultList();
    }

    /**
     * Returns a single Customer object, specified by a Long id.
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    public Customer findById(Long id) {
        return em.find(Customer.class, id);
    }

    /**
     * Returns a single Customer object, specified by a String email.
     *
     * If there is more than one Customer with the specified email, only the first encountered will be returned.
     *
     * @param email The email field of the Customer to be returned
     * @return The first Customer with the specified email
     */
    Customer findByEmail(String email) {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_BY_EMAIL, Customer.class).setParameter("email", email);
        return query.getSingleResult();
    }

    Customer findByPhone(String phoneNumber) {
        TypedQuery<Customer> query = em.createNamedQuery(Customer.FIND_BY_PHONE, Customer.class).setParameter("phoneNumber", phoneNumber);
        return query.getSingleResult();
    }

    /**
     * Returns a list of Customer objects, specified by a String name.
     *
     * @param name The name field of the Customers to be returned
     * @return The Customers with the specified name
     */
    List<Customer> findAllByName(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Customer> criteria = cb.createQuery(Customer.class);
        Root<Customer> customer = criteria.from(Customer.class);
        criteria.select(customer).where(cb.equal(customer.get("name"), name));
        return em.createQuery(criteria).getResultList();
    }

    /**
     * Persists the provided Customer object to the application database using the EntityManager.
     *
     * EntityManager#persist(Object) takes an entity instance, adds it to the context and makes that instance managed
     * (i.e., future updates to the entity will be tracked)
     *
     * persist(Object) will set the @GeneratedValue @Id for an object.
     *
     * @param customer The Customer object to be persisted
     * @return The Customer object that has been persisted
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Customer create(Customer customer) throws Exception {
        log.info("CustomerRepository.create() - Creating " + customer.getName());

        // Write the customer to the database.
        em.persist(customer);

        return customer;
    }

    /**
     * Updates an existing Customer object in the application database with the provided Customer object.
     *
     * EntityManager#merge(Object) creates a new instance of your entity, copies the state from the supplied entity, and
     * makes the new copy managed. The instance you pass in will not be managed (any changes you make will not be part
     * of the transaction - unless you call merge again).
     *
     * merge(Object) however must have an object with the @Id already generated.
     *
     * @param customer The Customer object to be merged with an existing Customer
     * @return The Customer that has been merged
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    Customer update(Customer customer) throws Exception {
        log.info("CustomerRepository.update() - Updating " + customer.getName());

        // Either update the customer or add it if it can't be found.
        em.merge(customer);

        return customer;
    }

    /**
     * Deletes the provided Customer object from the application database if found there.
     *
     * @param customer The Customer object to be removed from the application database
     * @return The Customer object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Customer delete(Customer customer) throws Exception {
        log.info("CustomerRepository.delete() - Deleting " + customer.getName());

        if (customer.getId() != null) {
            /*
             * The Hibernate session (aka EntityManager's persistent context) is closed and invalidated after the commit(),
             * because it is bound to a transaction. The object goes into a detached status. If you open a new persistent
             * context, the object isn't known as in a persistent state in this new context, so you have to merge it.
             *
             * Merge sees that the object has a primary key (id), so it knows it is not new and must hit the database
             * to reattach it.
             *
             * Note, there is NO remove method which would just take a primary key (id) and an entity class as argument.
             * You first need an object in a persistent state to be able to delete it.
             *
             * Therefore, we merge first and then we can remove it.
             */
            em.remove(em.merge(customer));
        } else {
            log.info("CustomerRepository.delete() - No ID was found, so can't Delete.");
        }

        return customer;
    }
}