package com.zywl.app.server.service;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Version;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.server.socket.AppSocket;

@Service
public class UpdateAppService extends BaseService {

	private static final Log logger = LogFactory.getLog(UpdateAppService.class);

	@Autowired
	private VersionService versionService;

	@Autowired
	private ServerConfigService serverConfigService;

	public void pushVersionUpdate(Version version) {
		Map<String, AppSocket> clients = SocketManager.getClients(TargetSocketType.app);

		JSONObject update = (JSONObject) JSON.toJSON(version);

		for (AppSocket appSocket : clients.values()) {
			WsidBean wsidBean = appSocket.getWsidBean();
			if (wsidBean != null && appSocket.getDevice() != null
					&& appSocket.getDevice().getDeviceType() == version.getType()) {
				Version appVersion = versionService.findVersionById(wsidBean.getVersionId());
				if (appVersion == null || (appVersion.getVersionNo() < version.getVersionNo())) {
					Push.push(appSocket, CommandBuilder.builder().push(PushCode.updateVersion, update).build());
				}
			}
		}
	}

	public void pushTableVersionUpdate(JSONObject obj) {
		Map<String, AppSocket> clients = SocketManager.getClients(TargetSocketType.app);
		for (AppSocket appSocket : clients.values()) {
			WsidBean wsidBean = appSocket.getWsidBean();
			if (wsidBean != null && wsidBean.getUserId() != null) {
				Push.push(appSocket, CommandBuilder.builder().push(PushCode.updateTableVersion, obj).build());
			}
		}
	}



	//推送至客户端，让客户端知道有新的红包发出
	public void pushRedPackageInfo(JSONObject obj) {
		Map<String, AppSocket> clients = SocketManager.getClients(TargetSocketType.app);
		for (AppSocket appSocket : clients.values()) {
			WsidBean wsidBean = appSocket.getWsidBean();
			if (wsidBean != null && wsidBean.getUserId() != null) {
				Push.push(appSocket, CommandBuilder.builder().push(PushCode.redPackageInfo, obj).build());
			}
		}
	}

	@Override
	protected Log logger() {
		return logger;
	}
}
