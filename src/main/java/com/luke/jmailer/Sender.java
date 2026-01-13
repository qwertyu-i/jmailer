package com.luke.jmailer;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

/* Acts as the class that sends emails, email bodies are composed from 
 * the main App class.
 */

// mailer wrapper that autofills information on emails
public class Sender {
    private final Mailer mailer;
    private final String server;
    private final String emailAddr;
    private final String username;
    private final String password;
    private final int port;

    Sender(String server, String emailAddr, String username, String password, int port) {
        this.server = server;
        this.emailAddr = emailAddr;
        this.username = username;
        this.password = password;
        this.port = port;

        // add option for tls in config
        this.mailer = MailerBuilder
            .withSMTPServer(this.server, this.port, this.username, this.password)
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .buildMailer();
    }

    public void sendMail(String body, String subject, String recipient) {
        Email email = EmailBuilder.startingBlank()
            .from(username, emailAddr)
            .to(recipient)
            .withSubject(subject)
            .withPlainText(body)
            .buildEmail();
        mailer.sendMail(email);
    }

    // UNUSED CODE
    // if no username is specified (check is done in function call)
    //Sender(String server, String emailAddr, String password, int port) {
    //    this(server, emailAddr, emailAddr, password, port);
    //}
}
