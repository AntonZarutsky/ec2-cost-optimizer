package de.zalando.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
public class AccessDeniedException extends RestException{

    public AccessDeniedException(String message, Throwable cause ) {
        super(message, cause);
    }

    public AccessDeniedException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return FORBIDDEN;
    }

}
