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
package org.springframework.samples.petclinic.customers.web;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.customers.exceptions.TooManyOwnersException;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Maciej Szarlinski
 */
@RequestMapping("/owners")
@RestController
//@Timed("petclinic.owner")
//@PreAuthorize("hasAuthority('ROLE_ADMIN')")
class OwnerResource {
    private static final Logger logger = LoggerFactory.getLogger(OwnerResource.class);

    private final OwnerRepository ownerRepository;
    private final Gauge numberOfOwners;
    @Autowired
    public OwnerResource(OwnerRepository ownerRepository, MeterRegistry meterRegistry) {
        this.ownerRepository = ownerRepository;
        this.numberOfOwners = Gauge
            .builder("owners", () -> findAll().size())
            .description("Number of owners currently in the database")
            .register(meterRegistry);
    }
    /**
     * Create Owner
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Owner createOwner(@Valid @RequestBody Owner owner) throws TooManyOwnersException {
        logger.debug("[createOwner] creating {}", owner);
        return ownerRepository.save(owner);
    }

    /**
     * Read single Owner
     */
    @GetMapping(value = "/{ownerId}")
    public Optional<Owner> findOwner(@PathVariable("ownerId") @Min(1) int ownerId) {
        logger.info("findOwner {}", ownerId);
        return ownerRepository.findById(ownerId);
    }

    @DeleteMapping(value = "/{ownerId}")
    public void deleteOwner(@PathVariable("ownerId") @Min(1) int ownerId) {
        logger.info("findOwner {}", ownerId);
        ownerRepository.deleteById(ownerId);
    }

    /**
     * Read List of Owners
     */
    @GetMapping
    public List<Owner> findAll() {
        logger.info("findAll");
        return ownerRepository.findAll();
    }

    /**
     * Update Owner
     */
    @PutMapping(value = "/{ownerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateOwner(@PathVariable("ownerId") @Min(1) int ownerId, @Valid @RequestBody Owner ownerRequest) {
        final Optional<Owner> owner = ownerRepository.findById(ownerId);
        final Owner ownerModel = owner.orElseThrow(() -> new ResourceNotFoundException("Owner "+ownerId+" not found"));

        // This is done by hand for simplicity purpose. In a real life use-case we should consider using MapStruct.
        ownerModel.setFirstName(ownerRequest.getFirstName());
        ownerModel.setLastName(ownerRequest.getLastName());
        ownerModel.setCity(ownerRequest.getCity());
        ownerModel.setAddress(ownerRequest.getAddress());
        ownerModel.setTelephone(ownerRequest.getTelephone());
        logger.info("Saving owner {}", ownerModel);
        ownerRepository.save(ownerModel);
    }
}
