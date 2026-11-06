package com.universidad.user.dto;

import com.universidad.user.model.EstadoUsuario;
import com.universidad.user.model.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El apellido es obligatorio") String apellido,
        @NotBlank(message = "El documento es obligatorio") String documento,
        @NotBlank(message = "El correo institucional es obligatorio") String correoInstitucional,
        String telefono,
        @NotNull(message = "Todo usuario debe tener un rol") Rol rol,
        EstadoUsuario estado) {
}
