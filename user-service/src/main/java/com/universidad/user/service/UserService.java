package com.universidad.user.service;

import com.universidad.user.client.AuditClient;
import com.universidad.user.dto.UserRequest;
import com.universidad.user.exception.BadRequestException;
import com.universidad.user.exception.ConflictException;
import com.universidad.user.exception.NotFoundException;
import com.universidad.user.model.EstadoUsuario;
import com.universidad.user.model.User;
import com.universidad.user.repository.UserRepository;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repo;
    private final AuditClient audit;
    private final String dominio;
    private final Pattern patronCorreo;

    public UserService(UserRepository repo, AuditClient audit, @Value("${app.email-domain}") String dominio) {
        this.repo = repo;
        this.audit = audit;
        this.dominio = dominio;
        this.patronCorreo = Pattern.compile("^[A-Za-z0-9._%+-]+@" + Pattern.quote(dominio) + "$",
                Pattern.CASE_INSENSITIVE);
    }

    @Transactional(readOnly = true)
    public List<User> listar() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public User obtener(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("No existe el usuario con id " + id));
    }

    @Transactional(readOnly = true)
    public User obtenerPorCorreo(String correo) {
        return repo.findByCorreoInstitucionalIgnoreCase(correo.trim())
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el correo " + correo));
    }

    @Transactional
    public User crear(UserRequest r) {
        String correo = validarCorreo(r.correoInstitucional());
        if (repo.existsByCorreoInstitucionalIgnoreCase(correo)) {
            throw new ConflictException("Ya existe un usuario con el correo " + correo);
        }
        if (repo.existsByDocumento(r.documento())) {
            throw new ConflictException("Ya existe un usuario con el documento " + r.documento());
        }
        User guardado = repo.save(User.builder()
                .nombre(r.nombre()).apellido(r.apellido()).documento(r.documento())
                .correoInstitucional(correo).telefono(r.telefono()).rol(r.rol())
                .estado(r.estado() == null ? EstadoUsuario.ACTIVO : r.estado())
                .build());
        audit.registrar(String.valueOf(guardado.getId()), "CREAR_USUARIO",
                "Se creó el usuario " + correo, "EXITOSO", "usuario:" + guardado.getId());
        return guardado;
    }

    @Transactional
    public User actualizar(Long id, UserRequest r) {
        User u = obtener(id);
        String correo = validarCorreo(r.correoInstitucional());
        if (repo.existsByCorreoInstitucionalIgnoreCaseAndIdNot(correo, id)) {
            throw new ConflictException("Ya existe otro usuario con el correo " + correo);
        }
        if (repo.existsByDocumentoAndIdNot(r.documento(), id)) {
            throw new ConflictException("Ya existe otro usuario con el documento " + r.documento());
        }
        u.setNombre(r.nombre());
        u.setApellido(r.apellido());
        u.setDocumento(r.documento());
        u.setCorreoInstitucional(correo);
        u.setTelefono(r.telefono());
        u.setRol(r.rol());
        if (r.estado() != null) {
            u.setEstado(r.estado());
        }
        User guardado = repo.save(u);
        audit.registrar(String.valueOf(id), "MODIFICAR_USUARIO",
                "Se modificó el usuario " + correo, "EXITOSO", "usuario:" + id);
        return guardado;
    }

    @Transactional
    public void eliminar(Long id) {
        User u = obtener(id);
        repo.delete(u);
        audit.registrar(String.valueOf(id), "ELIMINAR_USUARIO",
                "Se eliminó el usuario " + u.getCorreoInstitucional(), "EXITOSO", "usuario:" + id);
    }

    private String validarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new BadRequestException("El correo institucional es obligatorio");
        }
        String limpio = correo.trim().toLowerCase();
        if (!patronCorreo.matcher(limpio).matches()) {
            throw new BadRequestException("El correo debe tener formato institucional: usuario@" + dominio);
        }
        return limpio;
    }
}
