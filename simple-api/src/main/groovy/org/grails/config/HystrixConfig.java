package org.grails.config;

import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.Hystrix;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCacheAspect;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.netflix.hystrix.strategy.HystrixPlugins;
import org.grails.hystrix.HystrixDropwizardMetricsPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAspectJAutoProxy
public class HystrixConfig {

    @Autowired
    private MetricRegistry metricRegistry;

    @Bean
    public HystrixCommandAspect hystrixCommandAspect() {
        return new HystrixCommandAspect();
    }

    @Bean
    public HystrixCacheAspect hystrixCacheAspect() {
        return new HystrixCacheAspect();
    }

    @Bean
    public ServletRegistrationBean hystrixStream(ApplicationContext context) {
        return new ServletRegistrationBean(new HystrixMetricsStreamServlet(), "/hystrix.stream");
    }

    @PostConstruct
    public void initPlugins() {
        Hystrix.reset();
        HystrixPlugins.getInstance().registerMetricsPublisher(new HystrixDropwizardMetricsPublisher(metricRegistry));
    }

    @PreDestroy
    public void cleanup() {
        Hystrix.reset(10, TimeUnit.SECONDS);
    }
}
