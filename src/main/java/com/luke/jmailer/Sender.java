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

    Sender(String server, String emailAddr, String username, String password, int port, boolean tls) {
        this.server = server;
        this.emailAddr = emailAddr;
        this.username = username;
        this.password = password;
        this.port = port;
        TransportStrategy strategy = TransportStrategy.SMTP;
        if (tls) {
            strategy = TransportStrategy.SMTP_TLS;
        }

        // add option for tls in config
        this.mailer = MailerBuilder
            .withSMTPServer(this.server, this.port, this.username, this.password)
            .withTransportStrategy(strategy)
            .buildMailer();
    }

    // class that holds information for a sent mail
    class outMail {
        public String recipient;
        public String subject;
        public String body = "";
        // needed for rendering on gmail and such
        public String HTMLBody() {
            if (body == null) return "";
    
            StringBuilder sb = new StringBuilder();
            for (char c : body.toCharArray()) {
                switch (c) {
                case '<':  sb.append("&lt;");   break;
                case '>':  sb.append("&gt;");   break;
                case '&':  sb.append("&amp;");  break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;");  break;
                case '\n': sb.append("<br>");   break;
                default:   sb.append(c);
                }
            }
            return "<div dir=\"ltr\">" + sb.toString() + "</div>";
        }
    }

    // read from stdin
    public outMail composeMail(Scanner scan, String subject, String recipient) {
        outMail ret = new outMail();
        ret.subject = subject;
        // throws out first thing which is usually a new line
        ret.recipient = recipient;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.equals(".")) {
                break;
            }
            ret.body += line + "\n";
        }
        return ret;
    }

    // shorter code in app for reply command
    public outMail composeMail(Scanner scan) {
        scan.nextLine();
        return composeMail(scan, null, null);
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

    // for replying
    public void sendMail(outMail out, Email inRepTo) {
        Email email = EmailBuilder.replyingTo(inRepTo)
            .from(username, emailAddr)
            .prependText(out.body)
            .prependTextHTML(out.HTMLBody())
            .buildEmail();
        mailer.sendMail(email);
    }


    // UNUSED CODE
    // if no username is specified (check is done in function call)
    //Sender(String server, String emailAddr, String password, int port) {
    //    this(server, emailAddr, emailAddr, password, port);
    //}
}
