package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DailyTask;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DailyTaskService extends DaoService {

	public DailyTaskService() {
		super("DailyTaskMapper");
	}


	
	public List<DailyTask> findAllDailyTask() {
		return findAll();
	}
	
	
	
	public List<DailyTask> findDailyTaskByType(int type) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("type", type);
		return findList("findByType", params);
	}


//	public List<DailyTask> findDailyTaskById(Long userId) {
//		Map<String, Object> params = new HashedMap<String, Object>();
//		params.put("userId", userId);
//		return findList("selectById", params);
//	}


	
	

	
	
}
