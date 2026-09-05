package com.angeltlh31.lumora.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;

@Slf4j
@Component
public class SwaggerAutoLauncher implements ApplicationListener<ApplicationReadyEvent> {

    private static final String ALREADY_OPENED_FLAG = "lumora.swagger.browser.opened";

    @Value("${server.port:8080}")
    private String port;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        if (System.getProperty(ALREADY_OPENED_FLAG) != null) {
            return;
        }

        if (GraphicsEnvironment.isHeadless() || !Desktop.isDesktopSupported()) {
            log.debug("Moi truong khong co GUI (headless) - bo qua tu dong mo Swagger.");
            return;
        }

        String url = "http://localhost:" + port + "/swagger-ui/index.html";
        try {
            System.setProperty(ALREADY_OPENED_FLAG, "true");
            Desktop.getDesktop().browse(new URI(url));
            log.info("Da tu dong mo Swagger UI tai {}", url);
        } catch (Exception ex) {

            log.warn("Khong the tu dong mo trinh duyet toi {}: {}", url, ex.getMessage());
        }
    }
}
