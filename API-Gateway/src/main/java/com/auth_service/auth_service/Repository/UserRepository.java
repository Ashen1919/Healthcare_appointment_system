package com.auth_service.auth_service.Repository;

import com.auth_service.auth_service.Model.AuthModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AuthModel, Long> {
    boolean existsByEmail(String email);
    Optional<AuthModel> findByEmail(String email);
}
