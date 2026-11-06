package com.universidad.resource.exception;

/** Se lanza cuando algo no existe (HTTP 404). */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String mensaje) { super(mensaje); }
}
