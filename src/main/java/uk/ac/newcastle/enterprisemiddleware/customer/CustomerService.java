package uk.ac.newcastle.enterprisemiddleware.customer;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import uk.ac.newcastle.enterprisemiddleware.area.AreaService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.logging.Logger;

@Dependent
public class CustomerService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    CustomerValidator validator;

    @Inject
    CustomerRepository crud;
    @RestClient
    AreaService areaService;
    /**
     * Returns a List of all persisted {@link Customer} objects, sorted alphabetically by name.
     *
     * @return List of Customer objects
     */
    List<Customer> findAllOrderedByName() {
        return crud.findAllOrderedByName();
    }

    /**
     * Returns a single Customer object, specified by a Long id.
     *
     * @param id The id field of the Customer to be returned
     * @return The Customer with the specified id
     */
    Customer findById(Long id) {
        return crud.findById(id);
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
        return crud.findByEmail(email);
    }

    /**
     * Returns a single Customer object, specified by a String email.
     *
     * If there is more than one Customer with the specified email, only the first encountered will be returned.
     *
     * @param email The email field of the Customer to be returned
     * @return The first Customer with the specified email
     */
    Customer findByPhoneNumber(String phoneNumber) {
        return crud.findByPhone(phoneNumber);
    }



    /**
     * Returns a single Customer object, specified by a String name.
     *
     * @param name The name field of the Customer to be returned
     * @return The first Customer with the specified name
     */
    List<Customer> findAllByName(String name) {
        return crud.findAllByName(name);
    }

    /**
     * Writes the provided Customer object to the application database.
     *
     * Validates the data in the provided Customer object using a {@link CustomerValidator} object.
     *
     * @param customer The Customer object to be written to the database using a {@link CustomerRepository} object
     * @return The Customer object that has been successfully written to the application database
     * @throws ConstraintViolationException, ValidationException, Exception
     */
    public Customer create(Customer customer) throws Exception {
        log.info("CustomerService.create() - Creating " + customer.getName());
        // Check to make sure the data fits with the parameters in the Customer model and passes validation.
        validator.validateCustomer(customer);

        // Write the customer to the database.
        return crud.create(customer);
    }
    private void validateUniquePhoneNumber(String phoneNumber) {

        Customer existingCustomer = crud.findByPhone(phoneNumber);
        if (existingCustomer != null) {
            throw new IllegalArgumentException("That phone number is already used, please use a unique phone number");
        }
    }
        /**
         * Updates an existing Customer object in the application database with the provided Customer object.
         *
         * Validates the data in the provided Customer object using a CustomerValidator object.
         *
         * @param customer The Customer object to be passed as an update to the application database
         * @return The Customer object that has been successfully updated in the application database
         * @throws ConstraintViolationException, ValidationException, Exception
         */
    Customer update(Customer customer) throws Exception {
        log.info("CustomerService.update() - Updating " + customer.getName());

        // Check to make sure the data fits with the parameters in the Customer model and passes validation.
        validator.validateCustomer(customer);

        // Either update the customer or add it if it can't be found.
        return crud.update(customer);
    }

    /**
     * Deletes the provided Customer object from the application database if found there.
     *
     * @param customer The Customer object to be removed from the application database
     * @return The Customer object that has been successfully removed from the application database; or null
     * @throws Exception
     */
    Customer delete(Customer customer) throws Exception {
        log.info("delete() - Deleting " + customer.toString());

        Customer deletedCustomer = null;

        if (customer.getId() != null) {
            deletedCustomer = crud.delete(customer);
        } else {
            log.info("delete() - No ID was found so can't Delete.");
        }

        return deletedCustomer;
    }


}
