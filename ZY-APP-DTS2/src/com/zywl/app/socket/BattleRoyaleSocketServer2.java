package com.zywl.app.socket;

import java.util.Set;

import javax.websocket.server.ServerEndpoint;

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
import com.zywl.app.service.BattleRoyaleService2;

@ServerEndpoint(value = "/BattleRoyale2Server"
		+ SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class BattleRoyaleSocketServer2 extends BaseServerSocket {
	private static final Log logger = LogFactory.getLog(BattleRoyaleSocketServer2.class);

	private String address;

	private String host;

	private String name;

	private double weight = 1; // 权重

	private PropertiesUtil staticProperties;

	private PropertiesUtil globalProperties;

	private BattleRoyaleService2 battleRoyaleService2;

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
		battleRoyaleService2 = SpringUtil.getService(BattleRoyaleService2.class);
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
		Push.registPush(new PushBean(PushCode.updateDts2Info), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				if (data != null) {
					BattleRoyaleSocketServer2 managerSocketServer = ((BattleRoyaleSocketServer2) baseSocket);
					JSONObject pushData = (JSONObject) data;
					long userId = pushData.getLongValue("userId");
					String userNo = pushData.getString("userNo");
					int group = pushData.getIntValue("group");
					String sessionId = pushData.getString("sessionId");
					logger.debug(
							"用户[" + userNo + "]加入" + managerSocketServer.getName() + "房间" + pushData.toJSONString());
				}
			}
		}, this);

		// 注册加入房间推送
		Push.registPush(new PushBean(PushCode.rollbackCapital), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
			}
		}, this);

		// 注册更新游戏状态推送
		Push.registPush(new PushBean(PushCode.updateDts2Status), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				JSONObject json = (JSONObject) data;
				int gameId = json.getIntValue("gameId");
				if (gameId == 1) {
					BattleRoyaleService2.STATUS = json.getIntValue("status");
				}
			}
		}, this);

		// 注册APP离线推送
		Push.registPush(new PushBean(PushCode.syncAppOffline), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				if (data != null) {
					JSONObject pushData = (JSONObject) data;
					String userId = pushData.getString("userId");
					if (userId!=null && BattleRoyaleService2.ROOM.getPlayers().containsKey(userId)) {
						logger.info("id：" + userId + "在神尊护体房间离线");
						//判断是否是观众席，观众席的话 移除，通知房间所有人
						if (BattleRoyaleService2.ROOM.getLookList().containsKey(userId)) {
							BattleRoyaleService2.ROOM.getLookList().remove(userId);
							Push.push(PushCode.updateDts2Info, null, BattleRoyaleService2.ROOM.pushResult(2, userId, null, null));
							BattleRoyaleService2.ROOM.getPlayers().remove(userId);
						}
						if (!BattleRoyaleService2.ROOM.getUserBetInfo().containsKey(userId)) {
							BattleRoyaleService2.ROOM.setLookNum(BattleRoyaleService2.ROOM.getLookNum() - 1);
						}
					}

				}
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
