package de.zalando.configuration;

import de.zalando.exception.RestException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@ControllerAdvice
public class ExceptionHandlingConfiguration {

    @ExceptionHandler(RestException.class)
    @ResponseBody
    public ResponseEntity<?> restExceptionHandler(final RestException exception) {

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);

        return new ResponseEntity(exception.getMessage(), headers, exception.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<?> defaultHandler(final Exception exception) {

        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE);
        return new ResponseEntity(exception.getMessage(), headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
