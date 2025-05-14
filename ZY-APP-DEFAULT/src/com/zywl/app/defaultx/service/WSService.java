package com.zywl.app.defaultx.service;

import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.defaultx.cache.WsidCaCheService;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class WSService extends DaoService {

	@Autowired
	private WsidCaCheService wsidCaCheService;

	public WSService() {
		super("WSMapper");
	}

	@Transactional
	public void addUserWs(WsidBean ws) {
		save(ws);
	}

	@Transactional
	public int removeUserWs(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		return delete(params);
	}

	public WsidBean findWsByWsId(String wsId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("wsId", wsId);
		return findOne(params);
	}

	
	public WsidBean findWsByUserId(Long user) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", user);
		return (WsidBean) findOne("findWsByUserId", params);
	}

	@Transactional
	public int removeByUserId(Long userId) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		int a = delete(params);
		wsidCaCheService.removeUserWs(userId);
		return a;
	}

}
