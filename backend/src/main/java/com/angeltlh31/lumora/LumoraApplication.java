package com.angeltlh31.lumora;

import com.angeltlh31.lumora.config.AppTimeZone;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LumoraApplication {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(LumoraApplication.class);
        app.addListeners(new AppTimeZone());
        app.run(args);
    }
}
