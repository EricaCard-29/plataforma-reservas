package com.universidad.reservation.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/** Para modificar una reserva se cambia fecha, horario o motivo (el usuario y el recurso no cambian). */
public record ReservationUpdateRequest(
        @NotNull(message = "La fecha es obligatoria")
        @FutureOrPresent(message = "La fecha no puede estar en el pasado") LocalDate fechaReserva,
        @NotNull(message = "La hora de inicio es obligatoria") LocalTime horaInicio,
        @NotNull(message = "La hora de fin es obligatoria") LocalTime horaFin,
        @NotBlank(message = "El motivo es obligatorio") String motivo) {
}
