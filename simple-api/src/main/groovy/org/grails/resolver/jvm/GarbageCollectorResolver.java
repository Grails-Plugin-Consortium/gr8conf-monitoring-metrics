package org.grails.resolver.jvm;


import org.grails.resolver.PrefixedMetricInfoResolver;

public class GarbageCollectorResolver extends PrefixedMetricInfoResolver {

	public GarbageCollectorResolver() {
		super("jvm.gc", "gc", true);
	}
}
