package org.grails.health;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandMetrics;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class HystrixMetricsHealthIndicator extends AbstractHealthIndicator {
    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        List<String> breakers = new ArrayList<>();
        for (HystrixCommandMetrics metrics : HystrixCommandMetrics.getInstances()) {
            HystrixCircuitBreaker breaker = HystrixCircuitBreaker.Factory.getInstance(metrics.getCommandKey());
            boolean breakerOpen = breaker.isOpen();
            if (breakerOpen) {
                breakers.add(metrics.getCommandGroup().name() + "::" + metrics.getCommandKey().name());
            }
        }
        if (breakers.size() > 0) {
            builder.outOfService().withDetail("openCircuitBreakers", breakers);
        } else {
            builder.up();
        }
    }
}