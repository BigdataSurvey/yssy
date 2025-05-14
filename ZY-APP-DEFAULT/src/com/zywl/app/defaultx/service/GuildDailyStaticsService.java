package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.GuildDailyStatics;
import com.zywl.app.base.bean.vo.GuildDailyStaticsVo;
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
public class GuildDailyStaticsService extends DaoService{

	public GuildDailyStaticsService( ) {
		super("GuildDailyStaticsMapper");
	}

	@Transactional
	public void addStatics(String ymd,Long userId,Long guildId) {
		GuildDailyStatics obj = new GuildDailyStatics();
		obj.setUserId(userId);
		obj.setYmd(ymd);
		obj.setGuildId(guildId);
		obj.setRevenueNumber(0);
		obj.setExpendNumber(0);
		obj.setRevenue(BigDecimal.ZERO);
		obj.setExpend(BigDecimal.ZERO);
		save(obj);
	}
	
	
	public List<GuildDailyStatics> findAllGuildDailyStatics(){
		return findAll();
	}


	public List<GuildDailyStatics> findByUserId(Long userId,int page,int num){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("start",page*num);
		params.put("limit",num);
		return findList("findByUserId",params);
	}


	public GuildDailyStaticsVo findStatics(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return (GuildDailyStaticsVo) findOne("findStatics",params);
	}


	public List<GuildDailyStatics> findStaticsByYmd(Long userId,String ymd){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",ymd);
		return  findList("findStaticsByYmd",params);
	}

	@Transactional
	public int updateStatics(int type,Long userId,BigDecimal amount){

		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",DateUtil.format9(new Date()));
		params.put("type",type);
		if (type==1){
			//收入
			params.put("revenue",amount);
		}else{
			//支出
			params.put("expend",amount);
		}
		return execute("update",params);

	}


	@Transactional
	public int batchInsertStatics(JSONArray members){
		return execute("batchInsert",members);
	}
}
