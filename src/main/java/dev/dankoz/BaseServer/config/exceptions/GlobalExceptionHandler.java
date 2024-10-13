package dev.dankoz.BaseServer.config.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import dev.dankoz.BaseServer.general.dto.ExceptionResponse;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> badCredentials(BadCredentialsException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> userNotFound(UsernameNotFoundException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> userNotFound(EmailAlreadyExistsException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage()), HttpStatus.CONFLICT);
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> userNotFound(RuntimeException exception, WebRequest request) throws  JsonProcessingException {
        return new ResponseEntity<>(parseToJson(exception.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private String parseToJson(String message) throws JsonProcessingException {
        ExceptionResponse response = ExceptionResponse.builder()
                .date(new Date())
                .message(message)
                .build();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(response);
    }

}
