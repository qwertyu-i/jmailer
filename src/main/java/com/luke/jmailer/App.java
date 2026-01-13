package com.luke.jmailer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.yaml.snakeyaml.Yaml;
// to do: add interactive line oriented mail composition and make reader

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        String configPath = System.getProperty("user.home") + "/.jmailer.d/config.yaml";
        System.out.println(configPath);

        // Sender sender = loadSender(configPath);
        // sender.sendMail("testing", "Testing from JMail V0.0.2", "john.s12312355@gmail.com");

        Reader reader = loadReader(configPath);

    }

    // sets up sender with configuration properties
    // i probably could have put this just in the sender class
    public static Sender loadSender(String configPath) {
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

    public static Reader loadReader(String configPath) {
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
