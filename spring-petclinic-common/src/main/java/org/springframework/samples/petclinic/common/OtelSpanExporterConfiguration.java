package org.springframework.samples.petclinic.common;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
class OtelSpanExporterConfiguration {
    @Bean
    OtlpHttpSpanExporter otlpHttpSpanExporter() {
        return OtlpHttpSpanExporter.builder()
            .setEndpoint("https://use1-opentelemetrycollector.atm-osp.com/v1/traces")
            .build();
    }
}
