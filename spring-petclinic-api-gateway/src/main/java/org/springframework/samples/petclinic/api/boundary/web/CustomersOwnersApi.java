package org.springframework.samples.petclinic.api.boundary.web;

import org.springframework.samples.petclinic.api.application.CustomersService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
class CustomersApi {
    private final CustomersService customersService;

    CustomersApi(CustomersService customersService) {
        this.customersService = customersService;
    }

    @GetMapping("/owners")
    List<Owner>
}
