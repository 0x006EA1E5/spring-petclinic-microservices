package org.springframework.samples.petclinic.api.application;

import jakarta.validation.constraints.Min;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.web.VisitResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("petclinicvisitsservice")
public interface VisitsService {
    @GetMapping("/pets/visits")
    VisitResource.Visits getVisitsForPets(@RequestParam("petId") List<Integer> petId);

    @GetMapping("owners/*/pets/{petId}/visits")
    List<Visit> visits(@PathVariable("petId") @Min(1) int petId);

}
