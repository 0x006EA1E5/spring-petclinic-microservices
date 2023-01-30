package org.springframework.samples.petclinic.customers.exceptions;

public class TooManyOwnersException extends Exception {
    public TooManyOwnersException(String message) {
        super(message);
    }
}
