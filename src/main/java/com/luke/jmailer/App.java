package com.luke.jmailer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import org.simplejavamail.converter.EmailConverter;
import org.yaml.snakeyaml.Yaml;
// to do: add interactive line oriented mail composition and make reader

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class App {

    public static void main(String[] args) {
        // probably could have initialised these in a constructor instead of
        // dumping them in main
        String configPath = System.getProperty("user.home") + "/.jmailer.d/config.yaml";
        // stores the indices of the current 10 messages displayed
        // display messages as 0-9 but the actual index in folder is different
        // if we're short set index to -1 and check for if index == -1
        // selection of mail
        int[] messageIndex = new int[10];
        App app = new App();
        // uninitialised to load later
        Sender sender = app.loadSender(configPath);
        Scanner scan = new Scanner(System.in);
        int page = 0;
        // check if we have changed so we don't keep printing
        int lastPage = -1;
        boolean quit = false;
        String subject = "";
        String recipient = null;
        int lastMessage = -1;

        String[][] help = {
            {"?", "prints the help page"},
            {"q", "quit JMailer"},
            {"+p", "goes to next page"},
            {"-p", "goes to previous page"},
            {"Ng", "goes to page N"},
            {"N", "select message at index N"},
            {"r", "reply to last opened message"},
            {"s", "send message"}
        };

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-s")) {
                i++;
                subject = args[i];
            // regex for not starting with dash
            } else if (args[i].matches("^(?!-).*$")) {
                recipient = args[i];
            }
        }

        if (recipient != null) {
            try {
                sender.sendMail(sender.composeMail(scan, subject, recipient));
            } catch (Exception CouldNotSendMessage) {
                System.out.println("could not send message, check to address or config");
            }
            return;
        }

        // only load now since if user only used sending they won't reach this
        Reader reader = app.loadReader(configPath);

        while (!quit) {
            if (page != lastPage) {
                try {
                    System.out.println("page: " + page);
                    messageIndex = app.scrollMail(page, reader);
                } catch (MessagingException e) {
                    System.out.println("could not retrieve messages");
                } catch (Exception InvalidPage) {
                    System.out.println("invalid page number");
                }
            }
            lastPage = page;
            System.out.print("& ");
            String command = scan.next();
            if (command.equals("+p")) {
                page++;
            } else if (command.equals("-p")) {
                page--;
            } else if (command.equals("p")) {
                System.out.println("page: " + page);
                try {
                    messageIndex = app.scrollMail(page, reader);
                } catch (MessagingException e) {
                    System.out.println("page no longer can be loaded");
                }
            } else if (command.equals("q")) {
                quit = true;
                scan.close();
            } else if (command.matches("[0-9]")) {
                lastMessage = messageIndex[Integer.valueOf(command)];
                app.printMail(lastMessage, reader, app);
            } else if (command.equals("r")) {
                if (lastMessage != -1) {
                    try {
                        sender.sendMail(sender.composeMail(scan), EmailConverter.mimeMessageToEmail((MimeMessage) reader.deepFetch(lastMessage)));
                    } catch (MessagingException e) {
                        System.out.println("could not fetch message #" + command + " while replying");
                    }
                } else {
                    System.out.println("no message opened");
                }
            } else if (command.equals("s")) {
                try {
                    System.out.print("To: ");
                    String to = scan.next();
                    System.out.print("Subject: ");
                    scan.nextLine();
                    String sub = scan.nextLine();
                    sender.sendMail(sender.composeMail(scan, sub, to));
                } catch (Exception CouldNotSendMessage) {
                    System.out.println("could not send message, check to address or config");
                }
            } else if (command.matches("\\d+g")) {
                page = Integer.valueOf(command.substring(0, command.length() - 1));
            } else if (command.equals("?")) {
                for (String[] i : help) {
                    System.out.printf("%-5.5s%-75.75s", i[0], i[1]);
                    System.out.println();
                }
                System.out.println("NON-INTERACTIVE USE");
                System.out.println("the argument -r <email-address> sets recipient, -s \"subject line\" sets subject");
                System.out.println("body is read from stdin and terminated with a line containing only a period");
                System.out.println("e.g:");
                System.out.println("hey this is the only line of this email");
                System.out.println(".");
            } else {
                System.out.println("command not recognised");
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

            return new Sender(p.server, p.email, p.username, p.password, p.port, p.tls);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("no configuration file found at ~/.jmailer.d/config.yaml");
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
            throw new RuntimeException("no configuration file found at ~/.jmailer.d/config.yaml");
        }
    }

    public int[] scrollMail(int p, Reader reader) throws MessagingException {
        int[] messageIndices = new int[10];
        for (int i = 0; i < 10; i++) {
            PartMail email = reader.lightFetch(i + (p * 10));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
            String date = dateFormat.format(email.date);
            System.out.printf("%-2d%-20.18s%-46.46s%12.10s\n", i, email.from[0].getPersonal(), email.subject, date);
            messageIndices[i] = i + (p * 10) + 1;
        }
        return messageIndices;
    }

    // ai generated method, recursively searches through multi part emails
    // for plain text
    public String getPlainText(Part p) throws MessagingException, IOException {
        // 1. If the part itself is plain text, return it
        if (p.isMimeType("text/plain")) {
            return (String) p.getContent();
        }

        // 2. If it's a multipart, search the parts
        if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String s = getPlainText(bp);
                if (s != null) {
                    return s; // Return the first plain text part found
                }
            }
        }
    
        // 3. Handle nested messages
        if (p.isMimeType("message/rfc822")) {
            return getPlainText((Part) p.getContent());
        }

        return null;
    }

    public void printMail(int lastMessage, Reader reader, App app) {
        try {
            Message mail = reader.deepFetch(lastMessage);
            System.out.print("From: ");
            InternetAddress[] fromAddresses = (InternetAddress[]) mail.getFrom();
            for (int i = 0; i < fromAddresses.length; i++) {
                System.out.print(fromAddresses[i].getPersonal() + " <" + fromAddresses[i].getAddress() + ">, ");

            }
            System.out.println();
            System.out.println("Subject: " + mail.getSubject());
            System.out.print("To: ");
            InternetAddress[] toAddresses = (InternetAddress[]) mail.getAllRecipients();
            for (int i = 0; i < toAddresses.length; i++) {
                System.out.print(toAddresses[i].getPersonal() + " <" + toAddresses[i].getAddress() + ">, ");

            }
            System.out.println();
            try {
                System.out.println(app.getPlainText(mail));
            } catch (IOException e) {
                System.out.println("could not fetch message contents");
            }
        } catch (NumberFormatException e) {
            System.out.println("command matched regex [0-9] but was not int");
        } catch (MessagingException e) {
            System.out.println("could not fetch message #" + lastMessage);
        }
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
    public boolean tls = false;

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
