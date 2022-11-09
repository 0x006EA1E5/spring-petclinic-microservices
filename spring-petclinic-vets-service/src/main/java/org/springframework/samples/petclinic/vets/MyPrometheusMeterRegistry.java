package org.springframework.samples.petclinic.vets;

import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exemplars.ExemplarSampler;

public class MyPrometheusMeterRegistry extends io.micrometer.prometheus.PrometheusMeterRegistry {
    public MyPrometheusMeterRegistry(PrometheusConfig config) {
        super(config);
    }

    public MyPrometheusMeterRegistry(PrometheusConfig config, CollectorRegistry registry, Clock clock) {
        super(config, registry, clock);
    }

    public MyPrometheusMeterRegistry(PrometheusConfig config, CollectorRegistry registry, Clock clock, ExemplarSampler exemplarSampler) {
        super(config, registry, clock, exemplarSampler);
    }
}
