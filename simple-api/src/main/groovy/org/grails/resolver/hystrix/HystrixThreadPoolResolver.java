package org.grails.resolver.hystrix;

import org.grails.resolver.PrefixedMetricInfoResolver;

public class HystrixThreadPoolResolver extends PrefixedMetricInfoResolver {

	public HystrixThreadPoolResolver() {
		super("hystrix.threadPool", "threadPool", true);
	}
}
