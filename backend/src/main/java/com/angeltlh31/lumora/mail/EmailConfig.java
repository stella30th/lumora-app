package com.angeltlh31.lumora.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailConfig {

    @Bean
    public EmailSender emailSender(@Value("${spring.mail.host:}") String host,
                                   @Value("${app.mail.from:no-reply@lumora.local}") String from,
                                   ObjectProvider<JavaMailSender> javaMailSender) {
        JavaMailSender sender = javaMailSender.getIfAvailable();
        if (host.isBlank() || sender == null) {
            return new LoggingEmailSender();
        }
        return new SmtpEmailSender(sender, from);
    }
}
