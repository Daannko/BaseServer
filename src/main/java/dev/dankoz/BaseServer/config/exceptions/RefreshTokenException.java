package dev.dankoz.BaseServer.config.exceptions;

public class RefreshTokenException extends RuntimeException{
    public RefreshTokenException(String message) {
        super(message);
    }
}
