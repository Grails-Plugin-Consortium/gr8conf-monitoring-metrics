package org.grails.hystrix;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisherCommand;
import com.netflix.hystrix.util.HystrixRollingNumberEvent;

public class HystrixDropwizardMetricsPublisherCommand implements HystrixMetricsPublisherCommand {

	static final String HYSTRIX_COMMAND = "hystrix.command";

	private HystrixCommandKey key;
	private HystrixCommandMetrics metrics;
	private HystrixCircuitBreaker circuitBreaker;
	private MetricRegistry metricRegistry;
	private String metricGroup;
	private String metricType;

	public HystrixDropwizardMetricsPublisherCommand(
			HystrixCommandKey commandKey,
			HystrixCommandMetrics metrics,
			HystrixCircuitBreaker circuitBreaker,
			MetricRegistry metricRegistry) {

		this.key = commandKey;
		this.metrics = metrics;
		this.circuitBreaker = circuitBreaker;
		this.metricRegistry = metricRegistry;
		this.metricGroup = HYSTRIX_COMMAND;
		this.metricType = key.name();
	}

	@Override
	public void initialize() {
		metricRegistry.register(createMetricName("isCircuitBreakerOpen"), (Gauge<Boolean>) () -> circuitBreaker.isOpen());

		createCumulativeCountForEvent("countFailure", HystrixRollingNumberEvent.FAILURE);
		createCumulativeCountForEvent("countSuccess", HystrixRollingNumberEvent.SUCCESS);
		createCumulativeCountForEvent("countThreadPoolRejected", HystrixRollingNumberEvent.THREAD_POOL_REJECTED);
		createCumulativeCountForEvent("countTimeout", HystrixRollingNumberEvent.TIMEOUT);
		createCumulativeCountForEvent("countShortCircuited", HystrixRollingNumberEvent.SHORT_CIRCUITED);

		metricRegistry.register(createMetricName("latencyExecute_mean"), (Gauge<Integer>) () -> metrics.getExecutionTimeMean());
		metricRegistry.register(createMetricName("latencyExecute_percentile_95"), (Gauge<Integer>) () -> metrics.getExecutionTimePercentile(95));
		metricRegistry.register(createMetricName("latencyExecute_percentile_99"), (Gauge<Integer>) () -> metrics.getExecutionTimePercentile(99));
	}

	void createCumulativeCountForEvent(String name, final HystrixRollingNumberEvent event) {
		metricRegistry.register(createMetricName(name), (Gauge<Long>) () -> metrics.getCumulativeCount(event));
	}

	String createMetricName(String name) {
		return MetricRegistry.name(metricGroup, metricType, name);
	}
}
