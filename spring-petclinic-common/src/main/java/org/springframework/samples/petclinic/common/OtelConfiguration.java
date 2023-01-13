package org.springframework.samples.petclinic.common;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exemplars.DefaultExemplarSampler;
import io.prometheus.client.exemplars.ExemplarSampler;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContextSupplier;
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
//    ExemplarSampler exemplarSampler(SpanContextSupplier spanContextSupplier) {
//        return new DefaultExemplarSampler(spanContextSupplier);
//    }

    @Bean
    SpanContextSupplier spanContextSupplier() {
        return new OpenTelemetrySpanContextSupplier();
    }
}
