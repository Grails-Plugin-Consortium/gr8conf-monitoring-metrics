package org.grails.hystrix;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolMetrics;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherThreadPool;

public class HystrixDropwizardMetricsPublisherThreadPool implements HystrixMetricsPublisherThreadPool {

	static final String HYSTRIX_THREAD_POOL = "hystrix.threadPool";

	private HystrixThreadPoolKey key;
	private HystrixThreadPoolMetrics metrics;
	private MetricRegistry metricRegistry;
	private String metricGroup;
	private String metricType;

	public HystrixDropwizardMetricsPublisherThreadPool(
			HystrixThreadPoolKey threadPoolKey,
			HystrixThreadPoolMetrics metrics,
			MetricRegistry metricRegistry) {

		this.key = threadPoolKey;
		this.metrics = metrics;
		this.metricRegistry = metricRegistry;
		this.metricGroup = HYSTRIX_THREAD_POOL;
		this.metricType = key.name();
	}

	@Override
	public void initialize() {
		metricRegistry.register(createMetricName("threadActiveCount"), (Gauge<Number>) () -> metrics.getCurrentActiveCount());
		metricRegistry.register(createMetricName("countThreadsExecuted"), (Gauge<Number>) () -> metrics.getCumulativeCountThreadsExecuted());
		metricRegistry.register(createMetricName("queueSize"), (Gauge<Number>) () -> metrics.getCurrentQueueSize());
	}

	String createMetricName(String name) {
		return MetricRegistry.name(metricGroup, metricType, name);
	}
}
