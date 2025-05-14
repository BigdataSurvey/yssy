package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.UserMail;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Map;

@Service
public class UserMailService extends DaoService {

	public UserMailService() {
		super("UserMailMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(UserMailService.class);
	
	
	
	/**
	 * 初始化用户-邮件 信息
	 * @param userId
	 */
	@Transactional
	public void addUserMail(Long userId) {
		UserMail userMail = new UserMail();
		userMail.setUserId(userId);
		userMail.setReadMailList(new JSONArray());
		userMail.setDeleteMailList(new JSONArray());
		save(userMail);
	}
	
	
	/**
	 * 用户读取邮件
	 * @param userId
	 * @param
	 */
	@Transactional
	public Integer userReadMail(Long userId, JSONArray mails) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("readMailList", mails);
		params.put("userId", userId);
		return execute("updateUserReadMailList", params);
	}
	
	
	public UserMail findUserReadMailInfo(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		UserMail findByUserId = (UserMail) findOne("findByUserId", params);
		if (findByUserId==null){
			addUserMail( userId) ;
			return (UserMail) findOne("findByUserId", params);
		}
		return findByUserId;
	}

	@Transactional
	public void updateUserDeleteMailList(Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		execute("updateUserDeleteMailList",params);
	}
	

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}
