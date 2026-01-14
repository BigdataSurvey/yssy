package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserDtsAmount;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GameCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.service.UserDtsAmountService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
@ServiceClass(code = MessageCodeContext.LOTTERY_SERVER)
public class ServerLotteryGameService extends BaseService {

    public static ConcurrentHashMap<String, TargetSocketType> userLotteryPush = new ConcurrentHashMap<String, TargetSocketType>();

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private RequestLotteryService requestLotteryService;

    @Autowired
    private GameCacheService gameCacheService;

    @Autowired
    private UserDtsAmountService userDtsAmountService;

    public static List<BigDecimal> betList = new ArrayList<>();
    public static List<BigDecimal> canCash = new ArrayList<>();

    @PostConstruct
    public void _Construct() {
        betList.add(new BigDecimal("1"));
        betList.add(new BigDecimal("10"));
        betList.add(new BigDecimal("100"));

        canCash.add(new BigDecimal("0.1"));
        canCash.add(new BigDecimal("1"));
        canCash.add(new BigDecimal("10"));

        userLotteryPush.clear();

        Push.addPushSuport(PushCode.updateRoomDate, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateGameStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateGameDiyData, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDnsInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDnsStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateNhInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateNhStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts2Info, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts2Status, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts3Info, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts3Status, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDts3UserLeave, new DefaultPushHandler());

        Push.addPushSuport(PushCode.updateSgInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateSgStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateBtInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateBtStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDgsInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDgsStatus, new DefaultPushHandler());

        // PBX
        //Push.addPushSuport(PushCode.updatePbxInfo, new DefaultPushHandler());
        //Push.addPushSuport(PushCode.updatePbxStatus, new DefaultPushHandler());
    }

    public void registPush(AppSocket appSocket, String userId, String gameId) {
        if ("7".equals(gameId) || "1".equals(gameId)) {
            addCommonLotteryPush(appSocket, gameId, userId);
        } else if (gameId.equals("4")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDnsInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDnsStatus, gameId));
        } else if (gameId.equals("5")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateNhInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateNhStatus, userId));
        }  else if (gameId.equals("8")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateSgInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateSgStatus, userId));
        } else if (gameId.equals("9")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateBtInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateBtStatus, userId));
        } else if (gameId.equals("10")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDgsInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDgsStatus, userId));
        } else if (gameId.equals("12")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDts2Info, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDts2Status, userId));
        }
    }

    public void removePush(AppSocket appSocket, String userId, String gameId) {
        if ("7".equals(gameId) || "1".equals(gameId)) {
            removeCommonLotteryPush(appSocket, gameId, userId);
        } else if (gameId.equals("4")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDnsInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDnsStatus, gameId));
        } else if (gameId.equals("5")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateNhInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateNhStatus, userId));
        }  else if (gameId.equals("8")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateSgInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateSgStatus, userId));
        } else if (gameId.equals("9")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateBtInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateBtStatus, userId));
        } else if (gameId.equals("10")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDgsInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDgsStatus, userId));
        } else if (gameId.equals("12")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDts2Info, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDts2Status, userId));
        }
    }

    // 判断玩法服是否在线
    public boolean isOnline(int gameId) {
        Set<BaseClientSocket> clients = SocketManager.getServers(TargetSocketType.getServerEnum(gameId));
        return clients != null && !clients.isEmpty();
    }

    @ServiceMethod(code = "001", description = "加入房间")
    public Async jionRoom(final AppSocket appSocket, final Command appCommand, final JSONObject params) {
        checkNull(params);
        checkNull(params.get("gameId"));
        final int gameId = params.getIntValue("gameId");
        if (!isOnline(gameId)) {
            Executer.response(CommandBuilder.builder(appCommand).error("小游戏正在维护").build());
            return async();
        }

        final long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        if (user.getAuthentication() == 0) {
            throwExp("未通过实名认证");
        }
        if (user.getRiskPlus() != null && user.getRiskPlus() == 1) {
            throwExp("请求超时，请更换网络环境再试");
        }

        JSONObject data = new JSONObject();
        data.put("gameId", gameId);
        data.put("userId", userId);
        data.put("userNo", user.getUserNo());
        data.put("headImgUrl", user.getHeadImageUrl());
        data.put("userName", user.getName());
        data.put("bet", params.get("bet"));

        Listener cb = new Listener() {
            @Override
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    JSONObject result = JSONObject.from(command.getData());
                    Executer.response(CommandBuilder.builder(appCommand).success(result).build());

                    registPush(appSocket, String.valueOf(userId), String.valueOf(gameId));
                    userLotteryPush.put(String.valueOf(userId), TargetSocketType.getServerEnum(gameId));
                } else {
                    Executer.response(CommandBuilder.builder(appCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        };
        if (gameId == 12) {
            requestLotteryService.requestPbxJoinRoom(data, cb);
        } else {
            requestLotteryService.requestBattleRoyaleJoinRoom(data, cb);
        }
        return async();

    }

    @ServiceMethod(code = "002", description = "投入")
    public Async bet(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("gameId"));

        int gameId = params.getIntValue("gameId");
        if (!isOnline(gameId)) {
            throwExp("小游戏正在维护");
        }

        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }

        if (gameId == 12) {
            checkNull(params.get("elementId"));

            BigDecimal chip = params.getBigDecimal("chip");
            if (chip == null) {
                chip = params.getBigDecimal("betAmount");
            }
            if (chip == null) {
                throwExp("参数异常");
            }

            if (!betList.contains(chip)) {
                throwExp("非法请求");
            }

            params.put("userId", userId);
            params.put("headImgUrl", user.getHeadImageUrl());
            params.put("name", user.getName());
            params.put("betAmount", chip);

            requestLotteryService.requestPbxBetService(params, new RequestManagerListener(appCommand));
            return async();
        }

        checkNull(params.get("betAmount"), params.get("bet"));

        BigDecimal amount = params.getBigDecimal("betAmount");
        if (gameId != 5) {
            if (!betList.contains(amount)) {
                throwExp("非法请求");
            }
        }

        params.put("userId", userId);
        params.put("headImgUrl", user.getHeadImageUrl());
        params.put("name", user.getName());

        requestLotteryService.requestBattleRoyaleBetService(params, new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "003", description = "切换房间（离开+加入的整合）")
    public Object changeRoom(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("gameId"), params.get("bet"));
        Long userId = appSocket.getWsidBean().getUserId();
        Long bet = params.getLong("bet");

        int gameId = params.getIntValue("gameId");
        if (!isOnline(gameId)) {
            throwExp("小游戏正在维护");
        }

        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        if (user.getRiskPlus() != null && user.getRiskPlus() == 1) {
            throwExp("请求超时，请更换网络环境再试");
        }
        params.put("userId", userId);
        params.put("userNo", user.getUserNo());
        params.put("userName", user.getName());
        params.put("headImgUrl", user.getHeadImageUrl());
        params.put("gameId", gameId);
        params.put("bet", bet);
        requestLotteryService.requestBattleRoyaleChangeRoom(params, new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "004", description = "离开房间")
    public Async leaveRoom(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("gameId"));

        int gameId = params.getIntValue("gameId");

        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }

        JSONObject data = new JSONObject();
        data.put("gameId", gameId);
        data.put("userId", userId);

        // 本地移除
        userLotteryPush.remove(String.valueOf(userId));
        removePush(appSocket, String.valueOf(userId), String.valueOf(gameId));

        if (isOnline(gameId)) {
            if (gameId == 12) {
                requestLotteryService.requestPbxLeaveRoom(data, new RequestManagerListener(appCommand));
            } else {
                requestLotteryService.requestBattleRoyaleLeaveRoom(data, new RequestManagerListener(appCommand));
            }
        }

        return async();
    }

    @ServiceMethod(code = "015", description = "记录")
    public Async recordSg(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("gameId"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }

        String reqCode = "101004";
        if (params.getIntValue("gameId") == 12) {
            reqCode = "102108";
        }

        Executer.request(
                TargetSocketType.getServerEnum(params.getIntValue("gameId")),
                CommandBuilder.builder().request(reqCode, params).build(),
                new RequestManagerListener(appCommand)
        );
        return async();
    }


    @ServiceMethod(code = "014", description = "大逃杀排行榜")
    public JSONObject dtsRankList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        checkNull(params.get("gameId"));

        int gameId = params.getIntValue("gameId");
        if (gameId != GameTypeEnum.battleRoyale.getValue() && gameId != GameTypeEnum.dts2.getValue() && gameId != GameTypeEnum.txz.getValue()) {
            throwExp("gameId错误");
        }

        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }

        JSONObject result = new JSONObject();
        int type = params.getInteger("type");

        // PBX(推箱子)排行榜走 Manager 200722
        if (gameId == GameTypeEnum.txz.getValue()) {
            return pbxRankListFromManager(userId, type);
        }


        if (type == 2) {
            result.put("rankList", gameCacheService.getLastWeekRankList(gameId));
            Double userLastWeekRankScore = gameCacheService.getUserLastWeekRankScore(gameId, String.valueOf(userId));
            result.put("myScore", userLastWeekRankScore == null ? 0.0 : userLastWeekRankScore);
            Long rank = gameCacheService.getLastWeekUserRankByGame(gameId, String.valueOf(userId));
            result.put("myRank", rank == null ? -1 : rank + 1);
        } else if (type == 1) {
            result.put("remainingTime", DateUtil.thisWeekRemainingTime());
            result.put("rankList", gameCacheService.getThisWeekRankList(gameId));
            Double userRankScore = gameCacheService.getUserRankScore(gameId, String.valueOf(userId));
            result.put("myScore", userRankScore == null ? 0.0 : userRankScore);
            Long thisWeekUserRank = gameCacheService.getThisWeekUserRankByGame(gameId, String.valueOf(userId));
            result.put("myRank", thisWeekUserRank == null ? -1 : thisWeekUserRank + 1);
        }

        return result;
    }

    @ServiceMethod(code = "016", description = "游园宝箱")
    public JSONObject yybx(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        UserDtsAmount byUserId = userDtsAmountService.findByUserId(userId);
        JSONObject result = new JSONObject();
        if (byUserId==null){
            result.put("amount",BigDecimal.ZERO);
            result.put("day",180);
            return result;
        }

        result.put("amount",byUserId.getAmount());
        Date dateByDay = DateUtil.getDateByDay(byUserId.getCreateTime(), 180);
        long day = DateUtil.calculateDayDifference( dateByDay,new Date());
        result.put("day",day);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "017", description = "游园宝箱")
    public JSONObject openBox(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        throwExp("未到开启时间");
        return null;
    }

    @ServiceMethod(code = "018", description = "2选1排行榜")
    public JSONObject lhdRankList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        JSONObject result = new JSONObject();
        int type= params.getInteger("type");
        if (type==2){
            result.put("rankList",gameCacheService.getLhdLastWeekList());
            Double userLastWeekRankScore = gameCacheService.getUserLastWeekRankScore(GameTypeEnum.nh.getValue(), String.valueOf(userId));
            result.put("myScore",userLastWeekRankScore==null?0.0:userLastWeekRankScore);
            Long rank = gameCacheService.getLastWeekUserRankLhd(String.valueOf(userId));
            result.put("myRank",rank==null?-1:rank+1);
        } else if (type==1) {
            result.put("remainingTime", DateUtil.thisWeekRemainingTime());
            result.put("rankList",gameCacheService.getThisWeekListLhd());
            Double userRankScore = gameCacheService.getUserRankScore(GameTypeEnum.nh.getValue(), String.valueOf(userId));
            result.put("myScore", userRankScore ==null?0.0:userRankScore);
            Long thisWeekUserRank = gameCacheService.getThisWeekUserRankLhd(String.valueOf(userId));
            result.put("myRank",thisWeekUserRank==null?-1:thisWeekUserRank+1);
        }
        return result;
    }
    @ServiceMethod(code = "019", description = "打怪兽排行榜")
    public JSONObject dgsRankList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        JSONObject result = new JSONObject();
        int type= params.getInteger("type");
        if (type==2){
            result.put("rankList",gameCacheService.getDGSLastWeekList());
            Double userLastWeekRankScore = gameCacheService.getUserLastWeekRankScore(GameTypeEnum.dgs.getValue(), String.valueOf(userId));
            result.put("myScore",userLastWeekRankScore==null?0.0:userLastWeekRankScore);
            Long rank = gameCacheService.getLastWeekUserRankDgs(String.valueOf(userId));
            result.put("myRank",rank==null?-1:rank+1);
        } else if (type==1) {
            result.put("remainingTime", DateUtil.thisWeekRemainingTime());
            result.put("rankList",gameCacheService.getThisWeekListDgs());
            Double userRankScore = gameCacheService.getUserRankScore(GameTypeEnum.dgs.getValue(), String.valueOf(userId));
            result.put("myScore", userRankScore ==null?0.0:userRankScore);
            Long thisWeekUserRank = gameCacheService.getThisWeekUserRankDgs(String.valueOf(userId));
            result.put("myRank",thisWeekUserRank==null?-1:thisWeekUserRank+1);
        }
        return result;
    }


    @ServiceMethod(code = "020", description = "击打怪兽")
    public Async Jdgs(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull( params.get("bet"));
        int gameId = params.getIntValue("gameId");
        if (!isOnline(gameId)) {
            throwExp("小游戏正在维护");
        }
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }

        params.put("userId", userId);
        params.put("headImgUrl",user.getHeadImageUrl());
        params.put("name",user.getName());
        Executer.request(TargetSocketType.getServerEnum(gameId), CommandBuilder.builder().request("102103", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "021", description = "记录")
    public Async findRecord(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("page"), params.get("num"));
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        Executer.request(TargetSocketType.getServerEnum(params.getIntValue("gameId")), CommandBuilder.builder().request("102104", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    private void addCommonLotteryPush(AppSocket appSocket, String gameId, String userId) {
        Push.doAddPush(appSocket, new PushBean(PushCode.updateRoomDate, gameId));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateGameStatus, userId));
        Push.doAddPush(appSocket, new PushBean(PushCode.updateGameDiyData, gameId));
        if ("1".equals(gameId)) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDts3UserLeave, gameId));
        }
    }

    private void removeCommonLotteryPush(AppSocket appSocket, String gameId, String userId) {
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateRoomDate, gameId));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateGameStatus, userId));
        Push.doRemovePush(appSocket, new PushBean(PushCode.updateGameDiyData, gameId));
    }



    /**
     * PBX(推箱子)排行榜：调用 Manager 200722（pbxQuery）并包装为 004014 统一结构
     * type=1 本周榜；type=2 上周榜
     */
    private JSONObject pbxRankListFromManager(long userId, int type) {
        JSONObject req = new JSONObject();
        req.put("gameId", String.valueOf(GameTypeEnum.txz.getValue()));
        req.put("userId", String.valueOf(userId));

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JSONObject> ref = new AtomicReference<>();
        AtomicReference<String> err = new AtomicReference<>();

        Executer.request(
                TargetSocketType.manager,
                CommandBuilder.builder().request("200722", req).build(),
                new Listener() {
                    @Override
                    public void handle(BaseClientSocket socket, Command command) {
                        try {
                            if (command != null && command.isSuccess()) {
                                ref.set((JSONObject) command.getData());
                            } else if (command != null) {
                                err.set(command.getMessage());
                            } else {
                                err.set("manager response null");
                            }
                        } catch (Exception e) {
                            err.set(e.getMessage());
                        } finally {
                            latch.countDown();
                        }
                    }
                }
        );

        boolean ok;
        try {
            ok = latch.await(4, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            ok = false;
        }

        JSONObject result = new JSONObject();
        result.put("gameId", GameTypeEnum.txz.getValue());
        result.put("serverTime", System.currentTimeMillis());

        if (!ok || ref.get() == null) {
            result.put("rankList", new com.alibaba.fastjson2.JSONArray());
            result.put("myRank", -1);
            result.put("myScore", BigDecimal.ZERO);
            if (type == 1) {
                result.put("remainingTime", DateUtil.thisWeekRemainingTime());
            }
            result.put("success", false);
            result.put("message", err.get() == null ? "pbxRank timeout" : err.get());
            return result;
        }

        JSONObject q = ref.get();
        com.alibaba.fastjson2.JSONArray weekTop = q.getJSONArray("weekRankTop10");
        com.alibaba.fastjson2.JSONArray lastTop = q.getJSONArray("lastWeekRankTop10");

        if (weekTop == null) weekTop = new com.alibaba.fastjson2.JSONArray();
        if (lastTop == null) lastTop = new com.alibaba.fastjson2.JSONArray();

        if (type == 2) {
            result.put("rankList", lastTop);
            int myRank = q.getIntValue("myLastWeekRank");
            result.put("myRank", myRank <= 0 ? -1 : myRank);
            BigDecimal myScore = q.getBigDecimal("myLastWeekConsume");
            result.put("myScore", myScore == null ? BigDecimal.ZERO : myScore);
        } else if (type == 1) {
            result.put("remainingTime", DateUtil.thisWeekRemainingTime());
            result.put("rankList", weekTop);
            int myRank = q.getIntValue("myWeekRank");
            result.put("myRank", myRank <= 0 ? -1 : myRank);
            BigDecimal myScore = q.getBigDecimal("myWeekConsume");
            result.put("myScore", myScore == null ? BigDecimal.ZERO : myScore);
        } else {
            throwExp("参数错误");
        }

        // 透传 PBX 额外字段
        result.put("weekDividendPool", q.getBigDecimal("weekDividendPool"));
        result.put("lastWeekDividendPool", q.getBigDecimal("lastWeekDividendPool"));
        result.put("poolBalance", q.getBigDecimal("poolBalance"));
        result.put("weekSettled", q.getBooleanValue("weekSettled"));
        result.put("lastWeekSettled", q.getBooleanValue("lastWeekSettled"));

        result.put("success", true);
        return result;
    }

}
