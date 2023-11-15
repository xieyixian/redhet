package uk.ac.newcastle.enterprisemiddleware.booking;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import uk.ac.newcastle.enterprisemiddleware.area.AreaService;
import uk.ac.newcastle.enterprisemiddleware.hotel.HotelService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

@Dependent
public class BookingService {

    @Inject
    @Named("logger")
    private Logger log;

    @Inject
    private BookingValidator validator;

    @Inject
    private BookingRepository crud;

    @RestClient
    AreaService areaService;

    public List<Booking> findAll() {
        return crud.findAll();
    }

    public Booking findById(Long id) {
        return crud.findById(id);
    }

    List<Booking> findByCustomerId(long customerId) {
        return crud.findByCustomerId(customerId);
    }

    public Booking create(Booking booking) throws InvalidBookingException, InvalidHotelException {
        log.info("BookingService.create() - Creating booking with ID: " + booking.getId());

        validateBooking(booking);
        checkHotelAvailability(booking);

        crud.create(booking);
        return booking;
    }

    public void update(Long id, Booking booking) throws InvalidBookingException, InvalidHotelException {
        log.info("BookingService.update() - Updating booking with ID: " + id);

        validateBooking(booking);
        checkHotelAvailability(booking);

        crud.update(id, booking);
    }

    public void delete(Long id) throws Exception {
        log.info("BookingService.delete() - Deleting booking with ID: " + id);

        crud.delete(id);
    }

    private void validateBooking(Booking booking) throws InvalidBookingException {
        validator.validateBooking(booking);
    }

    private void checkHotelAvailability(Booking booking) throws InvalidHotelException {
        try {
            HotelService hotelService = new HotelService();
            hotelService.checkHotelAvailability(
                    booking.getHotel().getId(),
                    booking.getCheckInDate(),
                    booking.getCheckOutDate()
            );
        } catch (ClientErrorException e) {
            if (e.getResponse().getStatusInfo() == Response.Status.NOT_FOUND) {
                throw new InvalidHotelException("The hotel with ID " + booking.getHotel().getId() + " does not exist", e);
            } else {
                throw e;
            }
        }
    }
}
