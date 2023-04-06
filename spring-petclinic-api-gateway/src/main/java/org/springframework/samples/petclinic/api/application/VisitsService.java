package org.springframework.samples.petclinic.api.application;

import jakarta.validation.constraints.Min;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.samples.petclinic.common.model.Visit;
import org.springframework.samples.petclinic.common.model.Visits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("petclinicvisitsservice")
public interface VisitsService {
    @GetMapping("/pets/visits")
    Visits getVisitsForPets(@RequestParam("petId") List<Long> petId);

    @GetMapping("owners/*/pets/{petId}/visits")
    List<Visit> visits(@PathVariable("petId") @Min(1) int petId);

}
