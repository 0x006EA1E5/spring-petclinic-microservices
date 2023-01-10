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

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ReactiveAdapter;
import org.springframework.http.observation.reactive.ServerRequestObservationContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.samples.petclinic.api.application.CustomersService;
import org.springframework.samples.petclinic.api.application.VisitsService;
import org.springframework.samples.petclinic.api.dto.OwnerDetails;
import org.springframework.samples.petclinic.api.dto.Visits;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * @author Maciej Szarlinski
 */
@RestController
@RequestMapping("/api/gateway")
public class ApiGatewayController {
    private final Logger logger = LoggerFactory.getLogger(ApiGatewayController.class);

    private final CustomersService customersService;

    private final ObservationRegistry observationRegistry;
    private final VisitsService visitsService;


    private final ReactiveCircuitBreakerFactory cbFactory;

    public ApiGatewayController(CustomersService customersService, ObservationRegistry observationRegistry, VisitsService visitsService, ReactiveCircuitBreakerFactory cbFactory) {
        this.customersService = customersService;
        this.observationRegistry = observationRegistry;
        this.visitsService = visitsService;
        this.cbFactory = cbFactory;
    }

    @GetMapping(value = "owners/{ownerId}")
    public Mono<OwnerDetails> getOwnerDetails(ServerHttpRequest httpRequest, final @PathVariable int ownerId) {
        logger.info("getOwnerDetails {}", ownerId);
//        WebClient.builder()
//        Mono<String>
        var args = List.of("hello", "world")
        ;
        ReactiveAdapter adapter = this.reactiveAdapterRegistry.getAdapter(returnType.getParameterType());
        if (adapter == null) {
            return Mono.just(args)
                .handle((arguments, synchronousSink) -> synchronousSink.next(method.invoke(getBean(), arguments)))
                .map(value -> new HandlerResult(this, value, returnType, bindingContext));
        }


//        return Mono.deferContextual((contextView) -> {
////            Observation.createNotStarted("getOwnerDetails", observationRegistry);
////
////            return customersService.getOwner(ownerId)
////                .contextCapture()
////                .contextWrite(context -> context.put());
//
//            if ( contextView.get(ObservationThreadLocalAccessor.KEY) instanceof Observation o) {
//                if (o.getContext() instanceof ServerRequestObservationContext s) {
//                    var serverWebExchange = s.getServerWebExchange();
//
////                            s.getServerWebExchange().mutate()
////
////                    var route = Route.async()
////                        .id("customers-service")
////                        .uri("lb://customers-service")
//////                                .filter((exchange, chain) -> chain.filter(exchange))
////                        .predicate(serverWebExchange1 -> true)
//////                                .getPredicate()
////                        .build();
////                    serverWebExchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);
//                    var observation = Observation.createNotStarted("getOwnerDetails", observationRegistry);
//                    observation.start();
//                    return customersService.getOwner(ownerId)
//                        .doOnEach(signal -> {
//                            Throwable throwable = signal.getThrowable();
//                            if (throwable != null) {
//                                observation.error(throwable);
//                            }
//                            observation.stop();
//                        })
//                        .doOnCancel(() -> observation.stop())
//                        .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation));
//                }
//            }
//            return Mono.empty();
////            return customersService.getOwner(ownerId)
////                .contextWrite(context -> context.put("hello", "world!"));
//        });
        return customersService.getOwner(ownerId);
//        return customersService.getOwner(ownerId)
//            .flatMap(owner ->
//                visitsService.getVisitsForPets(owner.getPetIds())
//                    .transform(it -> {
//                        ReactiveCircuitBreaker cb = cbFactory.create("getOwnerDetails");
//                        return cb.run(it, throwable -> emptyVisitsForPets());
//                    })
//                    .map(addVisitsToOwner(owner))
//            )
//            .contextCapture();

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

    private Mono<Visits> emptyVisitsForPets() {
        return Mono.just(new Visits(Collections.emptyList()));
    }
}
