package com.zywl.app.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import org.springframework.stereotype.Service;

@Service
public class SGRequestMangerService {

	
	public void requestManagerBet(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.sg, CommandBuilder.builder().request("200712", data).build(), listener);
	}
}
