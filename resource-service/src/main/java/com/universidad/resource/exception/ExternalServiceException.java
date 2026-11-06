package com.universidad.resource.exception;

/** Se lanza cuando otro microservicio no responde (HTTP 502). */
public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String mensaje) { super(mensaje); }
    public ExternalServiceException(String mensaje, Throwable causa) { super(mensaje, causa); }
}
