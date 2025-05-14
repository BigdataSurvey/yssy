package com.zywl.app.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Version;
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.base.util.UID;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.WsidCaCheService;
import com.zywl.app.defaultx.service.ConfigService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.service.WSService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * APP获取socket地址服务
 * @author DOE
 *
 */
@Service
public class AuthService extends BaseService {

	private static final Log logger = LogFactory.getLog(AuthService.class);
	
	
	@Autowired
	private BTRequestMangerService loginRequestManagerService;

	@Autowired
	private LoginConfigService managerConfigService;
	
	@Autowired
	private VersionService versionService;
	
	@Autowired
	private WsidCaCheService wsidService;

	@Autowired
	private ConfigService configService;


	@Autowired
	private UserCacheService userCacheService;



	@Autowired
	private WSService wsService;
	


	public synchronized JSONObject checkVersionAndCreateWsid() {

		JSONObject result = new JSONObject();
		Version version1 = userCacheService.getVersion();
		JSONObject update = new JSONObject();
		result.put("server", update);
		update.put("downloadUrl", version1.getDownloadUrl());
		update.put("v", version1.getVersionName());
		update.put("qb", version1.getUpdateUrl());
		update.put("aa",managerConfigService.getString(Config.IPHONE_V));
		
		PropertiesUtil pro = new PropertiesUtil("global.properties");
		result.put("loginUrl", pro.get("login.server"));
		return result;
	}
	 
	public WsidBean createWsid(Long userId,String oldWsid, String versionId) {

		final WsidBean bean = new WsidBean();

		loginRequestManagerService.requestManagerGetGoodServer(new JSONObject(), new Listener() {
			public void handle(BaseClientSocket clientSocket, Command command) {
				WsidBean newWsidBean = null;
				if (command.isSuccess()) {
					JSONObject result = JSONObject.from(command.getData());
					if (result != null) {
						String filterAddress = null;
						bean.setWsid(UID.create());
						bean.setWsPrivateKey(UID.create());
						bean.setAuthServerAddress(result.getString("address"));
						bean.setAuthServerHost(result.getString("host"));
						bean.setVersionId(versionId);
						bean.setUserId(userId);
						WsidCaCheService.changeWsidLocale(bean, "zh_Cn");
						wsService.removeByUserId(userId);
						wsService.addUserWs(bean);
						if (oldWsid != null) {
							wsidService.removeWsid(oldWsid);
						}

						logger.info("创建WSID成功：" + JSON.toJSONString(bean));
					}
				} else {

				}
			}
		});
		return bean;
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
}
