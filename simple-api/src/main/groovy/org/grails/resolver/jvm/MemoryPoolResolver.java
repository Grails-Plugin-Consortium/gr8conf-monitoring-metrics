package org.grails.resolver.jvm;


import org.grails.resolver.PrefixedMetricInfoResolver;

public class MemoryPoolResolver extends PrefixedMetricInfoResolver {

	public MemoryPoolResolver() {
		super("jvm.memory.pools", "jvm.memory", "memory", true);
	}
}
