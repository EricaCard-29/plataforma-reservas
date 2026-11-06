package com.universidad.resource.exception;

/** Se lanza cuando hay un conflicto con reglas de negocio (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String mensaje) { super(mensaje); }
}
