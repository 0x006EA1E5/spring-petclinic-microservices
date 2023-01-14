package org.springframework.samples.petclinic.customers.actuator.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.stereotype.Component;

@Component
public class NumberOfOwnersHealthIndicator implements HealthIndicator {
    private final OwnerRepository ownerRepository;

    @Autowired
    public NumberOfOwnersHealthIndicator(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Health health() {
        if (ownerRepository.findAll().size() > 15) {
            return Health.down().build();
        }
        return Health.up().build();
    }
}
