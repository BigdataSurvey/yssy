package com.live.app.ws.interfacex;

import com.live.app.ws.bean.Command;
import com.live.app.ws.socket.BaseServerSocket;
import com.zywl.app.base.exp.AppException;

/**
 * 控制器，请求任务分发器
 * @author DOE
 *
 */
public interface ServiceController {

	public Object exec(BaseServerSocket serverSocket, Command command) throws Exception, AppException;
}
