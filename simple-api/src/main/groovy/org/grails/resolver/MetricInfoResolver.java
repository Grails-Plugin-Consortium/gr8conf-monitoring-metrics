package org.grails.resolver;

public interface MetricInfoResolver {

	public boolean canResolve(String name);
	public MetricInfo resolve(String name);
}
