package org.grails.config;

import com.codahale.metrics.MetricRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricRegistryConfig {

    @Bean
    public MetricRegistry metricRegistry() {
        return new MetricRegistry();
    }
}
