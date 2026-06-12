package com.gargotrust.gestion_achats_enligne.iam.repository;

import com.gargotrust.gestion_achats_enligne.iam.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    boolean existsByName(String name);
    List<Permission> findAllByResource(String resource);
}
