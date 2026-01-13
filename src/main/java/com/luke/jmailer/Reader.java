package com.luke.jmailer;

import java.util.Properties;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;

public class Reader {
    private final String server;
    private final String emailAddr;
    private final String username;
    private final String password;
    private final int port;
    Properties serverProps;
    Session session;
    Store store;
    Folder folder;
    Message[] inbox;

    Reader(String server, String emailAddr, String username, String password, int port) {
        this.server = server;
        this.emailAddr = emailAddr;
        this.username = username;
        this.password = password;
        this.port = port;

        serverProps = new Properties();
        serverProps.put("mail.imaps.host", server);
        serverProps.put("mail.imaps.ssl.trust", server);
        serverProps.put("mail.imaps.port", port);
        serverProps.put("mail.imaps.starttls", true);
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
            folder.open(Folder.READ_ONLY);
        } catch (MessagingException e) {
            System.out.println("could not open default folder");
            e.printStackTrace();
        }

        try {
            inbox = folder.getMessages();
            System.out.println(inbox[inbox.length - 1].getSubject());
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
}
