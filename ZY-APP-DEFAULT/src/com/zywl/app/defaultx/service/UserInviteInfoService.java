package com.zywl.app.defaultx.service;

import com.sun.corba.se.spi.ior.ObjectKey;
import com.zywl.app.base.bean.UserInviteInfo;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserInviteInfoService extends DaoService{

	public UserInviteInfoService( ) {
		super("UserInviteInfoMapper");
	}

	@Transactional
	public UserInviteInfo addUserInviteInfo(Long userId) {
		UserInviteInfo userInviteInfo = new UserInviteInfo();
		userInviteInfo.setUserId(userId);
		userInviteInfo.setIssue(1);
		userInviteInfo.setAdNumber(0);
		userInviteInfo.setCreateTime(new Date());
		userInviteInfo.setEffectiveNumber(0);
		userInviteInfo.setNumber(0);
		userInviteInfo.setEndTime(DateUtil.getDateByM(30*24*60*60));
		userInviteInfo.setStatus(1);
		save(userInviteInfo);
		return userInviteInfo;
	}
	@Transactional
	public UserInviteInfo addUserInviteInfo(Long userId,int issue) {
		UserInviteInfo userInviteInfo = new UserInviteInfo();
		userInviteInfo.setUserId(userId);
		userInviteInfo.setIssue(issue);
		userInviteInfo.setAdNumber(0);
		userInviteInfo.setCreateTime(new Date());
		userInviteInfo.setEffectiveNumber(0);
		userInviteInfo.setNumber(0);
		userInviteInfo.setEndTime(DateUtil.getDateByM(30*24*60*60));
		userInviteInfo.setStatus(1);
		save(userInviteInfo);
		return userInviteInfo;
	}


	@Transactional
	public UserInviteInfo addUserInviteInfo(Long userId,Date createTime,Integer issue) {
		UserInviteInfo userInviteInfo = new UserInviteInfo();
		userInviteInfo.setUserId(userId);
		userInviteInfo.setAdNumber(0);
		userInviteInfo.setIssue(issue);
		userInviteInfo.setCreateTime(createTime);
		userInviteInfo.setEffectiveNumber(0);
		userInviteInfo.setNumber(0);
		userInviteInfo.setStatus(1);
		userInviteInfo.setEndTime(DateUtil.getDateByM(createTime,30*24*60*60));
		save(userInviteInfo);
		return userInviteInfo;
	}
	
	
	public List<UserInviteInfo> findAllInviteInfo(){
		Map<String, Object> params = new HashMap<>();
		params.put("endTime",new Date());
		return findList("findAllByEndTime",params);
	}

	public UserInviteInfo findByUserId(Long userId){
		Map<String, Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("endTime",new Date());
		UserInviteInfo findByUserId = (UserInviteInfo) findOne("findByUserId", params);
		if (findByUserId==null){
			UserInviteInfo lastByUserId = findLastByUserId(userId);
			if (lastByUserId.getEndTime().getTime()<System.currentTimeMillis()){
				addUserInviteInfo(userId,lastByUserId.getIssue()+1);
			}
			return (UserInviteInfo) findOne("findByUserId", params);
		}
		return findByUserId;
	}


	public UserInviteInfo findLastByUserId(Long userId){
		Map<String, Object> params = new HashMap<>();
		params.put("userId",userId);

		UserInviteInfo findByUserId = (UserInviteInfo) findOne("findLastByUserId", params);
		if (findByUserId==null){
			addUserInviteInfo(userId);
			return (UserInviteInfo) findOne("findLastByUserId", params);
		}
		return findByUserId;
	}

	@Transactional
	public int addFriendNumber(Long userId){
		Map<String, Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("endTime",new Date());
		return execute("addFriendNumber",params);
	}

	@Transactional
	public int addEffectiveFriendNumber(Long userId){
		Map<String, Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("endTime",new Date());
		return execute("addEffectiveFriendNumber",params);
	}

	@Transactional
	public int addAdNumber(Long userId){
		Map<String, Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("endTime",new Date());
		return execute("addAdNumber",params);
	}
}
