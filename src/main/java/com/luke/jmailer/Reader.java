package com.luke.jmailer;

import java.util.Properties;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;

/* The class that fetches emails and sets up the IMAP client
 * @Author Luke
 * @Date Jan 5 2026
 * @Version 1.0.0
 */

public class Reader {
    Properties serverProps;
    Session session;
    Store store;
    Folder folder;
    public Message[] inbox;

    Reader(String server, String emailAddr, String username, String password, int port, boolean tls) {
        serverProps = new Properties();
        serverProps.put("mail.imaps.host", server);
        serverProps.put("mail.imaps.ssl.trust", server);
        serverProps.put("mail.imaps.port", port);
        serverProps.put("mail.imaps.starttls", tls);
        serverProps.put("mail.imaps.connectiontimeout", 10000);
        serverProps.put("mail.maps.timeout", 10000);

        session = Session.getInstance(serverProps);
        try {
            store = session.getStore("imaps");
            try {
                store.connect(server, username, password);
            } catch (MessagingException e) {
                System.out.println("could not connect to server, check config.");
                e.printStackTrace();
            }
        } catch (NoSuchProviderException e) {
            System.out.println("could not load imap session, check config?");
            e.printStackTrace();
        }

        try {
            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);
        } catch (MessagingException e) {
            System.out.println("could not open default folder");
            e.printStackTrace();
        }

        try {
            inbox = folder.getMessages();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }

    /* Gets the subject, date, and from address of the nth (starting from 0)
     * most recent email
     * @PRE: Integer n is provided as arg
     * @POST: Returns a PartMail object containing the subject, date, and from
     */
    public PartMail lightFetch(int n) throws MessagingException {
        PartMail emailInfo = new PartMail();

        emailInfo.date = inbox[inbox.length - n - 1].getReceivedDate();
        emailInfo.subject = inbox[inbox.length - n - 1].getSubject();
        emailInfo.from = (InternetAddress[]) inbox[inbox.length - n - 1].getFrom();
        return emailInfo;
    }

    /* Gets the entire message of the nth (starting from 1) most recent email
     * @PRE: Integer n is provided as arg
     * @POST: returns entire message of the nth email
     */
    public Message deepFetch(int i) throws MessagingException {
        return inbox[inbox.length - i];
    }
}
