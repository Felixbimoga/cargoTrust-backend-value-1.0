package com.gargotrust.gestion_achats_enligne.iam.service;

import com.gargotrust.gestion_achats_enligne.iam.domain.Account;
import com.gargotrust.gestion_achats_enligne.iam.profile.dto.response.ProfileResponse;
import com.gargotrust.gestion_achats_enligne.iam.profile.service.IProfileService;
import com.gargotrust.gestion_achats_enligne.iam.repository.AccountRepository;
import com.gargotrust.gestion_achats_enligne.shared.service.AccountDirectory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implémentation IAM du port {@link AccountDirectory}. Compose email (compte) et
 * nom (profil) pour fournir un résumé d'affichage aux autres modules.
 */
@Service
@RequiredArgsConstructor
public class AccountDirectoryImpl implements AccountDirectory {

    private final AccountRepository accountRepo;
    private final IProfileService   profileService;

    @Override
    @Transactional(readOnly = true)
    public Optional<AccountSummary> find(UUID accountId) {
        return accountRepo.findById(accountId).map(this::toSummary);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, AccountSummary> findAll(Collection<UUID> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            return Map.of();
        }
        return accountRepo.findAllById(accountIds).stream()
                .map(this::toSummary)
                .collect(Collectors.toMap(AccountSummary::accountId, Function.identity()));
    }

    private AccountSummary toSummary(Account account) {
        String firstName = null, lastName = null;
        try {
            ProfileResponse profile = profileService.getProfileByAccountId(account.getId());
            firstName = profile.getFirstName();
            lastName  = profile.getLastName();
        } catch (Exception ignored) {
            // Profil absent : on renvoie au moins l'email.
        }
        return new AccountSummary(account.getId(), account.getEmail(), firstName, lastName);
    }
}
