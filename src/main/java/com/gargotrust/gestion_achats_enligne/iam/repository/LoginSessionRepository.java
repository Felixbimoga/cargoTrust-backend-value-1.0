package com.gargotrust.gestion_achats_enligne.iam.repository;

import com.gargotrust.gestion_achats_enligne.iam.domain.LoginSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LoginSessionRepository extends JpaRepository<LoginSession, UUID> {
}
