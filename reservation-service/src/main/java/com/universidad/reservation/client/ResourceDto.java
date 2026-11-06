package com.universidad.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResourceDto(Long id, String nombre, String tipo, String ubicacion, Integer capacidad,
                          String descripcion, String estado) {
}
