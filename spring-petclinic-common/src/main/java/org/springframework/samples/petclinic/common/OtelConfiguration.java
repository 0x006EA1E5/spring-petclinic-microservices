package org.springframework.samples.petclinic.common;

import io.prometheus.client.exemplars.DefaultExemplarSampler;
import io.prometheus.client.exemplars.ExemplarSampler;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContextSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OtelConfiguration {
//    @Bean
//    PrometheusMeterRegistry prometheusMeterRegistry(PrometheusConfig prometheusConfig, CollectorRegistry collectorRegistry, Clock clock, ExemplarSampler exemplarSampler) {
//        return new PrometheusMeterRegistry(prometheusConfig, collectorRegistry, clock, exemplarSampler);
//    }
//
//    @Bean
//    DefaultExemplarSampler exemplarSampler(SpanContextSupplier spanContextSupplier) {
//        return new DefaultExemplarSampler(spanContextSupplier);
//    }

    @Bean
    SpanContextSupplier spanContextSupplier() {
        return new OpenTelemetryAgentSpanContextSupplier();
    }

    @Bean
    ExemplarSampler exemplarMarkingExemplarSampler(SpanContextSupplier spanContextSupplier) {
        return new ExemplarMarkingExemplarSampler(new DefaultExemplarSampler(spanContextSupplier));
    }
}
