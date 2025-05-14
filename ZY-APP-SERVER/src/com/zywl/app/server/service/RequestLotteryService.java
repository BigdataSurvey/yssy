package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class RequestLotteryService extends BaseService {

	


	
	// 下注
	public void requestBattleRoyaleBetService(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.getServerEnum(data.getIntValue("gameId")), CommandBuilder.builder().request("101103", data).build(), listener);
	}
	
	
	public void requestBattleRoyaleJoinRoom(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.getServerEnum(data.getIntValue("gameId")), CommandBuilder.builder().request("101101", data).build(), listener);
	}
	
	
	
}
