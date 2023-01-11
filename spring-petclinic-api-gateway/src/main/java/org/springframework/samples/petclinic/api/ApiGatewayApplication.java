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
package org.springframework.samples.petclinic.api;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.gateway.filter.headers.observation.ObservedRequestHttpHeadersFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.observation.reactive.ServerRequestObservationContext;
import org.springframework.samples.petclinic.api.application.CustomersService;
import org.springframework.samples.petclinic.api.application.VisitsService;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ExchangeFunctions;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;


/**
 * @author Maciej Szarlinski
 */
@SpringBootApplication

public class ApiGatewayApplication {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayApplication.class);
    public ApiGatewayApplication(Tracer tracer, ObservationRegistry observationRegistry) {
        this.tracer = tracer;
        this.observationRegistry = observationRegistry;
    }

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    @LoadBalanced
    RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }

    @Value("classpath:/static/index.html")
    private Resource indexHtml;

    /**
     * workaround solution for forwarding to index.html
     * @see <a href="https://github.com/spring-projects/spring-boot/issues/9785">#9785</a>
     */
    @Bean
    RouterFunction<?> routerFunction() {
        RouterFunction router = RouterFunctions.resources("/**", new ClassPathResource("static/"))
            .andRoute(RequestPredicates.GET("/"),
                request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
        return router;
    }

    @Bean
    VisitsService visitsService(WebClient.Builder builder) {
        var client = builder
            .uriBuilderFactory(new DefaultUriBuilderFactory("http://api-gateway/"))
//            .uriBuilderFactory(new DefaultUriBuilderFactory("http://visits-service/"))
            .build();

        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build()
            .createClient(VisitsService.class);
    }

    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;

    @Bean
    CustomersService customersService(WebClient.Builder builder, ObservedRequestHttpHeadersFilter observedRequestHttpHeadersFilter) {
        var reactorClientHttpConnector = new ReactorClientHttpConnector();
        var exchangeFunction = ExchangeFunctions.create(reactorClientHttpConnector);
        var client = builder
//            .uriBuilderFactory(new DefaultUriBuilderFactory("http://api-gateway/api/customer/"))
//            .uriBuilderFactory(new DefaultUriBuilderFactory("http://visits-service/"))
            .uriBuilderFactory(new DefaultUriBuilderFactory("lb://customers-service"))
            .filter((request, next) -> {

                return Mono.deferContextual(contextView -> {


                    if ( contextView.get(ObservationThreadLocalAccessor.KEY) instanceof Observation o) {
                        if (o.getContext() instanceof ServerRequestObservationContext s) {
                            var serverWebExchange = s.getServerWebExchange();

//                            s.getServerWebExchange().mutate()

                            var route = Route.async()
                                .id("customers-service")
                                .uri("lb://customers-service")
//                                .filter((exchange, chain) -> chain.filter(exchange))
                                .predicate(serverWebExchange1 -> true)
//                                .getPredicate()
                                .build();
                            serverWebExchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR, route);

                            var observation = Observation.createNotStarted("customersService.exchange", o::getContext, observationRegistry);
                            observation.start();
//                                var httpHeaders = observedRequestHttpHeadersFilter.filter(request.headers(), serverWebExchange);
//                                logger.info("traceparent {}", httpHeaders.get("traceparent"));
//                                var r = ClientRequest.from(request)
//                                    .headers(httpHeaders1 -> httpHeaders1.addAll(httpHeaders))
//                                    .build();
                                return next.exchange(request).doOnEach(signal -> {
                                        Throwable throwable = signal.getThrowable();
                                        if (throwable != null) {
                                            observation.error(throwable);
                                        }
                                        observation.stop();
                                    })
                                    .doOnCancel(() -> observation.stop())
                                    .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation));

                        }
                    }
                    return next.exchange(request);
//                    try (ContextSnapshot.Scope scope = ContextSnapshot.setThreadLocalsFrom(contextView, ObservationThreadLocalAccessor.KEY)) {
//
//
//                        var traceParent = "00-" + tracer.currentTraceContext().context().traceId() + "-" + tracer.currentTraceContext().context().spanId() + "-01";
//                        var r = ClientRequest.from(request)
//                            .header("traceparent", traceParent)
//                            .build();
//                        return next.exchange(r);
//
//                    }
                })
                    ;
            })
            .clientConnector((method, uri, requestCallback) -> {
                Function<? super ClientHttpRequest, Mono<Void>> requestCallback2 =
                    r -> requestCallback.apply(r);

                return reactorClientHttpConnector
                    .connect(method, uri, requestCallback2);
            })
            .exchangeFunction(request -> exchangeFunction.exchange(request))
            .build();

        return HttpServiceProxyFactory.builder(WebClientAdapter.forClient(client)).build()
            .createClient(CustomersService.class);
    }

    /**
     * Default Resilience4j circuit breaker configuration
     */
    @Bean
    public Customizer<ReactiveResilience4JCircuitBreakerFactory> defaultCustomizer() {
        return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
            .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
            .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(Duration.ofSeconds(4)).build())
            .build());
    }

    @Bean
//    @ConditionalOnMissingBean
//    @ConditionalOnClass({ ContextSnapshot.class, Hooks.class })
    public ApplicationListener<ContextRefreshedEvent> observationContextPropagationDecoratorReactorHookRegistrar() {
        return event -> Hooks.onEachOperator(org.springframework.boot.actuate.tracing.ObservationContextPropagationDecorator.class.getSimpleName(),
            org.springframework.boot.actuate.tracing.ObservationContextPropagationDecorator.decortator());
    }
}
