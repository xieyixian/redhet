package uk.ac.newcastle.enterprisemiddleware.util;

import javax.validation.ValidationException;

public class UniquePhoneException extends ValidationException {
    public UniquePhoneException(String message) {
        super(message);
    }

    public UniquePhoneException(String message, Throwable cause) {
        super(message, cause);
    }

    public UniquePhoneException(Throwable cause) {
        super(cause);
    }
}
