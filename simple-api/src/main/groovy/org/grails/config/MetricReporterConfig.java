package org.grails.config;

import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang.StringUtils;
import org.grails.resolver.DatadogReporter;
import org.grails.resolver.MetricInfoResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;

@Configuration
public class MetricReporterConfig {

	@Autowired
	@Qualifier("applicationName")
	private String applicationName;

	@Autowired(required = false)
	@Qualifier("groupName")
	private String groupName;

	@Autowired
	private MetricRegistry metricRegistry;

	@Autowired(required = false)
	private List<MetricInfoResolver> resolvers;

	@PostConstruct
	public void metricsDataDog() throws Exception {

		if (resolvers == null) {
			resolvers = newArrayList();
		}

		if (groupName == null) {
			groupName = "grails";
		}

		DatadogReporter.Builder builder = new DatadogReporter.Builder(
				"http://localhost",
				"apikey",
				metricRegistry)
				.withHost("computername")
				.withEnv("dev")
				.withGroup(groupName)
				.withApplication(applicationName)
				.withResolvers(resolvers);

			builder.withHystrixResolvers();
			builder.withJvmMemoryResolvers();
			builder.withJvmGarbageCollectorResolvers();
			builder.withJvmThreadResolvers();
			builder.withJvmClassesResolvers();

		DatadogReporter reporter = builder.build();

		//reporter.start(15, TimeUnit.SECONDS);
	}
}
