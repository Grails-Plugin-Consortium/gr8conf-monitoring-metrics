package org.grails.health;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomNumberHealthIndicator extends AbstractHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        Integer randomNumber = new Random().nextInt(10);
        if (randomNumber % 2 == 0) {
            builder.up();
        } else {
            builder.down().withDetail("ODD", randomNumber);
        }
    }
}
