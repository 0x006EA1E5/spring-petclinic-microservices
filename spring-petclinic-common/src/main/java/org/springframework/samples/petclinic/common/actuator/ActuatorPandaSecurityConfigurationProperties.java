package org.springframework.samples.petclinic.common.actuator;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties defined under <code>management.server.panda</code>. They are used to set up Kitemark
 * compliant Panda security for Actuator endpoints.
 */
@ConfigurationProperties(prefix = "management.server.panda")
public class ActuatorPandaSecurityConfigurationProperties {
    /**
     * Whether any Panda security should be configured for actuator endpoints.
     *
     * Set to <code>false</code> to completely disable this custom Panda configuration for Actuator endpoints, and fall
     * back on the default Panda behaviour, which is to allow all authenticated users access to the endpoints.
     *
     * n.b., this default Panda behaviour is inadequate to meet the Kitemarks which require endpoints to be configured
     * according to the principle of least privilege. See
     * https://internal-documentation.pages.tech.lastmile.com/engprod/development-handbook/#/sw-development/technical-standards?id=d12
     *
     * <strong>Optional</strong>. Default is <code>true</code>.
     */
    private boolean enabled = true;

    /**
     * Access role for Actuator endpoints.
     *
     * <strong>Optional</strong>. Default is <code>ACTUATOR</code>
     * <p>For this to work you need to configure OAUTH2 role with the matching name in your panda configuration e.g.:</p>
     * <code>
     * {
     *   "applicationId": "yourapplicationid",
     *   "formatVersion": 1,
     *   "permissions": {
     *       "ACTUATOR": {
     *           "description": "Access to Actuator endpoints",
     *           "grantedToRoles": [
     *               "rw_osp_monitoring"
     *           ]
     *       }
     *   }
     * }
     * </code>
     */
    private String accessRole = "ACTUATOR";

    /**
     * The client ID regex to match against the authentication principal for the Prometheus actuator endpoint.
     * This tests for client IDs ending with <code>prometheuspharos</code>, as some clients add an <code>env</code>
     * prefix to the client ID.
     *
     * <strong>Optional</strong>. Default is <code>^(.*)prometheuspharos$</code>
     */
    private String prometheusClientIdPattern = "^(.*)prometheuspharos$";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAccessRole() {
        return accessRole;
    }

    public void setAccessRole(String accessRole) {
        this.accessRole = accessRole;
    }

    public String getPrometheusClientIdPattern() {
        return prometheusClientIdPattern;
    }

    public void setPrometheusClientIdPattern(String prometheusClientIdPattern) {
        this.prometheusClientIdPattern = prometheusClientIdPattern;
    }
}
