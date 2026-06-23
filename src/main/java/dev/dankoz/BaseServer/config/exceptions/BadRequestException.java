package dev.dankoz.BaseServer.config.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message){
        super(message);
    }

}
