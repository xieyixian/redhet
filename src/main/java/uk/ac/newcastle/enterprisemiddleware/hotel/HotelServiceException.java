package uk.ac.newcastle.enterprisemiddleware.hotel;

import javax.ws.rs.core.Response;

/**
 * Custom exception class for handling errors specific to the HotelService.
 */
public class HotelServiceException extends RuntimeException {

    private Response.Status status;
    private Object responseObject;

    public HotelServiceException(String message, Response.Status status) {
        super(message);
        this.status = status;
    }

    public HotelServiceException(String message, Response.Status status, Object responseObject) {
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
