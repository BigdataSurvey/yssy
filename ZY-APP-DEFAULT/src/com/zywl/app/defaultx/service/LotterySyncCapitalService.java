package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.defaultx.cache.LockCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service 
public class LotterySyncCapitalService {

	
	@Autowired
	private UserCapitalService userCapitalService;
	
	@Autowired
	private LockCacheService lockCacheService;
	
	@Autowired
	private TaskOrderService taskOrderService;
	

	
	public void requestManagerCreateTask(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("600300", data).build(), listener);
	}
	
	public void requestManagerUpdateTask(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("600400", data).build(), listener);
	}
}
