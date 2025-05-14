package com.live.app.ws.socket;

import com.live.app.ws.constant.CommandConstants;
import com.live.app.ws.constant.SocketConstants;
import com.live.app.ws.enums.TargetSocketType;
import com.zywl.app.base.Base;
import com.zywl.app.base.bean.SystemLocale;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.util.AppDefaultThreadFactory;
import com.zywl.app.base.util.ByteUtil;
import com.zywl.app.base.util.GZIPUtils;
import com.zywl.app.base.util.UID;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseSocket extends Base {
	
	public TargetSocketType socketType;
	
	private Session session;
	
	private String sessionId;
	
	private boolean service;
	
	private boolean gzip;
	
	private boolean ping = false;
	
	private boolean close;

	private Date connectTime;
	
	private long lastPingTime;
	
	private ExecutorService executor;
	
	private String locale;

	private Object _shakeHands = new Object();
	
	public BaseSocket(TargetSocketType socketType, boolean gzip){
		this.socketType = socketType;
		this.gzip = gzip;
		this.locale=SystemLocale.DEFAULT_LOCALE;
	}

	@OnOpen
	public void onOpen(Session session, EndpointConfig config){
		synchronized (_shakeHands) {
			if(isNotNull(session.getQueryString())) {
				session.setMaxBinaryMessageBufferSize(SocketConstants.SOCKET_SIZE_BINARY_MINI);
			}else {
				session.setMaxBinaryMessageBufferSize(SocketConstants.SOCKET_SIZE_BINARY);
			}
			if(this instanceof BaseWebSocketServer) {
				session.setMaxBinaryMessageBufferSize(4 * 1024 * 1024);
			}
			session.setMaxTextMessageBufferSize(SocketConstants.SOCKET_SIZE_TEXT);
			session.setMaxIdleTimeout(SocketConstants.SOCKET_CONNECT_TIMEOUT);
			this.executor = Executors.newSingleThreadExecutor(new AppDefaultThreadFactory("socket-" + socketType));
			this.close = false;
			this.session = session;
			this.sessionId = UID.create();
			this.connectTime = new Date();
			this.lastPingTime = System.currentTimeMillis();
			Map<String, String> shakeHandsData = session.getPathParameters();
			HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
			onOpen(httpSession, shakeHandsData);
			
			startPing();
		}
	}
	
	@OnError
	public void onError(Session session, Throwable error) {
		if (error instanceof AppException || error instanceof EOFException || error instanceof IOException
				|| "org.eclipse.jetty.websocket.api.CloseException".equals(error.getClass().getName())) {
			logger().warn("已知连接异常[" + error + "]");
		}else{
			logger().error("未知连接异常[" + error + "]", error);
		}
		try {
			if (session!=null){
				session.close();
			}
		} catch (IOException e) {
			logger().error("onError关闭session异常：" + e, e);
		}
	}
	
	@OnClose
	public void onClose(Session session, CloseReason closeReason){
		synchronized (_shakeHands) {
			if(!close){
				close = true;
				logger().warn("连接关闭[" + sessionId + "][" + closeReason + "]");
				try {
					this.session.close();
					this.executor.shutdown();
				} catch (Exception e) {}
				this.session = null;
				this.executor = null;
				endPing();
				disconnect();
			}
		}
	}

	@OnMessage
	public void onMessage(ByteBuffer bytes, Session session){
		String message = CommandConstants.CMD_PING;
		if(gzip) {
			message = GZIPUtils.uncompressToString(bytes.array());
		}else {
			message = ByteUtil.ToString(bytes);
		}
		lastPingTime = System.currentTimeMillis();
		if(eq(CommandConstants.CMD_PING, message)){
//			logger().debug("received ping：" + sessionId);
			lastPingTime = System.currentTimeMillis();
		}else if(!close){
			onMessage(message);
		}
	}
	
	public void send(String data) {
		ByteBuffer buffer = null;
		if(gzip) {
			buffer = ByteBuffer.wrap(GZIPUtils.compress(data));
		}else {
			buffer = ByteUtil.ToByte(data);
		}
		if(this.session != null) {
			if(buffer.array().length >= session.getMaxBinaryMessageBufferSize()) {
				throwExp("数据超过最大限制");
			}
		}
		send(buffer);
	}

	private void send(ByteBuffer data) {
		if(executor != null && !executor.isShutdown()){
			executor.execute(new Runnable() {
				public void run() {
					try {
						if (!close) {
							synchronized (session) {
								session.getBasicRemote().sendBinary(data);
							}
						}
					} catch (Exception e) {
						close();
					}
				}
			});
		}else{
			close();
		}
	}
	
	private void startPing() {
		ping = true;
	}
	
	private void endPing(){
		ping = false;
	}

	public void ping() {
		try {
			send(CommandConstants.CMD_PING);
		} catch (Exception e) {
			logger().error("Ping异常", e);
		}
	}

	public void close(){
		if(this.session != null && this.executor != null && !this.executor.isShutdown()){
			/*
			 * 关闭session时，tomcat底层发送关闭帧走的是org.apache.tomcat.websocket.WsRemoteEndpointImplBase.sendMessageBlock方法
			 * 可能会卡住当前线程，所以走异步发送
			 */
			executor.execute(new Runnable() {
				public void run() {
					try {
						if(session != null) {
							session.close();
						}
					} catch (IOException e) {
						logger().error("关闭session异常：" + e, e);
					}
				}
			});
		}
	}

	public TargetSocketType getSocketType() {
		return socketType;
	}

	public boolean isService() {
		return service;
	}

	public void setService(boolean service) {
		this.service = service;
	}

	public Date getConnectTime() {
		return connectTime;
	}

	public long getLastPingTime() {
		return lastPingTime;
	}

	public boolean isPing() {
		return ping;
	}

	public String getSessionId(){
		return sessionId;
	}
	
	public String getLocale() {
		return SystemLocale.DEFAULT_LOCALE;
	}
	
	public void setLocale(String locale) {
		this.locale=locale;
	}

	public void onPingTimeout(){
		logger().warn("心跳超时");
		close();
	}

	public abstract void disconnect();
	
	protected abstract void onOpen(HttpSession httpSession, Map<String, String> shakeHandsData);
	
	protected abstract void onMessage(String message);
	
	
}
