package com.universidad.reservation.controller;

import com.universidad.reservation.dto.ReservaCompletaResponse;
import com.universidad.reservation.dto.ReservationRequest;
import com.universidad.reservation.dto.ReservationUpdateRequest;
import com.universidad.reservation.model.Reservation;
import com.universidad.reservation.service.ReservationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Reservation> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Reservation obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    /** Respuesta agregada: reserva + usuario + recurso + historial de auditoría. */
    @GetMapping("/{id}/completa")
    public ReservaCompletaResponse completa(@PathVariable Long id) {
        return service.obtenerCompleta(id);
    }

    @GetMapping("/user/{userId}")
    public List<Reservation> porUsuario(@PathVariable Long userId) {
        return service.porUsuario(userId);
    }

    @PostMapping
    public ResponseEntity<Reservation> crear(@Valid @RequestBody ReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public Reservation actualizar(@PathVariable Long id, @Valid @RequestBody ReservationUpdateRequest request) {
        return service.actualizar(id, request);
    }

    /** Cancela la reserva (queda en estado CANCELADA). */
    @DeleteMapping("/{id}")
    public Reservation cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }
}
