package uk.ac.newcastle.enterprisemiddleware.booking;

import uk.ac.newcastle.enterprisemiddleware.contact.UniqueEmailException;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerRepository;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelRepository;
import uk.ac.newcastle.enterprisemiddleware.util.CustomerNotExist;
import uk.ac.newcastle.enterprisemiddleware.util.HotelNotExist;
import uk.ac.newcastle.enterprisemiddleware.util.UniquePhoneException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class BookingValidator {

    @Inject
    Validator validator;

    @Inject
    CustomerRepository customercrud;
    @Inject
    HotelRepository hotlecrud;
    void validateBooking(Booking booking) throws ConstraintViolationException {
        Set<ConstraintViolation<Booking>> violations = validator.validate(booking);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(new HashSet<>(violations));
        }
        Customer customer = CustomerNotExists(booking.getCustomer().getId());
        if (customer == null) {
            throw new CustomerNotExist("Customer not Exist");
        } else {
          booking.setCustomer(customer);
        }
        Hotel hotel = HotelNotExists(booking.getHotel().getId());
        if (hotel == null) {
            throw new HotelNotExist("Hotel not Exist");
        } else {
            booking.setHotel(hotel);
        }
    }

    Customer CustomerNotExists(long customerId) {
        Customer customer = null;
        try {
            customer = customercrud.findById(customerId);
        } catch (NoResultException e) {
            // ignore
        }
        return customer;
    }
    Hotel HotelNotExists(long hotelId) {
        Hotel hotel = null;
        try {
            hotel = hotlecrud.findById(hotelId);
        } catch (NoResultException e) {
            // ignore
        }
        return hotel ;
    }

}
