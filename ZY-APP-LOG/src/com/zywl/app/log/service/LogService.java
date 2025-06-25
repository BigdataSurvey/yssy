package com.zywl.app.log.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.LogUserCapitalVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.log.socket.LogSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Service
@ServiceClass(code = "109")
public class LogService extends BaseService {

    @Autowired
    private LogUserCapitalService logUserCapitalService;

    @Autowired
    private LogUserBackpackService logUserBackpackService;

    @Autowired
    private PlatformStatementService platformStatementService;

    @Autowired
    private LogLoginService logLoginService;

    @Autowired
    private BackpackStatementService backpackStatementService;

    @Autowired
    private ItemService itemService;

    public static List<Item> items = new ArrayList<>();

    public void insertLog(JSONObject data) {
        if (data.getIntValue("logType") == 1) {
            insertCapitalLog(data);
        } else if (data.getIntValue("logType") == 2) {
            insertBackPackLog(data);
        } else if (data.getIntValue("logType") == 3) {
            insertLoginLog(data);
        }
    }

    @PostConstruct
    public void _construct() {
        Push.addPushSuport(PushCode.insertLog, new DefaultPushHandler());
        Push.addPushSuport(PushCode.addStatement, new DefaultPushHandler());
        items = itemService.findAll();
        PlatformStatement byYmd = platformStatementService.findByYmd(DateUtil.format9(new Date()));
        if (byYmd == null) {
            platformStatementService.addTodayStatement();
        }
        List<BackpackStatement> allBackpackStatement = backpackStatementService.findAllBackpackStatement();
        if (allBackpackStatement.size() == 0) {
            for (Item item : items) {
                backpackStatementService.addTodayStatement(item.getId());
            }
        }


        new Timer("定时增加每日报表数据").schedule(new TimerTask() {
            Long time = DateUtil.getAddStaticsDate();

            public void run() {
                platformStatementService.addStatement();
                for (Item item : items) {
                    backpackStatementService.addStatement(item.getId());
                }
            }
        }, DateUtil.getAddStaticsDate(), 1000 * 60 * 60 * 24);

        new Timer("日志定时删除").schedule(new TimerTask() {
            public void run() {
                try {
                    long time = System.currentTimeMillis();
                    logger.info("删除背包日志开始");
                    int res = logUserBackpackService.deleteLog(DateUtil.getOneDaysAgoBegin());
                    logger.info("删除背包结束，删除【" + res + "】条数据，用时【" + (System.currentTimeMillis() - time) + "】ms");
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, DateUtil.getTaskNeed(), 1000 * 60 * 60 * 24);
    }


    public void insertCapitalLog(JSONObject data) {
        int type = data.getIntValue("type");
        long userId = data.getLong("userId");
        BigDecimal balanceBefore = data.getBigDecimal("balanceBefore");
        BigDecimal occupyBalanceBefore = data.getBigDecimal("occupyBalanceBefore");
        BigDecimal amount = data.getBigDecimal("amount");
        LogCapitalTypeEnum em = data.getObject("em", LogCapitalTypeEnum.class);
        String orderNo = data.containsKey("orderNo") ? data.getString("orderNo") : null;
        Long sourceDataId = data.containsKey("sourceDataId") ? data.getLong("sourceDataId") : null;
        String tableName = data.containsKey("tableName") ? data.getString("tableName") : null;
        int capitalType = data.getIntValue("capitalType");
        logUserCapitalService.addLogUserCapital(type, userId, capitalType, balanceBefore, occupyBalanceBefore,
                amount, em, orderNo, sourceDataId, tableName);
        addStatement(em.getValue(), amount);

    }

    public void insertBackPackLog(JSONObject data) {
        long userId = data.getLong("userId");
        int numberBefore = data.getIntValue("numberBefore");
        int number = data.getIntValue("number");
        long itemId = data.getLong("itemId");
        LogUserBackpackTypeEnum em = data.getObject("em", LogUserBackpackTypeEnum.class);
        if (em.getValue()==LogUserBackpackTypeEnum.zs.getValue() || em.getValue()==LogUserBackpackTypeEnum.zsg.getValue()){
            logUserBackpackService.addLogUserBackpack(userId, itemId, numberBefore, number, em,data.getString("otherUserId"));
        }else{
            logUserBackpackService.addLogUserBackpack(userId, itemId, numberBefore, number, em);
        }
        addItemStatement(em.getValue(), number, itemId);

    }

    public void insertLoginLog(JSONObject data) {

        long userId = data.getLong("userId");
        String type = data.getString("type");
        String requestId = data.getString("requestId");
        String deviceId = data.getString("deviceId");
        String code = data.getString("code");
        String riskLevel = data.getString("riskLevel");
        String detail = data.getString("detail");
        String model = data.getString("model");
        String ip = data.getString("ip");
        String ipCountry = data.getString("ipCountry");
        String ipProvince = data.getString("ipProvince");
        String ipCity = data.getString("ipCity");
        String message = data.getString("message");
        logLoginService.addLog(userId, type, requestId, deviceId, code, riskLevel, detail, model, ip, ipCountry, ipProvince, ipCity, message);

    }


    @Transactional
    @ServiceMethod(code = "001", description = "获取玩家日志")
    public Object getUserCapitalLog(LogSocketServer socketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        int type = params.getIntValue("type");
        List<LogUserCapitalVo> vos = logUserCapitalService.getLog(userId, params.getIntValue("capitalType"), params.getIntValue("page"), params.getIntValue("num"), type);
        JSONObject logInfo = new JSONObject();
        logInfo.put("logInfo", vos);
        return logInfo;
    }

    @Transactional
    @ServiceMethod(code = "002", description = "获取平台报表")
    public Object getPlatformStatement(LogSocketServer socketServer, JSONObject data) {
        String startDate = data.getString("startDate");
        String endDate = data.getString("endDate");
        List<PlatformStatement> allPlatformStatement = platformStatementService.findAllPlatformStatement();
        allPlatformStatement.forEach(e -> e.init());
        JSONObject result = new JSONObject();
        Collections.reverse(allPlatformStatement);
        result.put("list", allPlatformStatement);
        result.put("count", allPlatformStatement.size());
        return result;
    }


    @Transactional
    @ServiceMethod(code = "003", description = "获取玩家报表")
    public Object getUserStatement(LogSocketServer socketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        JSONObject result = new JSONObject();
        return result;
    }

    @Transactional
    @ServiceMethod(code = "004", description = "获取玩家道具日志")
    public Object getUserItemLog(LogSocketServer socketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        int type = params.getIntValue("type");
        List<LogUserBackpack> vos = logUserBackpackService.getLog(userId, params.getString("itemId"), params.getIntValue("page"), params.getIntValue("num"), type,params.getString("mark"));
        JSONObject logInfo = new JSONObject();
        logInfo.put("logInfo", vos);
        return logInfo;
    }


    public void addStatement(int type, BigDecimal amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("ymd", DateUtil.format9(new Date()));
        if (type == LogCapitalTypeEnum.ios_test.getValue() || type == LogCapitalTypeEnum.cancel_bet.getValue() || type == LogCapitalTypeEnum.askbuy_sucess.getValue()) {
            return;
        }
        if (type == LogCapitalTypeEnum.pirze_draw.getValue()) {
            params.put("subPrize", amount);
        } else if (type == LogCapitalTypeEnum.transfer_friend.getValue()) {
            params.put("subSendMail", amount);
        } else if (type == LogCapitalTypeEnum.send_mail.getValue()) {
            params.put("subMailFee", amount);
        } else if (type == LogCapitalTypeEnum.buy.getValue()) {
            params.put("subTradingBuy", amount);
        } else if (type == LogCapitalTypeEnum.askbuy.getValue()) {
            params.put("subAskBuy", amount);
        } else if (type == LogCapitalTypeEnum.game_bet.getValue()) {
            params.put("subGameBetDts", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_food.getValue()) {
            params.put("subGameBetFood", amount);
        } else if (type == LogCapitalTypeEnum.sign.getValue()) {
            params.put("subSign", amount);
        } else if (type == LogCapitalTypeEnum.refine_speed.getValue()) {
            params.put("subRefineSpeed", amount);
        } else if (type == LogCapitalTypeEnum.get_pet.getValue()) {
            params.put("subPet", amount);
        } else if (type == LogCapitalTypeEnum.study_skill.getValue()) {
            params.put("subStudySkill", amount);
        } else if (type == LogCapitalTypeEnum.shopping.getValue() || type == LogCapitalTypeEnum.buy_user_no.getValue() || type == LogCapitalTypeEnum.update_idCard.getValue()) {
            params.put("subShop", amount);
        } else if (type == LogCapitalTypeEnum.guild.getValue()) {
            params.put("subGuild", amount);
        } else if (type == LogCapitalTypeEnum.cash_succrss.getValue()) {
            params.put("subCash", amount);
        } else if (type == LogCapitalTypeEnum.buy_coin.getValue()) {
            params.put("subBuyCoin", amount);
        } else if (type == LogCapitalTypeEnum.exchange.getValue()) {
            params.put("subExchange", amount);
        } else if (type == LogCapitalTypeEnum.pirze_draw_reward.getValue()) {
            params.put("addPirze", amount);
        } else if (type == LogCapitalTypeEnum.sign_reward.getValue() || type == LogCapitalTypeEnum.receive_total_sign_reward.getValue()) {
            params.put("addSign", amount);
        } else if (type == LogCapitalTypeEnum.sell.getValue()) {
            params.put("addSell", amount);
        } else if (type == LogCapitalTypeEnum.cancel_askbuy.getValue()) {
            params.put("addCancelAskBuy", amount);
        } else if (type == LogCapitalTypeEnum.cash_fail.getValue()) {
            params.put("addCashFail", amount);
        } else if (type == LogCapitalTypeEnum.receive_income.getValue()) {
            params.put("addReceiveIncome", amount);
        } else if (type == LogCapitalTypeEnum.mail.getValue()) {
            params.put("addReceiveMail", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_win.getValue()) {
            params.put("addGameWinDts", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_win_food.getValue()) {
            params.put("addGameWinFood", amount);
        } else if (type == LogCapitalTypeEnum.sell_sys.getValue()) {
            params.put("addSellSys", amount);
        } else if (type == LogCapitalTypeEnum.sell_sys_magic.getValue()) {
            params.put("addSellSysMagic", amount);
        } else if (type == LogCapitalTypeEnum.sell_sys2.getValue()) {
            params.put("addSellSys2", amount);
        } else if (type == LogCapitalTypeEnum.sell_sys3.getValue()) {
            params.put("addSellSys3", amount);
        } else if (type == LogCapitalTypeEnum.receive_invite.getValue()) {
            params.put("addInvite", amount);
        } else if (type == LogCapitalTypeEnum.guild_receive.getValue()) {
            params.put("addGuildSend", amount);
        } else if (type == LogCapitalTypeEnum.achievement_reward.getValue()) {
            params.put("addAchievement", amount);
        } else if (type == LogCapitalTypeEnum.balance_convert.getValue() || type == LogCapitalTypeEnum.cash_channel_income.getValue() || type == LogCapitalTypeEnum.c3_convert.getValue()) {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                params.put("addConvert", amount);
            } else {
                params.put("subConvert", amount);
            }
        } else if (type == LogCapitalTypeEnum.c3_to_rmb.getValue()) {
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                params.put("addC3ConvertRmb", amount);
            } else {
                params.put("subC3ConvertRmb", amount);
            }
        } else if (type == LogCapitalTypeEnum.friend_play_game.getValue()) {
            params.put("addFriendPlayGame", amount);
        } else if (type == LogCapitalTypeEnum.play_game.getValue()) {
            params.put("addPlayGame", amount);
        } else if (type == LogCapitalTypeEnum.daily_task.getValue()) {
            params.put("addDailyTask", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_ns.getValue()) {
            params.put("subNs", amount);
        } else if (type == LogCapitalTypeEnum.open_box.getValue() || type == LogCapitalTypeEnum.game_kill_ns.getValue() || type == LogCapitalTypeEnum.game_win_ns.getValue()) {
            params.put("addNs", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_nh.getValue()) {
            params.put("subNh", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_win_nh.getValue()) {
            params.put("addNh", amount);
        } else if (type == LogCapitalTypeEnum.add_magic.getValue() || type == LogCapitalTypeEnum.magic_convert.getValue()) {
            params.put("subMagic", amount);
        } else if (type == LogCapitalTypeEnum.receive_magic.getValue() || type == LogCapitalTypeEnum.get_magic_convert.getValue()) {
            params.put("addMagic", amount);
        } else if (type == LogCapitalTypeEnum.dz_dk_lq_reward.getValue()) {
            params.put("addDzGame", amount);
        } else if (type == LogCapitalTypeEnum.dz_kc_reward.getValue()) {
            params.put("subDzGame", amount);
        } else if (type == LogCapitalTypeEnum.lq_red_package.getValue()) {
            params.put("addRedGame", amount);
        } else if (type == LogCapitalTypeEnum.send_red_package.getValue()) {
            params.put("subRedGame", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_dts2.getValue()) {
            params.put("subDts2", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_win_dts2.getValue()) {
            params.put("addDts2", amount);
        } else if (type == LogCapitalTypeEnum.dts_rank_rebate.getValue()) {
            params.put("addDtsRebate", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_win_sg.getValue()) {
            params.put("addCq", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_sg.getValue()) {
            params.put("subCq", amount);
        } else if (type == LogCapitalTypeEnum.create_jsxm_gate.getValue()) {
            params.put("subCreateXm", amount);
        } else if (type == LogCapitalTypeEnum.contribution_jsxm_zj.getValue()) {
            params.put("subContribution", amount);
        } else if (type == LogCapitalTypeEnum.buy_jsxm_image.getValue()) {
            params.put("subBuyHead", amount);
        } else if (type == LogCapitalTypeEnum.update_jsxm_name.getValue()) {
            params.put("subUpdateName", amount);
        } else if (type == LogCapitalTypeEnum.jsxm_xm_reward.getValue()) {
            params.put("addXmReward", amount);
        } else if (type == LogCapitalTypeEnum.join_ancient.getValue()) {
            params.put("subJoinAncient", amount);
        } else if (type == LogCapitalTypeEnum.ancient_get_c2.getValue()) {
            params.put("addAncientC2", amount);
        } else if (type == LogCapitalTypeEnum.ancient_get_c5.getValue()) {
            params.put("addAncientC5", amount);
        } else if (type == LogCapitalTypeEnum.refresh.getValue()) {
            params.put("subRefresh", amount);
        } else if (type == LogCapitalTypeEnum.receive_cave.getValue()) {
            params.put("addCave", amount);
        } else if (type == LogCapitalTypeEnum.add_flag.getValue()) {
            params.put("subAddFlag", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_bt.getValue()) {
            params.put("subBt", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_win_bt.getValue()) {
            params.put("addBt", amount);
        } else if (type == LogCapitalTypeEnum.exchange_ticket.getValue()) {
            params.put("subTicket", amount);
        } else if (type == LogCapitalTypeEnum.shopping_magic.getValue()) {
            params.put("subShopMagic", amount);
        } else if (type == LogCapitalTypeEnum.receive_pop.getValue()) {
            params.put("addReceivePop", amount);
        } else if (type == LogCapitalTypeEnum.send_greeting_card.getValue()) {
            params.put("subSendGreetingCard", amount);
        } else if (type == LogCapitalTypeEnum.add_reward.getValue()) {
            params.put("addReward", amount);
        } else if (type == LogCapitalTypeEnum.receive_achievement.getValue()) {
            params.put("addReceiveAchievement", amount);
        } else if (type == LogCapitalTypeEnum.from_item.getValue()) {
            params.put("addFromItem", amount);
        } else if (type == LogCapitalTypeEnum.dispatch.getValue()) {
            params.put("addDispatch", amount);
        } else if (type == LogCapitalTypeEnum.wander.getValue()) {
            params.put("addWander", amount);
        } else if (type == LogCapitalTypeEnum.dice.getValue()) {
            params.put("addDice", amount);
        } else if (type == LogCapitalTypeEnum.buy_gift.getValue()) {
            params.put("subGift", amount);
        } else if (type == LogCapitalTypeEnum.buy_vip.getValue()) {
            params.put("subVip", amount);
        } else if (type == LogCapitalTypeEnum.afk.getValue()) {
            params.put("subAfk", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_escort.getValue()) {
            params.put("subGameEscort", amount);
        } else if (type == LogCapitalTypeEnum.game_escort_win.getValue()) {
            params.put("addGameEscort", amount);
        } else if (type == LogCapitalTypeEnum.buy_pass.getValue()) {
            params.put("subBuyPass", amount);
        } else if (type == LogCapitalTypeEnum.friend_buy.getValue()) {
            params.put("addFriendBuy", amount);
        } else if (type == LogCapitalTypeEnum.pet_buy.getValue()) {
            params.put("addPetBuy", amount);
        } else if (type == LogCapitalTypeEnum.receive_anima.getValue()) {
            params.put("addAnima", amount);
        } else if (type == LogCapitalTypeEnum.open_mine.getValue()) {
            params.put("subMine", amount);
        } else if (type == LogCapitalTypeEnum.game_bet_cards.getValue()) {
            params.put("subXhmj", amount);
        } else if (type == LogCapitalTypeEnum.game_cards_win.getValue()) {
            params.put("addXhmj", amount);
        }
        platformStatementService.updateStatement(params);
    }

    public void addItemStatement(int type, int num, Long itemId) {
        Map<String, Object> params = new HashMap<>();
        params.put("ymd", DateUtil.format9(new Date()));
        params.put("itemId", itemId);
        if (type == LogUserBackpackTypeEnum.events.getValue()) {
            params.put("addEvent", num);
        } else if (type == LogUserBackpackTypeEnum.shopping.getValue()) {
            params.put("addShop", num);
        } else if (type == LogUserBackpackTypeEnum.sign_reward.getValue()) {
            params.put("addSign", num);
        } else if (type == LogUserBackpackTypeEnum.prize_draw.getValue()) {
            params.put("addPrize", num);
        } else if (type == LogUserBackpackTypeEnum.elixir.getValue()) {
            params.put("addElixir", num);
        } else if (type == LogUserBackpackTypeEnum.achievement_reward.getValue()) {
            params.put("addAchievement", num);
        } else if (type == LogUserBackpackTypeEnum.listing.getValue()) {
            params.put("tradListing", num);
        } else if (type == LogUserBackpackTypeEnum.delist.getValue()) {
            params.put("tradDesting", num);
        } else if (type == LogUserBackpackTypeEnum.askbuy.getValue()) {
            params.put("tradAskBuy", num);
        } else if (type == LogUserBackpackTypeEnum.sell.getValue()) {
            params.put("tradSell", num);
        } else if (type == LogUserBackpackTypeEnum.buy.getValue()) {
            params.put("tradBuy", num);
        } else if (type == LogUserBackpackTypeEnum.cave_prize_draw.getValue()) {
            params.put("cavePrizeDraw", num);
        } else if (type == LogUserBackpackTypeEnum.search.getValue()) {
            params.put("search", num);
        } else if (type == LogUserBackpackTypeEnum.xm_buy.getValue()) {
            params.put("xmBuy", num);
        } else if (type == LogUserBackpackTypeEnum.ancient_get.getValue()) {
            params.put("ancientGet", num);
        }

        // backpackStatementService.updateStatement(params);
    }
}
