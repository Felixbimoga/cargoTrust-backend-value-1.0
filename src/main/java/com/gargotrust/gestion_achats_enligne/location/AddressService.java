package com.gargotrust.gestion_achats_enligne.location;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Gestion des adresses — API exposée aux autres modules. Ceux-ci créent / mettent à
 * jour une adresse et n'en conservent que l'{@link UUID}.
 */
public interface AddressService {

    AddressView create(AddressCommand command);

    AddressView update(UUID id, AddressCommand command);

    AddressView get(UUID id);

    /** Récupération en lot (évite le N+1 lors du rendu d'agrégats possédant plusieurs adresses). */
    Map<UUID, AddressView> getMany(Collection<UUID> ids);

    void delete(UUID id);
}
