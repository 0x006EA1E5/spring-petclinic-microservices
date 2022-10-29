package org.springframework.samples.petclinic.api.boundary.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.samples.petclinic.api.application.CustomersServiceClient;
import org.springframework.samples.petclinic.api.application.VisitsServiceClient;
import org.springframework.samples.petclinic.api.dto.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.net.ConnectException;
import java.util.Collections;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ApiGatewayController.class)
@Import({ReactiveResilience4JAutoConfiguration.class, CircuitBreakerConfiguration.class})
class ApiGatewayControllerTest {

    @MockBean
    private CustomersServiceClient customersServiceClient;

    @MockBean
    private VisitsServiceClient visitsServiceClient;

    @Autowired
    private WebTestClient client;


    @Test
    void getOwnerDetails_withAvailableVisitsService() {
        PetDetails cat = new PetDetails(20, "Garfield", new PetType("cat"), Collections.emptyList());
        OwnerDetails owner = new OwnerDetails(0, null, null, null, null, null, Collections.singletonList(cat));
        Mockito
            .when(customersServiceClient.getOwner(1))
            .thenReturn(Mono.just(owner));

        VisitDetails visit = new VisitDetails(
            300,
            cat.id(),
            "",
            "First visit"
        );
        Visits visits = new Visits(Collections.singletonList(visit));

        Mockito
            .when(visitsServiceClient.getVisitsForPets(Collections.singletonList(cat.id())))
            .thenReturn(Mono.just(visits));

        client.get()
            .uri("/api/gateway/owners/1")
            .exchange()
            .expectStatus().isOk()
            //.expectBody(String.class)
            //.consumeWith(response ->
            //    Assertions.assertThat(response.getResponseBody()).isEqualTo("Garfield"));
            .expectBody()
            .jsonPath("$.pets[0].name").isEqualTo("Garfield")
            .jsonPath("$.pets[0].visits[0].description").isEqualTo("First visit");
    }

    /**
     * Test Resilience4j fallback method
     */
    @Test
    void getOwnerDetails_withServiceError() {
        PetDetails cat = new PetDetails(20, "Garfield", new PetType("cat"), Collections.emptyList());
        OwnerDetails owner = new OwnerDetails(0, null, null, null, null, null, Collections.singletonList(cat));
        Mockito
            .when(customersServiceClient.getOwner(1))
            .thenReturn(Mono.just(owner));

        Mockito
            .when(visitsServiceClient.getVisitsForPets(Collections.singletonList(cat.id())))
            .thenReturn(Mono.error(new ConnectException("Simulate error")));

        client.get()
            .uri("/api/gateway/owners/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.pets[0].name").isEqualTo("Garfield")
            .jsonPath("$.pets[0].visits").isEmpty();
    }

}
