package com.zywl.app.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;

@Service
public class FoodRequestManagerService {

	public void requestManagerBet(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.foodServer, CommandBuilder.builder().request("200600", data).build(), listener);
	}

	public void requestManagerSettle (JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.foodServer, CommandBuilder.builder().request("200701", data).build(), listener);
	}

	public void requestManageReturnCapital(JSONObject data, Listener listener){
		Executer.request(TargetSocketType.foodServer, CommandBuilder.builder().request("200601", data).build(), listener);
	}
}
