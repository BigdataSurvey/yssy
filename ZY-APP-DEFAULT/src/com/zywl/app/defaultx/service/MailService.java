package com.zywl.app.defaultx.service;

import java.util.*;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.RedReminderIndexEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Mail;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class MailService extends DaoService{
	
	private static final Log logger = LogFactory.getLog(MailService.class);
	
	public MailService() {
		super("MailMapper");
	}

	@Autowired
	private AppConfigCacheService appConfigCacheService;

	@Autowired
	private UserCacheService userCacheService;
	

	public static List<Mail> mails = new ArrayList<Mail>();
	
	
	
	
	/**
	 * 发送邮件
	 * @param title
	 * @param context
	 * @param group
	 * @param isAttachments
	 * @param attachmentsDetails
	 */
	@Transactional
	public Long sendMail(Long fromUserId,Long toUserId,String title,String context,String group,int isAttachments,JSONArray attachmentsDetails) {
		Integer time = Integer.parseInt(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_MAIL_VALIDITY, Config.MAIL_VALIDITY));
		Mail mail = new Mail();
		//User fromUser = userCacheService.getUserInfoById(fromUserId);
		User toUser = userCacheService.getUserInfoById(toUserId);
		mail.setFromUserId(fromUserId);
		mail.setToUserId(toUserId);
		mail.setFromUserRoleId(0);
		mail.setToUserRoleId(toUser.getRoleId());
		mail.setFromUserNo(null);
		mail.setFromUserName(null);
		mail.setFromUserHeadImg(null);
		mail.setToUserNo(toUser.getUserNo());
		mail.setToUserName(toUser.getName());
		mail.setToUserHeadImg(toUser.getHeadImageUrl());
		mail.setType(1);
		mail.setSendTime(new Date());
		mail.setExpirationTime(DateUtil.getTimeByDay(time));
		mail.setContext(context);
		mail.setTitle(title);
		mail.setIsAttachments(isAttachments);
		mail.setAttachmentsDetails(attachmentsDetails);
		mail.setStatus(1);
		mail.setIsRead(0);
		int a= save(mail);
		if (a<1) {
			throwExp("邮件发送失败");
		}

		return mail.getId();
	}
	
	/**
	 * 获取仍在有效期的邮件
	 */
	public List<Mail> findVaildityMail() {
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


	public List<Mail> findMyEmail(Long toUserId,int type,int page,int num) {
		Map<String, Object> params = new HashedMap<>();
		params.put("toUserId", toUserId);
		params.put("start",page*num);
		params.put("num",num);
		if (type==1){
			return findList("findMyEmailSendToMe", params);
		}else{
			return findList("findMyEmailSendToOther", params);
		}

	}

	public List<Mail> findToMyEmail(Long toUserId) {
		Map<String, Object> params = new HashedMap<>();
		params.put("toUserId", toUserId);
		return findList("findToMyEmail", params);
	}

	public List<Mail> findToMyEmailNoRead(Long toUserId) {
		Map<String, Object> params = new HashedMap<>();
		params.put("toUserId", toUserId);
		return findList("findToMyEmailNoRead", params);
	}

	public List<Mail> findMySendEmail(Long fromUserId) {
		Map<String, Object> params = new HashedMap<>();
		params.put("fromUserId", fromUserId);
		return findList("findMySendEmail", params);
	}

	public Mail findMailById(Long id){
		Map<String, Object> params = new HashedMap<>();
		params.put("id", id);
		return findOne(params);
	}


	@Transactional
	public int batchUpdateIsRead(Long userId,List<Mail> mails){
		List<Mail> newMail = new ArrayList<>();
		for (Mail mail : mails) {
			if (mail.getType()==1){
				newMail.add(mail);
			}
		}
		if (mails.size()==0 || newMail.size()==0){
			return 1;
		}
		return execute("batchUpdateIsRead",newMail);
	}

	public List<Mail> findByUserId(Long userId,Long myId,int page,int num,int type) {
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

	public void pvpBatchInsertMail(JSONArray array,String title,String context){
		List<Mail> mails = new ArrayList<>();
		int time = Integer.parseInt(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_MAIL_VALIDITY, Config.MAIL_VALIDITY));
		for (Object o : array) {
			JSONObject jsonObject = (JSONObject)o;
			String toId = jsonObject.getString("userId");
			Mail mail = new Mail();
			User toUser = userCacheService.getUserInfoById(toId);
			if(toUser == null) {
				continue;
			}
			mail.setFromUserId(-1L);
			mail.setToUserId(toUser.getId());
			mail.setFromUserNo("");
			mail.setFromUserName("");
			mail.setFromUserHeadImg("");
			mail.setToUserNo(toUser.getUserNo());
			mail.setToUserName(toUser.getName());
			mail.setToUserHeadImg(toUser.getHeadImageUrl());
			mail.setType(1);
			mail.setSendTime(new Date());
			mail.setExpirationTime(DateUtil.getTimeByDay(time));
			mail.setContext(context);
			mail.setTitle(title);
			mail.setIsAttachments(1);
			mail.setAttachmentsDetails(jsonObject.getJSONArray("reward"));
			mail.setStatus(1);
			mail.setIsRead(0);
			//userCacheService.checkAddRedminder(toUser.getId(), RedReminderIndexEnum.mail);
			mails.add(mail);
		}

		if (mails != null) {
			List<Mail> newList = new ArrayList<>();
			for (int i = 0; i < mails.size(); i++) {
				newList.add(mails.get(i));
				if (i % 2000 == 0) {
					execute("batchInsertMail", newList);
					newList.clear();
				}
			}
			if (!newList.isEmpty()) {
				execute("batchInsertMail", newList);
			}
		}

	}

	@Transactional
	public void deleteReadMail(Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		execute("deleteReadMail",map);
	}

}
