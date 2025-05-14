package com.zywl.app.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.bean.FoodGameConfig;
import com.zywl.app.base.bean.FoodGameRecord;
import com.zywl.app.base.bean.Game;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.FoodGameRecordService;
import com.zywl.app.defaultx.service.GameService;
import com.zywl.app.defaultx.service.LogUserCapitalService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.room.FoodRoom;
import com.zywl.app.room.RoomManager;
import com.zywl.app.socket.FoodSocketServer;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = "101")
public class GameFoodService extends BaseService {
	private  static ConcurrentHashMap<Integer,RoomManager> managers = new ConcurrentHashMap();
	private static ConcurrentHashMap<String,Integer> players = new ConcurrentHashMap();

	public static int STATUS;
	
	@Autowired
	private GameFoodService gameFoodService;
	
	@Autowired
	private GameService gameService;

	@Autowired
	private UserCapitalService userCapitalService;

	@Autowired
	private FoodRequestManagerService requestManagerService;

	@Autowired
	private LogUserCapitalService logUserCapitalService;

	@Autowired
	private FoodGameRecordService foodGameRecordService;

	@PostConstruct
	public void _Construct() {
		addPushSuport();
		init();
	}
	
	public void init() {
		logger.info("初始化游戏配置");
		Game game = gameService.findGameById(3L);
		if (game == null) {
			logger.info("初始化游戏配置失败");
			return;
		}
		String gameSetting = game.getGameSetting();
		STATUS = game.getStatus();

		//场次0
		FoodGameConfig cfg = JSONObject.parseObject(gameSetting, FoodGameConfig.class);
		RoomManager manager = new RoomManager();
		cfg.setCostSit(new BigDecimal("1"));
		manager.Init(0, cfg,this);
		managers.put(0,manager);

		//场次1
		FoodGameConfig cfg1 = new FoodGameConfig();
		BeanUtils.copy(cfg, cfg1);
		cfg1.setCostSit(new BigDecimal("5"));
		RoomManager manager1 = new RoomManager();
		manager1.Init(1, cfg1,this);
		managers.put(1,manager1);

		//场次2
		FoodGameConfig cfg2 = new FoodGameConfig();
		BeanUtils.copy(cfg, cfg2);
		cfg2.setCostSit(new BigDecimal("10"));
		RoomManager manager2 = new RoomManager();
		manager2.Init(2, cfg2,this);
		managers.put(2,manager2);

		logger.info("初始化完成");
	}
	
	public void addPushSuport() {
		Push.addPushSuport(PushCode.foodGameSitDown, new DefaultPushHandler());
		Push.addPushSuport(PushCode.foodGameStatus, new DefaultPushHandler());
		Push.addPushSuport(PushCode.foodGameAddFood, new DefaultPushHandler());
	}

	public void removePlayerCache(String userId) {
		if(userId == null || userId.isEmpty()) {
			return;
		}
		if(players.containsKey(userId)) {
			players.remove(userId);
		}
	}

	public void removePlayer(String userId) {
		if(userId == null || userId.isEmpty()) {
			return;
		}
		if(players.containsKey(userId)) {
			int roomId = players.get(userId);
			managers.get(roomId/1000).getRoomById(roomId).exitRoom(userId);
			return;
		}
		for (Map.Entry<Integer,RoomManager> entry : managers.entrySet()){
			entry.getValue().exitRoom(userId);
		}
	}

	public Log getLogger() {
		return logger();
	}

	public void noticeGameStatus(ArrayList<String> noticeList, Object data) {
		JSONObject pushInfo = new JSONObject();
		pushInfo.put("noticeList",noticeList);
		pushInfo.put("data",data);
		Push.push(PushCode.foodGameStatus, null, pushInfo);
	}

	public void costMoney(String userId, int action, int type, BigDecimal amount, Listener listener ) {
		JSONObject data = new JSONObject();
		JSONObject info = new JSONObject();
		info.put("amount", amount);
		info.put("capitalType", type);
		info.put("em", action);
		info.put("tableName", "r_food_game_record");
		data.put(userId, info);
		requestManagerService.requestManagerBet(data, listener);
	}

	public void foodGameSettle(int type,  JSONArray arr, Listener listener) {
		int em = LogCapitalTypeEnum.game_bet_win_food.getValue();
		JSONObject data = new JSONObject();
		for (Object o : arr) {
			JSONObject obj = JSONObject.from(o);
			JSONObject info = new JSONObject();
			info.put("amount", obj.getBigDecimal("winScore"));
			info.put("capitalType", type);
			info.put("em", em);
			info.put("tableName", "r_food_game_record");
			data.put(obj.getString("userId"), info);
		}
		requestManagerService.requestManagerSettle(data, listener);
	}

	public void returnMoney(int roomId, String userId, int action, int type, BigDecimal amount) {
		JSONObject data = new JSONObject();
		JSONObject info = new JSONObject();
		info.put("amount", amount);
		info.put("capitalType", type);
		info.put("em", action);
		info.put("tableName", "");
		data.put(userId, info);
		requestManagerService.requestManageReturnCapital(data, new Listener() {
			@Override
			public void handle(BaseClientSocket clientSocket, Command command) {
				if(!command.isSuccess()) {
					logger.error("退还失败 uid:" + userId + " roomid:" + roomId + " amount:" + amount);
				}
			}
		});
	}

	public void recordInfo(Long userId, Integer roomId, BigDecimal betAmount, BigDecimal winAmount, String cardInfo, int winValue, int winLose, String winInfo) {
		foodGameRecordService.addRecord(userId,roomId,betAmount,winAmount,cardInfo,winValue,winLose,winInfo);
	}

	
	@Transactional
	@ServiceMethod(code = "101", description = "加入")
	public Object joinRoom(FoodSocketServer adminSocketServer, Command cmd, JSONObject params) {
		if (STATUS == 0) {
			throwExp("游戏即将维护，暂时不能进行游戏！");
		}
		checkNull(params);
		checkNull(params.get("userId"),params.get("action"));
		String userId = params.getString("userId");
		String userNo = params.getString("userNo");
		String userName = params.getString("userName");
		String headUrl = params.getString("headImgUrl");
		String action = params.getString("action");
		int roomType = params.getIntValue("roomType",0);
		if(userId == null || userId.isEmpty()) {
			throwExp("参数错误！");
		}

		if( action.equals("quickStart") ) {
			//没有指定房间查找有座位的房间并加入
			FoodRoom oldRoom = null;
			if (players.containsKey(userId)) {
				int roomid = players.get(userId);
				RoomManager oldManager = managers.get(roomid / 1000);
				oldRoom = oldManager.getRoomById(roomid);
				if (oldRoom != null) {
					if (oldRoom.isJoinGame(userId)) {
						throwExp("正在喝酒！");
					}
				}
			}

			RoomManager manager = managers.get(roomType);
			int type = manager.getCfg().getCostType();
			UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(Long.parseLong(userId),
					type);
			if (userCapital == null) {
				throwExp("系统出错啦！");
			}

			BigDecimal costmoney = manager.getCfg().getCostSit();
			FoodRoom finalOldRoom = oldRoom;
			FoodRoom finalOldRoom1 = oldRoom;
			costMoney(userId, LogCapitalTypeEnum.game_bet_food.getValue(), type, costmoney.negate(), new Listener() {
				@Override
				public void handle(BaseClientSocket clientSocket, Command command) {
					if(command.isSuccess()) {
						boolean flag = false;
						FoodRoom room = null;
						for (int i = 0; i < manager.getMaxRoom(); i++) {
							room = manager.getRoomForJoin();
							if(room == null) {
								continue;
							}
							if(!room.isCanJoin()) {
								continue;
							}
							int cid = room.getFreeChairID();
							if( cid < 0) {
								continue;
							}

							room.onEnter(userId, userName, headUrl, userNo);

							Integer rst = room.onSitDown(userId, cid);
							if(rst < 0) {
								room.removeWatchPlayer(userId);
								continue;
							}
							if(finalOldRoom1 != null && finalOldRoom1.getRoomId() != room.getRoomId()) {
								//从旧房间移除玩家
								finalOldRoom1.exitRoom(userId);
								logger.debug(userId + " 离开 " + finalOldRoom1.getRoomId());
							}
							logger.debug(userId + " 进入 " + room.getRoomId());
							players.put(userId, room.getRoomId());
							//扣款成功判断是否开始游戏
							noticeSitDown(cmd, room, userId);
							room.checkStart();
							flag = true;
							break;
						}

						if(!flag) {
							returnMoney(room.getRoomId(), userId, LogCapitalTypeEnum.food_cancel_bet.getValue(), type, costmoney);
						}
					}else {
						Executer.response(CommandBuilder.builder(cmd).error("唉！没有票票了！").build());
					}
				}
			});
		} else if(action.equals("enterRoom")) {
			int roomId = -1;
			FoodRoom room = null;
			if(players.containsKey(userId)) {
				roomId = players.get(userId);
				room = managers.get(roomId / 1000).getRoomById(roomId);
			}else {
				room = managers.get(roomType).getRoomForEnter(userId);
			}

			room.onEnter(userId,userName,headUrl,userNo);
			players.put(userId, room.getRoomId());
			return room.getRoomInfo(userId);
		}
		return async();
	}

	private void noticeSitDown(Command cmd, FoodRoom room, String userId) {
		Executer.response(CommandBuilder.builder(cmd).success(room.getRoomInfo(userId)).build());

		JSONObject pushInfo = new JSONObject();
		pushInfo.put("noticeList",room.getPushUID(userId));
		pushInfo.put("data",room.getPlayerInfo(userId));
		Push.push(PushCode.foodGameSitDown, null, pushInfo);
	}

	@Transactional
	@ServiceMethod(code = "102", description = "加菜")
	public Object reqAddFood(FoodSocketServer adminSocketServer, Command cmd, JSONObject params) {
		checkNull(params);
		checkNull(params.get("roomId"),params.get("userId"),params.get("action"));
		int roomId = params.getIntValue("roomId",-1);
		String userId = params.getString("userId");
		String action = params.getString("action");
		if(roomId < 0) {
			throwExp("系统出错啦！");
		}
		int roomType = roomId / 1000;
		FoodRoom room = managers.get(roomType).getRoomById(roomId);
		if(room == null) {
			throwExp("系统出错啦！");
		}
		int rst = room.opFood(userId,action.equals("pass"));
		if(rst < 0) {
			if(rst == -2) {
				throwExp("酒已添满了！");
			}
			throwExp("添酒失败！");
		}

		JSONObject obj = new JSONObject();
		obj.put("userId",userId);
		obj.put("chairId",room.getChairIDByUid(userId));
		obj.put("action", action);
		JSONObject pushInfo = new JSONObject();
		pushInfo.put("noticeList",room.getPushUID(userId));
		pushInfo.put("data",obj);
		Push.push(PushCode.foodGameAddFood, null, pushInfo);

		JSONObject food = new JSONObject();
		food.put("foodId", rst);
		return food;
	}
	
	@Transactional
	@ServiceMethod(code = "103", description = "退出")
	public Object exitRoom(FoodSocketServer adminSocketServer, Command cmd, JSONObject params) {
		checkNull(params);
		checkNull(params.get("roomId"),params.get("userId"));
		int roomId = params.getIntValue("roomId",-1);
		String userId = params.getString("userId");
		if(userId == null || userId.isEmpty()) {
			throwExp("参数错误！");
		}
		if(roomId < 0 && players.containsKey(userId)) {
			roomId = players.get(userId);
		}
		int roomType = roomId / 1000;
		FoodRoom room = managers.get(roomType).getRoomById(roomId);
		if(room != null) {
			room.exitRoom(userId);
		}

		return new JSONObject();
	}

	@Transactional
	@ServiceMethod(code = "104", description = "查询游戏记录")
	public Object getGameRecord(FoodSocketServer adminSocketServer, Command cmd, JSONObject params) {
		checkNull(params);
		checkNull(params.get("userId"), params.get("page"), params.get("pageNum"));
		String userId = params.getString("userId");
		int page = params.getIntValue("page",0);
		int limit = params.getIntValue("pageNum",20);

		Integer start = page * limit;
		Integer end = start + limit;
		JSONObject condition = new JSONObject();
		condition.put("start",start);
		condition.put("limit",end);
		condition.put("userId",userId);

		List<FoodGameRecord> recordList = foodGameRecordService.findList("findGameRecord", condition);
		JSONObject obj = new JSONObject();
		obj.put("list",recordList);
		return obj;
	}

	@Transactional
	@ServiceMethod(code = "105", description = "离开座位，不退出房间")
	public Object upSeat(FoodSocketServer adminSocketServer, Command cmd, JSONObject params) {
		checkNull(params);
		checkNull(params.get("userId"));

		String userId = params.getString("userId");
		int roomId = params.getIntValue("roomId",-1);
		if(userId == null || userId.isEmpty()) {
			throwExp("参数错误！");
		}
		if(roomId < 0 && players.containsKey(userId)) {
			roomId = players.get(userId);
		}
		if(roomId < 0) {
			throwExp("退出失败！");
		}

		int roomType = roomId / 1000;
		FoodRoom room = managers.get(roomType).getRoomById(roomId);
		if(room != null) {
			if(!room.isInRoom(userId)) {
				throwExp("当前不可退出！");
			}
			if(!room.isJoinGame(userId)) {
				throwExp("当前不可退出！");
			}

			int chairId = room.getChairIDByUid(userId);
			if(room.standUp(chairId)){
				room.checkStart();
				FoodGameConfig cfg = managers.get(roomType).getCfg();
				returnMoney(room.getRoomId(), userId, LogCapitalTypeEnum.food_cancel_bet.getValue(), cfg.getCostType(), cfg.getCostSit());
			}else {
				throwExp("当前不可退出！");
			}

		}

		return room.getRoomInfo(userId);
	}

	public void sendMessageToAll() {

	}

	public void sendMessage() {

	}

	public Object getAllInfo() {
		JSONArray arr = new JSONArray();
		for(Map.Entry<Integer,RoomManager> entry: managers.entrySet()) {
			JSONObject obj = new JSONObject();
			obj.put("type", entry.getKey());
			obj.put("config", entry.getValue().getCfg());
			obj.put("roomCount", entry.getValue().getRoomCount());
			obj.put("maxRoom", entry.getValue().getMaxRoom());
			arr.add(obj);
		}
		return arr;
	}

	public RoomManager getRoomManager(int type) {
		if(managers.containsKey(type)) {
			return managers.get(type);
		}
		return null;
	}

	public void clearAll() {
		for(Map.Entry<Integer,RoomManager> entry: managers.entrySet()) {
			RoomManager manager = entry.getValue();
			manager.clearAll();
		}
	}

}
