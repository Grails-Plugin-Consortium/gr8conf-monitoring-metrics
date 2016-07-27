package simple.api

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.config.*
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@Import([
        MetricsServletConfig,
        MetricRegistryConfig,
        HystrixConfig,
        HealthCheckRegistryConfig,
        JvmMetricConfig
])
@ComponentScan(["org.grails.config","org.grails.health"])
@EnableAutoConfiguration(exclude = [MetricRepositoryAutoConfiguration])
class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Bean
    public String applicationName() {
        return "simple-api";
    }

    @Bean
    public String groupName() {
        return "grails";
    }
}