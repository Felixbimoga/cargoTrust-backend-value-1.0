package com.gargotrust.gestion_achats_enligne.importer.domain.enums;

/** Mode de remboursement souhaité en cas de litige validé (option Escrow). */
public enum RefundMethod {
    MOBILE_MONEY,          // Orange Money, MTN MoMo, Wave…
    BANK_TRANSFER,         // Virement bancaire
    NEXT_SHIPMENT_CREDIT   // Crédit sur la prochaine expédition
}
