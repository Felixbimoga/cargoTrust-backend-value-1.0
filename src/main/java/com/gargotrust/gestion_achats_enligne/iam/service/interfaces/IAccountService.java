package com.gargotrust.gestion_achats_enligne.iam.service.interfaces;

import com.gargotrust.gestion_achats_enligne.iam.domain.Account;

import java.util.UUID;

public interface IAccountService {

    Account getAccountById(UUID id);

    Account getAccountByEmail(String email);

    void activateAccount(UUID accountId);

    void suspendAccount(UUID accountId);

    void changePassword(UUID accountId, String oldPassword, String newPassword);
}
