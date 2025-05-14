package com.live.app.ws.util;

import com.alibaba.fastjson2.JSON;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.constant.CommandConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.PushHandler;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.socket.BaseSocket;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.util.AppDefaultThreadFactory;
import com.zywl.app.base.util.PropertiesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 推送服务
 * @author DOE
 *
 */
public class Push {

	private static final Log logger = LogFactory.getLog(Push.class);
	
	private static ExecutorService pushExecutor;
	
	//服务器支持的推送列表
	private static Map<PushCode, PushHandler> pushSuportPool = new ConcurrentHashMap<PushCode, PushHandler>();
	
	//客户端接收任务集合
	private static Map<PushCode, Map<BaseSocket, PushListener>> pushListenerItem = new ConcurrentHashMap<PushCode, Map<BaseSocket,PushListener>>();
	
	//服务端推送任务集合
	private static Map<PushCode, Map<BaseSocket, String>> pushTaskItems = new ConcurrentHashMap<PushCode, Map<BaseSocket,String>>();
	
	static {
		PropertiesUtil propertiesUtil = new PropertiesUtil("thread.properties");
		pushExecutor = Executors.newFixedThreadPool(propertiesUtil.getInteger("thread.push.pool"), new AppDefaultThreadFactory("DoPush"));
		for (PushCode pushCode : PushCode.values()) {
			pushTaskItems.put(pushCode, new ConcurrentHashMap<BaseSocket, String>());
		}
	}

	/**
	 * 服务端添加一个支持的推送任务
	 * @author DOE
	 * @param pushCode
	 * @param handle
	 */
	public static void addPushSuport(PushCode pushCode, PushHandler handle){
		pushSuportPool.put(pushCode, handle);
	}
	
	/**
	 * 注册推送
	 * @author DOE
	 * @param pushBean
	 * @param listener
	 * @param socket 需要通过那个socket通道去发送注册指令
	 */
	public static void registPush(PushBean pushBean, PushListener listener, BaseSocket socket){
		logger.info("注册推送：" + JSON.toJSONString(pushBean));
		PushCode pushCode = pushBean.getPushCode();
		
		Map<BaseSocket, PushListener> listenerPool = pushListenerItem.get(pushCode);
		if(listenerPool == null){
			listenerPool = new ConcurrentHashMap<BaseSocket, PushListener>();
			pushListenerItem.put(pushCode, listenerPool);
		}
		listenerPool.put(socket, listener);

		Command command = new Command();
		command.setCode(CommandConstants.CMD_PUSH_REGIST);
		command.setData(pushBean);
		if(socket instanceof BaseServerSocket){
			BaseServerSocket serverSocket = (BaseServerSocket)socket;
			serverSocket.sendCommand(command);
		}else if(socket instanceof BaseClientSocket){
			BaseClientSocket baseClientSocket = (BaseClientSocket)socket;
			baseClientSocket.sendCommand(command);
		}
	}
	
	/**
	 * 取消注册推送
	 * @author DOE
	 * @param pushBean
	 */
	public static void unregistPush(PushBean pushBean){
		PushCode pushCode = pushBean.getPushCode();
		Map<BaseSocket, PushListener> listenerPool = pushListenerItem.get(pushCode);
		if(listenerPool != null && !listenerPool.isEmpty()){
			for(BaseSocket socket : listenerPool.keySet()){
				
				Command command = new Command();
				command.setCode(CommandConstants.CMD_PUSH_UNREGIST);
				command.setData(pushBean);
				if(socket instanceof BaseServerSocket){
					BaseServerSocket serverSocket = (BaseServerSocket)socket;
					serverSocket.sendCommand(command);
				}else if(socket instanceof BaseClientSocket){
					BaseClientSocket baseClientSocket = (BaseClientSocket)socket;
					baseClientSocket.sendCommand(command);
				}
				listenerPool.remove(socket);
			}
		}
	}
	
	/**
	 * 处理客户端注册推送指令
	 * @author DOE
	 * @param baseSocket
	 * @param pushBean 指定推送条件（一个PushCode只允许存在一个推送条件，暂不对多条件做支持）
	 */
	public static void doAddPush(BaseSocket baseSocket, PushBean pushBean){
		if(pushBean != null && pushBean.getPushCode() != null){
			PushCode pushCode = pushBean.getPushCode();
			PushHandler pushHandle = pushSuportPool.get(pushCode);
			if(pushHandle != null){
				try {
					
					pushHandle.onRegist(baseSocket, pushBean);
					Command response = new Command();
					response.setCode(CommandConstants.CMD_PUSH_REGIST_RESULT);
					response.setSuccess(true);
					response.setData(pushBean);
					getPushTask(pushCode).put(baseSocket, pushBean.getCondition() == null ? "" : pushBean.getCondition());
					if (baseSocket.getSocketType().equals(TargetSocketType.app)) {
						return;
					}
					if(baseSocket instanceof BaseServerSocket){
						((BaseServerSocket)baseSocket).sendCommand(response);
					}else{
						((BaseClientSocket)baseSocket).sendCommand(response);
					}
					
				}catch(AppException e){
					logger.warn("拒绝注册" + pushCode + "推送，原因：" + e);
				}catch (Exception e) {
					logger.error("注册推送异常：" + e, e);
				}
			}else{
				logger.warn("服务器未提供该推送服务" + pushCode);
			}
		}else{
			logger.error("未知的注册推送请求");
		}
	}

	/**
	 * 处理客户端移除推送指令
	 * @author DOE
	 * @param baseSocket
	 * @param pushBean
	 */
	public static void doRemovePush(BaseSocket baseSocket, PushBean pushBean) {
		removePushTask(baseSocket, pushBean.getPushCode(), pushBean.getCondition());
	}
	
	/**
	 * 注册推送成功触发
	 * @author DOE
	 * @param baseSocket
	 * @param pushBean
	 */
	public static void onRegistSuccess(BaseSocket baseSocket, PushBean pushBean) {
		Map<BaseSocket, PushListener> listenerPool = pushListenerItem.get(pushBean.getPushCode());
		if(listenerPool != null && listenerPool.containsKey(baseSocket)){
			pushExecutor.execute(new Runnable() {
				public void run() {
					try {
						listenerPool.get(baseSocket).onRegist(baseSocket, pushBean.getShakeHands());
					} catch (Exception e) {
						logger.error("处理推送握手数据异常：" + e, e);
					}
					
				}
			});
		}
	}
	
	/**
	 * 收到推送信息后触发
	 * @author DOE
	 * @param baseSocket
	 * @param pushCode
	 * @param data
	 */
	public synchronized static void onPush(final BaseSocket baseSocket, PushCode pushCode, final Object data){
		Map<BaseSocket, PushListener> listenerPool = pushListenerItem.get(pushCode);
		if(listenerPool != null && listenerPool.containsKey(baseSocket)){
			final PushListener pushListener = listenerPool.get(baseSocket);
			pushExecutor.execute(new Runnable() {
				public void run() {
					try {
						pushListener.onReceive(baseSocket, data);
					}catch (Exception e) {
						logger.error("处理推送数据异常：" + e, e);
					}
				}
			});
		}
	}
	
	/**
	 * 推送
	 * @author DOE
	 * @param pushCode
	 * @param condition
	 * @param data
	 */
	public static void push(PushCode pushCode, String condition, Object data){
		push(null, pushCode, condition, data);
	}
	
	/**
	 * 推送
	 * @author DOE
	 * @param pushCode
	 * @param condition
	 * @param data
	 */
	public static void push(String commandId, PushCode pushCode, String condition, Object data){
		PushHandler pushHandler = pushSuportPool.get(pushCode);
		if (pushHandler != null) {
			Map<BaseSocket, String> tasks = getPushTask(pushCode);
			if (!tasks.isEmpty()) {
				Command command = CommandBuilder.builder(commandId).push(pushCode, data).build();
				for (BaseSocket baseSocket : tasks.keySet()) {
					String clientCondition = tasks.get(baseSocket);
					try {
						if (pushHandler.checkedPush(baseSocket, condition, clientCondition, data)) {
							//command.setCondition(clientCondition);
							command.setRequestTime(null);
							push(baseSocket, command);
						}
					} catch (AppException e) {
						logger.warn("移除" + pushCode + " 推送，原因：" + e);
						doRemovePush(baseSocket, new PushBean(pushCode));
					} catch (Exception e) {
						logger.error("移除" + pushCode + " 推送，未知原因：" + e, e);
						doRemovePush(baseSocket, new PushBean(pushCode));
					}
				}
			}
		}
	}
	
	public synchronized static void push(final BaseSocket baseSocket, final Command command){
		pushExecutor.execute(new Runnable() {
			public void run() {
				if(baseSocket instanceof BaseClientSocket){
					((BaseClientSocket)baseSocket).sendCommand(command);
				}else{
					((BaseServerSocket)baseSocket).sendCommand(command);
				}
			}
		});
	}

	public static Map<BaseSocket, String> getPushTask(PushCode pushCode){
		return pushTaskItems.get(pushCode);
	}
	
	public static void removeAll(final BaseSocket baseSocket){
		for (PushCode pushCode : pushListenerItem.keySet()) {
			pushListenerItem.get(pushCode).remove(baseSocket);
		}
		for (PushCode pushCode : PushCode.values()) {
			removePushTask(baseSocket, pushCode, null);
		}
	}
	
	public static void removePushTask(BaseSocket baseSocket, PushCode pushCode, String condition){
		Map<BaseSocket, String> pushTasks = getPushTask(pushCode);
		if(pushTasks.containsKey(baseSocket)){
			if(condition == null || condition.equals(pushTasks.get(baseSocket))){
				String clientCondition = pushTasks.remove(baseSocket);
				PushHandler pushHandler = pushSuportPool.get(pushCode);
				if(pushHandler != null){
					pushHandler.onUnregist(baseSocket, clientCondition);
				}
			}
		}
	}
}
