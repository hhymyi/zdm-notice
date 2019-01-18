package com.hhymyi.study.smzdm.zdmnotice.configuration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hhymyi.study.smzdm.zdmnotice.entity.MailNotice;
import com.hhymyi.study.smzdm.zdmnotice.repository.MailNoticeRepository;
import com.hhymyi.study.smzdm.zdmnotice.util.SendMailUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;

@Component
public class MessageReceiver {
	Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

	@Autowired
	private MailNoticeRepository mailNoticeRepository;
	@Value("${mail.toMail.addr}")
	private String toAddr;
	@Autowired
	private SendMailUtil sendMailUtil;

	/**
	 * 接收消息的方法
	 */
	@Transactional
	public void receiveMessage(String message) throws IOException {
		logger.info("收到一条消息：" + message);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		MailNotice mailNotice = mapper.readValue(message, MailNotice.class);
		if (mailNotice != null) {
			mailNotice.setCreateDate(new Date());
			mailNoticeRepository.save(mailNotice);
		}
		sendMailUtil.send(toAddr, mailNotice.getTitle(), mailNotice.getUrl());
	}
}
