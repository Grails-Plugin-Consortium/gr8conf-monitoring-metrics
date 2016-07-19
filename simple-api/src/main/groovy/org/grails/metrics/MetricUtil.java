package org.grails.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.ClassLoadingGaugeSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;

public class MetricUtil {

    public static void registerJvmMetrics(MetricRegistry metricRegistry) {
        registerJvmClassMetrics(metricRegistry);
        registerJvmGarbageCollectorMetrics(metricRegistry);
        registerJvmMemoryMetrics(metricRegistry);
        registerJvmThreadMetrics(metricRegistry);
    }

    public static void registerJvmMemoryMetrics(MetricRegistry metricRegistry) {
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
    }

    public static void registerJvmGarbageCollectorMetrics(MetricRegistry metricRegistry) {
        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
    }

    public static void registerJvmThreadMetrics(MetricRegistry metricRegistry) {
        metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());
    }

    public static void registerJvmClassMetrics(MetricRegistry metricRegistry) {
        metricRegistry.register("jvm.classes", new ClassLoadingGaugeSet());
    }
}
