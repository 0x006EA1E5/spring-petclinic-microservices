package org.springframework.samples.petclinic.api.application;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient("petclinicvisitsservice")
public interface VisitsService {
    @GetMapping("/pets/visits?petId={petId}")
    Visits getVisitsForPets(@PathVariable List<Integer> petId);
}
