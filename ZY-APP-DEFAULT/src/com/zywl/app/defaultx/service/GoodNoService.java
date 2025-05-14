package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.GoodNo;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodNoService extends DaoService{

	public GoodNoService( ) {
		super("GoodNoMapper");
	}

	@Transactional
	public void addGoodNo(String goodNo, BigDecimal price){
		GoodNo no = new GoodNo();
		no.setGoodNo(goodNo);
		no.setPrice(price);
		no.setNumber(1);
		no.setStatus(1);
		save(no);

	}

	public void addGoodNo(String goodNo, BigDecimal price, int status){
		GoodNo no = new GoodNo();
		no.setGoodNo(goodNo);
		no.setPrice(price);
		no.setNumber(1);
		no.setStatus(status);
		save(no);

	}
	
	public List<GoodNo> findAllGoodNo(){
		return findAll();
	}

	public List<GoodNo> findCanBuyGoodNo(){
		return findList("findCanBuyGoodNo",null);
	}

	@Transactional
	public int buy(Long id){
		Map<String,Object> params = new HashMap<>();
		params.put("id",id);
		params.put("number",0);
		return execute("updateGoodNo",params);
	}

	@Transactional
	public int updateStatus(Long id,int status){
		Map<String,Object> params = new HashMap<>();
		params.put("status",status);
		params.put("id",id);
		return execute("updateGoodNo",params);
	}

	public GoodNo findById(Long id){
		Map<String,Object> params = new HashMap<>();
		params.put("id",id);
		return findOne(params);
	}

	public GoodNo findByNo(String goodNo) {
		Map<String,Object> params = new HashMap<>();
		params.put("goodNo",goodNo);
		return findOne(params);
	}
}
