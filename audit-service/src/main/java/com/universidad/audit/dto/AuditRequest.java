package com.universidad.audit.dto;

import jakarta.validation.constraints.NotBlank;

public record AuditRequest(
        @NotBlank(message = "El usuario es obligatorio") String usuario,
        @NotBlank(message = "La operación es obligatoria") String operacion,
        @NotBlank(message = "El microservicio es obligatorio") String microservicio,
        String descripcion,
        @NotBlank(message = "El resultado es obligatorio") String resultado,
        String referencia) {
}
