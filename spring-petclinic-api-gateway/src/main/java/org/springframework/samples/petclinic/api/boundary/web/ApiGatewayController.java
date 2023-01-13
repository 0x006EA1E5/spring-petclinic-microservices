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

import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.samples.petclinic.api.application.CustomersService;
import org.springframework.samples.petclinic.api.application.VisitsService;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;
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

    public ApiGatewayController(CustomersService customersService, VisitsService visitsService) {
        this.customersService = customersService;
        this.visitsService = visitsService;
    }

    @GetMapping(value = "owners/{ownerId}")
    public Optional<OwnerDetails> getOwnerDetails(final @PathVariable int ownerId) {
        Span.current().setAttribute("sampling.priority", 10);
        logger.info("getOwnerDetails {}", ownerId);
        return customersService.getOwner(ownerId)
            .map(owner -> {
                    var visits = visitsService.getVisitsForPets(owner.getPetIds());
                    return addVisitsToOwner(owner).apply(visits != null ? visits: emptyVisitsForPets());
                }
            );
    }

    private Function<Visits, OwnerDetails> addVisitsToOwner(OwnerDetails owner) {
        return visits -> {
            owner.pets()
                .forEach(pet -> {
                    if (pet.visits() != null) {
                        pet.visits()
                            .addAll(visits.items().stream()
                                .filter(v -> v.petId() == pet.id())
                                .toList());
                    }
                });
            return owner;
        };
    }

    private Visits emptyVisitsForPets() {
        return new Visits(Collections.emptyList());
    }
}
