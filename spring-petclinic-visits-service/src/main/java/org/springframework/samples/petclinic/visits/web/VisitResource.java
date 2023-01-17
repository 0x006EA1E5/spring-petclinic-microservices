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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.samples.petclinic.visits.model.Visits;
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
public class VisitResource {

    private static final Logger logger = LoggerFactory.getLogger(VisitResource.class);

    private final VisitRepository visitRepository;
    private final Tracer tracer;

    VisitResource(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
        tracer = GlobalOpenTelemetry.getTracer("org.springframework.samples.petclinic");
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
        logger.debug("[visits] getting visits for pet {}", petId);
        var visits = visitRepository.findByPetId(petId);
        logger.debug("[visitsMultiGet] found {} visits", visits.size());
        return visits;
    }

    @GetMapping("pets/visits")
    public Visits visitsMultiGet(@RequestParam("petId") List<Integer> petIds) {
        logger.info("[visitsMultiGet] {}", petIds);
        final List<Visit> byPetIdIn = findByPetIdIn(petIds);
        logger.info("[visitsMultiGet] found {} visits", byPetIdIn.size());
        return new Visits(byPetIdIn);
    }


    List<Visit> findByPetIdIn(Collection<Integer> petIds) {
        logger.debug("[findByPetIdIn] finding visits for pets {}", petIds);
        var visits = new ArrayList<Visit>();
        for (Integer petId : petIds) {
            var message = "Getting visits for Pet %d from repository...".formatted(petId);
            logger.debug("[findByPetIdIn] {}", message);
            var gettingVisitsForPet = tracer
                .spanBuilder(message)
                .setAttribute("petId", petId)
                .startSpan();
            try (var scope = gettingVisitsForPet.makeCurrent() ){
                for (Visit visit : visitRepository.findByPetId(petId)) {
                    logger.debug("[findByPetIdIn] adding visit {} to pet {}", visit.getId(), petId);
                    visits.add(visit);
                    gettingVisitsForPet.addEvent(
                        "Pharos Strong!",
                        Attributes.builder()
                            .put("Sleeping to simulate visitRepository latency", 100)
                            .build());
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                gettingVisitsForPet.end();
            }
        }
        logger.debug("[findByPetIdIn] found {} visits", visits.size());
        return visits;
    }
}
