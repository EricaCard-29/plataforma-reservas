package com.universidad.resource.controller;

import com.universidad.resource.dto.ResourceRequest;
import com.universidad.resource.model.Resource;
import com.universidad.resource.service.ResourceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @GetMapping
    public List<Resource> listar() {
        return service.listar();
    }

    @GetMapping("/available")
    public List<Resource> disponibles() {
        return service.disponibles();
    }

    @GetMapping("/{id}")
    public Resource obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping
    public ResponseEntity<Resource> crear(@Valid @RequestBody ResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @PutMapping("/{id}")
    public Resource actualizar(@PathVariable Long id, @Valid @RequestBody ResourceRequest request) {
        return service.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
