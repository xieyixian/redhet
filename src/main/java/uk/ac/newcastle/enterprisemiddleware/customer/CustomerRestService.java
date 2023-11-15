package uk.ac.newcastle.enterprisemiddleware.customer;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import uk.ac.newcastle.enterprisemiddleware.contact.UniqueEmailException;
import uk.ac.newcastle.enterprisemiddleware.util.RestServiceException;
import uk.ac.newcastle.enterprisemiddleware.util.UniquePhoneException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Path("/customers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerRestService {

    @Inject
    @Named("logger")
    Logger log;

    @Inject
    CustomerService service;

    @GET
    @Operation(summary = "Fetch all Customers", description = "Returns a JSON array of all stored Customer objects.")
    public Response getAllCustomers(@QueryParam("name") String name) {
        List<Customer> customers;
        if (name == null) {
            customers = service.findAllOrderedByName();
        } else {
            customers = service.findAllByName(name);
        }
        return Response.ok(customers).build();
    }

    @GET
    @Path("/email/{email:.+[%40|@].+}")
    @Operation(
            summary = "Fetch a Customer by Email",
            description = "Returns a JSON representation of the Customer object with the provided email."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "Customer found"),
            @APIResponse(responseCode = "404", description = "Customer with email not found")
    })
    public Response getCustomerByEmail(
            @Parameter(description = "Email of Customer to be fetched", required = true)
            @PathParam("email")
            String email) {

        Customer customer;
        try {
            customer = service.findByEmail(email);
        } catch (NoResultException e) {
            throw new RestServiceException("No Customer with the email " + email + " was found!", Response.Status.NOT_FOUND);
        }
        return Response.ok(customer).build();
    }


    /**
     * <p>Search for and return a Customer identified by phone number.</p>
     *
     * @param phoneNumber The string parameter value provided as a Customer's phone number
     * @return A Response containing a single Customer
     */
    @GET
    @Path("/phone/{phoneNumber}")
    @Operation(
            summary = "Fetch a Customer by Phone Number",
            description = "Returns a JSON representation of the Customer object with the provided phone number."
    )
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description ="Customer found"),
            @APIResponse(responseCode = "404", description = "Customer with phone number not found")
    })
    public Response getCustomerByPhone(
            @Parameter(description = "Phone number of Customer to be fetched", required = true)
            @PathParam("phoneNumber")
            String phoneNumber) {

        Customer customer;
        try {
            customer = service.findByPhoneNumber(phoneNumber);
        } catch (NoResultException e) {
            // Verify that the customer exists. Return 404 if not present.
            throw new RestServiceException("No Customer with the phone number " + phoneNumber + " was found!", Response.Status.NOT_FOUND);
        }

        return Response.ok(customer).build();
    }

    @SuppressWarnings("unused")
    @POST
    @Operation(description = "Add a new Customer to the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "201", description = "Customer created successfully."),
            @APIResponse(responseCode = "400", description = "Invalid Customer supplied in request body"),
            @APIResponse(responseCode = "409", description = "Customer supplied in request body conflicts with an existing Customer"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response createCustomer(
            @Parameter(description = "JSON representation of Customer object to be added to the database", required = true)
            Customer customer) {

        if (customer == null) {
            throw new RestServiceException("Bad Request", Response.Status.BAD_REQUEST);
        }

        // Initialize builder with a default response
        //Response.ResponseBuilder builder = Response.status(Response.Status.CREATED).entity(customer);

        Response.ResponseBuilder builder = null;
        try {
            customer.setId(null);
            service.create(customer);
            builder = Response.status(Response.Status.CREATED).entity(customer);

        } catch (ConstraintViolationException ce) {
            handleConstraintViolationException(ce);
        } catch (UniqueEmailException e) {
            handleUniqueEmailException(e);
        } catch (UniquePhoneException e) {
            handleUniquePhoneException(e);
        } catch (Exception e) {
            handleGenericException(e);
        }

        log.info("createCustomer completed. Customer = " + customer);
        return builder.build();
    }


    @DELETE
    @Path("/{id:[0-9]+}")
    @Operation(description = "Delete a Customer from the database")
    @APIResponses(value = {
            @APIResponse(responseCode = "204", description = "The customer has been successfully deleted"),
            @APIResponse(responseCode = "400", description = "Invalid Customer id supplied"),
            @APIResponse(responseCode = "404", description = "Customer with id not found"),
            @APIResponse(responseCode = "500", description = "An unexpected error occurred whilst processing the request")
    })
    @Transactional
    public Response deleteCustomer(
            @Parameter(description = "Id of Customer to be deleted", required = true)
            @Schema(minimum = "0")
            @PathParam("id")
            long id) {

        // Initialize builder with a default response
        Response.ResponseBuilder builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);

        Customer customer = service.findById(id);
        try {
            handleCustomerNotFound(id);

            service.delete(customer);
            builder = Response.noContent();
        } catch (Exception e) {
            handleGenericException(e);
            // Set a specific response in case of a generic exception
            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
        }

        log.info("deleteCustomer completed. Customer = " + customer);
        return builder.build();
    }

    // Helper methods for exception handling
    private void handleIdMismatch(long id, Customer customer) {
        if (customer.getId() != null && customer.getId() != id) {
            Map<String, String> responseObj = new HashMap<>();
            responseObj.put("id", "The Customer ID in the request body must match that of the Customer being updated");
            throw new RestServiceException("Customer details supplied in request body conflict with another Customer",
                    responseObj, Response.Status.CONFLICT);
        }
    }

    private void handleCustomerNotFound(long id) {
        Customer existingCustomer = service.findById(id);
        if (existingCustomer == null) {
            throw new RestServiceException("No Customer with the id " + id + " was found!", Response.Status.NOT_FOUND);
        }
    }

    private void handleConstraintViolationException(ConstraintViolationException ce) {
        Map<String, String> responseObj = new HashMap<>();

        for (ConstraintViolation<?> violation : ce.getConstraintViolations()) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        throw new RestServiceException("Bad Request", responseObj, Response.Status.BAD_REQUEST, ce);
    }

    private void handleUniqueEmailException(UniqueEmailException e) {
        Map<String, String> responseObj = new HashMap<>();
        responseObj.put("email", "That email is already used, please use a unique email");
        throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
    }

    private void handleUniquePhoneException(UniquePhoneException e) {
        Map<String, String> responseObj = new HashMap<>();
        responseObj.put("phone", "That phone is already used, please use a unique phone");
        throw new RestServiceException("Bad Request", responseObj, Response.Status.CONFLICT, e);
    }
    private void handleGenericException(Exception e) {
        throw new RestServiceException(e);
    }
}
