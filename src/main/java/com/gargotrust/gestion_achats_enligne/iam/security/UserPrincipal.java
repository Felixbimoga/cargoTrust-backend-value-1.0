package com.gargotrust.gestion_achats_enligne.iam.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private UUID         accountId;
    private String       email;
    private String       role;
    private List<String> permissions;
}
