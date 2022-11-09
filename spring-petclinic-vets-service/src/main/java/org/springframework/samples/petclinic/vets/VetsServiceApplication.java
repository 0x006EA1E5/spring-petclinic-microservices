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
package org.springframework.samples.petclinic.vets;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.core.instrument.search.MeterNotFoundException;
import io.micrometer.observation.Observation;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.TracingAwareMeterObservationHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.autoconfigure.tracing.zipkin.Config;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.samples.petclinic.vets.system.VetsProperties;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author Maciej Szarlinski
 */
@SpringBootApplication
@EnableConfigurationProperties(VetsProperties.class)
@Import({Config.class})
public class VetsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VetsServiceApplication.class, args);
	}

//    @Bean
    TracingAwareMeterObservationHandler<Observation.Context> myTracingAwareMeterObservationHandler(
        MeterRegistry meterRegistry, Tracer tracer,
        MetricsProperties metricsProperties) {
        return new TracingAwareMeterObservationHandler<>(new MyDefaultMeterObservationHandler(meterRegistry, metricsProperties),
            tracer);
    }

    static class MyDefaultMeterObservationHandler extends DefaultMeterObservationHandler {

        private final MeterRegistry meterRegistry;
        private final MetricsProperties metricsProperties;

        public MyDefaultMeterObservationHandler(MeterRegistry meterRegistry, MetricsProperties metricsProperties) {
            super(meterRegistry);
            this.meterRegistry = meterRegistry;
            this.metricsProperties = metricsProperties;
        }

        @Override
        public void onStop(Observation.Context context) {
            Timer.Sample sample = context.getRequired(Timer.Sample.class);

            try {
                sample.stop(this.meterRegistry.get(context.getName()).timer());
            } catch (MeterNotFoundException e) {
                sample.stop(Timer.builder(context.getName())
                    .tags(createErrorTags(context))
                    .tags(createTags(context))
                    .publishPercentileHistogram(
                        metricsProperties.getDistribution().getPercentilesHistogram().get(context.getName()))
                    .distributionStatisticExpiry(
                        metricsProperties.getDistribution().getExpiry().get(context.getName()))
                    .distributionStatisticBufferLength(
                        metricsProperties.getDistribution().getBufferLength().get(context.getName()))
                    .publishPercentiles(
                        metricsProperties.getDistribution().getPercentiles().get(context.getName()))
                    .serviceLevelObjectives(
                        Arrays.stream(metricsProperties.getDistribution().getSlo().get(context.getName()))
                            .map(serviceLevelObjectiveBoundary -> serviceLevelObjectiveBoundary.getValue(Meter.Type.TIMER).longValue())
                            .map(Duration::ofNanos)
                            .toArray(Duration[]::new))
                    .register(this.meterRegistry));
            }

            LongTaskTimer.Sample longTaskSample = context.getRequired(LongTaskTimer.Sample.class);
            longTaskSample.stop();
        }

        private Tags createErrorTags(Observation.Context context) {
            return Tags.of("error", getErrorValue(context));
        }

        private String getErrorValue(Observation.Context context) {
            Throwable error = context.getError();
            return error != null ? error.getClass().getSimpleName() : "none";
        }

        private Tags createTags(Observation.Context context) {
            return Tags.of(context.getLowCardinalityKeyValues().stream().map(tag -> Tag.of(tag.getKey(), tag.getValue()))
                .collect(Collectors.toList()));
        }
    }
}
