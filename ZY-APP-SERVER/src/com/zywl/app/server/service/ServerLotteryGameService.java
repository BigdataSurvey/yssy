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
        Push.addPushSuport(PushCode.updateSgInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateSgStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateBtInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateBtStatus, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDgsInfo, new DefaultPushHandler());
        Push.addPushSuport(PushCode.updateDgsStatus, new DefaultPushHandler());
    }

    public void registPush(AppSocket appSocket, String userId, String gameId) {
        if (gameId.equals("1")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateRoomDate, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateGameStatus, userId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateGameDiyData, gameId));
        } else if (gameId.equals("4")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDnsInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDnsStatus, gameId));
        } else if (gameId.equals("5")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateNhInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateNhStatus, userId));
        } else if (gameId.equals("7")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDts2Info, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDts2Status, userId));
        } else if (gameId.equals("8")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateSgInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateSgStatus, userId));
        }else if (gameId.equals("9")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateBtInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateBtStatus, userId));
        }
        else if (gameId.equals("10")) {
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDgsInfo, gameId));
            Push.doAddPush(appSocket, new PushBean(PushCode.updateDgsStatus, userId));
        }
    }

    public void removePush(AppSocket appSocket, String userId, String gameId) {
        if (gameId.equals("1")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateRoomDate, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateGameStatus, userId));
        } else if (gameId.equals("4")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDnsInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDnsStatus, gameId));
        } else if (gameId.equals("5")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateNhInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateNhStatus, userId));
        }else if (gameId.equals("7")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDts2Info, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDts2Status, userId));
        } else if (gameId.equals("8")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateSgInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateSgStatus, userId));
        } else if (gameId.equals("9")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateBtInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateBtStatus, userId));
        }
        else if (gameId.equals("10")) {
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDgsInfo, gameId));
            Push.doRemovePush(appSocket, new PushBean(PushCode.updateDgsStatus, userId));
        }
    }


    /**
     * 判断lottery是否在线
     *
     * @param
     * @return
     */
    public boolean isOnline(int gameId) {

        Set<BaseClientSocket> clients = SocketManager.getServers(TargetSocketType.getServerEnum(gameId));
        if (clients != null && !clients.isEmpty()) {
            return true;
        }
        return false;
    }

    @ServiceMethod(code = "001", description = "加入房间")
    public Async jionRoom(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("gameId"));
        //params.put("gameId",5);
        if (!isOnline(params.getIntValue("gameId"))) {
            throwExp("小游戏正在维护");
        }
        long userId = appSocket.getWsidBean().getUserId();
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
        String userNo = user.getUserNo();
        String headImgUrl = user.getHeadImageUrl();
        String UserName = user.getName();
        JSONObject data = new JSONObject();
        data.put("gameId", params.get("gameId"));
        data.put("userId", userId);
        data.put("userNo", userNo);
        data.put("headImgUrl", headImgUrl);
        data.put("userName", UserName);
        data.put("bet",params.get("bet"));
        requestLotteryService.requestBattleRoyaleJoinRoom(data, new Listener() {
            public void handle(BaseClientSocket clientSocket, Command command) {
                if (command.isSuccess()) {
                    JSONObject result = JSONObject.from(command.getData());
                    Executer.response(CommandBuilder.builder(appCommand).success(result).build());
                    registPush(appSocket, String.valueOf(userId), params.getString("gameId"));
                } else {
                    Executer.response(
                            CommandBuilder.builder(appCommand).error(command.getMessage(), command.getData()).build());
                }
            }
        });

        //
        //Executer.request(TargetSocketType.getServerEnum(params.getIntValue("gameId")),
        //	CommandBuilder.builder().request("101101", data).build(), new RequestManagerListener(appCommand));
        // 玩家同时只能呆在一个lottery服
        userLotteryPush.put(String.valueOf(userId), TargetSocketType.getServerEnum(params.getInteger("gameId")));
        return async();

    }

    @ServiceMethod(code = "002", description = "投入")
    public Async bet(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("betAmount"), params.get("bet"));
        int gameId = params.getIntValue("gameId");
        if (!isOnline(gameId)) {
            throwExp("小游戏正在维护");
        }
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        if (user.getRisk()==1){
            throwExp("账号存在风险，请联系客服进行核实。");
        }
        BigDecimal amount = params.getBigDecimal("betAmount");
        if (gameId != 5) {
            if (!betList.contains(amount)) {
                throwExp("非法请求");
            }
        }
        params.put("userId", userId);
        params.put("headImgUrl",user.getHeadImageUrl());
        params.put("name",user.getName());
        Executer.request(TargetSocketType.getServerEnum(gameId), CommandBuilder.builder().request("101103", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "003", description = "更换房间")
    public Async battleRoyaleUpdateBet(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("bet"));
        String bet = params.getString("bet");
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        String userNo = user.getUserNo();
        String headImgUrl = user.getHeadImageUrl();
        String UserName = user.getName();
        JSONObject data = new JSONObject();
        data.put("userId", userId);
        data.put("userNo", userNo);
        data.put("headImgUrl", headImgUrl);
        data.put("userName", UserName);
        data.put("bet", params.getString("bet"));
        if (params.getIntValue("gameId")==GameTypeEnum.bt.getValue()){
            data.put("floor",params.getString("floor"));
        }
        if (isOnline(params.getIntValue("gameId"))) {
            Executer.request(TargetSocketType.getServerEnum(params.getIntValue("gameId")), CommandBuilder.builder().request("101105", data).build(),
                    new RequestManagerListener(appCommand));
            // 玩家同时只能呆在一个lottery服
            userLotteryPush.put(String.valueOf(userId), TargetSocketType.getServerEnum(params.getIntValue("gameId")));
        }
        return async();
    }

    @ServiceMethod(code = "004", description = "离开房间")
    public Async leaveRoom(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("gameId"));
        long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        JSONObject data = new JSONObject();
        data.put("userId", userId);
        if (isOnline(params.getIntValue("gameId"))) {
            // 移除lottery信息
            userLotteryPush.remove(String.valueOf(userId));
            removePush(appSocket, String.valueOf(userId), params.getString("gameId"));
            Executer.request(TargetSocketType.getServerEnum(params.getIntValue("gameId")),
                    CommandBuilder.builder().request("101104", data).build(), new RequestManagerListener(appCommand));

        }
        return async();
    }




    @ServiceMethod(code = "015", description = "记录")
    public Async recordSg(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        Executer.request(TargetSocketType.getServerEnum(params.getIntValue("gameId")), CommandBuilder.builder().request("101004", params).build(), new RequestManagerListener(appCommand));
        return async();
    }


    @ServiceMethod(code = "014", description = "大逃杀排行榜")
    public JSONObject dtsRankList(final AppSocket appSocket, Command appCommand, JSONObject params) {
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
            result.put("rankList",gameCacheService.getLastWeekListDts());
            Double userLastWeekRankScore = gameCacheService.getUserLastWeekRankScore(GameTypeEnum.battleRoyale.getValue(), String.valueOf(userId));
            result.put("myScore",userLastWeekRankScore==null?0.0:userLastWeekRankScore);
            Long rank = gameCacheService.getLastWeekUserRankDts(String.valueOf(userId));
            result.put("myRank",rank==null?-1:rank+1);
        } else if (type==1) {
            result.put("remainingTime", DateUtil.thisWeekRemainingTime());
            result.put("rankList",gameCacheService.getThisWeekListDts());
            Double userRankScore = gameCacheService.getUserRankScore(GameTypeEnum.battleRoyale.getValue(), String.valueOf(userId));
            result.put("myScore", userRankScore ==null?0.0:userRankScore);
            Long thisWeekUserRank = gameCacheService.getThisWeekUserRankDts(String.valueOf(userId));
            result.put("myRank",thisWeekUserRank==null?-1:thisWeekUserRank+1);
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
      /*  BigDecimal amount = params.getBigDecimal("betAmount");
        if (gameId != 5) {
            if (!betList.contains(amount)) {
                throwExp("非法请求");
            }
        }*/
        params.put("userId", userId);
        params.put("headImgUrl",user.getHeadImageUrl());
        params.put("name",user.getName());
        Executer.request(TargetSocketType.getServerEnum(gameId), CommandBuilder.builder().request("101103", params).build(),
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
        Executer.request(TargetSocketType.getServerEnum(params.getIntValue("gameId")), CommandBuilder.builder().request("101004", params).build(), new RequestManagerListener(appCommand));
        return async();
    }





}
