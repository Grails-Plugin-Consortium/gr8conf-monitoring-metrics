package org.grails.resolver;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.grails.reporter.domain.DatadogCounter;
import org.grails.reporter.domain.DatadogGauge;
import org.grails.reporter.domain.DatadogMetric;
import org.grails.reporter.domain.DatadogSeries;
import org.grails.resolver.hystrix.HystrixCommandResolver;
import org.grails.resolver.hystrix.HystrixThreadPoolResolver;
import org.grails.resolver.jvm.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

public class DatadogReporter extends ScheduledReporter {

	private transient final Logger logger = LoggerFactory.getLogger(DatadogReporter.class);

	private static final Expansion[] STATS_EXPANSIONS = {
			Expansion.MAX,
			Expansion.MEAN,
			Expansion.MIN,
			//Expansion.STD_DEV,
			//Expansion.MEDIAN,
			//Expansion.P75,
			Expansion.P95,
			//Expansion.P98,
			Expansion.P99,
			//Expansion.P999
	};

	/*
	private static final Expansion[] RATE_EXPANSIONS = {
			//Expansion.RATE_1_MINUTE,
			//Expansion.RATE_5_MINUTE,
			//Expansion.RATE_15_MINUTE,
			//Expansion.RATE_MEAN
	};
	*/

	public enum Expansion {
		COUNT("count"),
		RATE_MEAN("meanRate"),
		RATE_1_MINUTE("1MinuteRate"),
		RATE_5_MINUTE("5MinuteRate"),
		RATE_15_MINUTE("15MinuteRate"),
		MIN("min"),
		MEAN("mean"),
		MAX("max"),
		STD_DEV("stddev"),
		MEDIAN("median"),
		P75("p75"),
		P95("p95"),
		P98("p98"),
		P99("p99"),
		P999("p999");

		private String displayName;

		Expansion(String displayName) {
			this.displayName = displayName;
		}

		@Override
		public String toString() {
			return displayName;
		}
	}

	private RestTemplate restTemplate;
	private String url;
	private String apiKey;
	private Clock clock;
	private String host;
	private List<String> tags;
	private List<MetricInfoResolver> resolvers;

	private long lastRun = 0;

	private DatadogReporter(RestTemplate restTemplate,
                            String url,
                            String apiKey,
                            MetricRegistry metricRegistry,
                            MetricFilter filter,
                            String host,
                            List<String> tags,
                            List<MetricInfoResolver> resolvers) {
		super(metricRegistry, "datadog-reporter", filter, TimeUnit.SECONDS, TimeUnit.MILLISECONDS);

		this.restTemplate = restTemplate;
		this.url = url + "/api/v1/series?api_key={apiKey}";
		this.apiKey = apiKey;
		this.clock = Clock.defaultClock();
		this.host = host;
		this.tags = tags == null ? new ArrayList<>() : tags;
		this.resolvers = resolvers == null ? new ArrayList<>() : resolvers;
	}

	@Override
	public void report(SortedMap<String, Gauge> gauges,
			SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters,
			SortedMap<String, Timer> timers) {

		try {

			//prevent a bunch of reports from backing up on heavy load
			if (clock.getTime() - lastRun < 1000) {
				logger.info("Skipping metrics reporting to Datadog");
				return;
			}

			final long timestamp = clock.getTime() / 1000;

			DatadogSeries series = new DatadogSeries();

			series.addAll(createGauges(gauges, timestamp));
			series.addAll(createCounters(counters, timestamp));
			series.addAll(createHistograms(histograms, timestamp));
			series.addAll(createMeters(meters, timestamp));
			series.addAll(createTimers(timers, timestamp));

			if (series.getSeries().isEmpty()) {
				return;
			}

			restTemplate.postForObject(url, series, String.class, apiKey);
		} catch (Throwable e) {
			logger.error("Error reporting metrics to Datadog", e);
		} finally {
			lastRun = clock.getTime();
		}
	}

	List<DatadogMetric> createGauges(SortedMap<String, Gauge> gauges, long timestamp) {
		return gauges.entrySet()
				.stream()
				.filter(e -> toNumber(e.getValue().getValue()) != null)
				.map(e -> {
					MetricInfo metricInfo = getMetricInfo(e.getKey());
					return new DatadogGauge(metricInfo.getName(), toNumber(e.getValue().getValue()), timestamp, host, metricInfo.getTags());
				})
				.collect(Collectors.toList());
	}

	List<DatadogMetric> createCounters(SortedMap<String, Counter> counters, long timestamp) {
		return counters.entrySet()
				.stream()
				.map(e -> {
					MetricInfo metricInfo = getMetricInfo(e.getKey());
					return new DatadogCounter(metricInfo.getName(), e.getValue().getCount(), timestamp, host, metricInfo.getTags());
				})
				.collect(Collectors.toList());
	}

	List<DatadogMetric> createHistograms(SortedMap<String, Histogram> histograms, long timestamp) {
		return histograms.entrySet()
				.stream()
				.flatMap(e -> {
					List<DatadogMetric> metrics = newArrayList();
					MetricInfo metricInfo = getMetricInfo(e.getKey());

					metrics.add(new DatadogCounter(
							appendExpansion(metricInfo.getName(), Expansion.COUNT),
							e.getValue().getCount(),
							timestamp,
							host,
							metricInfo.getTags()));

					Snapshot snapshot = e.getValue().getSnapshot();

					metrics.addAll(Arrays.stream(STATS_EXPANSIONS)
							.map(s -> new DatadogGauge(appendExpansion(metricInfo.getName(), s), getValue(snapshot, s), timestamp, host, metricInfo.getTags()))
							.collect(Collectors.toList()));

					return metrics.stream();
				})
				.collect(Collectors.toList());
	}

	List<DatadogMetric> createMeters(SortedMap<String, Meter> meters, long timestamp) {
		return meters.entrySet()
				.stream()
				.flatMap(e -> {
					List<DatadogMetric> metrics = newArrayList();
					MetricInfo metricInfo = getMetricInfo(e.getKey());

					metrics.add(new DatadogCounter(
							appendExpansion(metricInfo.getName(), Expansion.COUNT),
							e.getValue().getCount(),
							timestamp,
							host,
							metricInfo.getTags()));

					/*
					Meter meter = e.getValue();

					metrics.addAll(Arrays.stream(RATE_EXPANSIONS)
							.map(s -> new DatadogGauge(appendExpansion(metricInfo.getName(), s), convertRate(getValue(meter, s)), timestamp, host, metricInfo.getTags()))
							.collect(Collectors.toList()));
					*/

					return metrics.stream();
				})
				.collect(Collectors.toList());
	}

	List<DatadogMetric> createTimers(SortedMap<String, Timer> timers, long timestamp) {
		return timers.entrySet()
				.stream()
				.flatMap(e -> {
					List<DatadogMetric> metrics = newArrayList();
					MetricInfo metricInfo = getMetricInfo(e.getKey());

					metrics.add(new DatadogCounter(
							appendExpansion(metricInfo.getName(), Expansion.COUNT),
							e.getValue().getCount(),
							timestamp,
							host,
							metricInfo.getTags()));

					Timer timer = e.getValue();
					Snapshot snapshot = timer.getSnapshot();

					/*
					metrics.addAll(Arrays.stream(RATE_EXPANSIONS)
							.map(s -> new DatadogGauge(appendExpansion(metricInfo.getName(), s), convertRate(getValue(timer, s)), timestamp, host, metricInfo.getTags()))
							.collect(Collectors.toList()));
					*/

					metrics.addAll(Arrays.stream(STATS_EXPANSIONS)
							.map(s -> new DatadogGauge(appendExpansion(metricInfo.getName(), s), convertDuration(getValue(snapshot, s).doubleValue()), timestamp, host, metricInfo.getTags()))
							.collect(Collectors.toList()));

					return metrics.stream();
				})
				.collect(Collectors.toList());
	}

	Number getValue(Snapshot snapshot, Expansion expansion) {
		switch (expansion) {
			case MAX:
				return snapshot.getMax();
			case MEAN:
				return snapshot.getMean();
			case MIN:
				return snapshot.getMin();
			case STD_DEV:
				return snapshot.getStdDev();
			case MEDIAN:
				return snapshot.getMedian();
			case P75:
				return snapshot.get75thPercentile();
			case P95:
				return snapshot.get95thPercentile();
			case P98:
				return snapshot.get98thPercentile();
			case P99:
				return snapshot.get99thPercentile();
			case P999:
				return snapshot.get999thPercentile();
			default:
				throw new IllegalArgumentException("Unsuppported snapshot expansion " + expansion);
		}
	}

	/*
	double getValue(Metered meter, Expansion expansion) {
		switch (expansion) {
			case RATE_1_MINUTE:
				return meter.getOneMinuteRate();
			case RATE_5_MINUTE:
				return meter.getFiveMinuteRate();
			case RATE_15_MINUTE:
				return meter.getFifteenMinuteRate();
			case RATE_MEAN:
				return meter.getMeanRate();
			default:
				throw new IllegalArgumentException("Unsuppported meter expansion " + expansion);
		}
	}
	*/

	Number toNumber(Object o) {
		if (o instanceof Number) {
			return (Number) o;
		}

		if (o instanceof Boolean) {
			return (Boolean) o ? 1 : 0;
		}

		return null;
	}

	String appendExpansion(String name, Expansion expansion) {
		return name + "." + expansion.toString();
	}

	MetricInfo getMetricInfo(String name) {
		Optional<MetricInfoResolver> found = resolvers.stream().filter(r -> r.canResolve(name)).findFirst();

		MetricInfoResolver metricInfoResolver = found.orElse(new MetricInfoResolver() {

			@Override
			public boolean canResolve(String name) {
				return true;
			}

			@Override
			public MetricInfo resolve(String name) {
				return new MetricInfo(name, newArrayList());
			}
		});

		MetricInfo metricInfo = metricInfoResolver.resolve(name);
		metricInfo.getTags().addAll(tags);

		return metricInfo;
	}

	public static class Builder {

		private RestTemplate restTemplate;
		private MetricRegistry registry;
		private String url;
		private String apiKey;
		private String host;
		private String env;
		private String group;
		private String application;
		private int connectTimeout;
		private int socketTimeout;
		private MetricFilter filter;
		private List<String> tags;
		private List<MetricInfoResolver> resolvers;

		public Builder(String url, String apiKey, MetricRegistry registry) {
			this.url = url;
			this.apiKey = apiKey;
			this.registry = registry;
			this.filter = MetricFilter.ALL;
			this.tags = newArrayList();
			this.connectTimeout = 2000;
			this.socketTimeout = 2000;
			this.resolvers = newArrayList();
		}

		public Builder withHost(String host) {
			this.host = host;
			return this;
		}

		public Builder withEnv(String env) {
			this.env = env;
			return this;
		}

		public Builder withGroup(String group) {
			this.group = group;
			return this;
		}

		public Builder withApplication(String application) {
			this.application = application;
			return this;
		}

		public Builder withTags(List<String> tags) {
			this.tags = tags;
			return this;
		}

		public Builder withFilter(MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		public Builder withHystrixResolvers() {
			this.resolvers.add(new HystrixCommandResolver());
			this.resolvers.add(new HystrixThreadPoolResolver());

			return this;
		}

		public Builder withJvmResolvers() {
			return this.withJvmClassesResolvers()
					.withJvmGarbageCollectorResolvers()
					.withJvmMemoryResolvers()
					.withJvmThreadResolvers();
		}

		public Builder withJvmClassesResolvers() {
			this.resolvers.add(new ClassesResolver());

			return this;
		}

		public Builder withJvmGarbageCollectorResolvers() {
			this.resolvers.add(new GarbageCollectorResolver());

			return this;
		}

		public Builder withJvmMemoryResolvers() {
			this.resolvers.add(new MemoryResolver());
			this.resolvers.add(new MemoryPoolResolver());

			return this;
		}

		public Builder withJvmThreadResolvers() {
			this.resolvers.add(new ThreadsResolver());
			this.resolvers.add(new ThreadCountResolver());

			return this;
		}

		public Builder withResolvers(List<MetricInfoResolver> resolvers) {
			this.resolvers.addAll(resolvers);

			return this;
		}

		public Builder withConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
			return this;
		}

		public Builder withSocketTimeout(int socketTimeout) {
			this.socketTimeout = socketTimeout;
			return this;
		}

		public Builder withRestTemplate(RestTemplate restTemplate) {
			this.restTemplate = restTemplate;
			return this;
		}

		public DatadogReporter build() {
			Set<String> tagSet = newHashSet();

			addTag("env", env, tagSet);
			addTag("group", group, tagSet);
			addTag("application", application, tagSet);

			if (tags != null) {
				tagSet.addAll(tags);
			}

			if (restTemplate == null) {
				restTemplate = new RestTemplate();
				restTemplate.setMessageConverters(getMessageConverters());
				restTemplate.setRequestFactory(getRequestFactory());
			}

			return new DatadogReporter(
					restTemplate,
					url,
					apiKey,
					registry,
					filter,
					host,
					newArrayList(tagSet),
					resolvers);
		}

		void addTag(String label, String value, Set<String> tagSet) {
			if (StringUtils.isNotBlank(value)) {
				tagSet.add(String.format("%s:%s", label, value));
			}
		}

		List<HttpMessageConverter<?>> getMessageConverters() {
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.setVisibilityChecker(objectMapper.getVisibilityChecker().with(Visibility.NONE));
			objectMapper.enable(SerializationFeature.WRITE_BIGDECIMAL_AS_PLAIN);

			MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
			jsonConverter.setObjectMapper(objectMapper);

			return newArrayList(jsonConverter, new StringHttpMessageConverter());
		}

		ClientHttpRequestFactory getRequestFactory() {
			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(connectTimeout)
					.setSocketTimeout(socketTimeout)
					.build();

			HttpClient httpClient = HttpClients.custom()
					.setMaxConnTotal(1)
					.setMaxConnPerRoute(1)
					.setRedirectStrategy(new LaxRedirectStrategy())
					.setDefaultRequestConfig(requestConfig)
					.build();

			return new HttpComponentsClientHttpRequestFactory(httpClient);
		}
	}
}
