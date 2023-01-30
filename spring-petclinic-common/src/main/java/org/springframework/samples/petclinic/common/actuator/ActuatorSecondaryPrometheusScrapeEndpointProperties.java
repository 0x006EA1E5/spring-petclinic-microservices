package org.springframework.samples.petclinic.common.actuator;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "management.endpoint.prometheus.secondary-endpoint")
public class ActuatorSecondaryPrometheusScrapeEndpointProperties {
    /**
     * Enables a secondary Prometheus scrape endpoint, in addition to the default actuator one.
     * <strong>Optional</strong>. Default is <code>true</code>.
     */
    private boolean enabled = true;

    /**
     * The actual endpoint (path) to expose for Prometheus scraping.
     * <strong>Optional</strong>. Default is <code>/prometheus</code>.
     */
    private String path = "/prometheus";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
