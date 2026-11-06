package com.universidad.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.universidad.user.client.AuditClient;
import com.universidad.user.dto.UserRequest;
import com.universidad.user.exception.BadRequestException;
import com.universidad.user.exception.ConflictException;
import com.universidad.user.model.EstadoUsuario;
import com.universidad.user.model.Rol;
import com.universidad.user.model.User;
import com.universidad.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repo;
    @Mock AuditClient audit;
    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(repo, audit, "universidad.edu.co");
    }

    private UserRequest request(String correo, Rol rol) {
        return new UserRequest("Ana", "Pérez", "123", correo, "3001112233", rol, null);
    }

    @Test
    void registraUsuarioValido() {
        when(repo.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });
        User creado = service.crear(request("Ana@universidad.edu.co", Rol.ESTUDIANTE));
        assertEquals(1L, creado.getId());
        assertEquals("ana@universidad.edu.co", creado.getCorreoInstitucional());
        assertEquals(EstadoUsuario.ACTIVO, creado.getEstado());
        verify(audit).registrar(eq("1"), eq("CREAR_USUARIO"), anyString(), eq("EXITOSO"), anyString());
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(repo.existsByCorreoInstitucionalIgnoreCase("ana@universidad.edu.co")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.crear(request("ana@universidad.edu.co", Rol.DOCENTE)));
        verify(repo, never()).save(any());
    }

    @Test
    void rechazaCorreoSinFormatoInstitucional() {
        assertThrows(BadRequestException.class, () -> service.crear(request("ana@gmail.com", Rol.DOCENTE)));
    }

    @Test
    void rechazaCorreoVacio() {
        assertThrows(BadRequestException.class, () -> service.crear(request("  ", Rol.DOCENTE)));
    }

    @Test
    void actualizaUsuarioExistente() {
        User existente = User.builder().id(5L).nombre("Ana").apellido("Pérez").documento("123")
                .correoInstitucional("ana@universidad.edu.co").rol(Rol.ESTUDIANTE).estado(EstadoUsuario.ACTIVO).build();
        when(repo.findById(5L)).thenReturn(Optional.of(existente));
        when(repo.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        User actualizado = service.actualizar(5L, request("ana@universidad.edu.co", Rol.INVESTIGADOR));
        assertEquals(Rol.INVESTIGADOR, actualizado.getRol());
    }
}
