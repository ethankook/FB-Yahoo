package com.example.fbyahoo.controller;

import com.example.fbyahoo.exception.OAuthFlowException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class OAuthExceptionHandler {

    @ExceptionHandler(OAuthFlowException.class)
    public String handle(OAuthFlowException ex) {
        return switch (ex.getReason()) {
            case TOKEN_MISSING, REFRESH_FAILED, ACCESS_REVOKED, STATE_MISMATCH, TOKEN_EXPIRED, TOKEN_EXCHANGE_ERROR -> "redirect:/login";

        };
    }

}