package com.gargotrust.gestion_achats_enligne.iam.repository;

import com.gargotrust.gestion_achats_enligne.iam.domain.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Long> {

    @Modifying
    @Query("DELETE FROM AccountRole ar WHERE ar.account.id = :accountId")
    void deleteAllByAccountId(UUID accountId);
}
