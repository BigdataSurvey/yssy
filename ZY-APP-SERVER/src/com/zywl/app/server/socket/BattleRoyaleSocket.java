package com.zywl.app.server.socket;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LotteryGameStatusEnum;
import com.zywl.app.defaultx.service.IncomeRecordService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.ServerConfigService;
import com.zywl.app.server.service.ServerStateService;
import com.zywl.app.server.service.TemplateLoadService;
import com.zywl.app.server.service.UpdateAppService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.ClientEndpoint;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class BattleRoyaleSocket extends BaseClientSocket {
	private static final Log logger = LogFactory.getLog(BattleRoyaleSocket.class);

	private VersionService versionService;

	private UpdateAppService updateAppService;

	private IncomeRecordService incomeRecordService;

	private ServerConfigService serverConfigService;

	private UserCapitalService userCapitalService;

	private UserCapitalCacheService userCapitalCacheService;


	public BattleRoyaleSocket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
		super(socketType, false, reconnect, server, shakeHandsDatas);
		versionService = SpringUtil.getService(VersionService.class);
		updateAppService = SpringUtil.getService(UpdateAppService.class);
		serverConfigService = SpringUtil.getService(ServerConfigService.class);
		incomeRecordService = SpringUtil.getService(IncomeRecordService.class);
		userCapitalService = SpringUtil.getService(UserCapitalService.class);
		userCapitalCacheService= SpringUtil.getService(UserCapitalCacheService.class);

		Push.addPushSuport(PushCode.syncTaskNum, new DefaultPushHandler() {
			public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
				pushBean.setShakeHands(Executer.size() + "," + Executer.QPS());
			}
		});
		Push.addPushSuport(PushCode.syncIsService, new DefaultPushHandler() {
			public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
				pushBean.setShakeHands(ServerStateService.isService());
			}
		});

		Push.addPushSuport(PushCode.updateRoomDate, new DefaultPushHandler());
		Push.addPushSuport(PushCode.updateGameDiyData, new DefaultPushHandler());
		Push.addPushSuport(PushCode.updateGameStatus, new DefaultPushHandler());
	}

	@Override
	public void onConnect(Object data) {
		CountDownLatch downLatch = new CountDownLatch(2);
		Push.registPush(new PushBean(PushCode.rollbackCapital), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
			}
			public void onReceive(BaseSocket baseSocket, Object data) {
			}
		}, this);

		Push.registPush(new PushBean(PushCode.updateGameDiyData), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {}
			public void onReceive(BaseSocket baseSocket, Object data) {
				logger.info("大逃杀diy数据" + data);
				JSONObject object = JSONObject.from(data);
				Push.push(PushCode.updateGameDiyData, "7", object);

			}
		}, this);

		Push.registPush(new PushBean(PushCode.updateRoomDate), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
				downLatch.countDown();
			}
			public void onReceive(BaseSocket baseSocket, Object data) {
				// logger.info("收到大逃杀房间信息变更" + data);
				JSONArray array = JSONArray.from(data);
				for (Object o : array) {
					JSONObject obj = JSONObject.from(o);
					String gameId = obj.getString("gameId");
					if ("7".equals(gameId)) {
						Push.push(PushCode.updateRoomDate, gameId, obj);
					}
				}


			}
		}, this);

		Push.registPush(new PushBean(PushCode.updateGameStatus), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {
				downLatch.countDown();
			}
			public void onReceive(BaseSocket baseSocket, Object data) {
				// logger.info("大逃杀游戏状态变更" + data);
				JSONObject obj = JSONObject.from(data);
				String gameId = obj.getString("gameId");
				JSONArray ids = obj.getJSONArray("userIds");

				if (!"7".equals(gameId) || ids == null || ids.isEmpty()) {
					return;
				}

				int status = obj.getIntValue("status");

				for (Object id : ids) {
					String userId = String.valueOf(id);
					JSONObject result = new JSONObject();

					String myRoomId = enrichRoomListAndGetMyRoomId(obj, userId);

					BattleRoyale2Socket.dtsPublic(obj, result);

					// 每个推送包已是单播（userIds=singleton），顶层字段由 buildUserSettlePayload 填入
					String perRoundWinAmount = "0";
					String perRoundBetAmount = "0";
					if (LotteryGameStatusEnum.settle.getValue() == status) {
						perRoundWinAmount = obj.getString("winAmount");
						perRoundBetAmount = obj.getString("betAmount");
						String isWinStr = obj.getString("isWin");

						if (perRoundWinAmount == null) perRoundWinAmount = "0";
						if (perRoundBetAmount == null) perRoundBetAmount = "0";

						result.put("winAmount", perRoundWinAmount);
						result.put("betAmount", perRoundBetAmount);
						result.put("roomResult", isWinStr != null ? Integer.parseInt(isWinStr) : 2);
					}

					result.put("allLoseAmount", obj.get("allLoseAmount"));

					// 统一开奖结果
					JSONArray roomIds = new JSONArray();
					Object ridObj = obj.get("roomIds");

					if (ridObj instanceof JSONArray) {
						roomIds = (JSONArray) ridObj;
					} else if (ridObj instanceof java.util.List) {
						roomIds = JSONArray.from(ridObj);
					} else if (ridObj != null && String.valueOf(ridObj).trim().length() > 0) {
						roomIds.add(String.valueOf(ridObj));
					} else if (obj.get("roomId") != null && String.valueOf(obj.get("roomId")).trim().length() > 0) {
						roomIds.add(String.valueOf(obj.get("roomId")));
					}

					result.put("roomIds", roomIds);

					String finalRoomId = "";
					if (roomIds.size() > 0) {
						finalRoomId = roomIds.getString(0);
					} else if (myRoomId != null && myRoomId.length() > 0) {
						finalRoomId = myRoomId;
					}

					if (finalRoomId != null && finalRoomId.length() > 0) {
						result.put("roomId", finalRoomId);
					} else {
						result.remove("roomId");
					}

					result.put("status", status);
					result.put("userId", userId);

					mergeUnifiedSummary(result, obj, userId);

					// 结算状态下，totalGain/totalInvest 必须用当局值覆盖历史累计值
					// 避免前端弹窗"获得"显示历史总额而非当局获得
					if (LotteryGameStatusEnum.settle.getValue() == status) {
						result.put("totalGain", perRoundWinAmount == null ? "0" : perRoundWinAmount);
						result.put("totalInvest", perRoundBetAmount == null ? "0" : perRoundBetAmount);
					}

					Push.push(PushCode.updateGameStatus, userId, result);
				}
			}

		}, this);

		JSONObject connectedData = ((JSONObject)data).getJSONObject("responseShakeHandsData");
		if(connectedData != null){
			TemplateLoadService.staticWebUrl = connectedData.getString("staticWebUrl");
			TemplateLoadService.managerWebUrl = connectedData.getString("managerWebUrl");
		}
		new Thread("同步握手数据监测") {
			public void run() {
				try {
					long t1 = System.currentTimeMillis();
					downLatch.await();
					logger.info("握手数据初始化完毕[" + (System.currentTimeMillis() - t1) + "ms]");
					ServerStateService.startService();
				} catch (Exception e) {
					logger.error("同步握手数据异常：" + e, e);
				}
			};
		}.start();
	}

	@Override
	public void onDisconnect(int surplusReconnectNum) {
		logger.debug("剩余重连次数：" + surplusReconnectNum);
		//ServerStateService.stopService();
		//ServerNoticeService.setOpenNotice(false);
	}
	@Override
	public boolean isEncrypt(Command command) {
		return false;
	}
	@Override
	protected Log logger() {
		return logger;
	}


	/**
	 * 补齐 roomList 内玩家信息（roomId/userId），并反查当前用户所在 roomId
	 * { "0": { "937228": {betAmount:10}, ... }, "1": {...} }
	 */
	@SuppressWarnings({"unchecked"})
	private static String enrichRoomListAndGetMyRoomId(JSONObject obj, String userId) {
		String myRoomId = "";
		try {
			Object roomListObj = obj.get("roomList");
			if (roomListObj == null) {
				return myRoomId;
			}

			// roomList 已是 JSONObject（最常见）
			if (roomListObj instanceof JSONObject) {
				JSONObject rl = (JSONObject) roomListObj;
				for (String rid : rl.keySet()) {
					Object usersObj = rl.get(rid);
					if (usersObj == null) {
						continue;
					}

					JSONObject users;
					if (usersObj instanceof JSONObject) {
						users = (JSONObject) usersObj;
					} else {
						users = JSONObject.from(usersObj);
						rl.put(rid, users);
					}

					for (String uid : users.keySet()) {
						Object infoObj = users.get(uid);
						JSONObject info;
						if (infoObj instanceof JSONObject) {
							info = (JSONObject) infoObj;
						} else if (infoObj instanceof java.util.Map) {
							info = JSONObject.from(infoObj);
							users.put(uid, info);
						} else if (infoObj == null) {
							info = new JSONObject();
							users.put(uid, info);
						} else {
							info = JSONObject.from(infoObj);
							users.put(uid, info);
						}

						if (!info.containsKey("userId")) {
							info.put("userId", uid);
						}
						if (!info.containsKey("roomId")) {
							info.put("roomId", rid);
						}

						if (uid != null && uid.equals(userId)) {
							myRoomId = rid;
						}
					}
				}
				return myRoomId;
			}

			// roomList 是 Map（某些 fastjson 反序列化场景）
			if (roomListObj instanceof java.util.Map) {
				java.util.Map<Object, Object> rl = (java.util.Map<Object, Object>) roomListObj;
				for (Object ridKey : rl.keySet()) {
					String rid = String.valueOf(ridKey);
					Object usersObj = rl.get(ridKey);
					if (!(usersObj instanceof java.util.Map)) {
						continue;
					}

					java.util.Map<Object, Object> users = (java.util.Map<Object, Object>) usersObj;
					for (Object uidKey : users.keySet()) {
						String uid = String.valueOf(uidKey);
						Object infoObj = users.get(uidKey);
						if (infoObj instanceof java.util.Map) {
							java.util.Map<Object, Object> info = (java.util.Map<Object, Object>) infoObj;
							if (!info.containsKey("userId")) {
								info.put("userId", uid);
							}
							if (!info.containsKey("roomId")) {
								info.put("roomId", rid);
							}
						}
						if (uid.equals(userId)) {
							myRoomId = rid;
						}
					}
				}
				return myRoomId;
			}

		} catch (Exception ignore) {
		}
		return myRoomId;
	}

	/**
	 * 合并统一摘要字段到推送结果：
	 */
	private static void mergeUnifiedSummary(JSONObject result, JSONObject obj, String userId) {
		if (result == null || obj == null) {
			return;
		}

		JSONObject summary = null;

		try {
			Object m = obj.get("userRecordSummaryMap");
			if (m instanceof JSONObject) {
				JSONObject map = (JSONObject) m;
				Object s = map.get(userId);
				if (s instanceof JSONObject) {
					summary = (JSONObject) s;
				} else if (s != null) {
					summary = JSONObject.from(s);
				}
			} else if (m instanceof java.util.Map) {
				java.util.Map<Object, Object> map = (java.util.Map<Object, Object>) m;
				Object s = map.get(userId);
				if (s instanceof JSONObject) {
					summary = (JSONObject) s;
				} else if (s != null) {
					summary = JSONObject.from(s);
				}
			}
		} catch (Exception ignore) {
		}

		if (summary == null) {
			return;
		}

		if (summary.containsKey("recent16Summary")) {
			result.put("recent16Summary", summary.get("recent16Summary"));
		}
		if (summary.containsKey("recent100Periods")) {
			result.put("recent100Periods", summary.get("recent100Periods"));
		}
		if (summary.containsKey("totalInvest")) {
			result.put("totalInvest", summary.get("totalInvest"));
		}
		if (summary.containsKey("totalGain")) {
			result.put("totalGain", summary.get("totalGain"));
		}
		if (summary.containsKey("serverTime")) {
			result.put("serverTime", summary.get("serverTime"));
		}
	}
}
