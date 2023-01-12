package org.springframework.samples.petclinic.api.application;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient("petcliniccustomersservice")
public interface CustomersService {
    @GetMapping("/owners/{ownerId}")
    Optional<OwnerDetails> getOwner(@PathVariable int ownerId);
}
