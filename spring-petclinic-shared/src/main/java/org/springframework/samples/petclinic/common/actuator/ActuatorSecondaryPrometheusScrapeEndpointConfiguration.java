package org.springframework.samples.petclinic.common.actuator;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.actuate.metrics.export.prometheus.TextOutputFormat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Set;

/**
 * Configurations for Actuator secondary Prometheus scrape endpoint.
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ActuatorSecondaryPrometheusScrapeEndpointProperties.class)
//@AutoConfigureAfter(PrometheusMetricsExportAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = PrometheusScrapeEndpoint.class)
//@ConditionalOnBean(PrometheusScrapeEndpoint.class)
@ConditionalOnProperty(prefix = "management.endpoint.prometheus.secondary-endpoint", name = "enabled", matchIfMissing = true)
public class ActuatorSecondaryPrometheusScrapeEndpointConfiguration {
    @Bean
    RouterFunction<ServerResponse> secondaryPrometheusEndpoint(
            PrometheusScrapeEndpoint prometheusScrapeEndpoint,
            ActuatorSecondaryPrometheusScrapeEndpointProperties actuatorSecondaryPrometheusScrapeEndpointProperties,
            ConversionService mvcConversionService) {
        return RouterFunctions.route()
                .GET(actuatorSecondaryPrometheusScrapeEndpointProperties.getPath(), req -> {
                    @SuppressWarnings("unchecked")
                    Set<String> includedNames = req.param("includedNames").map(value -> mvcConversionService.convert(value, Set.class)).orElse(null);
                    var scrapeResponse = prometheusScrapeEndpoint.scrape(TextOutputFormat.CONTENT_TYPE_004, includedNames);
                    return ServerResponse.status(scrapeResponse.getStatus())
                            .contentType(new MediaType(TextOutputFormat.CONTENT_TYPE_004.getProducedMimeType()))
                            .body(scrapeResponse.getBody());
                }).build();
    }
}
