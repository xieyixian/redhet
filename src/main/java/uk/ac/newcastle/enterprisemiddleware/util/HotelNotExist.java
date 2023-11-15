package uk.ac.newcastle.enterprisemiddleware.util;

import javax.validation.ValidationException;

public class HotelNotExist extends ValidationException {
    public HotelNotExist(String message) {
        super(message);
    }

    public HotelNotExist(String message, Throwable cause) {
        super(message, cause);
    }

    public HotelNotExist(Throwable cause) {
        super(cause);
    }
}
