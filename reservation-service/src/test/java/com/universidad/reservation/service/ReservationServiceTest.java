package com.universidad.reservation.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.universidad.reservation.client.AuditClient;
import com.universidad.reservation.client.NotificationClient;
import com.universidad.reservation.client.ResourceClient;
import com.universidad.reservation.client.ResourceDto;
import com.universidad.reservation.client.UserClient;
import com.universidad.reservation.client.UserDto;
import com.universidad.reservation.dto.ReservationRequest;
import com.universidad.reservation.exception.BadRequestException;
import com.universidad.reservation.exception.ConflictException;
import com.universidad.reservation.exception.NotFoundException;
import com.universidad.reservation.model.EstadoReserva;
import com.universidad.reservation.model.Reservation;
import com.universidad.reservation.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock ReservationRepository repo;
    @Mock UserClient userClient;
    @Mock ResourceClient resourceClient;
    @Mock AuditClient auditClient;
    @Mock NotificationClient notificationClient;
    ReservationService service;

    final LocalDate fecha = LocalDate.now().plusDays(1);

    @BeforeEach
    void setUp() {
        service = new ReservationService(repo, userClient, resourceClient, auditClient, notificationClient);
    }

    private ReservationRequest request() {
        return new ReservationRequest(1L, 2L, fecha, LocalTime.of(9, 0), LocalTime.of(11, 0), "Práctica de redes");
    }

    private UserDto usuarioActivo() {
        return new UserDto(1L, "Ana", "Pérez", "ana@universidad.edu.co", "ESTUDIANTE", "ACTIVO");
    }

    private ResourceDto recurso(String estado) {
        return new ResourceDto(2L, "Laboratorio 1", "LABORATORIO", "Bloque A", 30, "Lab", estado);
    }

    @Test
    void creaReservaAprobadaConAuditoriaYNotificacion() {
        when(userClient.obtener(1L)).thenReturn(usuarioActivo());
        when(resourceClient.obtener(2L)).thenReturn(recurso("DISPONIBLE"));
        when(repo.save(any(Reservation.class))).thenAnswer(i -> {
            Reservation r = i.getArgument(0);
            r.setId(7L);
            return r;
        });

        Reservation creada = service.crear(request());

        assertEquals(EstadoReserva.APROBADA, creada.getEstado());
        verify(auditClient).registrar(eq("1"), eq("CREAR_RESERVA"), anyString(), eq("EXITOSO"), eq("reserva:7"));
        verify(notificationClient).enviar(eq(1L), eq("CONFIRMACION"), anyString(), anyString());
    }

    @Test
    void rechazaReservaConHorarioSuperpuesto() {
        when(userClient.obtener(1L)).thenReturn(usuarioActivo());
        when(resourceClient.obtener(2L)).thenReturn(recurso("DISPONIBLE"));
        when(repo.existeSolape(any(), any(), any(), any(), any(), any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.crear(request()));
        verify(repo, never()).save(any());
        verifyNoInteractions(notificationClient);
    }

    @Test
    void rechazaRecursoEnMantenimiento() {
        when(userClient.obtener(1L)).thenReturn(usuarioActivo());
        when(resourceClient.obtener(2L)).thenReturn(recurso("MANTENIMIENTO"));

        assertThrows(ConflictException.class, () -> service.crear(request()));
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(userClient.obtener(1L)).thenThrow(new NotFoundException("El usuario con id 1 no existe"));

        assertThrows(NotFoundException.class, () -> service.crear(request()));
        verifyNoInteractions(resourceClient);
    }

    @Test
    void rechazaHorarioInvalido() {
        ReservationRequest mala = new ReservationRequest(1L, 2L, fecha, LocalTime.of(11, 0), LocalTime.of(9, 0), "x");
        assertThrows(BadRequestException.class, () -> service.crear(mala));
    }

    @Test
    void cancelaReservaYNotifica() {
        Reservation existente = Reservation.builder().id(7L).userId(1L).resourceId(2L).fechaReserva(fecha)
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(11, 0)).motivo("x")
                .estado(EstadoReserva.APROBADA).build();
        when(repo.findById(7L)).thenReturn(Optional.of(existente));
        when(repo.save(any(Reservation.class))).thenAnswer(i -> i.getArgument(0));

        Reservation cancelada = service.cancelar(7L);

        assertEquals(EstadoReserva.CANCELADA, cancelada.getEstado());
        verify(auditClient).registrar(eq("1"), eq("CANCELAR_RESERVA"), anyString(), eq("EXITOSO"), eq("reserva:7"));
        verify(notificationClient).enviar(eq(1L), eq("CANCELACION"), anyString(), anyString());
    }
}
