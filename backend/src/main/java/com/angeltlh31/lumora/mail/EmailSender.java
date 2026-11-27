package com.angeltlh31.lumora.mail;

public interface EmailSender {
    void send(String to, String subject, String body);
}
