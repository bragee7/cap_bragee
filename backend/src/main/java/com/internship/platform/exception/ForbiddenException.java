package com.internship.platform.exception;

/** 403 — authenticated but not allowed. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) { super(message); }
}
