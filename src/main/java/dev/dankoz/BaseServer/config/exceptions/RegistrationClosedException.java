package dev.dankoz.BaseServer.config.exceptions;

public class RegistrationClosedException extends RuntimeException{

    public RegistrationClosedException(String message) {
        super(message);
    }
}
