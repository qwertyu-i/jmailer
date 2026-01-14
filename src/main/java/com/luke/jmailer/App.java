package com.luke.jmailer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import org.yaml.snakeyaml.Yaml;
// to do: add interactive line oriented mail composition and make reader

import jakarta.mail.MessagingException;

public class App {

    public static void main(String[] args) {
        String configPath = System.getProperty("user.home") + "/.jmailer.d/config.yaml";
        // stores the indices of the current 10 messages displayed
        // display messages as 0-9 but the actual index in folder is different
        // if we're short set index to -1 and check for if index == -1
        // selection of mail
        int[] messageIndex = new int[10];
        App app = new App();
        Reader reader = app.loadReader(configPath);
        // uninitialised to load later
        Sender sender;
        Scanner scan = new Scanner(System.in);
        int page = 0;
        boolean quit = false;
        String subject = "";
        String recipient = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-s")) {
                i++;
                subject = args[i];
            // regex for not starting with dash
            } else if (args[i].matches("^(?!-).*$")) {
                recipient = args[i];
                System.out.println(recipient);
            }
        }

        if (recipient != null) {
            sender = app.loadSender(configPath);
            sender.sendMail(sender.composeMail(scan, subject, recipient));
            return;
        }

        while (!quit) {
            try {
                messageIndex = app.scrollMail(page, reader);
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.print("& ");
            String command = scan.next();
            if (command.equals("+p")) {
                page++;
            } else if (command.equals("-p")) {
                page--;
            } else if (command.equals("q")) {
                quit = true;
                scan.close();
            }
        }
    }

    // sets up sender with configuration properties
    // i probably could have put this just in the sender class
    public Sender loadSender(String configPath) {
        Yaml yaml = new Yaml();
        try {
            InputStream inputStream = new FileInputStream(configPath);
            Config config = yaml.loadAs(
                inputStream,
                Config.class
            );
            // by default, if no username specified, set it to email
            config.SMTP.checkUsername();

            // p stands for properties to keep things shorter
            SMTPConfig p = config.SMTP;

            return new Sender(p.server, p.email, p.username, p.password, p.port);
        } catch (FileNotFoundException e) {
            // add config wizard
            System.out.println("config not found");
            return new Sender("a", "a", "a", "a", 2);
        }
    }

    public Reader loadReader(String configPath) {
        Yaml yaml = new Yaml();
        try {
            InputStream inputStream = new FileInputStream(configPath);
            Config config = yaml.loadAs(
                inputStream,
                Config.class
            );
            // by default, if no username specified, set it to email
            config.IMAP.checkUsername();

            // p stands for properties to keep things shorter
            IMAPConfig p = config.IMAP;

            return new Reader(p.server, p.email, p.username, p.password, p.port);
        } catch (FileNotFoundException e) {
            // add config wizard
            System.out.println("config not found");
            return new Reader("a", "a", "a", "a", 2);
        }
    }

    public int[] scrollMail(int p, Reader reader) throws MessagingException {
        int[] messageIndices = new int[10];
        for (int i = 0; i < 10; i++) {
            PartMail email = reader.lightFetch(i + (p * 10));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
            String date = dateFormat.format(email.date);
            System.out.printf("%-2d%-20.18s%-46.46s%12.10s", i, email.from[0].getPersonal(), email.subject, date);
            System.out.println();
            messageIndices[i] = i + (p * 10);
        }
        return messageIndices;
    }

    // make this later
    //public static void configurationWizard
}

// map the config to these classes
// config will be formatted as
// config
// +- SMTP details
// \- IMAP details
// see: App.loadSender as well as the IMAPConfig and Config classes
class SMTPConfig {
    public String server;
    public int port;
    public String email;
    public String username;
    public String password;

    public void checkUsername() {
        if (this.username == null) {
            this.username = this.email;
        }
    }
}

class IMAPConfig {
    public String server;
    public int port;
    public String email;
    public String username;
    public String password;
    public void checkUsername() {
        if (this.username == null) {
            this.username = this.email;
        }
    }
}

class Config {
    public SMTPConfig SMTP;
    public IMAPConfig IMAP;
}
