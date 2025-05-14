package com.live.app.ws.socket.manager;

import com.live.app.ws.constant.SocketConstants;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseServerSocket;
import com.zywl.app.base.util.ConcurrentHashSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class SocketManager {

	private static final Log logger = LogFactory.getLog(SocketManager.class);
	
	private static final Map<TargetSocketType, Map<String, BaseServerSocket>> serverPool = new ConcurrentHashMap<TargetSocketType, Map<String,BaseServerSocket>>();
	
	private static final Map<TargetSocketType, Set<BaseClientSocket>> clientPool = new ConcurrentHashMap<TargetSocketType, Set<BaseClientSocket>>();
	
	private static Timer pingClientTimer = new Timer("ping client timer");
	
	private static Timer pingServerTimer = new Timer("ping server timer");

	static{
		for(TargetSocketType targetSocketType : TargetSocketType.values()){
			serverPool.put(targetSocketType, new ConcurrentHashMap<String, BaseServerSocket>());
			clientPool.put(targetSocketType, new ConcurrentHashSet<BaseClientSocket>());
		}
		
		//send ping
		pingClientTimer.schedule(new TimerTask() {
			public void run() {
				try {
					for (TargetSocketType targetSocketType : TargetSocketType.values()) {
						if (targetSocketType.equals(TargetSocketType.app)) {
							continue;
						}
						Map<String, BaseServerSocket> clients = getClients(targetSocketType);
						
						if(!clients.isEmpty()) {
							for(BaseServerSocket serverSocket : clients.values()){
								
								if(serverSocket.isPing()){
									if(System.currentTimeMillis() - serverSocket.getLastPingTime() > SocketConstants.SOCKET_CONNECT_TIMEOUT){
										//serverSocket.onPingTimeout();
									}else{
										serverSocket.ping();
									}
								}
							}
						}
					}
				}catch (Exception e) { }
			}
		}, 2000, SocketConstants.SOCKET_CONNECT_HEARTINTERVAL);
		
		pingServerTimer.schedule(new TimerTask() {
			public void run() {
				try {
					for (TargetSocketType targetSocketType : TargetSocketType.values()) {
						Set<BaseClientSocket> servers = getServers(targetSocketType);
						if(!servers.isEmpty()) {
							for (BaseClientSocket clientSocket : servers) {
								if(clientSocket.isPing()){
									if(System.currentTimeMillis() - clientSocket.getLastPingTime() > SocketConstants.SOCKET_CONNECT_TIMEOUT){
										clientSocket.onPingTimeout();
									}else{
										clientSocket.ping();
									}
								}
							}
						}
					}
				} catch (Exception e) {}
			}
		}, 2000, SocketConstants.SOCKET_CONNECT_HEARTINTERVAL);
	}
	
	public static void addClient(TargetSocketType socketType, String id, BaseServerSocket baseServerSocket){
		Map<String, BaseServerSocket> servers = getClients(socketType);
		BaseServerSocket oldServerSocket = servers.put(id, baseServerSocket);
		logger.info("加入[" + socketType + "] 在线个数：" + servers.size());
		
		if(oldServerSocket != null){
			oldServerSocket.close();
		}
	}
	
	public static void removeClient(TargetSocketType socketType, String id){
		Map<String, BaseServerSocket> servers = serverPool.get(socketType);
		servers.remove(id);
		logger.info("移除[" + socketType + "] 剩余个数：" + servers.size());
	}
	
	public static void addServer(TargetSocketType socketType, BaseClientSocket baseClientSocket){
		Set<BaseClientSocket> clients = clientPool.get(socketType);
		clients.add(baseClientSocket);
		logger.info("加入[" + socketType + "] 在线个数：" + clients.size());
	}
	
	public static void removeServer(TargetSocketType socketType, BaseClientSocket baseClientSocket){
		Set<BaseClientSocket> clients = clientPool.get(socketType);
		clients.remove(baseClientSocket);
		logger.info("移除[" + socketType + "] 剩余个数：" + clients.size());
	}

	public static Set<BaseClientSocket> getServers(TargetSocketType socketType) {
		return clientPool.get(socketType);
	}

	@SuppressWarnings("unchecked")
	public static <T> Map<String, T> getClients(TargetSocketType socketType) {
		return (Map<String, T>) serverPool.get(socketType);
	}

}
