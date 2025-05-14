package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserAnimaTree;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserAnimaTreeService extends DaoService{

	public UserAnimaTreeService( ) {
		super("UserAnimaTreeMapper");
	}

	@Transactional
	public UserAnimaTree addUserAnimaTree(Long userId) {
		UserAnimaTree obj = new UserAnimaTree();
		obj.setUserId(userId);
		obj.setAnimaNumber(BigDecimal.ZERO);
		obj.setCreateTime(new Date());
		save(obj);
		return obj;
	}
	



	@Transactional
	public int addAnimaNumber(Long userId,BigDecimal number){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("amount",number);
		return execute("insertOrUpdate",params);
	}




	@Transactional
	public int subAnimaNumber(Long userId,BigDecimal number){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		params.put("amount",number.negate());
		return execute("insertOrUpdate",params);
	}

	public UserAnimaTree findByUserId(Long userId){
		Map<String,Object> params = new HashMap<>();
		params.put("userId",userId);
		return (UserAnimaTree) findOne("findByUserId",params);
	}

	public List<UserAnimaTree> findAllTree(){
		return findAll();
	}


}
