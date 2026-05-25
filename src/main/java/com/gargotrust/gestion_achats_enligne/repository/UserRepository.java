package com.gargotrust.gestion_achats_enligne.repository;

import com.gargotrust.gestion_achats_enligne.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
    Users findByUsername(String username);
    Users findByMail(String mail);
}
