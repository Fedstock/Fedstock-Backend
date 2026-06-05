package com.fedstock.backend.main.error;

import org.springframework.http.HttpStatus;

public class BadGatewayException extends ApiException {

    public BadGatewayException(String message) {
        super(HttpStatus.BAD_GATEWAY, "BAD_GATEWAY", message);
    }
}
