package com.hhymyi.study.smzdm.zdmnotice.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SendMailUtil {

	@Autowired
	JavaMailSender jms;
	@Value("${mail.fromMail.addr}")
	private String from;

	public void send(String to,String title,String content){
		SimpleMailMessage message=new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(title);
		message.setText(content);

		jms.send(message);
	}
}
