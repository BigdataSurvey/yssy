package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Guild;
import com.zywl.app.defaultx.cache.GuildCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class GuildService extends DaoService{

	public GuildService( ) {
		super("GuildMapper");
	}

	@Autowired
	private GuildCacheService guildCacheService;

	@Transactional
	public Long applyAddGuild(String guildName,Long userId,int memberNumber,BigDecimal bailAmount,int type) {
		Guild obj = new Guild();
		obj.setGuildName(guildName);
		obj.setUserId(userId);
		obj.setMemberNumber(memberNumber);
		obj.setRemark("");
//		obj.setCreateTime(new Date());
		obj.setBailAmount(bailAmount);
		obj.setType(type);
		obj.setStatus(2);
		obj.setFreeNum(4);
		obj.setApplyTime(new Date());
		save(obj);
		guildCacheService.removeGuilds();
		return obj.getId();
	}

	public Guild findByUserId(Long userId){
		Map<String, Object> params = new HashedMap<>();
		params.put("userId", userId);
		return (Guild) findOne("findByUserId",params);
	}
	
	public List<Guild> findAllGuild(){
		return findAll();
	}

	public Guild findById(Long id){
		Map<String, Object> params = new HashedMap<>();
		params.put("id", id);
		return findOne(params);
	}

	@Transactional
	public int updateGuildBailAmount(BigDecimal addBailAmount,Long id){
		Map<String, Object> params = new HashedMap<>();
		params.put("addBailAmount", addBailAmount);
		params.put("id",id);
		guildCacheService.removeGuilds();
		return execute("updateGuildBailAmount",params);
	}

	/**
	 * 修改成员数量及免费成员数量
	 * @param id
	 * @param
	 * @return
	 */
	public int updateGuildMemberNumber(long id, int memberNumber, int freeNumber) {
		Map<String, Object> params = new HashedMap<>();
		params.put("id",id);
		params.put("memberNumber",memberNumber);
		params.put("freeNum",freeNumber);
		guildCacheService.removeGuilds();
		return execute("updateMemberNumber",params);
	}

	public int passGuildApply(long id) {
		Map<String, Object> params = new HashedMap<>();
		params.put("id",id);
		params.put("createTime",new Date());
		guildCacheService.removeGuilds();
		return execute("passGuildApply",params);
	}
}
