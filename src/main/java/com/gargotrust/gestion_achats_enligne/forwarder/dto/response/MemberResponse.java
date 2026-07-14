package com.gargotrust.gestion_achats_enligne.forwarder.dto.response;

import com.gargotrust.gestion_achats_enligne.forwarder.domain.enums.MemberRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Membre d'une entreprise de transit (OWNER ou AGENT), enrichi des informations
 * d'affichage du compte IAM (email, nom) résolues via l'annuaire des comptes.
 */
@Data
@Builder
public class MemberResponse {
    private Long       id;
    private UUID       accountId;
    private String     email;
    private String     firstName;
    private String     lastName;
    private MemberRole memberRole;
    private String     position;
    private Instant    joinedAt;
}
