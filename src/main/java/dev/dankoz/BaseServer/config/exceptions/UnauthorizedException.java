package dev.dankoz.BaseServer.config.exceptions;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message){
        super(message);
    }

}
