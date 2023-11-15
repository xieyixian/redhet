package uk.ac.newcastle.enterprisemiddleware.booking;

import javax.ws.rs.core.Response;

/**
 * Custom exception for BookingService.
 */
public class BookingServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Response.Status status;
    private final Object responseObject;

    public BookingServiceException(String message, Response.Status status, Object responseObject) {
        super(message);
        this.status = status;
        this.responseObject = responseObject;
    }

    public Response.Status getStatus() {
        return status;
    }

    public Object getResponseObject() {
        return responseObject;
    }
}
