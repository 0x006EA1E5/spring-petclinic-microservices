package org.springframework.samples.petclinic.common;

import io.opentelemetry.api.trace.Span;
import io.prometheus.client.exemplars.Exemplar;
import io.prometheus.client.exemplars.ExemplarSampler;

/**
 * Assumes {@code delegateExemplarSampler} we can rely on ThreadLocal to get the current span
 */
class ExemplarMarkingExemplarSampler implements ExemplarSampler {
    public final ExemplarSampler delegateExemplarSampler;

    public ExemplarMarkingExemplarSampler(ExemplarSampler delegateExemplarSampler) {
        this.delegateExemplarSampler = delegateExemplarSampler;
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
        Span.current().setAttribute("sampling.exemplar", "true");
    }
}
