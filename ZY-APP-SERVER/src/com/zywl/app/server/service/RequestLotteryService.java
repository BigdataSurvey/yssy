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
		Executer.request(
				TargetSocketType.getServerEnum(data.getIntValue("gameId")),
				CommandBuilder.builder().request("101103", data).build(),
				listener
		);
	}

	// 加入房间
	public void requestBattleRoyaleJoinRoom(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.getServerEnum(data.getIntValue("gameId")),
				CommandBuilder.builder().request("101101", data).build(),
				listener
		);
	}

	// PBX 加入房间
	public void requestPbxJoinRoom(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.getServerEnum(data.getIntValue("gameId")),
				CommandBuilder.builder().request("102101", data).build(),
				listener
		);
	}

	// PBX 下注
	public void requestPbxBetService(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.getServerEnum(data.getIntValue("gameId")),
				CommandBuilder.builder().request("102103", data).build(),
				listener
		);
	}

	// PBX 离开房间
	public void requestPbxLeaveRoom(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.getServerEnum(data.getIntValue("gameId")),
				CommandBuilder.builder().request("102104", data).build(),
				listener
		);
	}

	// 通用 离开房间
	public void requestBattleRoyaleLeaveRoom(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.getServerEnum(data.getIntValue("gameId")),
				CommandBuilder.builder().request("101104", data).build(),
				listener
		);
	}
}

