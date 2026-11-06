package com.universidad.notification.dto;

import com.universidad.notification.model.TipoNotificacion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record NotificationRequest(
        @NotNull(message = "El userId es obligatorio") Long userId,
        @NotBlank(message = "El asunto es obligatorio") String asunto,
        @NotBlank(message = "El mensaje es obligatorio") String mensaje,
        @NotNull(message = "El tipo es obligatorio") TipoNotificacion tipo) {
}
