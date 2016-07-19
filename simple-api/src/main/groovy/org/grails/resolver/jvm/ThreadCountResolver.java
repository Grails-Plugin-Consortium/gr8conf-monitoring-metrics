package org.grails.resolver.jvm;


import org.grails.resolver.MetricInfo;
import org.grails.resolver.MetricInfoResolver;

import static com.google.common.collect.Lists.newArrayList;

public class ThreadCountResolver implements MetricInfoResolver {

	@Override
	public boolean canResolve(String name) {
		return "jvm.threads.count".equals(name);
	}

	@Override
	public MetricInfo resolve(String name) {
		return new MetricInfo(name, newArrayList("threads:total"));
	}
}
