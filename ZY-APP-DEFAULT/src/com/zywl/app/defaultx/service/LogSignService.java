package com.zywl.app.defaultx.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.LogSign;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.UserSignCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class LogSignService extends DaoService{
	
	
	@Autowired
	private UserSignCacheService userSignCacheService;

	public LogSignService() {
		super("LogSignMapper");
	}
	
	@Transactional
	public Long addSignLog(Long userId,String signContext,int type,int patchDays) {
		LogSign logSign = new LogSign();
		logSign.setUserId(userId);
		if (type==2) {
			logSign.setCreateTime(DateUtil.getTimeByDay(-patchDays));
		}else {
			logSign.setCreateTime(new Date());
		}
		logSign.setPatchDays(patchDays);
		logSign.setSignContext(signContext);
		logSign.setType(type);
		save(logSign);
		userSignCacheService.addLogSignCache(userId, logSign);
		return logSign.getId();
	}
	
	
	public List<LogSign> findLogSignByUserId(Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		params.put("createTime", DateUtil.getFirstDayOfMonth());
		return findList("findUserSignLog", params);
	}
	
	
	

}
