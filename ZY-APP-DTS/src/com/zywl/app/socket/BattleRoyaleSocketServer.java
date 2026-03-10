package com.zywl.app.socket;

import java.math.BigDecimal;
import java.util.Set;

import javax.websocket.server.ServerEndpoint;

import com.live.app.ws.constant.CommandConstants;
import com.live.app.ws.util.Mass;
import com.zywl.app.base.bean.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.ConnectedData;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.config.HttpSessionConfigurator;
import com.live.app.ws.constant.SocketConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.constant.TableNameConstant;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.BatchCashRecordService;
import com.zywl.app.defaultx.service.BattleRoyaleRecordService;
import com.zywl.app.defaultx.service.LotterySyncCapitalService;
import com.zywl.app.defaultx.service.TaskOrderService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.BattleRoyaleRequsetMangerService;
import com.zywl.app.service.BattleRoyaleService;

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

		// 注册加入房间推送
		Push.registPush(new PushBean(PushCode.updateRoomDate), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				if (data != null) {
					BattleRoyaleSocketServer managerSocketServer = ((BattleRoyaleSocketServer) baseSocket);
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
		Push.registPush(new PushBean(PushCode.updateGameStatus), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				JSONObject json = (JSONObject) data;
				int gameId = json.getIntValue("gameId");
				if (gameId == 7) {
					BattleRoyaleService.STATUS = json.getIntValue("status");
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
					if (userId!=null && BattleRoyaleService.ROOM.getPlayers().containsKey(userId)) {
						logger.info("id：" + userId + "在大逃杀房间离线");
						//判断是否是观众席，观众席的话 移除，通知房间所有人
						if (BattleRoyaleService.ROOM.getLookList().containsKey(userId)) {
							BattleRoyaleService.ROOM.getLookList().remove(userId);
							Push.push(PushCode.updateRoomDate, null, BattleRoyaleService.ROOM.pushResult(2, userId, null, null));
							BattleRoyaleService.ROOM.getPlayers().remove(userId);
						}
						if (!BattleRoyaleService.ROOM.getUserBetInfo().containsKey(userId)) {
							BattleRoyaleService.ROOM.setLookNum(BattleRoyaleService.ROOM.getLookNum() - 1);
						}
					}
					/*
					if (BattleRoyaleService.ROOM.getPlayers().containsKey(userId)) {
						logger.info("id：" + userId + "在大逃杀房间离线，自动资金划转");
						//判断是否是观众席，观众席的话 移除，通知房间所有人
						if (BattleRoyaleService.ROOM.getLookList().containsKey(userId)) {
							BattleRoyaleService.ROOM.getLookList().remove(userId);
							Push.push(PushCode.updateRoomDate, null, BattleRoyaleService.ROOM.pushResult(2, userId, null, null));
							BattleRoyaleService.ROOM.getPlayers().remove(userId);
						}
						if (!BattleRoyaleService.ROOM.getUserBetInfo().containsKey(userId)) {
							BattleRoyaleService.ROOM.setLookNum(BattleRoyaleService.ROOM.getLookNum() - 1);
						}
						UserCapitalTypeEnum[] ems = UserCapitalTypeEnum.values();
						for (UserCapitalTypeEnum userCapitalTypeEnum : ems) {
							UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(Long.parseLong( userId),
									userCapitalTypeEnum.getValue());
							if (userCapital != null && userCapital.getBalance().compareTo(BigDecimal.ZERO) == 1) {
								JSONObject data2 = (JSONObject) data;
								data2.put("balance", userCapital.getBalance());
								data2.put("server", TargetSocketType.battleRoyale.toString());
								data2.put("type", 2);
								data2.put("capitalType", userCapitalTypeEnum.getValue());
								requestService.requestManagerCreateTask2(data2, new Listener() {
									public void handle(BaseClientSocket clientSocket, Command command) {
										if (command.isSuccess()) {
											// 订单创建成功
											JSONObject result1 = (JSONObject) command.getData();
											String orderNo = result1.getString("orderNo");
											BigDecimal balance = result1.getBigDecimal("balance");
											Long dataId = taskOrderService.addOrder(orderNo, "",
													TargetSocketType.battleRoyale.toString(), 2, Long.parseLong(userId), balance,
													userCapitalTypeEnum.getValue());
											// 减少余额并冻结
											userCapitalService.subUserBalanceAndAddOccupyBalance(
													userCapital.getBalance(), Long.parseLong(userId), userCapitalTypeEnum.getValue(),
													userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo,
													dataId, LogCapitalTypeEnum.lottery_to_forzen,
													TableNameConstant.TASK_ORDER);
											data2.put("orderNo", orderNo);
											data2.put("dataId", dataId);
											data2.put("amount", balance);
											requestService.requestManagerUpdateTask2(data2, new Listener() {
												public void handle(BaseClientSocket clientSocket, Command command) {
													if (command.isSuccess()) {
														// manger 订单完成 扣除冻结资产
														UserCapital userCapital2 = userCapitalService
																.findUserCapitalByUserIdAndCapitalType(Long.parseLong(userId),
																		userCapitalTypeEnum.getValue());
														userCapitalService.subUserOccupyBalance(balance, Long.parseLong(userId),
																userCapitalTypeEnum.getValue(),
																userCapital2.getBalance(),
																userCapital2.getOccupyBalance(), orderNo, dataId,
																LogCapitalTypeEnum.lottery_to,
																TableNameConstant.TASK_ORDER);
														taskOrderService.updateTaskOrder(1, "", orderNo);
													}
												}
											});
										}
									}
								});
							}
						}
					}*/
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
