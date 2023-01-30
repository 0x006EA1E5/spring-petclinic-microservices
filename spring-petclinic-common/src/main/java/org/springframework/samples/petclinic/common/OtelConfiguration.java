package org.springframework.samples.petclinic.common;

import io.micrometer.tracing.Tracer;
import io.prometheus.client.exemplars.DefaultExemplarSampler;
import io.prometheus.client.exemplars.ExemplarSampler;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel_agent.OpenTelemetryAgentSpanContextSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(PrometheusMetricsExportAutoConfiguration.class)
class OtelConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(OtelConfiguration.class);
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
        if (OpenTelemetryAgentSpanContextSupplier.isAvailable()) {
            logger.debug("OpenTelemetryAgentSpanContextSupplier is available, using it.");
            return new OpenTelemetryAgentSpanContextSupplier();
        }
        return new OpenTelemetrySpanContextSupplier();
    }

    @Bean
    @Primary
    ExemplarSampler exemplarMarkingExemplarSampler(SpanContextSupplier spanContextSupplier, Tracer tracer) {
        var defaultExemplarSampler = new DefaultExemplarSampler(spanContextSupplier);
        if (OpenTelemetryAgentSpanContextSupplier.isAvailable()) {
            logger.debug("OpenTelemetryAgentSpanContextSupplier is available, using ExemplarMarkingExemplarSampler");
            return new ExemplarMarkingExemplarSampler(defaultExemplarSampler, tracer);
        }
        return defaultExemplarSampler;

    }
}
