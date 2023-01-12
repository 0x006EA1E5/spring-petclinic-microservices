package org.springframework.samples.petclinic.api.application;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("petclinicvisitsservice")
public interface VisitsService {
    @GetMapping("/pets/visits")
    Visits getVisitsForPets(@RequestParam("petId") List<Integer> petId);
}
