package com.mv.data.panda.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * author: houying
 * date  : 17-3-13
 * desc  : 通用email静态工具类
 */
public class Mail {
    private static final Logger logger = LoggerFactory.getLogger(Mail.class);
    private static final String BODY_HTML = "<!DOCTYPE html PUBLIC -//W3C//DTD HTML 4.01 Transitional//ENhttp://www.w3.org/TR/html4/loose.dtd><html><head><meta http-equiv=Content-Type content=text/html; charset=utf-8 pageEncoding=UTF-8></head><body>%s</body></html>";
    private static Properties prop;

    public static void send(String address, String title, String content) {
        try {
            String[] addr = address.split(",");
            InternetAddress[] addresses = new InternetAddress[addr.length];
            for (int i = 0; i < addr.length; i++) {
                addresses[i] = new InternetAddress(addr[i]);
            }
            SMTPAuthenticator authenticator = new SMTPAuthenticator("monitor.dataPlatform@mobvista.com", "dataPlatform1");
            setProperties();
            Session sendMailSession = Session.getDefaultInstance(prop, authenticator);
            Message newMessage = new MimeMessage(sendMailSession);
            newMessage.setFrom(new InternetAddress("monitor.dataPlatform@mobvista.com"));
            newMessage.setRecipients(Message.RecipientType.TO, addresses);
            newMessage.setSubject(title);
            String html = String.format(BODY_HTML, content);
            newMessage.setContent(html, "text/html;charset=utf-8");
            Transport.send(newMessage);
            logger.info("send an email to address[{}] title[{}] content[{}]", addresses, title, content);
        } catch (MessagingException e) {
            logger.info("error occur when mail", e);
        }
    }

    private static void setProperties() {
        if (prop == null) {
            prop = new Properties();
            prop.put("mail.smtp.host","smtp.exmail.qq.com");
            prop.put("mail.smtp.auth","true");
            prop.put("mail.smtp.connectiontimeout","10000");
            prop.put("mail.smtp.timeout","20000");
        }
    }

    public static class SMTPAuthenticator extends Authenticator {
        private String username;
        private String password;

        public SMTPAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    }

    public static void main(String[] args) {
        Mail.send("ying.hou@mobvista.com", "test", "test");
    }
}
