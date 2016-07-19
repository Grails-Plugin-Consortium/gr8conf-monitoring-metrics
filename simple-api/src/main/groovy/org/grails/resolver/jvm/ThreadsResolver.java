package org.grails.resolver.jvm;

import org.grails.resolver.PrefixedMetricInfoResolver;

public class ThreadsResolver extends PrefixedMetricInfoResolver {

	public ThreadsResolver() {
		super("jvm.threads", "threads", true);
	}
}
