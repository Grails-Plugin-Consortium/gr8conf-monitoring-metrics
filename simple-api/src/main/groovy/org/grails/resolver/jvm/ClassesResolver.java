package org.grails.resolver.jvm;


import org.grails.resolver.PrefixedMetricInfoResolver;

public class ClassesResolver extends PrefixedMetricInfoResolver {

	public ClassesResolver() {
		super("jvm.classes", "classes", false);
	}
}
