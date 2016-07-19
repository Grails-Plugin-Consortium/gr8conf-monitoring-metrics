package org.grails.hystrix;

import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.*;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;

public class HystrixDropwizardMetricsPublisher extends HystrixMetricsPublisher {

    private MetricRegistry metricRegistry;

    public HystrixDropwizardMetricsPublisher(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public HystrixMetricsPublisherCommand getMetricsPublisherForCommand(
            HystrixCommandKey commandKey,
            HystrixCommandGroupKey commandGroupKey,
            HystrixCommandMetrics metrics,
            HystrixCircuitBreaker circuitBreaker,
            HystrixCommandProperties properties) {

        return new HystrixDropwizardMetricsPublisherCommand(commandKey, metrics, circuitBreaker, metricRegistry);
    }

    @Override
    public HystrixMetricsPublisherThreadPool getMetricsPublisherForThreadPool(
            HystrixThreadPoolKey threadPoolKey,
            HystrixThreadPoolMetrics metrics,
            HystrixThreadPoolProperties properties) {

        return new HystrixDropwizardMetricsPublisherThreadPool(threadPoolKey, metrics, metricRegistry);
    }
}
