package com.universidad.notification.controller;

import com.universidad.notification.dto.NotificationRequest;
import com.universidad.notification.model.Notification;
import com.universidad.notification.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<Notification> listar() {
        return service.listar();
    }

    @GetMapping("/user/{userId}")
    public List<Notification> porUsuario(@PathVariable Long userId) {
        return service.porUsuario(userId);
    }

    @PostMapping
    public ResponseEntity<Notification> enviar(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enviar(request));
    }
}
