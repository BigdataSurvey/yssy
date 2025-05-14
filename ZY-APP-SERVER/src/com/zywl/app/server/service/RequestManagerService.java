package com.zywl.app.server.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;

@Service
public class RequestManagerService extends BaseService {

	/**
	 * 请求Manager修改用户信息
	 * 
	 * @param user
	 * @param appSocket
	 * @param appCommand
	 */
	public void requestManagerUpdateUser(User user, AppSocket appSocket, Command appCommand) {
		requestManagerUpdateUser(user, new RequestManagerListener(appCommand));
	}

	// 通知manager更改用户信息
	public void requestManagerUpdateUser(User user, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("010003", user).build(), listener);
	}

	// 通知manager用户上架物品
	public void requestManagerUserListingItem(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("300100", data).build(), listener);
	}

	// 通知manager用户下架物品 或者撤销求购
	public void requestManagerUserDelistItemOrCancelAskBuy(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("300200", data).build(), listener);
	}

	// 通知manager用户添加求购信息
	public void requestManagerUserAddTradingAskBuy(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("300300", data).build(), listener);
	}

	// 用户交易行购买物品
	public void requestManagerTradingUserBuy(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("300500", data).build(), listener);
	}
	
	// 用户卖给求购
		public void requestManagerTradingUserSell(JSONObject data, Listener listener) {
			Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("300600", data).build(), listener);
		}

	// 余额提现
	public void requestManagerUserCash(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("200100", data).build(), listener);
	}
	
	// 余额转换
	public void requestManagerAssetConversion(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("200200", data).build(), listener);
	}
	public void requestManagerAssetConversion2(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("200201", data).build(), listener);
	}
	// 余额转换
	public void requestManagerReceivceIncome(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("200300", data).build(), listener);
	}
	
	// 余额或积分兑换商品
	public void requestManagerExchange(JSONObject data, Listener listener) {
			Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("200400", data).build(), listener);
	}
	
	// 读取邮件
	public void requestManagerReadMail(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("800100", data).build(), listener);
	}

	public void requestManagerBattleCheckpoint(JSONObject data, Listener listener) {
		Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("2001100", data).build(), listener);
	}
	
	

	public AppSocket getAppSocketByUserId(Long userId) {
		Map<String, AppSocket> clients = SocketManager.getClients(TargetSocketType.app);
		if (clients != null) {
			for (AppSocket appSocket : clients.values()) {
				User user = appSocket.getUser();
				if (user != null && userId == user.getId()) {
					return appSocket;
				}
			}
		}
		return null;
	}
}
