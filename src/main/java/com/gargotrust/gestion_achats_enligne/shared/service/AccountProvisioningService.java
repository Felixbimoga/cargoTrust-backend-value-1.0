package com.gargotrust.gestion_achats_enligne.shared.service;

import java.util.UUID;

/**
 * Port de provisionnement de comptes IAM exposé aux autres modules.
 * Permet à un module métier (ex. forwarder) de créer un compte utilisateur
 * sans dépendre des internes du module IAM (frontière : référence par UUID).
 *
 * <p>L'implémentation crée un compte <b>actif</b> avec un mot de passe temporaire,
 * pré-remplit le profil et envoie les identifiants par email. L'autorisation
 * (qui a le droit de provisionner) reste à la charge du module appelant.
 */
public interface AccountProvisioningService {

    /**
     * Provisionne un nouveau compte actif et notifie l'utilisateur par email.
     *
     * @throws com.gargotrust.gestion_achats_enligne.iam.IamException si l'email existe déjà
     *         ({@code ERR_ACCOUNT_ALREADY_EXISTS}) ou si le rôle est introuvable
     *         ({@code ERR_ROLE_NOT_FOUND}).
     */
    ProvisionedAccount provision(ProvisionAccountCommand command);

    /**
     * @param email     identifiant de connexion du compte à créer
     * @param roleName  rôle à attribuer, avec ou sans préfixe {@code ROLE_}
     * @param firstName prénom (optionnel, pré-remplit le profil)
     * @param lastName  nom (optionnel, pré-remplit le profil)
     */
    record ProvisionAccountCommand(String email, String roleName, String firstName, String lastName) {}

    record ProvisionedAccount(UUID accountId, String email) {}
}
