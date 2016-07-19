package org.grails.config;

import com.codahale.metrics.health.HealthCheckRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HealthCheckRegistryConfig {

	@Bean
	public HealthCheckRegistry healthCheckRegistry() { return new HealthCheckRegistry(); }
}
