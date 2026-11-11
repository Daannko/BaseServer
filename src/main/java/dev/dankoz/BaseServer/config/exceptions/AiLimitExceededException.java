package dev.dankoz.BaseServer.config.exceptions;

public class AiLimitExceededException extends RuntimeException{

    public AiLimitExceededException(String message) {
        super(message);
    }
}
