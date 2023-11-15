package uk.ac.newcastle.enterprisemiddleware.booking;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import uk.ac.newcastle.enterprisemiddleware.contact.UniqueEmailException;
import uk.ac.newcastle.enterprisemiddleware.hotel.Hotel;
import uk.ac.newcastle.enterprisemiddleware.util.CustomerNotExist;
import uk.ac.newcastle.enterprisemiddleware.util.HotelNotExist;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;
import uk.ac.newcastle.enterprisemiddleware.util.UniquePhoneException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/bookings")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BookingRestService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    BookingService service;

    @GET
    @Operation(summary = "Fetch all Bookings", description = "Returns a JSON array of all stored Booking objects.")
    public Response retrieveAllBookings() {
        List<Booking> bookings = service.findAll();
        return Response.ok(bookings).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a Booking by id",
            description = "Returns a JSON representation of the Booking object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Booking found"),
            @APIResponse(responseCode = "404", description = "Booking with id not found")
    })
    public Response retrieveBookingById(
            @Parameter(description = "Id of Booking to be fetched")
            @Schema(minimum = "0", required = true)
            @PathParam("id")
            long id) {
        Booking booking = service.findById(id);
        if (booking == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("findById " + id + ": found Booking = " + booking);
        return Response.ok(booking).build();
    }

    @GET
    @Path("/customerId/{customerId:[0-9]+}")
    @Operation(
            summary = "Fetch a Booking by customerId",
            description = "Returns a JSON representation of the Booking object with the provided customerId."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Booking found"),
            @APIResponse(responseCode = "404", description = "Booking with id not found")
    })
    public Response retrieveBookingByCustomerId(
            @Parameter(description = "CustomerId of Booking to be fetched")
            @Schema(minimum = "0", required = true)
            @PathParam("customerId")
            long customerId) {
        List<Booking> booking = service.findByCustomerId(customerId);
        if (booking == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("findById " + customerId + ": found Booking = " + booking);
        return Response.ok(booking).build();
    }

    @POST
    @Operation(description = "Add a new Booking to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Booking created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Booking supplied in request body"),
            @APIResponse(responseCode = "409", description = "Booking supplied in request body conflicts with an existing Booking"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createBooking(
            @Parameter(description = "JSON representation of Booking object to be added to the database", required = true)
            Booking booking) {
        if (booking == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        Response.ResponseBuilder builder = null;
        try {
            booking.setId(null);
            service.create(booking);
            builder = Response.status(Response.Status.CREATED).entity(booking);
        } catch (CustomerNotExist e) {
            handleCustomerNotExistException(e);
        }catch (HotelNotExist e) {
            handleHotelNotExistException(e);
        }catch (Exception e) {
            return handleException(e);
        }
        log.info("createBooking completed. Booking = " + booking);
        return builder.build();
    }

    @PUT
    @Path("/{id:[0-9]+}")
    @Operation(description = "Update a Booking in the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Booking updated successfully"),
            @APIResponse(responseCode = "400", description = "Invalid Booking supplied in request body"),
            @APIResponse(responseCode = "404", description = "Booking with id not found"),
            @APIResponse(responseCode = "409", description = "Booking details supplied in request body conflict with another existing Booking"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response updateBooking(
            @Parameter(description = "Id of Booking to be updated", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id,
            @Parameter(description = "JSON representation of Booking object to be updated in the database", required = true)
            Booking booking) {
        if (booking == null || booking.getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (!booking.getId().equals(id)) {
            return Response.status(Response.Status.CONFLICT).build();
        }

        try {
            service.update(id, booking);
            return Response.ok(booking).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Booking from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The booking has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Booking id supplied"),
            @APIResponse(responseCode = "404", description = "Booking with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteBooking(
            @Parameter(description = "Id of Booking to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id) {
        try {
            service.delete(id);
            return Response.noContent().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    private void handleCustomerNotExistException(CustomerNotExist e) {
        Map<String, String> responseObj = new HashMap<>();
        responseObj.put("customer", "That Customer Not Exist,can not create ");
        throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
    }

    private void handleHotelNotExistException(HotelNotExist e) {
        Map<String, String> responseObj = new HashMap<>();
        responseObj.put("hotel", "That Hotel Not Exist,can not create ");
        throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
    }

    private Response handleException(Exception e) {
        if (e instanceof BookingServiceException) {
            BookingServiceException bse = (BookingServiceException) e;
            return Response.status(bse.getStatus()).entity(bse.getResponseObject()).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
