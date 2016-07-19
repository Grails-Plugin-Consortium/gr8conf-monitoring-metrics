package org.grails.resolver.hystrix;

import org.grails.resolver.PrefixedMetricInfoResolver;

public class HystrixCommandResolver extends PrefixedMetricInfoResolver {

	public HystrixCommandResolver() {
		super("hystrix.command", "command", true);
	}
}
