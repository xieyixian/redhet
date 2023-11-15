package uk.ac.newcastle.enterprisemiddleware.hotel;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerRepository;
import uk.ac.newcastle.enterprisemiddleware.util.UniquePhoneException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

/**
 * This class provides methods to validate Hotel objects against arbitrary requirements.
 *
 * @author [Your Name]
 * @see Hotel
 * @see HotelRepository (if you have a repository)
 * @see Validator
 */
@ApplicationScoped
public class HotelValidator {

    @Inject
    Validator validator;
    @Inject
    HotelRepository crud;
    // You can inject HotelRepository if you need to perform additional validations

    /**
     * Validates the given Hotel object and throws validation exceptions based on the type of error.
     *
     * @param hotel The Hotel object to be validated
     * @throws ConstraintViolationException If Bean Validation errors exist
     */
    public void validateHotel(Hotel hotel) throws ConstraintViolationException {
        // Create a bean validator and check for issues.
        Set<ConstraintViolation<Hotel>> violations = validator.validate(hotel);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        if (phoneAlreadyExists(hotel.getPhoneNumber(), hotel.getId())) {
            throw new UniquePhoneException("Unique PhoneNumber Violation");
        }

        // You can perform additional custom validations here, if needed
        // For example, check if the hotel name is unique in the database
        // If you have a HotelRepository, you can use it for this purpose
        // Example: if (nameAlreadyExists(hotel.getName(), hotel.getId())) {
        //              throw new UniqueNameException("Hotel name must be unique");
        //          }
    }
    boolean phoneAlreadyExists(String phoneNumber, Long id) {
        Hotel hotel = null;
        Hotel hotelWithID = null;
        try {
            hotel = crud.findByPhoneNumber(phoneNumber);
        } catch (NoResultException e) {
            // ignore
        }

        if (hotel != null && id != null) {
            try {
                hotelWithID = crud.findById(id);
                if (hotelWithID != null && hotelWithID.getPhoneNumber().equals(phoneNumber)) {
                    hotel = null;
                }
            } catch (NoResultException e) {
                // ignore
            }
        }
        return hotel != null;
    }
    // Add additional validation methods if needed
}