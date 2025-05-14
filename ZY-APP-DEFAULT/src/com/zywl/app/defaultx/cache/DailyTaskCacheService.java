package com.zywl.app.defaultx.cache;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.DailyTask;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.DailyTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class DailyTaskCacheService extends RedisService{

	
	@Autowired
	private DailyTaskService dailyTaskService;
	
	
	public List<DailyTask> getAlldailyTask(){
		String key = RedisKeyConstant.APP_DAILY_TASK_LIST;;
		List<DailyTask> dailyTasks = getList(key,DailyTask.class);
		if (dailyTasks==null || dailyTasks.size()==0) {
			dailyTasks = dailyTaskService.findAllDailyTask();
			if (dailyTasks!=null) {
				set(key, dailyTasks);
			}
		}
		return dailyTasks;
	}
	
	public List<UserDailyTaskVo> getUserDailyTask(Long userId){
		String key = RedisKeyConstant.APP_USER_DAILY_TASK_LIST+DateUtil.getCurrent2()+":"+userId+"-";
		List<UserDailyTaskVo> list = getList(key, UserDailyTaskVo.class);
		if (list==null||list.size()==0) {
			list = new ArrayList<UserDailyTaskVo>();
			List<DailyTask> allDailyTask = getAlldailyTask();
			for (DailyTask dailyTask : allDailyTask) {
				UserDailyTaskVo vo = new UserDailyTaskVo();
				vo.setId(dailyTask.getId());
				vo.setReward(dailyTask.getReward());
				vo.setSort(dailyTask.getSort());
				vo.setStatus(0);
				list.add(vo);
			}
		}
		return list;
	}

	public void removeTaskCache(Long userId){
		String key= RedisKeyConstant.APP_USER_DAILY_TASK_LIST+DateUtil.getCurrent2()+":"+userId+"-";
		del(key);
	}

	
	public JSONObject getTaskInfo(Long userId) {
		List<UserDailyTaskVo> list = getUserDailyTask(userId);
		JSONObject result = new JSONObject();
		result.put("taskList", list);
		return result;
	}
	
	public void removeDailyTask() {
		del(RedisKeyConstant.APP_DAILY_TASK_LIST);
	}
	
}
