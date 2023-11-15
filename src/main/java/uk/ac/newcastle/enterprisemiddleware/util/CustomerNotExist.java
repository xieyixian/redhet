package uk.ac.newcastle.enterprisemiddleware.util;

import javax.validation.ValidationException;

public class CustomerNotExist extends ValidationException {
    public CustomerNotExist(String message) {
        super(message);
    }

    public CustomerNotExist(String message, Throwable cause) {
        super(message, cause);
    }

    public CustomerNotExist(Throwable cause) {
        super(cause);
    }
}