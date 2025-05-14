package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.UserDailyTask;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.DailyTaskCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserDailyTaskService extends DaoService{

	@Autowired
	private DailyTaskCacheService dailyTaskCacheService;

	public UserDailyTaskService() {
		super("UserDailyTaskMapper");
	}
	
	@Transactional
	public UserDailyTask addUserDailyTask(Long userId,JSONArray taskList) {
		UserDailyTask record = new UserDailyTask();
		record.setUserId(userId);
		record.setYmd(DateUtil.format9(new Date()));
		record.setTaskList(taskList);
		save(record);
		return record;
	}
	
	public List<UserDailyTask> findAllUserDailyTask() {
		return findAll();
	}

	public UserDailyTask findTodayByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",DateUtil.format9(new Date()));
		return (UserDailyTask) findOne("findByUserIdAndYmd",params);
	}

	public List<UserDailyTask> findListTodayByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",DateUtil.format9(new Date()));
		return  findList("findListByUserIdAndYmd",params);
	}

	public UserDailyTask findByUserIdAndYmd(Long userId,String ymd){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",ymd);
		return (UserDailyTask) findOne("findByUserIdAndYmd",params);
	}


	@Transactional
	public int updateUserTask(Long userId,JSONArray taskList){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("ymd",DateUtil.format9(new Date()));
		params.put("taskList",taskList);
		return execute("update",params);
	}


	public List<UserDailyTask> findToday() {
		Map<String,Object> params = new HashMap<>();
		params.put("ymd",DateUtil.format9(new Date()));
		return findList("findToday",params);
	}


}
