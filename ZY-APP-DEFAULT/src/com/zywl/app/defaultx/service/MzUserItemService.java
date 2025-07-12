package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Chat;
import com.zywl.app.base.bean.MzUserItem;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class MzUserItemService extends DaoService{

	private static final Log logger = LogFactory.getLog(MzUserItemService.class);



	public MzUserItemService() {
		super("MzUserItemMapper");
	}
	
	
	
	
	@Transactional
	public Long addMzItem(Long userId,Long mzItemId,String lastUserNo,String lastUserName) {
		MzUserItem mzUserItem = new MzUserItem();
		mzUserItem.setUserId(userId);
		mzUserItem.setMzItemId(mzItemId);
		mzUserItem.setLastUserNo(lastUserNo);
		mzUserItem.setLastUserName(lastUserName);
		mzUserItem.setStatus(0);
		mzUserItem.setUpTime(null);
		mzUserItem.setUpEndTime(null);
		mzUserItem.setCreateTime(new Date());
		save(mzUserItem);
		return mzUserItem.getId();
	}

	public List<MzUserItem> findByUserId(Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		return findList("findByConditions",map);
	}

	public MzUserItem findById(Long id){
		Map<String,Object> map = new HashMap<>();
		map.put("id",id);
		return (MzUserItem) findOne("findByConditions",map);
	}

	@Transactional
	public void updateMzUserItem(MzUserItem mzUserItem){
		execute("updateMzUserItem",mzUserItem);
	}





}
