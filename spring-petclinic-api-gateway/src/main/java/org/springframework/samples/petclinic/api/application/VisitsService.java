package org.springframework.samples.petclinic.api.application;

import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VisitsService {
    @GetExchange("/api/visits/pets/visits?petId={petId}")
    Mono<Visits> getVisitsForPets(@PathVariable List<Integer> petId);
}
