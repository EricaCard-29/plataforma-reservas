package com.universidad.audit.exception;

/** Se lanza cuando los datos enviados son inválidos (HTTP 400). */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String mensaje) { super(mensaje); }
}
