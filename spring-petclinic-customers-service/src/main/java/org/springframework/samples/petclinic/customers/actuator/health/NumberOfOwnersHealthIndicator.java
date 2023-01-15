package org.springframework.samples.petclinic.customers.actuator.health;

import com.ocadotechnology.jarjar.core.health.model.HealthDetails;
import com.ocadotechnology.jarjar.core.health.model.HealthStatus;
import com.ocadotechnology.jarjar.core.health.service.HealthDetailsIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "petclinic.feature-flags.health", name = "too-many-events", matchIfMissing = false)
public class NumberOfOwnersHealthIndicator implements HealthIndicator, HealthDetailsIndicator {
    private final OwnerRepository ownerRepository;

    @Autowired
    public NumberOfOwnersHealthIndicator(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Health health() {
        return ownerRepository.findAll().size() > 15
            ? Health.down().build()
            : Health.up().build();
    }

    @Override
    public HealthDetails getHealthDetails() {
        var healthStatus = health().getStatus().equals(Status.DOWN)
            ? HealthStatus.unhealthy
            : HealthStatus.healthy;
        return HealthDetails.Builder
            .healthDetails()
            .withHealth(healthStatus)
            .build();
    }
}
