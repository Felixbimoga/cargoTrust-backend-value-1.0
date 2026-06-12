package com.gargotrust.gestion_achats_enligne.iam.service;

import com.gargotrust.gestion_achats_enligne.iam.IamException;
import com.gargotrust.gestion_achats_enligne.iam.domain.Account;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRepository;
import com.gargotrust.gestion_achats_enligne.iam.service.interfaces.IAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService implements IAccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public Account getAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IamException(IamException.ACCOUNT_NOT_FOUND));
    }

    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email)
                .orElseThrow(() -> new IamException(IamException.ACCOUNT_NOT_FOUND));
    }

    @Transactional
    public void activateAccount(UUID accountId) {
        Account account = getAccountById(accountId);
        account.setStatus(Account.AccountStatus.ACTIVE);
        accountRepository.save(account);
    }

    @Transactional
    public void suspendAccount(UUID accountId) {
        Account account = getAccountById(accountId);
        account.setStatus(Account.AccountStatus.SUSPENDED);
        accountRepository.save(account);
    }

    @Transactional
    public void changePassword(UUID accountId, String oldPassword, String newPassword) {
        Account account = getAccountById(accountId);
        if (!passwordEncoder.matches(oldPassword, account.getPasswordHash())) {
            throw new IamException(IamException.INVALID_CREDENTIALS);
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }
}
