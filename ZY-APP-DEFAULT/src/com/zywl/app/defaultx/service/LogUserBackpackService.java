package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.LogUserBackpack;
import com.zywl.app.base.bean.LogUserCapital;
import com.zywl.app.base.bean.vo.LogUserCapitalVo;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class LogUserBackpackService extends DaoService{

	
	public LogUserBackpackService() {
		super("LogUserBackpackMapper");
		// TODO Auto-generated constructor stub
	}

	@Transactional
	public void addLogUserBackpack(Long userId,Long itemId,int numberBefore,int number,LogUserBackpackTypeEnum em) {
		LogUserBackpack logUserBackpack = new LogUserBackpack();
		logUserBackpack.setUserId(userId);
		logUserBackpack.setItemId(itemId);
		logUserBackpack.setNumberBefore(numberBefore);
		logUserBackpack.setNumber(number);
		logUserBackpack.setNumberAfter(numberBefore+number);
		logUserBackpack.setType(em.getValue());
		logUserBackpack.setMark(em.getName());
		logUserBackpack.setCreateTime(new Date());
		logUserBackpack.setUpdateTime(new Date());
		Map params = objectToMap(logUserBackpack);
		params.put("tableName", LogUserBackpack.tablePrefix+userId.toString().charAt(userId.toString().length()-1));
		insert(params);
	}

	@Transactional
	public void addLogUserBackpack(Long userId,Long itemId,int numberBefore,int number,LogUserBackpackTypeEnum em,String otherUserId) {
		LogUserBackpack logUserBackpack = new LogUserBackpack();
		logUserBackpack.setUserId(userId);
		logUserBackpack.setItemId(itemId);
		logUserBackpack.setNumberBefore(numberBefore);
		logUserBackpack.setNumber(number);
		logUserBackpack.setNumberAfter(numberBefore+number);
		logUserBackpack.setType(em.getValue());
		if (em.getValue()==LogUserBackpackTypeEnum.zsg.getValue()){
			logUserBackpack.setMark(em.getName()+otherUserId);
		} else if (em.getValue()==LogUserBackpackTypeEnum.zs.getValue()) {
			logUserBackpack.setMark(otherUserId+em.getName());
		}else {
			logUserBackpack.setMark(em.getName());
		}
		logUserBackpack.setCreateTime(new Date());
		logUserBackpack.setUpdateTime(new Date());
		Map params = objectToMap(logUserBackpack);
		params.put("tableName", LogUserBackpack.tablePrefix+userId.toString().charAt(userId.toString().length()-1));
		insert(params);
	}

	@Transactional
	public int deleteLog(Date time){
		Map<String,Object> params = new HashMap<>();
		params.put("time",time);
		int res = 0;
		for (int i = 0; i < 10; i++) {
			params.put("tableName",LogUserBackpack.tablePrefix+i);
			res +=delete(params);
		}
		return res;
	}

	public List<LogUserBackpack> findLQG(Long userId){
		JSONObject params = new JSONObject();
		params.put("tableName", LogUserBackpack.tablePrefix+userId.toString().charAt(userId.toString().length()-1));
		params.put("userId",userId);
		params.put("itemId",48);
		params.put("type",22);
		return findList("findLQG",params);
	}


	public List<LogUserBackpack> getLog(Long userId, String itemId ,int start, int limit, int type,String mark) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		params.put("tableName", LogUserBackpack.tablePrefix+userId.toString().charAt(userId.toString().length()-1));
		params.put("itemId", itemId);
		params.put("start", start * limit);
		params.put("limit", limit);
		params.put("logType",type);
		params.put("mark",mark);
		List<LogUserBackpack> list = findByConditions(params);
		List<LogUserBackpack> vos = new ArrayList<>();
		for (LogUserBackpack logUserBackpack : list) {
			LogUserBackpack vo = new LogUserBackpack();
			BeanUtils.copy(logUserBackpack, vo);
			vos.add(vo);
		}
		return vos;
	}
}
