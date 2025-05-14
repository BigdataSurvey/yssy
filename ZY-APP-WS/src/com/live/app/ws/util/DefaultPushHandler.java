package com.live.app.ws.util;

import com.live.app.ws.bean.PushBean;
import com.live.app.ws.interfacex.PushHandler;
import com.live.app.ws.socket.BaseSocket;
import com.zywl.app.base.Base;

/**
 * 默认的PushHandler实现
 * @author DOE
 *
 */
public class DefaultPushHandler implements PushHandler {

	public void onRegist(BaseSocket baseSocket, PushBean pushBean) {}

	public void onUnregist(BaseSocket baseSocket, String clientCondition) {}
	
	public boolean checkedPush(BaseSocket baseSocket, String condition, String clientCondition, Object pushData) {
		return Base.eq(clientCondition, condition == null ? "" : condition);
	}
}
