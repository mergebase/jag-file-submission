package ca.bc.gov.open.jag.efilingdiligenclientstarter;

import ca.bc.gov.open.jag.efilingdiligenclient.api.HealthCheckApi;
import ca.bc.gov.open.jag.efilingdiligenclient.api.handler.ApiClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Diligen Client configuration
 */
@Configuration
@EnableConfigurationProperties(DiligenProperties.class)
public class AutoConfiguration {

    private final DiligenProperties diligenProperties;

    public AutoConfiguration(DiligenProperties diligenProperties) {
        this.diligenProperties = diligenProperties;
    }

    /**
     * @return ApiClient - a configured api client for diligen
     */
    @Bean
    public ApiClient apiClient() {
        ApiClient apiClient = new ApiClient();

        if(StringUtils.isBlank(diligenProperties.getBasePath()))
            throw new DiligenConfigurationException("Diligen base path is required");

        apiClient.setBasePath(diligenProperties.getBasePath());

        return apiClient;
    }

    /**
     * @param apiClient - a diligen api client
     * @return HealthCheckApi - diligen healthCheckApi client
     */
    @Bean
    public HealthCheckApi healthCheckApi(ApiClient apiClient) {
        return new HealthCheckApi(apiClient);
    }

    /**
     * @param healthCheckApi - diligen healthcheck api
     * @return Diligen implementation of HealthIndicator
     */
    @Bean
    @ConditionalOnProperty(prefix = "jag.efiling.diligen", name = "health.enabled", havingValue = "true")
    public HealthIndicator diligenHealthIndicator(HealthCheckApi healthCheckApi) {
        return new DiligenHealthIndicator(healthCheckApi);
    }

}
