package com.zywl.app.socket;

import java.util.Set;

import javax.websocket.server.ServerEndpoint;

import com.live.app.ws.util.DefaultPushHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.ConnectedData;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.config.HttpSessionConfigurator;
import com.live.app.ws.constant.SocketConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.Push;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.service.LotterySyncCapitalService;
import com.zywl.app.defaultx.service.TaskOrderService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.BattleRoyaleRequsetMangerService2;
//import com.zywl.app.service.BattleRoyaleService2;

@ServerEndpoint(value = "/BattleRoyale2Server" + SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class BattleRoyaleSocketServer2 extends BaseServerSocket {

	private static final Log logger = LogFactory.getLog(BattleRoyaleSocketServer2.class);

	private String address;

	private String host;

	private String name;

	private double weight = 1; // 权重
	/** 静态配置读取 */
	private PropertiesUtil staticProperties;
	/** 全局配置读取 */
	private PropertiesUtil globalProperties;

	//private BattleRoyaleService2 battleRoyaleService2;

	private LotterySyncCapitalService lotterySyncCapitalService;

	private BattleRoyaleRequsetMangerService2 requestService;

	private UserCapitalService userCapitalService;

	private TaskOrderService taskOrderService;

	public BattleRoyaleSocketServer2() {
		super(TargetSocketType.server, false, true);
		staticProperties = new PropertiesUtil("static.properties");
		globalProperties = new PropertiesUtil("global.properties");
		lotterySyncCapitalService = SpringUtil.getService(LotterySyncCapitalService.class);
		requestService = SpringUtil.getService(BattleRoyaleRequsetMangerService2.class);
		userCapitalService = SpringUtil.getService(UserCapitalService.class);
		taskOrderService = SpringUtil.getService(TaskOrderService.class);
		//battleRoyaleService2 = SpringUtil.getService(BattleRoyaleService2.class);
	}

	public ConnectedData onConnect(JSONObject shakeHandsData) {
		this.address = shakeHandsData.getString("address");
		this.name = shakeHandsData.getString("name");
		this.host = shakeHandsData.getString("host");
		this.weight = shakeHandsData.getDoubleValue("weight");
		initPush();
		JSONObject responseShakeHandsData = new JSONObject();
		responseShakeHandsData.put("staticWebUrl", staticProperties.get("base.webPath"));
		responseShakeHandsData.put("managerWebUrl", "http://" + globalProperties.get("host"));
		return new ConnectedData(address, responseShakeHandsData);
	}

	@Override
	protected void onDisconnect() {
	}

	protected String getPrivateKey(String pk) {
		return pk;
	}

	@Override
	public boolean isEncrypt(Command command) {
		return false;
	}

	@Override
	protected void filterCommand(Command command) {
	}

	protected Set<String> getWhiteList() {
		return null;
	}

	private void initPush() {
		// PBX：允许 PbxService 内 Push.push(updatePbxInfo/updatePbxStatus) 发到已连接的 SERVER
		Push.addPushSuport(PushCode.updatePbxInfo, new DefaultPushHandler());
		Push.addPushSuport(PushCode.updatePbxStatus, new DefaultPushHandler());

		// 服务器可用状态（通用）
		Push.registPush(new PushBean(PushCode.syncIsService), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
				if (data != null) {
					baseSocket.setService(Boolean.parseBoolean(data.toString()));
				}
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				if (data != null) {
					baseSocket.setService(Boolean.parseBoolean(data.toString()));
				}
			}
		}, this);
	}


	public String getName() {
		return name;
	}

	public String getHost() {
		return host;
	}

	public String getAddress() {
		return address;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	protected Log logger() {
		return logger;
	}

}
