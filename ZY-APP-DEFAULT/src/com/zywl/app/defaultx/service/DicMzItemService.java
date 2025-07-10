package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DicMzItem;
import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicMzItemService extends DaoService{

	public DicMzItemService( ) {
		super("DicMzItemMapper");
	}

	public List<DicMzItem> findAllMzItem(){
		return findAll();
	}

	public List<DicMzItem> findCanBuy(){
		return findList("findCanBuy",null);
	}

	public DicMzItem findById(Long id){
		Map<String,Object> map = new HashMap<>();
		map.put("id",id);
		return (DicMzItem) findOne("findById",map);
	}


	@Transactional
	public void updateNumber(Long id){
		Map<String,Object> map = new HashMap<>();
		map.put("id",id);
		execute("updateNumber",map);
	}




}
