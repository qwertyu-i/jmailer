package com.luke.jmailer;

import java.util.Scanner;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        System.out.println("JMAILER TEST - COCK.LI");
        System.out.println("email: ");
        String username = scan.nextLine();
        System.out.println("password: ");
        String password = scan.nextLine();
        scan.close();
        
        Email email = EmailBuilder.startingBlank()
            .from("Testing", username)
            .to("qwertyu", "")
            .withSubject("Testing from JMail v0.0.1")
            .withPlainText("testing")
            .buildEmail();

        MailerBuilder
            .withSMTPServer("mail.cock.li", 587, username, password)
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .withDebugLogging(true)
            .buildMailer()
            .sendMail(email);
    }
}
