package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.BalanceShop;
import com.zywl.app.base.bean.MzTrad;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MzTradService extends DaoService{


	public MzTradService() {
		super("MzTradMapper");
	}

	
	
	
	
	
	
	@Transactional
	public void addMzTrad(Long mzItemId,Long userItemId,Long sellUserId,BigDecimal price,BigDecimal fee,BigDecimal getAmount,String name,int icon) {
		MzTrad mzTrad = new MzTrad();
		mzTrad.setFee(fee);
		mzTrad.setUserItemId(userItemId);
		mzTrad.setCreateTime(new Date());
		mzTrad.setGetAmount(getAmount);
		mzTrad.setStatus(1);
		mzTrad.setName(name);
		mzTrad.setIcon(icon);
		mzTrad.setMzItemId(mzItemId);
		mzTrad.setSellPrice(price);
		mzTrad.setSellUserId(sellUserId);
		save(mzTrad);
	}
	
	
	public List<MzTrad> findMySell(Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("userId",userId);
		return findList("findByUserId",map);
	}


	public List<MzTrad> findAllTrad(int page,int num,Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("page",page*num);
		map.put("num",num);
		map.put("userId",userId);
		return findList("findAll",map);
	}

	@Transactional
	public void updateTrad(MzTrad trad){
		execute("updateMzTrad",trad);
	}

	public List<MzTrad> findByTypeAndLv(String type,int lv,int page,int num,Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("type",type);
		map.put("lv",lv);
		map.put("page",page*num);
		map.put("num",num);
		map.put("userId",userId);
		return findList("findByItemTypeAndLv",map);
	}



	public List<MzTrad> findByItemType(String type,int page,int num,Long userId){
		Map<String,Object> map = new HashMap<>();
		map.put("type",type);
		map.put("page",page*num);
		map.put("num",num);
		map.put("userId",userId);
		return findList("findByItemType",map);
	}

	public MzTrad findById(Long id){
		Map<String,Object> map = new HashMap<>();
		map.put("id",id);
		return (MzTrad) findOne("findById",map);
	}

}
