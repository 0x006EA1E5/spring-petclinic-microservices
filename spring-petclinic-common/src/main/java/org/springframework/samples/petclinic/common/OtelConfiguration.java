package org.springframework.samples.petclinic.common;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.opentelemetry.api.trace.Span;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exemplars.DefaultExemplarSampler;
import io.prometheus.client.exemplars.ExemplarSampler;
import io.prometheus.client.exemplars.tracer.common.SpanContextSupplier;
import io.prometheus.client.exemplars.tracer.otel.OpenTelemetrySpanContextSupplier;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

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
        return new OpenTelemetrySpanContextSupplier();
    }

    @Bean
    Filter setSamplingPriorityFilter() {
        return (servletRequest, servletResponse, filterChain) -> {
            Span.current().setAttribute("sampling.priority", 10);
            filterChain.doFilter(servletRequest, servletResponse);
        };
    }
}
