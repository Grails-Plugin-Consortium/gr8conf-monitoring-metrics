package org.grails.config;

import com.codahale.metrics.MetricRegistry;
import org.grails.metrics.MetricUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class JvmMetricConfig {

    @Autowired
    private MetricRegistry metricRegistry;

    @PostConstruct
    public void initMetrics() {
        MetricUtil.registerJvmMemoryMetrics(metricRegistry);
        MetricUtil.registerJvmGarbageCollectorMetrics(metricRegistry);
        MetricUtil.registerJvmThreadMetrics(metricRegistry);
        MetricUtil.registerJvmClassMetrics(metricRegistry);
    }
}
