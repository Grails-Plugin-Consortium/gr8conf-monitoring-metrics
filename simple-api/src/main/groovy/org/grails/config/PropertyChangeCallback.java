package org.grails.config;

import com.netflix.config.Property;

public interface PropertyChangeCallback<T> {

    public void propertyChange(Property<T> property);
}
