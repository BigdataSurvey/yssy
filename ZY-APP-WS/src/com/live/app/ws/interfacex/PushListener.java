package com.live.app.ws.interfacex;

import com.live.app.ws.socket.BaseSocket;

/**
 * 推送监听
 * @author DOE
 *
 */
public interface PushListener {
	
	public void onRegist(BaseSocket baseSocket, Object data);
	
	public void onReceive(BaseSocket baseSocket, Object data);
}
