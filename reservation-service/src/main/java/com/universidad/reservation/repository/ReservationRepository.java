package com.universidad.reservation.repository;

import com.universidad.reservation.model.EstadoReserva;
import com.universidad.reservation.model.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserIdOrderByFechaReservaDescHoraInicioDesc(Long userId);

    /**
     * Hay solapamiento cuando: mismo recurso, misma fecha, y los horarios se cruzan
     * (inicio existente < fin nuevo  Y  fin existente > inicio nuevo).
     * excluirId sirve al modificar una reserva para que no choque consigo misma (-1 si no aplica).
     */
    @Query("""
            select count(r) > 0 from Reservation r
            where r.resourceId = :resourceId
              and r.fechaReserva = :fecha
              and r.estado in :estados
              and r.horaInicio < :fin
              and r.horaFin > :inicio
              and r.id <> :excluirId
            """)
    boolean existeSolape(@Param("resourceId") Long resourceId,
                         @Param("fecha") LocalDate fecha,
                         @Param("inicio") LocalTime inicio,
                         @Param("fin") LocalTime fin,
                         @Param("estados") List<EstadoReserva> estados,
                         @Param("excluirId") Long excluirId);

    @Query("""
            select r from Reservation r
            where r.estado = :estado
              and (r.fechaReserva < :hoy or (r.fechaReserva = :hoy and r.horaFin < :ahora))
            """)
    List<Reservation> findVencidas(@Param("estado") EstadoReserva estado,
                                   @Param("hoy") LocalDate hoy,
                                   @Param("ahora") LocalTime ahora);
}
