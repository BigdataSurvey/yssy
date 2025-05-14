package com.zywl.app.manager.service;

import java.util.List;

import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.ConfigService;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerBTSocketServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Version;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.i18n.I18n;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.base.util.UID;
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.defaultx.cache.WsidCaCheService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.service.WSService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
 * APP获取socket地址服务
 * @author DOE
 *
 */
@Service
@ServiceClass(code = MessageCodeContext.AUTH)
public class AuthService extends BaseService {

	private static final Log logger = LogFactory.getLog(AuthService.class);
	
	
	
	@Autowired
	private ManagerSocketService managerSocketService;
	
	@Autowired
	private ManagerConfigService managerConfigService;
	
	@Autowired
	private VersionService versionService;
	
	@Autowired
	private WsidCaCheService wsidService;

	@Autowired
	private ConfigService configService;

	@ServiceMethod(code = "001", description = "领取成就奖励")
	public JSONObject receiveReward(ManagerBTSocketServer adminSocketServer, JSONObject data) {
		ManagerSocketServer goodServer = managerSocketService.getGoodServer(null);
		JSONObject result = new JSONObject();
		result.put("address",goodServer.getAddress());
		result.put("host",goodServer.getHost());
		return result;
	}

	@Autowired
	private WSService wsService;
	
	public static  Version version;
	
	public Version getVersion() {
		if (version == null) {
			List<Version> versions = versionService.getReleaseVersions(1);
			if(versions != null && !versions.isEmpty()){
				version = versions.get(0);	//当前最新版本
			}else{
				throwExp("版本维护中");
			}
		}
		return version;
	}
	
	public synchronized JSONObject checkVersionAndCreateWsid() {

		JSONObject result = new JSONObject();
		if (version == null) {
			List<Version> versions = versionService.getReleaseVersions(1);
			if(versions != null && !versions.isEmpty()){
				version = versions.get(0);	//当前最新版本
			}else{
				throwExp("版本维护中");
			}
		}
		JSONObject update = new JSONObject();
		result.put("server", update);
		update.put("downloadUrl", version.getDownloadUrl());
		update.put("v", version.getVersionName());
		update.put("qb", version.getUpdateUrl());
		update.put("aa",managerConfigService.getString(Config.IPHONE_V));
		
		PropertiesUtil pro = new PropertiesUtil("global.properties");
		result.put("loginUrl", pro.get("login.server"));
		return result;
	}
	 
	public WsidBean createWsid(Long userId,String oldWsid, String versionId) {
		WsidBean newWsidBean = null;
		String filterAddress = null;
		ManagerSocketServer managerSocket = managerSocketService.getGoodServer(filterAddress);
		if(managerSocket != null){
			newWsidBean = new WsidBean();
			newWsidBean.setWsid(UID.create());
			newWsidBean.setWsPrivateKey(UID.create());
			newWsidBean.setAuthServerAddress(managerSocket.getAddress());
			newWsidBean.setAuthServerHost(managerSocket.getHost());
			newWsidBean.setVersionId(versionId);
			newWsidBean.setUserId(userId);
			WsidCaCheService.changeWsidLocale(newWsidBean, "zh_Cn");
			wsService.removeByUserId(userId);
			wsService.addUserWs(newWsidBean);
			if(oldWsid != null){
				wsidService.removeWsid(oldWsid);
			}
			
			logger.info("创建WSID成功：" + JSON.toJSONString(newWsidBean));
		}
		return newWsidBean;
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
}
