package com.zywl.app.socket;
import com.alibaba.fastjson2.JSONArray;
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
import com.zywl.app.service.BattleRoyaleRequsetMangerService;
import com.zywl.app.service.BattleRoyaleService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.server.ServerEndpoint;
import java.util.Set;

@ServerEndpoint(value = "/BattleRoyaleServer"
		+ SocketConstants.SOCKET_CONNECT_SHAKE_HANDS, configurator = HttpSessionConfigurator.class)
public class BattleRoyaleSocketServer extends BaseServerSocket {
	private static final Log logger = LogFactory.getLog(BattleRoyaleSocketServer.class);

	private String address;

	private String host;

	private String name;

	private double weight = 1; // 权重

	private PropertiesUtil staticProperties;
	private PropertiesUtil globalProperties;

	private BattleRoyaleService battleRoyaleService;
	private LotterySyncCapitalService lotterySyncCapitalService;
	private BattleRoyaleRequsetMangerService requestService;
	private UserCapitalService userCapitalService;
	private TaskOrderService taskOrderService;

	public BattleRoyaleSocketServer() {
		super(TargetSocketType.server, false, true);
		staticProperties = new PropertiesUtil("static.properties");
		globalProperties = new PropertiesUtil("global.properties");
		lotterySyncCapitalService = SpringUtil.getService(LotterySyncCapitalService.class);
		requestService = SpringUtil.getService(BattleRoyaleRequsetMangerService.class);
		userCapitalService = SpringUtil.getService(UserCapitalService.class);
		taskOrderService = SpringUtil.getService(TaskOrderService.class);
		battleRoyaleService = SpringUtil.getService(BattleRoyaleService.class);
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

		// 收到后需要继续转推给 SERVER
		Push.registPush(new PushBean(PushCode.updateGameDiyData), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
				logger.debug("BattleRoyaleSocketServer.initPush.updateGameDiyData.onRegist");
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				logger.debug("BattleRoyaleSocketServer.initPush.updateGameDiyData.onReceive, data=" + data);

				// 如果 payload 带 gameId，且不是本服(7)就忽略
				try {
					if (data instanceof JSONObject) {
						int gameId = ((JSONObject) data).getIntValue("gameId");
						if (gameId != 0 && gameId != 7) {
							return;
						}
					} else if (data instanceof JSONArray) {
						boolean match = false;
						for (Object o : (JSONArray) data) {
							if (o instanceof JSONObject) {
								int gameId = ((JSONObject) o).getIntValue("gameId");
								if (gameId == 7) {
									match = true;
									break;
								}
							}
						}
						if (!match) {
							return;
						}
					}
				} catch (Exception e) {
					logger.error("updateGameDiyData 解析异常 data=" + data, e);
				}

				//condition 必须为空
				Push.push(PushCode.updateGameDiyData, null, data);
			}
		}, this);

		// 房间变更推送 收到后必须继续转推给 SERVER
		Push.registPush(new PushBean(PushCode.updateRoomDate), new PushListener() {
			@Override
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			@Override
			public void onReceive(BaseSocket baseSocket, Object data) {
				if (data == null) {
					return;
				}

				// 只处理本服
				try {
					if (data instanceof JSONObject) {
						int gameId = ((JSONObject) data).getIntValue("gameId");
						if (gameId != 0 && gameId != 7) {
							return;
						}
					} else if (data instanceof JSONArray) {
						boolean match = false;
						for (Object o : (JSONArray) data) {
							if (o instanceof JSONObject) {
								int gameId = ((JSONObject) o).getIntValue("gameId");
								if (gameId == 7) {
									match = true;
									break;
								}
							}
						}
						if (!match) {
							return;
						}
					}
				} catch (Exception e) {
					logger.error("updateRoomDate 解析异常 data=" + data, e);
				}

				// 打日志
				try {
					if (data instanceof JSONObject) {
						BattleRoyaleSocketServer s = (BattleRoyaleSocketServer) baseSocket;
						JSONObject pushData = (JSONObject) data;
						String userNo = pushData.getString("userNo");
						logger.debug("用户[" + userNo + "]加入" + s.getName() + "房间 " + pushData.toJSONString());
					}
				} catch (Exception ignore) {}

				// condition 为空，否则 SERVER 侧 Push 条件匹配不上
				Push.push(PushCode.updateRoomDate, null, data);
			}
		}, this);

		// 回滚资产
		Push.registPush(new PushBean(PushCode.rollbackCapital), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
			}
		}, this);

		// 状态变更推送收到后，必须转推给 SERVER
		Push.registPush(new PushBean(PushCode.updateGameStatus), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				JSONObject json = (JSONObject) data;
				int gameId = json.getIntValue("gameId");
				if (gameId == 7) {
					BattleRoyaleService.STATUS = json.getIntValue("status");

					// 把状态变更转推给 SERVER -- SERVER 再推给玩家
					Push.push(PushCode.updateGameStatus, null, json);
				}
			}
		}, this);

		// APP 离线推送
		Push.registPush(new PushBean(PushCode.syncAppOffline), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				if (data != null) {
					JSONObject pushData = (JSONObject) data;
					String userId = pushData.getString("userId");
					if (userId != null && BattleRoyaleService.ROOM != null) {
						if (BattleRoyaleService.ROOM.getPlayers().containsKey(userId)) {
							BattleRoyaleService.ROOM.getPlayers().remove(userId);
						}
						if (!BattleRoyaleService.ROOM.getUserBetInfo().containsKey(userId)) {
							BattleRoyaleService.ROOM.setLookNum(BattleRoyaleService.ROOM.getLookNum() - 1);
						}
					}
				}
			}
		}, this);

		// 服务器可用状态
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
