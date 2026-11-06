package com.universidad.resource.dto;

import com.universidad.resource.model.EstadoRecurso;
import com.universidad.resource.model.TipoRecurso;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ResourceRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "El tipo es obligatorio") TipoRecurso tipo,
        @NotBlank(message = "La ubicación es obligatoria") String ubicacion,
        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 0, message = "La capacidad no puede ser negativa") Integer capacidad,
        String descripcion,
        EstadoRecurso estado) {
}
