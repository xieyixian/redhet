package uk.ac.newcastle.enterprisemiddleware.booking;

public class InvalidHotelException extends RuntimeException {

    public InvalidHotelException(String message, Throwable cause) {
        super(message, cause);
    }


    // You can add more constructors or custom logic based on your requirements.
}
