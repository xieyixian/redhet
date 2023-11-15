package uk.ac.newcastle.enterprisemiddleware.hotel;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import uk.ac.newcastle.enterprisemiddleware.area.AreaService;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;

/**
 * This Service class assumes the Control responsibility in the ECB pattern.
 * The validation is done here so that it may be used by other Boundary Resources.
 * Other Business Logic would go here as well.
 *
 * @author [Your Name]
 * @see HotelValidator
 * @see HotelRepository
 */
@Dependent
public class HotelService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    HotelValidator validator;

    @Inject
    HotelRepository crud;

    @RestClient
    AreaService areaService;
    /**
     * Returns a List of all persisted Hotel objects.
     *
     * @return List of Hotel objects
     */
    public List<Hotel> findAllHotels() {
        return crud.findAll();
    }

    /**
     * Returns a single Hotel object, specified by an id.
     *
     * @param id The id field of the Hotel to be returned
     * @return The Hotel with the specified id
     */
    public Hotel findHotelById(Long id) {
        return crud.findById(id);
    }

    /**
     * Writes the provided Hotel object to the application database.
     *
     * Validates the data in the provided Hotel object using a HotelValidator object.
     *
     * @param hotel The Hotel object to be written to the database
     * @return The Hotel object that has been successfully written to the application database
     * @throws ConstraintViolationException If validation errors exist
     */
    /**
     * Returns a single Hotel object, specified by a phone number.
     *
     * @param phoneNumber The phone number of the Hotel to be returned
     * @return The Hotel with the specified phone number
     */
    public Hotel findHotelByPhone(String phoneNumber) {
        return crud.findByPhoneNumber(phoneNumber);
    }

    /**
     * Returns a single Hotel object, specified by a postal code.
     *
     * @param postalCode The postal code of the Hotel to be returned
     * @return The Hotel with the specified postal code
     */
    List<Hotel> findHotelByPostalCode(String postalCode) {
        return crud.findAllByPostalCode(postalCode);
    }

    List<Hotel> findAllByHName(String name) {
        return crud.findAllByName(name);
    }
    /**
     * Returns a single Hotel object, specified by a location.
     *
     * @param location The location of the Hotel to be returned
     * @return The Hotel with the specified location
     */
    List<Hotel> findHotelByLocation(String location) {
        return crud.findAllByLocation(location);
    }

    public Hotel createHotel(Hotel hotel) throws ConstraintViolationException {
        log.info("HotelService.createHotel() - Creating hotel with name: " + hotel.getName());

        // Check to make sure the data fits with the parameters in the Hotel model and passes validation.
        validator.validateHotel(hotel);

        // Write the hotel to the database.
        return crud.create(hotel);
    }

    /**
     * Updates an existing Hotel object in the application database with the provided Hotel object.
     *
     * Validates the data in the provided Hotel object using a HotelValidator object.
     *
     * @param hotel The Hotel object to be passed as an update to the application database
     * @return The Hotel object that has been successfully updated in the application database
     * @throws ConstraintViolationException If validation errors exist
     */

    public Hotel updateHotel(Hotel hotel) throws ConstraintViolationException {
        log.info("HotelService.updateHotel() - Updating hotel with name: " + hotel.getName());

        // Check to make sure the data fits with the parameters in the Hotel model and passes validation.
        validator.validateHotel(hotel);

        // Either update the hotel or add it if it can't be found.
        return crud.update(hotel);
    }

    /**
     * Deletes the provided Hotel object from the application database if found there.
     *
     * @param hotel The Hotel object to be removed from the application database
     * @return The Hotel object that has been successfully removed from the application database; or null
     */

    public Hotel deleteHotel(Hotel hotel) {
        log.info("HotelService.deleteHotel() - Deleting hotel with name: " + hotel.getName());

        Hotel deletedHotel = null;

        if (hotel.getId() != null) {
            deletedHotel = crud.delete(hotel);
        } else {
            log.info("deleteHotel() - No ID was found so can't Delete.");
        }

        return deletedHotel;
    }


    public void checkHotelAvailability(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
    }
}