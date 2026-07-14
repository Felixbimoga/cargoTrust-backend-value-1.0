package com.gargotrust.gestion_achats_enligne.location.repository;

import com.gargotrust.gestion_achats_enligne.location.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
