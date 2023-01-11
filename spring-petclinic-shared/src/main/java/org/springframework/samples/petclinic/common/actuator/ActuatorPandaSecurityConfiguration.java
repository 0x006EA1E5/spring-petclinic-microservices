package org.springframework.samples.petclinic.common.actuator;

import com.ocadotechnology.jarjar.security.web.customizer.api.JarJarSecurityFilterChainCustomizer;
import com.ocadotechnology.jarjar.security.web.panda.auth.PandaOAuth2ClientAuthentication;
import com.ocadotechnology.jarjar.security.web.panda.customizer.PandaSsoSecurityCustomizerOrder;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AndRequestMatcher;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * If Panda is present on the classpath, provides an {@link PandaOAuth2ClientAuthentication}
 * to configure security for Actuator endpoints.
 **/
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ActuatorPandaSecurityConfigurationProperties.class)
@ConditionalOnClass({PandaOAuth2ClientAuthentication.class, EndpointRequest.class})
@ConditionalOnProperty(prefix = "management.server.panda", name = "enabled", matchIfMissing = true)
class ActuatorPandaSecurityConfiguration {
    @Bean
    JarJarSecurityFilterChainCustomizer jarJarActuatorAuthoriseFilterChainCustomizer(
            ActuatorPandaSecurityConfigurationProperties actuatorPandaSecurityConfigurationProperties) {
        return new JarJarActuatorAuthoriseFilterChainCustomizer(actuatorPandaSecurityConfigurationProperties);
    }

    /**
     * {@link JarJarSecurityFilterChainCustomizer} implementation which configures
     * security for Actuator Endpoints via Panda.
     * <p>
     * We need to set an order of as most
     * {@code PandaSsoSecurityCustomizerOrder.OPTIONAL_AUTH - 1}, so that this
     * configuration takes precedence over the default Panda config.
     *
     * @see PandaSsoSecurityCustomizerOrder
     */
    @Order(PandaSsoSecurityCustomizerOrder.OPTIONAL_AUTH - 1)
    private static class JarJarActuatorAuthoriseFilterChainCustomizer implements JarJarSecurityFilterChainCustomizer {
        private final ActuatorPandaSecurityConfigurationProperties actuatorPandaSecurityConfigurationProperties;

        JarJarActuatorAuthoriseFilterChainCustomizer(ActuatorPandaSecurityConfigurationProperties actuatorPandaSecurityConfigurationProperties) {
            this.actuatorPandaSecurityConfigurationProperties = actuatorPandaSecurityConfigurationProperties;
        }

        @Override
        public void customize(HttpSecurity httpSecurity) throws Exception {
            final var prometheusClientIdPattern = Objects.requireNonNull(actuatorPandaSecurityConfigurationProperties.getPrometheusClientIdPattern(),
                    "prometheusClientIdPattern cannot be null");
            httpSecurity.authorizeHttpRequests()
                    .requestMatchers(new AndRequestMatcher(EndpointRequest.to(PrometheusScrapeEndpoint.class),
                            ActuatorPandaSecurityConfiguration::isPrometheusClientOnly))
                    .access(clientIdMatches(prometheusClientIdPattern));

            httpSecurity.authorizeHttpRequests().requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole(
                    actuatorPandaSecurityConfigurationProperties.getAccessRole());

            httpSecurity.authorizeHttpRequests().requestMatchers(EndpointRequest.toAnyEndpoint()).denyAll();
        }
    }

    static AuthorizationManager<RequestAuthorizationContext> clientIdMatches(String prometheusClientIdPattern) {
        return (authentication, requestAuthorizationContext) -> {
            var pandaOAuth2ClientAuthentication = (PandaOAuth2ClientAuthentication) authentication.get();
            return new AuthorizationDecision(
                    pandaOAuth2ClientAuthentication.getPrincipal().toString().matches(prometheusClientIdPattern));
        };
    }

    static boolean isPrometheusClientOnly(HttpServletRequest request) {
        return request.getUserPrincipal() instanceof PandaOAuth2ClientAuthentication;
    }
}
