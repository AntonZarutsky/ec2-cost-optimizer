package de.zalando.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
public class BadRequestException extends RestException{

    public BadRequestException(String message, Throwable cause ) {
        super(message, cause);
    }

    public BadRequestException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return BAD_REQUEST;
    }
}
