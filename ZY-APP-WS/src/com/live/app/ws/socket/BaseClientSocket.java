package com.live.app.ws.socket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.constant.CommandConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.KeyUtil;
import com.live.app.ws.util.Push;
import com.zywl.app.base.util.DesUtil;

import javax.servlet.http.HttpSession;
import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Map;

public abstract class BaseClientSocket extends BaseSocket {

	public static final SimplePropertyPreFilter filter = new SimplePropertyPreFilter();

	private int reconnect;

	private int reconnectCount;

	protected String server;

	protected JSONObject shakeHandsDatas;

	protected String privateKey;

	protected boolean stop;

	private  static  WebSocketContainer container;

	/**
	 *
	 * @param reconnect 指定reconnect重连次数  reconnect 小于 0无限重连，等于0不重连
	 * @param server 服务器地址
	 * @param shakeHandsDatas 握手数据
	 */
	public BaseClientSocket(TargetSocketType socketType, boolean gzip, int reconnect, String server,JSONObject shakeHandsDatas){
		super(socketType, gzip);
		this.reconnect = reconnect;
		this.server = server;
		this.shakeHandsDatas = shakeHandsDatas;
	}

	protected String getShakeHandsStr(String pk){
		String shakeHandsStr = "";
		this.privateKey = KeyUtil.privateKey(pk, "create");
		if(privateKey == null){
			throwExp("未获取到私匙");
		}
		try {
			if(shakeHandsDatas != null)
				shakeHandsStr = DesUtil.encrypt(shakeHandsDatas.toJSONString(), privateKey);
		} catch (Exception e) {
			throwExp("加密握手数据失败");
		}
		try {
			return URLEncoder.encode(URLEncoder.encode(shakeHandsStr, "UTF-8"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger().error(e);
		}
		return null;
	}

	public void connect(){
		stop = false;
		try{
			String pk = KeyUtil.publicKey();
			String url = server + "/" + pk + "/" + getShakeHandsStr(pk);
			logger().info("尝试连接到" + url);
			if (container==null){
				container = ContainerProvider.getWebSocketContainer();
			}
			container.connectToServer(this, URI.create(url));
		}catch(IllegalStateException e){
			logger().error(e);
		}catch(Exception e){
			logger().error("创建连接失败：" + e.getMessage());
			disconnect();
		}
	}

	public void onOpen(HttpSession httpSession, Map<String, String> shakeHandsData) {
		reconnectCount = 0;
	}

	public void onMessage(String message) {
		try {
			if(!message.startsWith("{")){
				message = DesUtil.decrypt(message, privateKey);
			}
		} catch (Exception e) {
			throwExp("不能解密的消息");
		}
		Command command = JSON.parseObject(message, Command.class);
		if (!command.getCode().equals("700200")) {
			//logger().info("收到服务端消息：" + message);
		}
		if(eq(CommandConstants.CMD_CONNECTED, command.getCode())){
			logger().info("服务器连接成功 -> " + server);
			onConnect(command.getData());
			SocketManager.addServer(getSocketType(), this);

		}else if(eq(CommandConstants.CMD_PUSH_REGIST, command.getCode())){
			logger().info("注册推送：" + JSON.toJSONString(command.getData()));
			Push.doAddPush(this, JSON.parseObject(JSON.toJSONString(command.getData()), PushBean.class));

		}else if(eq(CommandConstants.CMD_PUSH_UNREGIST, command.getCode())){
			logger().info("取消推送：" + JSON.toJSONString(command.getData()));
			Push.doRemovePush(this, JSON.parseObject(JSON.toJSONString(command.getData()), PushBean.class));

		}else if(eq(CommandConstants.CMD_PUSH_REGIST_RESULT, command.getCode())){
			logger().info("注册推送成功："+JSON.toJSONString(command.getData()));
			Push.onRegistSuccess(this, JSON.parseObject(JSON.toJSONString(command.getData()), PushBean.class));

		}else if(command.isPush()){
			Push.onPush(this, PushCode.valueOf(command.getCode()), command.getData());
		}else{
			Executer.disposeResponse(this, command);
		}
	}

	public void disconnect() {
		Push.removeAll(this);
		SocketManager.removeServer(getSocketType(), this);
		if(!stop){
			onDisconnect(reconnect < 0 ? reconnect : reconnect - reconnectCount);
			if (reconnectCount++ != reconnect) {
				reconnect();
			} else {

			}
		}
	}

	private void reconnect() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			logger().error("重连异常", e);
		}
		new Thread(){
			public void run() {
				connect();
			};
		}.start();
	}

	public boolean sendCommand(Command command){
		try {
			if(isNull(command.getLocale())) {
				command.setLocale(getLocale());
			}
			String data = JSON.toJSONString(command, filter);
			if (!command.getCode().equals("700200") && !command.getCode().equals("syncTaskNum")) {
				logger().debug("发送到服务端：" + data);
			}
			if(isEncrypt(command)) {
				data = DesUtil.encrypt(data, privateKey);
			}
			send(data);
			return true;
		} catch (Exception e) {
			logger().error("发送失败：" + JSON.toJSONString(command), e);
			return false;
		}
	}

	public boolean isEncrypt(Command command) {
		return true;
	}

	public void stop(){
		stop = true;
		super.close();
	}

	/**
	 * 连接成功触发，如果握手数据不合法可以抛出异常中断客户端连接
	 * @author DOE
	 * @param data
	 * @param shakeHandsData
	 * @return
	 */
	public abstract void onConnect(Object data);

	/**
	 * socket连接断开
	 * @author DOE
	 * @param surplusReconnectNum 剩余重连次数
	 */
	public abstract void onDisconnect(int surplusReconnectNum);

}
