package com.zywl.app.service;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;

@Service
public class BattleRoyaleRequsetMangerService2 {

	/** 倩女幽魂/大逃杀等结算用*/
	public void requestManagerBet(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.dts2,
				CommandBuilder.builder().request("200710", data).build(),
				listener
		);
	}

	/** PBX 下注扣款 */
	public void requestPbxBet(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.dts2,
				CommandBuilder.builder().request("200720", data).build(),
				listener
		);
	}

	/** PBX 结算派奖*/
	public void requestPbxSettle (JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.dts2,
				CommandBuilder.builder().request("200721", data).build(),
				listener
		);
	}

	/** PBX 查询（奖池/记录/榜单） */
	public void requestPbxQuery(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.dts2,
				CommandBuilder.builder().request("200722", data).build(),
				listener
		);
	}

	/** PBX 周榜结算 */
	public void requestPbxWeekSettle(JSONObject data, Listener listener) {
		Executer.request(
				TargetSocketType.dts2,
				CommandBuilder.builder().request("200723", data).build(),
				listener
		);
	}

}
