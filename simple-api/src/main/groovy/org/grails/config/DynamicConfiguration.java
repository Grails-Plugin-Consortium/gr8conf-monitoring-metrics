package org.grails.config;

import com.netflix.config.Property;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

public abstract class DynamicConfiguration implements PropertyChangeCallback {

    private final transient Logger logger = LoggerFactory.getLogger(DynamicConfiguration.class);

    private volatile long changeTimestamp;

    public long getChangeTimestamp() {
        return changeTimestamp;
    }

    @Override
    public void propertyChange(Property property) {
        logger.debug("Property change for {}", property.getName());
        changeTimestamp = property.getChangedTimestamp();
    }

    @PostConstruct
    void log() {
        logger.info(this.toString());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
