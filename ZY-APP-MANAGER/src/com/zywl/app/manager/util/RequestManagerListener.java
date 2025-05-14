package com.zywl.app.manager.util;

import com.live.app.ws.bean.Command;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;

public class RequestManagerListener implements Listener {
	
	private Command appCommand;

	public RequestManagerListener(Command appCommand) {
		this.appCommand = appCommand;
	}
	
	public void handle(BaseClientSocket clientSocket, Command command) {
		if(appCommand != null) {
			if(command.isSuccess()) {
				Executer.response(CommandBuilder.builder(appCommand).success(command.getData()).build());
			}else {
				Executer.response(CommandBuilder.builder(appCommand).error(command.getMessage(), command.getData()).build());
			}
		}
    }
}
