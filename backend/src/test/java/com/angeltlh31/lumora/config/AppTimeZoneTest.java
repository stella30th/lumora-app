package com.angeltlh31.lumora.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.mock.env.MockEnvironment;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTimeZoneTest {
    private final TimeZone original = TimeZone.getDefault();

    @AfterEach
    void restore() {
        TimeZone.setDefault(original);
    }

    private void fire(MockEnvironment env) {
        ConfigurableBootstrapContext bootstrap = new DefaultBootstrapContext();
        new AppTimeZone().onApplicationEvent(
                new ApplicationEnvironmentPreparedEvent(bootstrap, new SpringApplication(), new String[0], env));
    }

    @Test
    void defaultsToVietnamTime_evenOnUtcServer() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

        fire(new MockEnvironment());

        assertEquals("Asia/Ho_Chi_Minh", TimeZone.getDefault().getID());
    }

    @Test
    void usesConfiguredZone() {
        fire(new MockEnvironment().withProperty(AppTimeZone.PROPERTY, "Asia/Tokyo"));

        assertEquals("Asia/Tokyo", TimeZone.getDefault().getID());
    }
}
