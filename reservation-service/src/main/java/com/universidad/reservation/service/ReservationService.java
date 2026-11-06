package com.universidad.reservation.service;

import com.universidad.reservation.client.AuditClient;
import com.universidad.reservation.client.NotificationClient;
import com.universidad.reservation.client.ResourceClient;
import com.universidad.reservation.client.ResourceDto;
import com.universidad.reservation.client.UserClient;
import com.universidad.reservation.client.UserDto;
import com.universidad.reservation.dto.ReservaCompletaResponse;
import com.universidad.reservation.dto.ReservationRequest;
import com.universidad.reservation.dto.ReservationUpdateRequest;
import com.universidad.reservation.exception.BadRequestException;
import com.universidad.reservation.exception.ConflictException;
import com.universidad.reservation.exception.NotFoundException;
import com.universidad.reservation.model.EstadoReserva;
import com.universidad.reservation.model.Reservation;
import com.universidad.reservation.repository.ReservationRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final List<EstadoReserva> ESTADOS_ACTIVOS = List.of(EstadoReserva.PENDIENTE, EstadoReserva.APROBADA);

    private final ReservationRepository repo;
    private final UserClient userClient;
    private final ResourceClient resourceClient;
    private final AuditClient auditClient;
    private final NotificationClient notificationClient;

    public ReservationService(ReservationRepository repo, UserClient userClient, ResourceClient resourceClient,
                              AuditClient auditClient, NotificationClient notificationClient) {
        this.repo = repo;
        this.userClient = userClient;
        this.resourceClient = resourceClient;
        this.auditClient = auditClient;
        this.notificationClient = notificationClient;
    }

    @Transactional(readOnly = true)
    public List<Reservation> listar() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public Reservation obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("No existe la reserva con id " + id));
    }

    @Transactional(readOnly = true)
    public List<Reservation> porUsuario(Long userId) {
        return repo.findByUserIdOrderByFechaReservaDescHoraInicioDesc(userId);
    }

    /**
     * Caso de uso "Crear reserva":
     * validar usuario -> validar recurso -> verificar disponibilidad -> registrar -> auditoría -> notificación.
     */
    @Transactional
    public Reservation crear(ReservationRequest r) {
        validarHorario(r.horaInicio(), r.horaFin());

        UserDto usuario = userClient.obtener(r.userId());                       // 404 si no existe
        if (!"ACTIVO".equals(usuario.estado())) {
            throw new ConflictException("El usuario " + r.userId() + " está inactivo y no puede reservar");
        }

        ResourceDto recurso = resourceClient.obtener(r.resourceId());          // 404 si no existe
        if (!"DISPONIBLE".equals(recurso.estado())) {
            throw new ConflictException("El recurso '" + recurso.nombre() + "' no está disponible (estado: "
                    + recurso.estado() + ")");
        }

        if (repo.existeSolape(r.resourceId(), r.fechaReserva(), r.horaInicio(), r.horaFin(), ESTADOS_ACTIVOS, -1L)) {
            throw new ConflictException("El recurso ya tiene una reserva que se cruza con ese horario");
        }

        Reservation guardada = repo.save(Reservation.builder()
                .userId(r.userId()).resourceId(r.resourceId()).fechaReserva(r.fechaReserva())
                .horaInicio(r.horaInicio()).horaFin(r.horaFin()).motivo(r.motivo())
                .estado(EstadoReserva.APROBADA)            // pasó todas las validaciones: se aprueba
                .build());

        // Si la auditoría falla se lanza excepción y toda la transacción se revierte
        auditClient.registrar(String.valueOf(r.userId()), "CREAR_RESERVA",
                "Reserva aprobada del recurso " + recurso.nombre() + " el " + r.fechaReserva()
                        + " de " + r.horaInicio() + " a " + r.horaFin(),
                "EXITOSO", "reserva:" + guardada.getId());

        notificationClient.enviar(r.userId(), "CONFIRMACION", "Reserva aprobada",
                "Tu reserva de " + recurso.nombre() + " para el " + r.fechaReserva() + " de "
                        + r.horaInicio() + " a " + r.horaFin() + " fue aprobada.");
        return guardada;
    }

    @Transactional
    public Reservation actualizar(Long id, ReservationUpdateRequest r) {
        Reservation reserva = obtener(id);
        if (!ESTADOS_ACTIVOS.contains(reserva.getEstado())) {
            throw new ConflictException("Solo se pueden modificar reservas pendientes o aprobadas");
        }
        validarHorario(r.horaInicio(), r.horaFin());
        if (repo.existeSolape(reserva.getResourceId(), r.fechaReserva(), r.horaInicio(), r.horaFin(),
                ESTADOS_ACTIVOS, reserva.getId())) {
            throw new ConflictException("El recurso ya tiene una reserva que se cruza con ese horario");
        }
        reserva.setFechaReserva(r.fechaReserva());
        reserva.setHoraInicio(r.horaInicio());
        reserva.setHoraFin(r.horaFin());
        reserva.setMotivo(r.motivo());
        Reservation guardada = repo.save(reserva);

        auditClient.registrar(String.valueOf(reserva.getUserId()), "MODIFICAR_RESERVA",
                "Reserva modificada: " + r.fechaReserva() + " de " + r.horaInicio() + " a " + r.horaFin(),
                "EXITOSO", "reserva:" + id);
        return guardada;
    }

    /** DELETE /reservations/{id}: no borra el registro, lo marca como CANCELADA (así queda trazabilidad). */
    @Transactional
    public Reservation cancelar(Long id) {
        Reservation reserva = obtener(id);
        if (reserva.getEstado() == EstadoReserva.CANCELADA) {
            throw new ConflictException("La reserva ya estaba cancelada");
        }
        if (reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new ConflictException("No se puede cancelar una reserva finalizada");
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        Reservation guardada = repo.save(reserva);

        auditClient.registrar(String.valueOf(reserva.getUserId()), "CANCELAR_RESERVA",
                "Se canceló la reserva " + id, "EXITOSO", "reserva:" + id);
        notificationClient.enviar(reserva.getUserId(), "CANCELACION", "Reserva cancelada",
                "Tu reserva del " + reserva.getFechaReserva() + " de " + reserva.getHoraInicio()
                        + " a " + reserva.getHoraFin() + " fue cancelada.");
        return guardada;
    }

    /** Caso de uso "Consultar reserva completa": reserva + usuario + recurso + historial. */
    @Transactional(readOnly = true)
    public ReservaCompletaResponse obtenerCompleta(Long id) {
        Reservation reserva = obtener(id);
        UserDto usuario = userClient.obtener(reserva.getUserId());
        ResourceDto recurso = resourceClient.obtener(reserva.getResourceId());
        return new ReservaCompletaResponse(reserva, usuario, recurso, auditClient.historial("reserva:" + id));
    }

    /** Cada minuto marca como FINALIZADA las reservas aprobadas cuya hora de fin ya pasó. */
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void finalizarVencidas() {
        List<Reservation> vencidas = repo.findVencidas(EstadoReserva.APROBADA, LocalDate.now(), LocalTime.now());
        for (Reservation r : vencidas) {
            r.setEstado(EstadoReserva.FINALIZADA);
        }
        if (!vencidas.isEmpty()) {
            repo.saveAll(vencidas);
            log.info("{} reserva(s) marcadas como FINALIZADA", vencidas.size());
        }
    }

    private void validarHorario(LocalTime inicio, LocalTime fin) {
        if (inicio == null || fin == null || !fin.isAfter(inicio)) {
            throw new BadRequestException("La hora de fin debe ser posterior a la hora de inicio");
        }
    }
}
