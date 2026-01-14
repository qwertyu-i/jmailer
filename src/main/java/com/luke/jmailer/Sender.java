package com.luke.jmailer;

import java.util.Scanner;

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

    // class that holds information for a sent mail
    class outMail {
        public String recipient;
        public String subject;
        public String body = "";
    }

    // read from stdin
    public outMail composeMail(Scanner scan, String subject, String recipient) {
        outMail ret = new outMail();
        ret.subject = subject;
        ret.recipient = recipient;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.equals(".")) {
                return ret;
            } else if (!line.equals(null)) {
                ret.body += line + "\n";
            }
        }
        return ret;
    }

    public void sendMail(outMail out) {
        Email email = EmailBuilder.startingBlank()
            .from(username, emailAddr)
            .to(out.recipient)
            .withSubject(out.subject)
            .withPlainText(out.body)
            .buildEmail();
        mailer.sendMail(email);
    }

    // UNUSED CODE
    // if no username is specified (check is done in function call)
    //Sender(String server, String emailAddr, String password, int port) {
    //    this(server, emailAddr, emailAddr, password, port);
    //}
}
