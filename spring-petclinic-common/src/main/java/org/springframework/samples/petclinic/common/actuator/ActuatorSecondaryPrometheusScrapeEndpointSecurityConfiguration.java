package org.springframework.samples.petclinic.common.actuator;

import com.ocadotechnology.jarjar.security.web.customizer.api.JarJarSecurityFilterChainCustomizer;
import com.ocadotechnology.jarjar.security.web.panda.auth.PandaOAuth2ClientAuthentication;
import com.ocadotechnology.jarjar.security.web.panda.customizer.PandaSsoSecurityCustomizerOrder;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.samples.petclinic.common.actuator.ActuatorPandaSecurityConfiguration.clientIdMatches;


/**
 * Configurations for Actuator secondary Prometheus scrape endpoint security, if panda is on classpath.
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ActuatorSecondaryPrometheusScrapeEndpointProperties.class)
@AutoConfigureAfter(PrometheusMetricsExportAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = PrometheusScrapeEndpoint.class)
@ConditionalOnBean(PrometheusScrapeEndpoint.class)
@ConditionalOnProperty(prefix = "management.endpoint.prometheus.secondary-endpoint", name = "enabled", matchIfMissing = true)
class ActuatorSecondaryPrometheusScrapeEndpointSecurityConfiguration {
    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ActuatorSecondaryPrometheusScrapeEndpointProperties.class)
    @ConditionalOnClass(PandaOAuth2ClientAuthentication.class)
    @ConditionalOnProperty(prefix = "management.server.panda", name = "enabled", matchIfMissing = true)
    static class SecurityConfiguration {
        @Bean
        PrometheusEndpointFilterChainCustomizer prometheusSecondaryEndpointFilterChainCustomizer(
                ActuatorPandaSecurityConfigurationProperties actuatorPandaSecurityConfigurationProperties,
                ActuatorSecondaryPrometheusScrapeEndpointProperties actuatorSecondaryPrometheusScrapeEndpointProperties) {
            return new PrometheusEndpointFilterChainCustomizer(
                    actuatorSecondaryPrometheusScrapeEndpointProperties.getPath(), actuatorPandaSecurityConfigurationProperties.getAccessRole(),
                    actuatorPandaSecurityConfigurationProperties.getPrometheusClientIdPattern()
            );
        }
    }

    @Order(PandaSsoSecurityCustomizerOrder.OPTIONAL_AUTH - 1)
    static final class PrometheusEndpointFilterChainCustomizer implements JarJarSecurityFilterChainCustomizer {
        private final String prometheusEndpointPath;
        private final String accessRole;
        private final String prometheusClientIdPattern;

        private PrometheusEndpointFilterChainCustomizer(String prometheusEndpointPath, String accessRole, String prometheusClientIdPattern) {
            this.prometheusEndpointPath = prometheusEndpointPath;
            this.accessRole = accessRole;
            this.prometheusClientIdPattern = prometheusClientIdPattern;
        }

        @Override
        public void customize(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests().requestMatchers(
                            new AndRequestMatcher(
                                    new AntPathRequestMatcher(prometheusEndpointPath),
                                    ActuatorPandaSecurityConfiguration::isPrometheusClientOnly
                            ))
                    .access(clientIdMatches(prometheusClientIdPattern))
                    .requestMatchers(AntPathRequestMatcher.antMatcher(prometheusEndpointPath))
                    .hasRole(accessRole);
        }
    }
}
