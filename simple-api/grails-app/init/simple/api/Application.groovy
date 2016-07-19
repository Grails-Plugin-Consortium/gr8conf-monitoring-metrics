package simple.api

import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.servlets.MetricsServlet
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import org.grails.config.HealthCheckRegistryConfig
import org.grails.config.HystrixConfig
import org.grails.config.JvmMetricConfig
import org.grails.config.MetricRegistryConfig
import org.grails.config.MetricsServletConfig
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.embedded.ServletRegistrationBean
import org.springframework.context.ApplicationContext
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
@ComponentScan(["org.grails.config"])
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