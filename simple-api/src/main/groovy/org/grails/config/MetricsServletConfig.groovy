package org.grails.config

import com.codahale.metrics.servlets.MetricsServlet
import org.springframework.boot.context.embedded.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetricsServletConfig {

    @Bean
    public ServletRegistrationBean metricServlet(ApplicationContext applicationContext) {
        applicationContext.servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY, applicationContext.getBean('metricRegistry'))
        return new ServletRegistrationBean(new MetricsServlet(), "/metrics")
    }
}
