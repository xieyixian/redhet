package uk.ac.newcastle.enterprisemiddleware.hotel;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import uk.ac.newcastle.enterprisemiddleware.contact.Contact;
import uk.ac.newcastle.enterprisemiddleware.customer.Customer;
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

@Path("/hotels")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class HotelRestService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    HotelService service;

    @GET
    @Operation(summary = "Fetch all Hotels", description = "Returns a JSON array of all stored Hotel objects.")
    public Response retrieveAllHotels(@QueryParam("name") String name) {
        List<Hotel> hotels ;

        if (name == null) {
            hotels = service.findAllHotels();
        } else {
            hotels = service.findAllByHName(name);
        }

        return Response.ok(hotels).build();
    }

    @GET
    @Path("/{id:[0-9]+}")
    @Operation(
            summary = "Fetch a Hotel by id",
            description = "Returns a JSON representation of the Hotel object with the provided id."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Hotel found"),
            @APIResponse(responseCode = "404", description = "Hotel with id not found")
    })
    public Response retrieveHotelById(
            @Parameter(description = "Id of Hotel to be fetched")
            @Schema(minimum = "0", required = true)
            @PathParam("id")
            long id) {
        Hotel hotel = service.findHotelById(id);
        if (hotel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("findById " + id + ": found Hotel = " + hotel);
        return Response.ok(hotel).build();
    }

    @POST
    @Operation(description = "Add a new Hotel to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Hotel created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Hotel supplied in request body"),
            @APIResponse(responseCode = "409", description = "Hotel supplied in request body conflicts with an existing Hotel"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createHotel(
            @Parameter(description = "JSON representation of Hotel object to be added to the database", required = true)
            Hotel hotel) {
        if (hotel == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }
        Response.ResponseBuilder builder = null;
        try {
            hotel.setId(null);
            service.createHotel(hotel);
            builder = Response.status(Response.Status.CREATED).entity(hotel);
        } catch (UniquePhoneException e) {
            handleUniquePhoneException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }
        log.info("createHotel completed. hotel = " + hotel);
        return builder.build();
    }

    @PUT
    @Path("/{id:[0-9]+}")
    @Operation(description = "Update a Hotel in the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Hotel updated successfully"),
            @APIResponse(responseCode = "400", description = "Invalid Hotel supplied in request body"),
            @APIResponse(responseCode = "404", description = "Hotel with id not found"),
            @APIResponse(responseCode = "409", description = "Hotel details supplied in request body conflict with another existing Hotel"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response updateHotel(
            @Parameter(description = "Id of Hotel to be updated", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id,
            @Parameter(description = "JSON representation of Hotel object to be updated in the database", required = true)
            Hotel hotel) {
        if (hotel == null || hotel.getId() == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if (hotel.getId() != null && hotel.getId() != id) {
            Map<String, String> responseObj = Map.of("id", "The Hotel ID in the request body must match that of the Hotel being updated");
            return Response.status(Response.Status.CONFLICT).entity(responseObj).build();
        }

        try {
            service.updateHotel(hotel);
            return Response.ok(hotel).build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Hotel from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The hotel has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Hotel id supplied"),
            @APIResponse(responseCode = "404", description = "Hotel with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteHotel(
            @Parameter(description = "Id of Hotel to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id) {
        Hotel hotel = service.findHotelById(id);
        try {
            service.deleteHotel(hotel);
            return Response.noContent().build();
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @GET
    @Path("/{hotelId}/availability")
    public String checkHotelAvailability(
            @PathParam("hotelId") Long hotelId,
            @QueryParam("checkInDate") String checkInDate,
            @QueryParam("checkOutDate") String checkOutDate) {
        // Implement your logic to check hotel availability here

        // For example, you might check a database or external service
        // to determine if the hotel is available during the specified dates.

        // If available, return a success message or some relevant information.
        // If not available, you may throw an exception or return an error message.

        // For simplicity, this example returns a string. You should customize it based on your use case.
        return "Hotel is available for booking";
    }


    @GET
    @Path("/byPhone/{phoneNumber}")
    @Operation(
            summary = "Fetch a Hotel by phone number",
            description = "Returns a JSON representation of the Hotel object with the provided phone number."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Hotel found"),
            @APIResponse(responseCode = "404", description = "Hotel with phone number not found")
    })
    public Response retrieveHotelByPhone(
            @Parameter(description = "Phone number of Hotel to be fetched", required = true)
            @PathParam("phoneNumber")
            String phoneNumber) {
        Hotel hotel = service.findHotelByPhone(phoneNumber);
        if (hotel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("findByPhone " + phoneNumber + ": found Hotel = " + hotel);
        return Response.ok(hotel).build();
    }

    @GET
    @Path("/byPostalCode/{postalCode}")
    @Operation(
            summary = "Fetch a Hotel by postal code",
            description = "Returns a JSON representation of the Hotel object with the provided postal code."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Hotel found"),
            @APIResponse(responseCode = "404", description = "Hotel with postal code not found")
    })
    public Response retrieveHotelByPostalCode(
            @Parameter(description = "Postal code of Hotel to be fetched", required = true)
            @PathParam("postalCode")
            String postalCode) {
        List<Hotel> hotel = service.findHotelByPostalCode(postalCode);
        if (hotel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("findByPostalCode " + postalCode + ": found Hotel = " + hotel);
        return Response.ok(hotel).build();
    }

    @GET
    @Path("/byLocation/{location}")
    @Operation(
            summary = "Fetch a Hotel by location",
            description = "Returns a JSON representation of the Hotel object with the provided location."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Hotel found"),
            @APIResponse(responseCode = "404", description = "Hotel with location not found")
    })
    public Response retrieveHotelByLocation(
            @Parameter(description = "Location of Hotel to be fetched", required = true)
            @PathParam("location")
            String location) {
        List<Hotel> hotel = service.findHotelByLocation(location);
        if (hotel == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("findByLocation " + location + ": found Hotel = " + hotel);
        return Response.ok(hotel).build();
    }


    private void handleUniquePhoneException(UniquePhoneException e) {
        Map<String, String> responseObj = new HashMap<>();
        responseObj.put("phone", "That phone is already used, please use a unique phone");
        throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
    }

    private Response handleException(Exception e) {
        if (e instanceof HotelServiceException) {
            HotelServiceException hse = (HotelServiceException) e;
            return Response.status(hse.getStatus()).entity(hse.getResponseObject()).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    private void handleGenericException(Exception e) {
        throw new RestServiceException(e);
    }
}
