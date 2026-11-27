package com.angeltlh31.lumora.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;

import java.time.ZoneId;
import java.util.TimeZone;

public class AppTimeZone implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {
    public static final String PROPERTY = "app.time-zone";
    public static final String DEFAULT_ZONE = "Asia/Ho_Chi_Minh";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String zone = event.getEnvironment().getProperty(PROPERTY, DEFAULT_ZONE);
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(zone)));
        System.setProperty("user.timezone", zone);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 15;
    }
}
