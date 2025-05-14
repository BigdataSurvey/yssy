package com.live.app.ws.util;

import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.socket.manager.SocketManager;
import com.zywl.app.base.util.AppDefaultThreadFactory;
import com.zywl.app.base.util.PropertiesUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mass {

	private static ExecutorService massExecutor;
	
	static {
		PropertiesUtil propertiesUtil = new PropertiesUtil("thread.properties");
		massExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.mass.pool"), new AppDefaultThreadFactory("DoMass"));
	}

	public synchronized static void mass(TargetSocketType socketType, final Command command){
		Map<String, BaseServerSocket> clients = SocketManager.getClients(socketType);
		if(clients != null && !clients.isEmpty()){
			for (final BaseServerSocket baseServerSocket : clients.values()) {
				massExecutor.execute(new Runnable() {
					public void run() {
						baseServerSocket.sendCommand(command);
					}
				});
			}
		}
		Set<BaseClientSocket> servers = SocketManager.getServers(socketType);
		if(servers != null && !servers.isEmpty()){
			for (final BaseClientSocket baseClientSocket : servers) {
				massExecutor.execute(new Runnable() {
					public void run() {
						baseClientSocket.sendCommand(command);
					}
				});
			}
		}
	}
}
