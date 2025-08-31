package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Achievement;
import com.zywl.app.base.bean.UserHandbook;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class UserHandbookService extends DaoService {

	public UserHandbookService() {
		super("UserHandbookMapper");
	}

	@Transactional
	public void addUserHandbook(Long userId,int handbookType,Long handbookId){
		UserHandbook userHandbook = new UserHandbook();
		userHandbook.setUserId(userId);
		userHandbook.setHandbookId(handbookId);
		userHandbook.setHandbookType(handbookType);
		userHandbook.setStatus(1);
		userHandbook.setBuyTime(new Date());
		userHandbook.setEndTime(DateUtil.getDateByDay(36));
		userHandbook.setDays(0);
		userHandbook.setCreateTime(new Date());
		userHandbook.setUpdateTime(new Date());
		save(userHandbook);
	}


	
	public List<UserHandbook> findByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<>();
		params.put("userId", userId);
		return findList("findByUserId",params);
	}
	
	
	
	public UserHandbook findByUserIdAndHandbookType(Long userId,int type) {
		Map<String, Object> params = new HashedMap<>();
		params.put("type", type);
		params.put("userId",userId);
		return (UserHandbook) findOne("findByUserIdAndHandbookType", params);
	}

	@Transactional
	public void updateUserHandbook(UserHandbook userHandbook){
		int update = update(userHandbook);
		if (update<1){
			throwExp("操作失败");
		}
	}
	
	

	
	
}
