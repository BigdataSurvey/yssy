package com.zywl.app.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;

@Service
public class BattleRoyaleRequsetMangerService {

	
	public void requestManagerBet(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.battleRoyale, CommandBuilder.builder().request("200700", data).build(), listener);
	}
	public void requestManagerGameBet(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.battleRoyale, CommandBuilder.builder().request("200800", data).build(), listener);
	}
}
