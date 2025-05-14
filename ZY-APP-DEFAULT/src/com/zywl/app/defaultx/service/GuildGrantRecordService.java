package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.GuildGrantRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GuildGrantRecordService extends DaoService{

	public GuildGrantRecordService( ) {
		super("GuildGrantRecordMapper");
	}

	@Transactional
	public Long addRecord(Long guildId ,Long userId,String orderNo,Long operatorUserId,BigDecimal allAmount,BigDecimal rate,BigDecimal memberAmount,BigDecimal leaderAmount,String userNo) {
		GuildGrantRecord obj = new GuildGrantRecord();
		obj.setUserId(userId);
		obj.setCreateTime(new Date());
		obj.setGuildId(guildId);
		obj.setAllAmount(allAmount);
		obj.setMemberAmount(memberAmount);
		obj.setLeaderAmount(leaderAmount);
		obj.setOperatorUserId(operatorUserId);
		obj.setOrderNo(orderNo);
		obj.setRate(rate);
		obj.setUserNo(userNo);
		save(obj);
		return obj.getId()	;
	}

	@Transactional
	public void addCreateRecord(Long guildId ,Long userId,String orderNo,Long operatorUserId,BigDecimal allAmount,BigDecimal rate) {
		GuildGrantRecord obj = new GuildGrantRecord();
		obj.setUserId(userId);
		obj.setCreateTime(new Date());
		obj.setGuildId(guildId);
		obj.setAllAmount(allAmount);
		obj.setMemberAmount(BigDecimal.ZERO);
		obj.setLeaderAmount(allAmount);
		obj.setOperatorUserId(operatorUserId);
		obj.setOrderNo(orderNo);
		save(obj);
	}
	
	
	public List<GuildGrantRecord> findAllGuildGrantRecord(){
		return findAll();
	}

	public List<GuildGrantRecord> findByUserId(Long userId,int page,int num){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("start",page*num);
		params.put("limit",num);
		return findList("findByUserId",params);
	}

	public List<GuildGrantRecord> findByOperatorUserId(Long userId,int page,int num){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("start",page*num);
		params.put("limit",num);
		return findList("findByOperatorUserId",params);
	}
}
