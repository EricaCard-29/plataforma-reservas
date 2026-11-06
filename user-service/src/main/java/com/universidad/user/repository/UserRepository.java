package com.universidad.user.repository;

import com.universidad.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCorreoInstitucionalIgnoreCase(String correo);
    boolean existsByCorreoInstitucionalIgnoreCase(String correo);
    boolean existsByCorreoInstitucionalIgnoreCaseAndIdNot(String correo, Long id);
    boolean existsByDocumento(String documento);
    boolean existsByDocumentoAndIdNot(String documento, Long id);
}
