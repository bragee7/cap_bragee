package com.internship.platform.exception;

/** 400 — validation / business rule violation. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
