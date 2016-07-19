package org.grails.reporter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.math.BigDecimal;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public abstract class DatadogMetric<T extends Number> {

	@JsonProperty
	private String metric;

	@JsonProperty
	private String type;

	@JsonProperty
	private BigDecimal count;

	@JsonProperty
	private Long epoch;

	@JsonProperty
	private String host;

	@JsonProperty
	private List<String> tags;

	@JsonProperty
	private List<List<Number>> points;

	public DatadogMetric(String metric, String type, T count, Long epoch, String host, List<String> tags) {
		this.metric = metric;
		this.type = type;
		this.tags = newArrayList(tags);
		this.count = new BigDecimal(count.toString());
		this.epoch = epoch;
		this.host = host;

		this.points = newArrayList();
		points.add(newArrayList(epoch, new BigDecimal(count.toString())));
	}

	public String getMetric() {
		return metric;
	}

	public String getType() {
		return type;
	}

	public BigDecimal getCount() {
		return count;
	}

	public Long getEpoch() {
		return epoch;
	}

	public String getHost() {
		return host;
	}

	public List<String> getTags() {
		return tags;
	}

	public List<List<Number>> getPoints() {
		return points;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
