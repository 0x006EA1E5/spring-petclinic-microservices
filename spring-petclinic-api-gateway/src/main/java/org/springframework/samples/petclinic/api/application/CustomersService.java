package org.springframework.samples.petclinic.api.application;

import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface CustomersService {
    @GetExchange("/owners/{ownerId}")
    Mono<OwnerDetails> getOwner(@PathVariable int ownerId);
}
