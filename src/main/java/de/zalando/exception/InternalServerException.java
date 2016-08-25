package de.zalando.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
public class InternalServerException extends RestException{


    public InternalServerException(String message, Throwable cause ) {
        super(message, cause);
    }

    public InternalServerException(String message) {
        super(message);
    }

    @Override
    public HttpStatus getHttpStatus() {
        return INTERNAL_SERVER_ERROR;
    }


}
