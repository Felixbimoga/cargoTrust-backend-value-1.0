package com.gargotrust.gestion_achats_enligne.entity.Enum;

public enum Roles {
    CLIENT(1, "CLIENT"),
    SUPER_ADMIN(2, "SUPER_ADMIN"),
    ADMIN_TRANSITAIRE(3, "ADMIN_TRANSITAIRE"),
    EMPLOYE_TRANSITAIRE(4, "EMPLOYE_TRANSITAIRE");

    private final String name;
    private final Integer id;
    Roles(Integer id, String name) {
        this.name = name;
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Roles findById(Integer id) {
        for (Roles role : Roles.values()) {
            if (role.getId().equals(id)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Aucun rôle trouvé pour l'id : " + id);
    }
}
