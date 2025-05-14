package com.zywl.app.defaultx.service.card;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Mail;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.card.GreetingCard;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GreetingCardService extends DaoService{

	private static final Log logger = LogFactory.getLog(GreetingCardService.class);

	public GreetingCardService() {
		super("GreetingCardMapper");
	}

	@Autowired
	private AppConfigCacheService appConfigCacheService;

	@Autowired
	private UserCacheService userCacheService;
	


	
	
	@Transactional
	public Long sendGreeting(Long fromUserId,Long toUserId,String context,int number,int cardType) {
		Integer time = Integer.parseInt(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_MAIL_VALIDITY, Config.MAIL_VALIDITY));
		GreetingCard greetingCard = new GreetingCard();
		User fromUser = userCacheService.getUserInfoById(fromUserId);
		User toUser = userCacheService.getUserInfoById(toUserId);
		greetingCard.setFromUserId(fromUserId);
		greetingCard.setToUserId(toUserId);
		greetingCard.setFromUserRoleId(fromUser.getRoleId());
		greetingCard.setToUserRoleId(toUser.getRoleId());
		greetingCard.setFromUserNo(fromUser.getUserNo());
		greetingCard.setFromUserName(fromUser.getName());
		greetingCard.setFromUserHeadImg(fromUser.getHeadImageUrl());
		greetingCard.setToUserNo(toUser.getUserNo());
		greetingCard.setToUserName(toUser.getName());
		greetingCard.setToUserHeadImg(toUser.getHeadImageUrl());
		greetingCard.setType(cardType);
		greetingCard.setSendTime(new Date());
		greetingCard.setExpirationTime(DateUtil.getTimeByDay(time));
		greetingCard.setContext(context);
		greetingCard.setNumber(number);
		greetingCard.setStatus(1);
		int a= save(greetingCard);
		if (a<1) {
			throwExp("贺卡发送失败");
		}

		return greetingCard.getId();
	}
	
	/**
	 * 获取仍在有效期的邮件
	 */
	public List<GreetingCard> findVaildityMail() {
		return findList("findVaildityMail", null);
	}
	
	@Transactional
	public int mailExpired() {
		Map<String, Object> params = new HashedMap<>();
		params.put("now", new Date());
		return execute("updateMailToExpiration", params);
	}

	public Mail getMailByMailId(Long id) {
		Map<String, Object> params = new HashedMap<>();
		params.put("id", id);
		return findOne(params);
	}


	public List<GreetingCard> findMyEmail(Long toUserId,int type) {
		Map<String, Object> params = new HashedMap<>();
		params.put("toUserId", toUserId);
		if (type==1){
			return findList("findMyEmailSendToMe", params);
		}else{
			return findList("findMyEmailSendToOther", params);
		}

	}

	public List<GreetingCard> findToMyEmail(Long toUserId) {
		Map<String, Object> params = new HashedMap<>();
		params.put("toUserId", toUserId);
		return findList("findToMyEmail", params);
	}

	public List<GreetingCard> findMySendEmail(Long fromUserId) {
		Map<String, Object> params = new HashedMap<>();
		params.put("fromUserId", fromUserId);
		return findList("findMySendEmail", params);
	}

	public GreetingCard findMailById(Long id){
		Map<String, Object> params = new HashedMap<>();
		params.put("id", id);
		return findOne(params);
	}


	@Transactional
	public int batchUpdateIsRead(Long userId,List<GreetingCard> mails){
		List<GreetingCard> newMail = new ArrayList<>();
		for (GreetingCard mail : mails) {
			if (mail.getType()==1){
				newMail.add(mail);
			}
		}
		if (mails.size()==0 || newMail.size()==0){
			return 1;
		}
		return execute("batchUpdateIsRead",newMail);
	}

	public List<GreetingCard> findByUserId(Long userId,Long myId,int page,int num,int type) {
		Map<String, Object> params = new HashedMap<>();
		params.put("userId", userId);
		params.put("myId",myId);
		params.put("start",page*num);
		params.put("num",num);
		if (type==1){
			return findList("findByUserIdSendToMe", params);
		}else{
			return findList("findByUserIdSendToOther", params);
		}

	}


}
