package com.zywl.app.socket;

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
import com.zywl.app.service.DnsService;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.service.LotterySyncCapitalService;
import com.zywl.app.defaultx.service.TaskOrderService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.util.SpringUtil;
import okhttp3.Dns;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.server.ServerEndpoint;
import java.util.Set;

@ServerEndpoint(value = "/NSServer"
		+ SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class NSSocketServer extends BaseServerSocket {
	private static final Log logger = LogFactory.getLog(NSSocketServer.class);

	private String address;

	private String host;

	private String name;

	private double weight = 1; // 权重

	private PropertiesUtil staticProperties;

	private PropertiesUtil globalProperties;

	private DnsService dnsService;

	private LotterySyncCapitalService lotterySyncCapitalService;


	private UserCapitalService userCapitalService;

	private TaskOrderService taskOrderService;

	public NSSocketServer() {
		super(TargetSocketType.server, false, true);
		staticProperties = new PropertiesUtil("static.properties");
		globalProperties = new PropertiesUtil("global.properties");
		lotterySyncCapitalService = SpringUtil.getService(LotterySyncCapitalService.class);
		userCapitalService = SpringUtil.getService(UserCapitalService.class);
		taskOrderService = SpringUtil.getService(TaskOrderService.class);
		dnsService = SpringUtil.getService(DnsService.class);

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

		// 注册加入房间推送
		Push.registPush(new PushBean(PushCode.updateDnsInfo), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
			}
		}, this);
		Push.registPush(new PushBean(PushCode.updateDnsStatus), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
			}
		}, this);

		// 注册更新游戏状态推送
		Push.registPush(new PushBean(PushCode.updateGameStatus), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}
			public void onReceive(BaseSocket baseSocket, Object data) {
			}
		}, this);

		// 注册服务器可用状态
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
