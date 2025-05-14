package com.live.app.ws.socket;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.ConnectedData;
import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.exp.AppException;

import javax.servlet.http.HttpSession;

public abstract class BaseWebSocketServer extends BaseServerSocket {
	
	protected HttpSession httpSession;
	
	public BaseWebSocketServer(TargetSocketType socketType) {
		super(socketType, false, false);
	}

	public ConnectedData onConnect(JSONObject shakeHandsData) throws AppException {
		return null;
	}

	@Override
	protected void onDisconnect() {
	}

	@Override
	protected String getPrivateKey(String pk) {
		return null;
	}

	@Override
	protected void filterCommand(Command command) {
	}
	
	@Override
	public boolean isEncrypt(Command command) {
		return false;
	}
	
	public HttpSession getHttpSession() {
		return httpSession;
	}
}
