package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.GuildMember;
import com.zywl.app.defaultx.cache.GuildCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GuildMemberService extends DaoService{

	@Autowired
	private GuildCacheService guildCacheService;

	public GuildMemberService( ) {
		super("GuildMemberMapper");
	}

	@Transactional
	public void addGuildMember(Long guildId,Long userId,BigDecimal profitRate,int roleId,Long createUserId,BigDecimal bailAmount) {
		GuildMember obj = new GuildMember();
		obj.setUserId(userId);
		obj.setGuildId(guildId);
		obj.setProfitRate(profitRate);
		obj.setRoleId(roleId);
		obj.setCreateUserId(createUserId);
		obj.setProfitBalance(BigDecimal.ZERO);
		obj.setRemark("");
		obj.setBailAmount(bailAmount);
		obj.setCreateTime(new Date());
		obj.setStatus(1);
		save(obj);
	}
	
	
	public List<GuildMember> findAllGuildMember(){
		return findAll();
	}

	public List<GuildMember> findByGuildId(Long guildId){
		Map<String,Object> params = new HashMap<>();
		params.put("guildId",guildId);
		return findList("findByGuildId",params);

	}

	public  List<GuildMember> findAllByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return findList("findAllByUserId",params);
	}

	public GuildMember findByGuildIdAndUserId(Long guildId,Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("guildId",guildId);
		params.put("userId",userId);
		return (GuildMember) findOne("findByGuildIdAndUserId",params);
	}

	public GuildMember findByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return (GuildMember) findOne("findByUserId",params);
	}


	@Transactional
	public int receiveProfitBalance(Long guildId,Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("guildId",guildId);
		params.put("userId",userId);
		guildCacheService.removeMember(userId);
		return execute("receiveProfitBalance",params);
	}

	@Transactional
	public int addProfitBalance(Long userId,BigDecimal amount){

		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("amount",amount);
		guildCacheService.removeMember(userId);
		return execute("addProfitBalance",params);
	}

	@Transactional
	public int updateRate(Long userId,BigDecimal rate){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("profitRate",rate);
		guildCacheService.removeMember(userId);
		return execute("updateRate",params);
	}
}
