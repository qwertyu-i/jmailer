# JMailer
Packaged as an uber jar when using:
```bash
mvn clean package
```

# Configuration
JMailer is configured through a YAML file located at ~/.jmailer.d/config.yaml. <br />
The following is an example of a configuration file:

```yaml
SMTP:
  server: "smtp.gmail.com"
  username: "this is optional"
  port: 587
  email: "exampleemail@gmail.com"
  password: "passwordpassword"
  tls: true
IMAP:
  server: "imap.gmail.com"
  username: "this is optional"
  port: 993
  email: "exampleemail@gmail.com"
  password: "passwordforimap"
  tls: true
```