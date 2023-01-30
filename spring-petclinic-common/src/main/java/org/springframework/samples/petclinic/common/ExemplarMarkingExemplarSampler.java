package org.springframework.samples.petclinic.common;

import io.micrometer.tracing.Tracer;
import io.opentelemetry.api.trace.Span;
import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exemplars.ExemplarSampler;

/**
 * Assumes {@code delegateExemplarSampler} we can rely on ThreadLocal to get the current span
 */
class ExemplarMarkingExemplarSampler implements ExemplarSampler {
    public final ExemplarSampler delegateExemplarSampler;
    private final Tracer tracer;

    public ExemplarMarkingExemplarSampler(ExemplarSampler delegateExemplarSampler, Tracer tracer) {
        this.delegateExemplarSampler = delegateExemplarSampler;
        this.tracer = tracer;
    }

    @Override
    public Exemplar sample(double increment, Exemplar previous) {
        var sample = delegateExemplarSampler.sample(increment, previous);
        if (sample != null) {
            markSpanAsExemplar();
        }
        return sample;
    }

    @Override
    public Exemplar sample(double value, double bucketFrom, double bucketTo, Exemplar previous) {
        var sample = delegateExemplarSampler.sample(value, bucketFrom, bucketTo, previous);
        if (sample != null) {
            markSpanAsExemplar();
        }
        return sample;
    }

    private void markSpanAsExemplar() {
        tracer.currentSpan().tag("hello", "world");
        Span.current().setAttribute("exemplar", 1);
    }
}
