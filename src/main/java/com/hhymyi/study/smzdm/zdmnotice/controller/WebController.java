package com.hhymyi.study.smzdm.zdmnotice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hhymyi.study.smzdm.zdmnotice.entity.MailNotice;
import com.hhymyi.study.smzdm.zdmnotice.repository.MailNoticeRepository;
import com.hhymyi.study.smzdm.zdmnotice.util.SendMailUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class WebController {
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	@Autowired
	SendMailUtil sendMailUtil;
	@Autowired
	MailNoticeRepository mailNoticeRepository;

	@RequestMapping("/getRedis")
	public String getRedis(String key) {
		String value = redisTemplate.opsForValue().get(key);
		return value;
	}

	@RequestMapping("/setRedis")
	public String setRedis(String key, String value) {
		redisTemplate.opsForValue().set(key, value);
		return value;
	}

	@RequestMapping("/redisLeftPush")
	public Long redisLeftPush(String list, String value) {
		Long aLong = redisTemplate.opsForList().leftPush(list, value);
		return aLong;
	}

	@RequestMapping("/redisRightPop")
	public String redisRightPop(String list) {
		String s = redisTemplate.opsForList().rightPop(list);
		return s;
	}

	@ApiOperation(value = "发送redis消息", notes = "测试用")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "topic", value = "通道名称", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "content", value = "消息内容", required = true, dataType = "String", paramType = "query")
	})
	@RequestMapping(value = "/sendMessage", method = RequestMethod.POST)
	public String sendMessage(String topic, String content) {
		redisTemplate.convertAndSend(topic, content);
		return content;
	}

	@ApiOperation(value = "发送redis消息 模拟zdm", notes = "测试用")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "topic", value = "通道名称", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "articleId", value = "文章ID", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "title", value = "文章标题", required = true, dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "url", value = "文章URL", required = true, dataType = "String", paramType = "query")
	})
	@RequestMapping(value = "/sendZDMMessage", method = RequestMethod.POST)
	public String sendZDMMessage(String topic, Long articleId, String title, String url) throws JsonProcessingException {
		Map<String, Object> map = new HashMap<>();
		map.put("articleId", articleId);
		map.put("title", title);
		map.put("url", url);

		redisTemplate.convertAndSend(topic, map);
		return "";
	}

	@RequestMapping("/sendMail")
	public String sendMail(String to, String title, String content) {
		//sendMailUtil.send(to, title, content);
		MailNotice mailNotice = new MailNotice();
		mailNotice.setToAddr(to);
		mailNotice.setTitle(title);
		mailNotice.setContent(content);
		mailNotice.setUrl("localhost");
		mailNotice.setCreateDate(new Date());
		mailNoticeRepository.save(mailNotice);
		return content;
	}

	@RequestMapping("/showNoticeHistory")
	public ModelAndView showNoticeHistory() {
		Map<String, Object> map = new HashMap<>();
		map.put("host", "通知历史");
		return new ModelAndView("notice_history", map);
	}

	@ApiOperation(value = "通知历史", notes = "查询通知历史")
	@RequestMapping(value = "/noticeHistoryContent", method = RequestMethod.GET)
	public Object noticeHistoryContent() {
		List<MailNotice> all = mailNoticeRepository.findAll();
		return all;
	}

	@ApiOperation(value = "通知历史 分页查询", notes = "查询通知历史")
	@RequestMapping(value = "/noticeHistoryPage", method = RequestMethod.GET)
	public Object noticeHistoryPage(int pageNum, int size, String startDate, String endDate, String searchContent) {
		Page<MailNotice> page = mailNoticeRepository.findAll((Specification<MailNotice>) (root, criteriaQuery, cb) -> {
			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			List<Predicate> predicates = new ArrayList<>();
			if (!StringUtils.isEmpty(startDate)) {
				//大于或等于传入时间
				LocalDate start = LocalDate.parse(startDate, dtf);
				predicates.add(cb.greaterThanOrEqualTo(root.get("createDate").as(LocalDate.class), start));
			}
			if (!StringUtils.isEmpty(endDate)) {
				//小于或等于传入时间
				LocalDate end = LocalDate.parse(endDate, dtf);
				predicates.add(cb.lessThanOrEqualTo(root.get("createDate").as(LocalDate.class), end));
			}
			if (!StringUtils.isEmpty(searchContent)) {
				//模糊查找
				predicates.add(cb.like(root.get("title").as(String.class), "%" + searchContent + "%"));
			}
			// and到一起的话所有条件就是且关系，or就是或关系
			return cb.and(predicates.toArray(new Predicate[predicates.size()]));
		}, PageRequest.of(pageNum, size));
		return page;
	}

	@RequestMapping("/getMailNoticeByArticleId")
	public MailNotice getMailNoticeByArticleId(Long articleId) {
		Optional<MailNotice> byId = mailNoticeRepository.findByArticleId(articleId);
		return byId.orElse(null);
	}
}
