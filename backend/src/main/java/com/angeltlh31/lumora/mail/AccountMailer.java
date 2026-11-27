package com.angeltlh31.lumora.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountMailer {
    private final EmailSender emailSender;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    public void sendVerification(String to, String rawToken) {
        send(to, "Verify your Lumora email", """
                Welcome to Lumora!

                Confirm your email address by opening this link (valid for 24 hours):
                %s

                If you did not create a Lumora account, you can ignore this email.
                """.formatted(verifyLink(rawToken)));
    }

    public void sendAlreadyRegistered(String to) {
        send(to, "Someone tried to sign up with your email", """
                Someone tried to create a new Lumora account with this email address,
                but an account already exists for it.

                If this was you, just log in at %s/login.
                If it was not you, no action is needed - nothing has changed on your account.
                """.formatted(frontendUrl));
    }

    public void sendChangeEmailConfirmation(String to, String rawToken) {
        send(to, "Confirm your new Lumora email", """
                You asked to change your Lumora email address to this one.

                Confirm the change by opening this link (valid for 24 hours):
                %s

                If you did not request this, ignore this email - your account email will stay the same.
                """.formatted(verifyLink(rawToken)));
    }

    public void sendEmailChangeRequestedNotice(String to, String newEmail) {
        send(to, "Your Lumora email is about to change", """
                Someone (hopefully you) asked to change the email of your Lumora account to %s.

                The change only happens after the new address is confirmed.
                If this was not you, change your password right away.
                """.formatted(newEmail));
    }

    private String verifyLink(String rawToken) {
        return frontendUrl + "/verify-email?token=" + URLEncoder.encode(rawToken, StandardCharsets.UTF_8);
    }

    private void send(String to, String subject, String body) {
        try {
            emailSender.send(to, subject, body);
        } catch (RuntimeException ex) {
            log.error("Failed to send email '{}' to {}", subject, to, ex);
        }
    }
}
