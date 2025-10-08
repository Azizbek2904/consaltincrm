package com.crm.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public class CustomException extends RuntimeException {
    private final HttpStatus status;

    public CustomException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST; // yoki NOT_FOUND — istagingga qarab
    }

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }
}
