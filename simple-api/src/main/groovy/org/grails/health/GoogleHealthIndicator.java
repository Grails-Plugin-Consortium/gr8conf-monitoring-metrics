package org.grails.health;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class GoogleHealthIndicator extends AbstractHealthIndicator {

    private final Logger logger = LoggerFactory.getLogger(GoogleHealthIndicator.class);

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        boolean isUp = addHealthMetric(builder, "http://www.google.com");
        isUp &= addHealthMetric(builder, "https://www.google.com/maps");
        isUp &= addHealthMetric(builder, "https://www.google.com/doesnotexist");

        if (isUp) {
            builder.up();
        } else {
            builder.down();
        }
    }

    private boolean addHealthMetric(Health.Builder builder, String url) {
        String status = "DOWN";
        CloseableHttpClient httpClient = HttpClients.createMinimal();
        try {
            if (httpClient.execute(new HttpGet(url)).getStatusLine().getStatusCode() == 200) {
                status = "UP";
            }
        } catch (IOException e) {
            logger.error("Health check exception calling fop wsdld", e);
            status = e.getMessage();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                logger.error("Could not close connection", e);
            }
        }
        builder.withDetail(url, status);
        return Objects.equals(status, "UP");
    }
}
