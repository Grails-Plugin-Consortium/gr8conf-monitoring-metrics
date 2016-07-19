package org.grails.resolver.jvm;


import org.grails.resolver.PrefixedMetricInfoResolver;

public class MemoryResolver extends PrefixedMetricInfoResolver {

	public MemoryResolver() {
		super("jvm.memory", "memory", true);
	}

	@Override
	public boolean canResolve(String name) {
		return super.canResolve(name) && !name.startsWith("jvm.memory.pools");
	}
}
