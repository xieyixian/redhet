package uk.ac.newcastle.enterprisemiddleware.customer;

import uk.ac.newcastle.enterprisemiddleware.contact.UniqueEmailException;
import uk.ac.newcastle.enterprisemiddleware.util.UniquePhoneException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides methods to check Customer objects against arbitrary requirements.
 *
 * @author Joshua Wilson
 * @see Customer
 * @see CustomerRepository
 * @see javax.validation.Validator
 */
@ApplicationScoped
public class CustomerValidator {
    @Inject
    Validator validator;

    @Inject
    CustomerRepository crud;

    /**
     * Validates the given Customer object and throws validation exceptions based on the type of error.
     * If the error is standard bean validation errors, it will throw a ConstraintViolationException
     * with the set of constraints violated.
     * If the error is caused because an existing customer with the same email is registered,
     * it throws a regular validation exception so that it can be interpreted separately.
     *
     * @param customer The Customer object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     * @throws ValidationException If a customer with the same email already exists
     */
    void validateCustomer(Customer customer) throws ConstraintViolationException, ValidationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }

        // Check the uniqueness of the email address
        if (emailAlreadyExists(customer.getEmail(), customer.getId())) {
            throw new UniqueEmailException("Unique Email Violation");
        }

        if (phoneAlreadyExists(customer.getPhoneNumber(), customer.getId())) {
            throw new UniquePhoneException("Unique PhoneNumber Violation");
        }
    }

    /**
     * Checks if a customer with the same email address is already registered.
     * This is the only way to easily capture the "@UniqueConstraint(columnNames = "email")"
     * constraint from the Customer class.
     *
     * Since Update will use an email that is already in the database,
     * we need to make sure that it is the email from the record being updated.
     *
     * @param email The email to check is unique
     * @param id The user id to check the email against if it was found
     * @return boolean which represents whether the email was found,
     * and if so, if it belongs to the user with id
     */
    boolean emailAlreadyExists(String email, Long id) {
        Customer customer = null;
        Customer customerWithID = null;
        try {
            customer = crud.findByEmail(email);
        } catch (NoResultException e) {
            // ignore
        }

        if (customer != null && id != null) {
            try {
                customerWithID = crud.findById(id);
                if (customerWithID != null && customerWithID.getEmail().equals(email)) {
                    customer = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return customer != null;
    }

    boolean phoneAlreadyExists(String phoneNumber, Long id) {
        Customer customer = null;
        Customer customerWithID = null;
        try {
            customer = crud.findByPhone(phoneNumber);
        } catch (NoResultException e) {
            // ignore
        }

        if (customer != null && id != null) {
            try {
                customerWithID = crud.findById(id);
                if (customerWithID != null && customerWithID.getPhoneNumber().equals(phoneNumber)) {
                    customer = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return customer != null;
    }

}