package org.grails.config;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import org.grails.resolver.MetricInfoResolver;
import org.grails.resolver.RestMetricsReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.collect.Lists.newArrayList;

@Configuration
public class MetricReporterConfig {

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    @Qualifier("applicationName")
    private String applicationName;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired(required = false)
    @Qualifier("groupName")
    private String groupName;

    @Autowired
    private MetricRegistry metricRegistry;

    @Autowired(required = false)
    private List<MetricInfoResolver> resolvers;

    @PostConstruct
    public void metricsReporter() throws Exception {

        if (resolvers == null) {
            resolvers = newArrayList();
        }

        if (groupName == null) {
            groupName = "grails";
        }

        RestMetricsReporter.Builder builder = new RestMetricsReporter.Builder(
                "https://app.datadoghq.com/api/v1/series?api_key=cc68f1c50761ec3dea32f160fbf0e7b0", //trial key 14 day
                metricRegistry)
                .withHost("ACETRIKEMAC") //make these properties
                .withEnv("dev") //make these properties
                .withGroup(groupName)
                .withApplication(applicationName)
                .withResolvers(resolvers)
                .withHystrixResolvers()
                .withJvmMemoryResolvers()
                .withJvmGarbageCollectorResolvers()
                .withJvmThreadResolvers()
                .withJvmClassesResolvers();

        builder.build().start(5, TimeUnit.SECONDS);

        //Can also do jmx reporting
        final JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).build();
        reporter.start();

    }
}
