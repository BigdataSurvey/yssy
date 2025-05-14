package com.live.app.ws.interfacex;

import com.live.app.ws.bean.PushBean;
import com.live.app.ws.socket.BaseSocket;
import com.zywl.app.base.exp.AppException;

/**
 * 推送注册处理类处理类
 * @author DOE
 *
 */
public interface PushHandler {
	
	/**
	 * 收到客户端注册推送时触发
	 * @author DOE
	 * @param shakeHands
	 */
	public void onRegist(BaseSocket baseSocket, PushBean pushBean);

	/**
	 * 客户端取消注册后触发
	 * @author DOE
	 * @param baseSocket
	 * @param clientCondition
	 */
	public void onUnregist(BaseSocket baseSocket, String clientCondition);
	
	/**
	 * 检查是否需要推送，如果抛出异常则直接移除客户端注册的推送
	 * @author DOE
	 * @param baseSocket
	 * @param condition
	 * @param clientCondition
	 * @param pushData
	 * @return
	 */
	public boolean checkedPush(BaseSocket baseSocket, String condition, String clientCondition, Object pushData) throws AppException;
	
}
