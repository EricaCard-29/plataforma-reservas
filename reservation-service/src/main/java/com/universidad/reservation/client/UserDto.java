package com.universidad.reservation.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDto(Long id, String nombre, String apellido, String correoInstitucional, String rol, String estado) {
}
