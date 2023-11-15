package uk.ac.newcastle.enterprisemiddleware.guestBooking;

import uk.ac.newcastle.enterprisemiddleware.booking.Booking;
import uk.ac.newcastle.enterprisemiddleware.booking.BookingService;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
import uk.ac.newcastle.enterprisemiddleware.customer.CustomerService;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/guest-bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GuestBookingRestService {

    @Inject
    CustomerService customerService;

    @Inject
    BookingService bookingService;

    @Inject
    UserTransaction userTransaction;

    @POST
    @Transactional
    public Response createGuestBooking(GuestBooking guestBooking) {
        try {
            userTransaction.begin();

            // Save Customer
            Customer savedCustomer = customerService.create(guestBooking.getCustomer());

            // Set the saved Customer in Booking
            guestBooking.getBooking().setCustomer(savedCustomer);

            // Save Booking
            Booking savedBooking = bookingService.create(guestBooking.getBooking());

            userTransaction.commit();

            return Response.status(Response.Status.CREATED).entity(savedBooking).build();
        } catch (Exception e) {
            try {
                userTransaction.rollback();
            } catch (Exception rollbackException) {
                // Handle rollback exception
                rollbackException.printStackTrace();
            }

            // Handle the original exception
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing the request").build();
        }
    }
}
