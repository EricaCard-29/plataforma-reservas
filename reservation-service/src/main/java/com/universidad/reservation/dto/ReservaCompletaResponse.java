package com.universidad.reservation.dto;

import com.universidad.reservation.client.ResourceDto;
import com.universidad.reservation.client.UserDto;
import com.universidad.reservation.model.Reservation;
import java.util.List;
import java.util.Map;

/** Respuesta agregada: reserva + usuario + recurso + historial de auditoría. */
public record ReservaCompletaResponse(
        Reservation reserva,
        UserDto usuario,
        ResourceDto recurso,
        List<Map<String, Object>> historial) {
}
