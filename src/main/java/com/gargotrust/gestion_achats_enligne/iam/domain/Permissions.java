package com.gargotrust.gestion_achats_enligne.iam.domain;

/**
 * Constantes pour les noms de permissions — utilisées dans @PreAuthorize("hasAuthority(...)").
 * Toute modification ici doit être répercutée dans V6__insert_default_permissions.sql.
 */
public final class Permissions {

    private Permissions() {}

    public static final String ORDERS_CREATE   = "orders:create";
    public static final String ORDERS_READ     = "orders:read";
    public static final String ORDERS_READ_ALL = "orders:read_all";
    public static final String ORDERS_UPDATE   = "orders:update";
    public static final String ORDERS_CANCEL   = "orders:cancel";

    public static final String SHIPMENTS_READ   = "shipments:read";
    public static final String SHIPMENTS_UPDATE = "shipments:update";

    public static final String PROOFS_CREATE = "proofs:create";
    public static final String PROOFS_READ   = "proofs:read";

    public static final String PAYMENTS_READ     = "payments:read";
    public static final String PAYMENTS_INITIATE = "payments:initiate";
    public static final String PAYMENTS_VALIDATE = "payments:validate";

    public static final String USERS_READ   = "users:read";
    public static final String USERS_MANAGE = "users:manage";

    public static final String FORWARDERS_READ   = "forwarders:read";
    public static final String FORWARDERS_MANAGE = "forwarders:manage";

    public static final String ANALYTICS_READ = "analytics:read";

    public static final String INCIDENTS_READ   = "incidents:read";
    public static final String INCIDENTS_MANAGE = "incidents:manage";
}
