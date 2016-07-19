package org.grails.reporter.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class ApplicationGauge extends ApplicationMetric<Number> {

	public ApplicationGauge(String metric, Number count, Long epoch, String host, List<String> tags) {
		super(metric, "gauge", count, epoch, host, tags);
	}

}
