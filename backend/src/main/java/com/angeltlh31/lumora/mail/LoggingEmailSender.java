package com.angeltlh31.lumora.mail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingEmailSender implements EmailSender {
    @Override
    public void send(String to, String subject, String body) {
        log.info("""

                ===== EMAIL (not sent - MAIL_HOST is not configured) =====
                To: {}
                Subject: {}

                {}
                ==========================================================""", to, subject, body);
    }
}
