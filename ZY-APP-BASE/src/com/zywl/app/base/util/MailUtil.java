package com.zywl.app.base.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class MailUtil {

	
	public static void sendEmail(String recipientEmail, String code) {
		String subject  = "YOU ARE REGISTERING";
		String content = "You are registering for the game and your CAPTCHA is :"+code+",It works in five minutes";
        String senderEmail = "nnjiema@outlook.com"; // 发件人邮箱地址
        String senderPassword = "aa15650986"; // 发件人邮箱密码或授权码
 
        // 设置邮件服务器属性，我这里是网易邮箱，你们用其他邮箱的可以去搜搜对应的属性
        Properties properties = new Properties();
        properties.put("mail.transport.protocol", "smtp");// 连接协议
        properties.put("mail.smtp.host", "smtp.office365.com"); // 邮件服务器主机地址，根据你的邮箱配置填写
        properties.put("mail.smtp.port", "587"); // 邮件服务器端口号，网易的SSL端口号,经过SSL加密
        properties.put("mail.smtp.auth", "true"); // 启用身份验证
        properties.put("mail.smtp.starttls.enable", "true"); // 启用TLS加密
      //  properties.put("mail.smtp.ssl.enable", "true");// 设置是否使用ssl安全连接 ---一般都使用
      //  properties.put("mail.debug", "true");// 设置是否显示debug信息 true 会在控制台显示相关信息
 
 
        // 创建会话
        Session session = Session.getInstance(properties, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });
 
        try {
            // 创建邮件对象
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setText(content);
 
            // 发送邮件
            Transport.send(message);
            System.out.println("邮件已发送成功！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
	
	public static void main(String[] args) {
		sendEmail("847502314@qq.com", "123456");
	}
	
}
