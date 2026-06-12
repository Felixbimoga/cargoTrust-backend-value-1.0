/**
 * Module IAM (Identity & Access Management).
 *
 * API publique exposée aux autres modules :
 * - shared.security.CurrentUserContext
 * - shared.events.AccountCreatedEvent
 * - shared.events.OtpRequestedEvent
 *
 * Les packages internes (domain, repository, service, security)
 * ne doivent jamais être importés par un autre module.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "IAM — Identity & Access Management"
)
package com.gargotrust.gestion_achats_enligne.iam;
