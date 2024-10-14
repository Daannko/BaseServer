package dev.dankoz.BaseServer.config.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import dev.dankoz.BaseServer.general.dto.ExceptionResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final int JWT_EXPIRED = 461;

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> badCredentials(BadCredentialsException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage(),UNAUTHORIZED.value()), UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> userNotFound(UsernameNotFoundException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage(),NOT_FOUND.value()), NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> emailUsed(EmailAlreadyExistsException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage(), CONFLICT.value()), CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> serverError(RuntimeException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage(), INTERNAL_SERVER_ERROR.value()),INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> expiredToken(ExpiredJwtException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage(),JWT_EXPIRED), UNAUTHORIZED);
    }

    private String parseToJson(String message, int code) throws JsonProcessingException {
        ExceptionResponse response = ExceptionResponse.builder()
                .date(new Date())
                .message(message)
                .code(code)
                .build();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(response);
    }

}
