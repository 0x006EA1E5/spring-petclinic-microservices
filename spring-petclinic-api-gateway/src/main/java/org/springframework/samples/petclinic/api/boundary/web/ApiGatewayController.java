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
package org.springframework.samples.petclinic.api.boundary.web;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.api.application.CustomersService;
import org.springframework.samples.petclinic.api.application.VisitsService;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.Visit;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.function.Function;

/**
 * @author Maciej Szarlinski
 */
@RestController
@RequestMapping("/api/gateway")
class ApiGatewayController {
    private final Logger logger = LoggerFactory.getLogger(ApiGatewayController.class);

    private final CustomersService customersService;
    private final VisitsService visitsService;
    private final Tracer tracer;


    public ApiGatewayController(CustomersService customersService, VisitsService visitsService) {
        this.customersService = customersService;
        this.visitsService = visitsService;
        tracer = GlobalOpenTelemetry.getTracer("org.springframework.samples.petclinic");

    }

    @GetMapping(value = "owners/{ownerId}")
    public Optional<OwnerDetails> getOwnerDetails(final @PathVariable int ownerId) {
        logger.info("getOwnerDetails {}", ownerId);
        return customersService.getOwner(ownerId)
            .map(owner -> {

                    var visits = new Visits(findByPetIdIn(owner.getPetIds()));
                    return addVisitsToOwner(owner).apply(visits);
                }
            );
    }
    List<Visit> findByPetIdIn(Collection<Integer> petIds) {
        var visits = new ArrayList<Visit>();
        for (Integer petId : petIds) {

            var gettingVisitsForPet = tracer.spanBuilder("Getting visits for Pet").setAttribute("petId", petId).startSpan();

            visits.addAll(visitsService.visits(petId));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            gettingVisitsForPet.end();
        }
        return visits;
    }

    private Function<Visits, OwnerDetails> addVisitsToOwner(OwnerDetails owner) {
        return visits -> {
            owner.pets()
                .forEach(pet -> {
                    if (pet.visits() != null) {
                        pet.visits()
                            .addAll(visits.items().stream()
                                .filter(v -> v.getPetId() == pet.id())
                                .toList());
                    }
                });
            return owner;
        };
    }
}
