package org.grails.resolver;

import org.springframework.util.StringUtils;

import static com.google.common.collect.Lists.newArrayList;

public abstract class PrefixedMetricInfoResolver implements MetricInfoResolver {

	private String tagLabel;
	private String prefix;
	private String newPrefix;
	private boolean suffixed;
	private int prefixComponents;

	public PrefixedMetricInfoResolver(String prefix, String tagLabel, boolean suffixed) {
		this(prefix, prefix, tagLabel, suffixed);
	}

	public PrefixedMetricInfoResolver(String prefix, String newPrefix, String tagLabel, boolean suffixed) {
		this.prefix = prefix;
		this.newPrefix = newPrefix;
		this.tagLabel = tagLabel;
		this.suffixed = suffixed;
		this.prefixComponents = StringUtils.countOccurrencesOf(this.prefix, ".");
	}

	@Override
	public boolean canResolve(String name) {
		return name != null
				&& name.startsWith(prefix + ".")
				&& (suffixed
					? StringUtils.countOccurrencesOf(name, ".") > prefixComponents + 1
					: StringUtils.countOccurrencesOf(name, ".") == prefixComponents + 1)
				&& name.charAt(name.length() - 1) != '.';
	}

	@Override
	public MetricInfo resolve(String name) {
		int metricIndex = name.indexOf('.', prefix.length() + 1);

		if (metricIndex == -1) {
			metricIndex = name.length();
		}

		String metricName = newPrefix + name.substring(metricIndex);
		String tagValue = name.substring(prefix.length() + 1, metricIndex);

		return new MetricInfo(metricName, newArrayList(String.format("%s:%s", tagLabel, tagValue)));
	}
}
