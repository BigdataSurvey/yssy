package com.live.app.ws.interfacex;

import com.live.app.ws.bean.Command;
import com.live.app.ws.socket.BaseClientSocket;

public interface Listener {
	
	public void handle(BaseClientSocket clientSocket, Command command);
}
