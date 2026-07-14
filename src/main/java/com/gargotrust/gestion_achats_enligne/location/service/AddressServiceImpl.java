package com.gargotrust.gestion_achats_enligne.location.service;

import com.gargotrust.gestion_achats_enligne.location.AddressCommand;
import com.gargotrust.gestion_achats_enligne.location.AddressService;
import com.gargotrust.gestion_achats_enligne.location.AddressView;
import com.gargotrust.gestion_achats_enligne.location.LocationException;
import com.gargotrust.gestion_achats_enligne.location.domain.Address;
import com.gargotrust.gestion_achats_enligne.location.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository repo;

    @Override
    @Transactional
    public AddressView create(AddressCommand c) {
        Address a = new Address();
        apply(a, c);
        return toView(repo.save(a));
    }

    @Override
    @Transactional
    public AddressView update(UUID id, AddressCommand c) {
        Address a = repo.findById(id).orElseThrow(LocationException::addressNotFound);
        apply(a, c);
        return toView(repo.save(a));
    }

    @Override
    @Transactional(readOnly = true)
    public AddressView get(UUID id) {
        return toView(repo.findById(id).orElseThrow(LocationException::addressNotFound));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, AddressView> getMany(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return Map.of();
        return repo.findAllById(ids).stream()
                .map(this::toView)
                .collect(Collectors.toMap(AddressView::id, Function.identity()));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }

    private void apply(Address a, AddressCommand c) {
        a.setCountryCode(c.countryCode());
        a.setCountryName(c.countryName());
        a.setStateRegion(c.stateRegion());
        a.setCity(c.city());
        a.setNeighborhood(c.neighborhood());
        a.setLine(c.line());
        a.setLatitude(c.latitude());
        a.setLongitude(c.longitude());
        a.setPlaceLabel(c.placeLabel());
    }

    private AddressView toView(Address a) {
        return new AddressView(
                a.getId(), a.getCountryCode(), a.getCountryName(), a.getStateRegion(),
                a.getCity(), a.getNeighborhood(), a.getLine(),
                a.getLatitude(), a.getLongitude(), a.getPlaceLabel());
    }
}
