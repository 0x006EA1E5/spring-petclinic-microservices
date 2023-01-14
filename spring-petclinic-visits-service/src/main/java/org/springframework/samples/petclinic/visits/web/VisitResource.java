/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.visits.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 * @author Ramazan Sakin
 */
@RestController

//@PreAuthorize("hasAuthority('ROLE_ADMIN')")
class VisitResource {

    private static final Logger logger = LoggerFactory.getLogger(VisitResource.class);

    private final VisitRepository visitRepository;

    VisitResource(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @PostMapping("owners/*/pets/{petId}/visits")
    @ResponseStatus(HttpStatus.CREATED)
    public Visit create(
        @Valid @RequestBody Visit visit,
        @PathVariable("petId") @Min(1) int petId) {

        visit.setPetId(petId);
        logger.info("Saving visit {}", visit);
        return visitRepository.save(visit);
    }

    @GetMapping("owners/*/pets/{petId}/visits")
    public List<Visit> visits(@PathVariable("petId") @Min(1) int petId) {
        return visitRepository.findByPetId(petId);
    }

    @GetMapping("pets/visits")
    public Visits visitsMultiGet(@RequestParam("petId") List<Integer> petIds) {
        logger.info("visitsMultiGet {}", petIds);
        final List<Visit> byPetIdIn = findByPetIdIn(petIds);
        return new Visits(byPetIdIn);
    }


    List<Visit> findByPetIdIn(Collection<Integer> petIds) {
        var visits = new ArrayList<Visit>();
        for (Integer petId : petIds) {
            Span.current().addEvent("Getting visits for Pet", Attributes.builder().put("petId", petId).build());
            visits.addAll(visitRepository.findByPetId(petId));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return visits;
    }

    record Visits(List<Visit> items) {
    }
}
