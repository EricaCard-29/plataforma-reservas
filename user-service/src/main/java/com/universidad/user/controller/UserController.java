package com.universidad.user.controller;

import com.universidad.user.dto.UserRequest;
import com.universidad.user.model.User;
import com.universidad.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<User> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public User obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @GetMapping("/email/{correo}")
    public User porCorreo(@PathVariable String correo) {
        return service.obtenerPorCorreo(correo);
    }

    @PostMapping
    public ResponseEntity<User> crear(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public User actualizar(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
