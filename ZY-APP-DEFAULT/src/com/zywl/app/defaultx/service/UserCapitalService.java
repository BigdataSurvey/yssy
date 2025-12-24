package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.CapitalTopVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.constant.TableNameConstant;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.TradingRecordTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserCapitalService extends DaoService {


    @Autowired
    private TradingRecordService tradingRecordService;


    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private AppConfigCacheService appConfigCacheService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private TransferRecordService transferRecordService;

    @Autowired
    private DzService dzService;


    @Autowired
    private DzInfoService dzInfoService;


    public UserCapitalService() {
        super("UserCapitalMapper");
    }

    @Transactional
    public void insertUserCapital(UserCapital userCapital) {
        userCapital.setCreateTime(new Date());
        userCapital.setUpdateTime(new Date());
        save(userCapital);
    }
    @Transactional
    public int updateUserCapital( List<Map<String, Object>> list) {
         return execute("betUpdateBalance2", list);
    }

    public List<UserCapital> findUserCapitalByUserId(Long userId) {
        Map<String,Object> parameters = new HashedMap<>();
        parameters.put("userId", userId);
        return findList("findUserCapitalByUserId", parameters);

    }

    /**
     * 检查资产是否重组
     * **/
    public UserCapital findUserCapitalByUserIdAndCapitalType(Long userId, Integer capitalType) {
        Map<String,Object> parameters = new HashedMap<>();
        parameters.put("userId", userId);
        parameters.put("capitalType", capitalType);
        return (UserCapital) findOne("findUserCapitalByUserIdAndCapitalType", parameters);
    }
    

    @Transactional
    public int addUserBalanceByUserId(Long userId,BigDecimal amount,Integer capitalType,long id,UserCapital userCapital){
        synchronized (LockUtil.getlock(userId+"")) {
            String orderNo = OrderUtil.getOrder5Number();
            int a = addUserBalance(amount,userId,capitalType,userCapital.getBalance(),userCapital.getOccupyBalance(),orderNo,id,LogCapitalTypeEnum.dz_dk_lq_reward,TableNameConstant.USER_DZ_RECORD);
            if(a<1){
                throwExp("领取失败");
            }
        }
        return 1;
    }@Transactional
    public int addUserBalanceByDonate(Long userId,BigDecimal amount,Integer capitalType,long id,UserCapital userCapital){
        synchronized (LockUtil.getlock(userId+"")) {
            String orderNo = OrderUtil.getOrder5Number();
            int a = addUserBalance(amount,userId,capitalType,userCapital.getBalance(),userCapital.getOccupyBalance(),orderNo,id,LogCapitalTypeEnum.dice,TableNameConstant.USER_DZ_RECORD);
            if(a<1){
                throwExp("领取失败");
            }
        }
        return 1;
    }


    @Transactional
    public int subBalanceByCash(Long userId, String orderNo, Long sourceDataId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore) {
        int a = subUserBalanceAndAddOccupyBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), balanceBefore, occupyBalanceBefore, orderNo, sourceDataId, LogCapitalTypeEnum.user_cash, TableNameConstant.CASH_ORDER);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = subUserBalanceAndAddOccupyBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.user_cash, TableNameConstant.CASH_ORDER);
            if (b < 1) {
                throwExp("执行失败");
            }

        }
        return a;
    }

    @Transactional
    public void betUpdateBalance(JSONObject obj) {
        int capitalType = UserCapitalTypeEnum.yyb.getValue();
        List<Map<String, Object>> list = new ArrayList<>();
        Set<String> set = obj.keySet();
        LogCapitalTypeEnum em = null;
        Map<String, BigDecimal> beforeMoney = new HashMap<>();

        for (String key : set) {
            Map<String, Object> map = new HashedMap<>();
            map.put("userId", key);
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(key), UserCapitalTypeEnum.currency_2.getValue());
            beforeMoney.put(key, userCapital.getBalance());
            JSONObject o = JSONObject.parse(obj.getString(key));
            map.put("amount", o.get("amount"));
            map.put("id",userCapital.getId());
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            map.put("capitalType", o.get("capitalType"));
            capitalType = o.getIntValue("capitalType");
            list.add(map);
        }
        int a = execute("betUpdateBalance", list);
        if (a < 1) {
            for (String key : set) {
                userCapitalCacheService.deltedUserCapitalCache(Long.parseLong(key), UserCapitalTypeEnum.currency_2.getValue());
            }
            if (em.getValue() == LogCapitalTypeEnum.game_bet.getValue()) {
                throwExp("灵石不足，参与失败！");
            } else {
                throwExp("结算失败！");
            }
        }
        if (a > 0) {
            for (String userId : set) {
                BigDecimal before;
                if (!beforeMoney.containsKey(userId)) {
                    before = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userId), UserCapitalTypeEnum.currency_2.getValue()).getBalance();
                } else {
                    before = beforeMoney.get(userId);
                }
                BigDecimal occupyBefore = BigDecimal.ZERO;
                JSONObject o = (JSONObject) obj.get(userId);
                userCapitalCacheService.add(Long.parseLong(userId), capitalType, o.getBigDecimal("amount"), BigDecimal.ZERO);
                pushLog(1, Long.parseLong(userId), capitalType, before, occupyBefore, o.getBigDecimal("amount"), em, (String) o.getOrDefault("orderNo", null), null, (String) o.getOrDefault("tableName", null));
            }
        }
    }

    //

    /**
     * 批量更新用户资产余额（仅仅用来解决大逃杀死锁问题）
     *  通过一次批量 SQL 完成余额变更,减少数据库锁竞争;
     *  在内存缓存与日志系统中同步变更
     * **/
    @Transactional
    public void betUpdateBalance2(JSONObject obj, int capitalType) {
        if (obj == null || obj.isEmpty()) {
            return;
        }

        // 组装批量更新参数,保证一个用户一条update
        List<Map<String, Object>> list = new ArrayList<>();
        Set<String> set = obj.keySet();
        LogCapitalTypeEnum em = null;

        // 记录变更前余额
        Map<String, BigDecimal> beforeMoney = new HashMap<>();

        for (String userIdStr : set) {
            // 取本用户的变更对象
            JSONObject o;
            Object raw = obj.get(userIdStr);

            if (raw instanceof JSONObject) {
                o = (JSONObject) raw;
            } else {
                o = JSONObject.parseObject(obj.getString(userIdStr));
            }

            if (o == null) { continue; }

            BigDecimal amount = o.getBigDecimal("amount");
            if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // 如果 payload 里带 capitalType，必须与入参一致，避免扣错币种
            Integer ctInObj = o.getInteger("capitalType");
            if (ctInObj != null && ctInObj != 0 && ctInObj.intValue() != capitalType) {
                throwExp("capitalType mismatch: param=" + capitalType + ", obj=" + ctInObj);
            }

            // 查询目标资本记录
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userIdStr), capitalType);
            beforeMoney.put(userIdStr, userCapital.getBalance());

            //资产校验
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                if (userCapital.getBalance().add(amount).compareTo(BigDecimal.ZERO) < 0) {
                    throwExp("用户[" + userIdStr + "]余额不足，无法扣费！当前余额:" + userCapital.getBalance());
                }
            }

            // 批量更新
            Map<String, Object> map = new HashedMap<>();
            map.put("userId", userIdStr);
            map.put("amount", amount);
            map.put("id", userCapital.getId());
            map.put("capitalType", capitalType);
            em = LogCapitalTypeEnum.getEm(o.getIntValue("em"));
            list.add(map);
        }

        if (list.isEmpty()) {
            return;
        }

        // 执行批量更新
        int a = execute("betUpdateBalance2", list);

        // 更新失败 清理缓存并抛错
        if (a < 1) {
            for (String userIdStr : set) {
                userCapitalCacheService.deltedUserCapitalCache(Long.parseLong(userIdStr), capitalType);
            }
            if (em != null && em.getValue() == LogCapitalTypeEnum.game_bet.getValue()) {
                throwExp("失败！");
            } else {
                throwExp("结算失败！");
            }
        }

        // 更新成功 更新缓存 + 写流水日志
        for (String userIdStr : set) {
            JSONObject o;
            Object raw = obj.get(userIdStr);
            if (raw instanceof JSONObject) {
                o = (JSONObject) raw;
            } else {
                o = JSONObject.parseObject(obj.getString(userIdStr));
            }
            if (o == null) continue;

            BigDecimal amount = o.getBigDecimal("amount");
            if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) continue;

            // 余额更新前值 用于日志
            BigDecimal before = beforeMoney.containsKey(userIdStr)
                    ? beforeMoney.get(userIdStr)
                    : userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(userIdStr), capitalType).getBalance();

            BigDecimal occupyBefore = BigDecimal.ZERO;

            // 更新缓存
            userCapitalCacheService.add(Long.parseLong(userIdStr), capitalType, amount, BigDecimal.ZERO);

            // 写流水
            pushLog(
                    1,
                    Long.parseLong(userIdStr),
                    capitalType,
                    before,
                    occupyBefore,
                    amount,
                    em,
                    (String) o.getOrDefault("orderNo", null),
                    null,
                    (String) o.getOrDefault("tableName", null)
            );
        }
    }





    @Transactional
    public void subUserBalanceByAskBuy(Long userId, Long itemId, BigDecimal amount, BigDecimal balance, BigDecimal occupyBalance) {
        // 求购不添加求购记录 只有资产流水 但是求购的资产会被冻结 需要解冻资产
        int a = subUserBalanceAndAddOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), balance, occupyBalance, null, null, LogCapitalTypeEnum.askbuy, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalanceAndAddOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.askbuy, null);
            if (b < 1) {
                throwExp("添加求购失败，请重试");
            }

        }
        // 清理缓存
    }

    @Transactional
    public void subUserBalanceByOpenPit(Long userId,  BigDecimal amount) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.buy_pit, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.buy_pit, null);
            if (b < 1) {
                throwExp("开通通行证失败，请重试");
            }
        }
    }

    /**
     * 交易行用户取消求购 添加用户资产 减少冻结资产
     */
    @Transactional
    public void addUserBalanceByCancelAskBuy(Long userId, Long itemId, BigDecimal amount) {
        // 查询撤销的的单子有多少钱
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        // 求购不添加求购记录 只有资产流水 但是求购的资产会被冻结 需要解冻资产
        int a = addUserBalanceAndSubOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.cancel_askbuy, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = addUserBalanceAndSubOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.cancel_askbuy, null);
            if (b < 1) {
                throwExp("取消求购失败，请重试！");
            }
        }
    }

    /**
     * 交易行用户购买物品 添加交易记录 减少用户资产
     */
    @Transactional
    public void subUserBalanceByTradingBuy(Long userId, Long tradId,Long itemId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, int number, BigDecimal price) {
        // 交易记录
        Long tradingId = tradingRecordService.addTradingRecord(userId,tradId, itemId, orderNo, amount, BigDecimal.ZERO, TradingRecordTypeEnum.buy.getValue(), TradingRecordTypeEnum.buy.getName(), number, price);

        // 扣除资产
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), balanceBefore, occupyBalanceBefore, orderNo, tradingId, LogCapitalTypeEnum.buy, TableNameConstant.TRADING_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, tradingId, LogCapitalTypeEnum.buy, TableNameConstant.TRADING_RECORD);
            if (b < 1) {
                throwExp("有其他玩家正在购买，请刷新后重新购买!");
            }

        }
    }

    @Transactional
    public void subUserBalanceByMzTradingBuy(Long userId,  BigDecimal amount,Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        // 扣除资产
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.buy, TableNameConstant.TRADING_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.buy, TableNameConstant.TRADING_RECORD);
            if (b < 1) {
                throwExp("有其他玩家正在购买，请刷新后重新购买!");
            }

        }
    }
    @Transactional
    public void addUserBalanceByTradingSell(Long userId,Long tradId, Long itemId, BigDecimal amount, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, int number, BigDecimal price) {
        BigDecimal tradingRate = appConfigCacheService.getTradingRate();
        BigDecimal fee = amount.multiply(tradingRate);
        // 交易记录
        Long tradingId = tradingRecordService.addTradingRecord(userId,tradId, itemId, orderNo, amount, fee, TradingRecordTypeEnum.sell.getValue(), TradingRecordTypeEnum.sell.getName(), number, price);
        // 增加资产
        int a = addUserBalance(amount.subtract(fee), userId, UserCapitalTypeEnum.currency_2.getValue(), balanceBefore, occupyBalanceBefore, orderNo, tradingId, LogCapitalTypeEnum.sell, TableNameConstant.TRADING_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = addUserBalance(amount.subtract(fee), userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, tradingId, LogCapitalTypeEnum.sell, TableNameConstant.TRADING_RECORD);
            if (b < 1) {
                throwExp("有其他玩家正在购买，请刷新后重新购买!");
            }
        }
    }

    /**
     * 查询资产排行榜
     */
    public List<CapitalTopVo> findCapitalTop(int capitalType) {
        Map<String, Object> params = new HashedMap<>();
        String value = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_TOP_NUMBER, Config.TOP_NUMBER);
        Integer number = Integer.parseInt(value);
        params.put("capitalType", capitalType);
        params.put("limit", number);
        return findList("findCapitalTop", params);

    }

    @Transactional
    public void addUserBalanceByPop(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.receive_pop, TableNameConstant.POP_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.receive_pop, TableNameConstant.POP_RECORD);
            if (b < 1) {
                throwExp("领取失败");
            }
        }
    }

    @Transactional
    public void addUserBalanceByMzTrad(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.sell, TableNameConstant.MZ_TRAD_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.sell, TableNameConstant.MZ_TRAD_RECORD);
            if (b < 1) {
                throwExp("失败");
            }
        }
    }

    @Transactional
    public void addUserBalanceByGameEscort(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.game_escort_win, TableNameConstant.GAME_ESCORT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.game_escort_win, TableNameConstant.GAME_ESCORT);
            if (b < 1) {
                throwExp("游戏结算失败");
            }
        }
    }

    @Transactional
    public void subUserBalanceByAFK(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.afk, TableNameConstant.MAIL);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.afk, TableNameConstant.MAIL);
            if (b < 1) {
                throwExp("快速作战失败，请重试");
            }
        }
    }

    public void addUserBalanceByReceiveInvite(BigDecimal amount, Long userId, int capitalType, Long dataId, String orderNo) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_invite, TableNameConstant.RECEIVE_INVITE_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_invite, TableNameConstant.RECEIVE_INVITE_RECORD);
            if (b < 1) {
                throwExp("领取奖励失败，请稍后重试");
            }
        }
    }

    @Transactional
    public void subUserBalanceByBuyPass(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.buy_pass, TableNameConstant.R_WEEK_DICE_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.buy_pass, TableNameConstant.R_WEEK_DICE_RECORD);
            if (b < 1) {
                throwExp("开通通行证失败，请重试");
            }
        }
    }
    @Transactional
    public int subUserOccupyBalanceByLotteryBet(Long userId, BigDecimal amount) {
        int a = subUserBalance2(amount, userId, UserCapitalTypeEnum.yyb.getValue());
        // 清理缓存
        if (a < 1) {
            throwExp(UserCapitalTypeEnum.yyb.getName()+"不足");
        }
        return a;
    }

    public void addUserBalanceByCancelBet(BigDecimal amount, Long userId, int capitalType, Long dataId, String orderNo) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.cancel_bet, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.cancel_bet, null);
            if (b < 1) {
                throwExp("返还失败");
            }
        }
    }


    public void addUserBalanceByAddReward(BigDecimal amount, Long userId,int capitalType,LogCapitalTypeEnum em) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType,userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, em, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId,  capitalType);
            throwExp("领取失败");
        }
    }

    public void addUserBalanceByAddBox(BigDecimal amount, Long userId,int capitalType,LogCapitalTypeEnum em) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType,userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, em, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId,  capitalType);
            throwExp("领取失败");
        }
    }

    public void addUserBalanceByCancelPit(BigDecimal amount, Long userId,int capitalType,LogCapitalTypeEnum em) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType,userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, em, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId,  capitalType);
            throwExp("退款失败");
        }
    }



    /**
     * 领取累计签到增加资产
     */
    public void addUserBalanceByReceiveTotalSign(BigDecimal amount, Long userId, int capitalType, Long dataId, String orderNo) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_total_sign_reward, TableNameConstant.TOTAL_SIGN_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_total_sign_reward, TableNameConstant.TOTAL_SIGN_RECORD);
            if (b < 1) {
                throwExp("领取奖励失败，请稍后重试");
            }
        }
    }

    /**
     * 领取奖池
     */
    public void addUserBalanceByPrizePool(BigDecimal amount, Long userId,  Long dataId, String orderNo) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.pirze_draw_reward, TableNameConstant.PRIZE_POOL);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            throwExp("领取奖励失败，请稍后重试");
        }
    }






    @Transactional
    public int addUserBalanceByReceiveGuild(Long userId, BigDecimal amount, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.guild_receive, TableNameConstant.GUILD_GRANT_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.guild_receive, TableNameConstant.GUILD_GRANT_RECORD);
            if (b < 1) {
                throwExp("发放失败");
            }
        }
        return a;
    }

    @Transactional
    public void addUserBalanceByDailyTask(Long userId, BigDecimal amount, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.daily_task, TableNameConstant.USER_DAILY_TASK_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.daily_task, TableNameConstant.USER_DAILY_TASK_RECORD);
            if (b < 1) {
                throwExp("领取失败，请重试");
            }
        }
    }

    @Transactional
    public void addUserBalanceByStatic(Long userId, String orderNo, Long dataId,BigDecimal amount) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_income, TableNameConstant.CONVERT_INCOME_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_income, TableNameConstant.CONVERT_INCOME_RECORD);
            if (b < 1) {
                throwExp("转至余额失败");
            }
        }
    }


    @Transactional
    public void subUserBalanceByOpenMine(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.open_mine, TableNameConstant.USER_OPEN_MINE_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.open_mine, TableNameConstant.USER_OPEN_MINE_RECORD);
            if (b < 1) {
                throwExp("开通矿场失败");
            }
        }
    }

    @Transactional
    public void subUserBalanceByBuyMz(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.open_mine, TableNameConstant.USER_OPEN_MINE_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.open_mine, TableNameConstant.USER_OPEN_MINE_RECORD);
            if (b < 1) {
                throwExp("开通矿场失败");
            }
        }
    }

    @Transactional
    public void subUserBalanceByBuyGift(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.buy_gift, TableNameConstant.BUY_GIFT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.buy_gift, TableNameConstant.BUY_GIFT);
            if (b < 1) {
                throwExp("购买每日礼包失败");
            }
        }
    }
    @Transactional
    public int addUserBalanceByDtsRank(Long userId, BigDecimal amount) {
        int a = addUserBalance2(amount, userId, UserCapitalTypeEnum.yyb.getValue());
        return a;
    }


    @Transactional
    public int subUserOccupyBalanceByDtsBet(Long userId, BigDecimal amount) {
        int a = subUserBalance2(amount, userId, UserCapitalTypeEnum.yyb.getValue());
        // 清理缓存
        if (a < 1) {
            throwExp(UserCapitalTypeEnum.yyb.getName()+"不足");
        }
        return a;
    }

    /**
     * 支援给好友玉石 扣除from用户资产 增加to用户资产
     */
    @Transactional
    public void subUserBalanceByTransferFriend(Long fromUserId, Long toUserId, BigDecimal amount, String orderNo) {
        // 交易记录
        Long transferRecordId = transferRecordService.addTransferRecord(orderNo, fromUserId, toUserId, amount);
        UserCapital fromUserCapital = userCapitalCacheService.getUserCapitalCacheByType(fromUserId, UserCapitalTypeEnum.currency_2.getValue());
        // 扣除资产
        int a = subUserBalance(amount, fromUserId, UserCapitalTypeEnum.currency_2.getValue(), fromUserCapital.getBalance(), fromUserCapital.getOccupyBalance(), orderNo, transferRecordId, LogCapitalTypeEnum.transfer_friend, TableNameConstant.TRANSFER_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(fromUserId, UserCapitalTypeEnum.currency_2.getValue());
            userCapitalCacheService.deltedUserCapitalCache(toUserId, UserCapitalTypeEnum.currency_2.getValue());
            fromUserCapital = userCapitalCacheService.getUserCapitalCacheByType(fromUserId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, fromUserId, UserCapitalTypeEnum.currency_2.getValue(), fromUserCapital.getBalance(), fromUserCapital.getOccupyBalance(), orderNo, transferRecordId, LogCapitalTypeEnum.transfer_friend, TableNameConstant.TRANSFER_RECORD);
            if (b < 1) {
                throwExp("邮件支援失败，请稍后重试");
            }
        }
    }


    @Transactional
    public void subUserBalanceByGameEscort(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.game_bet_escort, TableNameConstant.GAME_ESCORT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.game_bet_escort, TableNameConstant.GAME_ESCORT);
            if (b < 1) {
                throwExp("参与失败");
            }
        }
    }
    @Transactional
    public int subUserBalanceByUserId(Long userId,BigDecimal amount,Integer capitalType,Integer periods,UserCapital userCapital){
        long a ;
//        synchronized (LockUtil.getlock(userId+"")) {
        //玩家可以重复报名
        UserDzRecord record =   dzService.selectByUserIdAndPeriods(userId,periods);
        if(null == record){
            a = dzService.addUserRecord(userId,amount,periods);
            if(a<1 || dzInfoService.addUserRecordInfo(userId, periods, amount) < 1){
                throwExp("执行失败!!!");
            }
        }else {
            //update
            a = record.getId();
            record.setDzMoney(record.getDzMoney().add(amount));
            long c = dzService.updateUsersDzMoney(record);
            UserDzRecordInfo userDzRecordInfo = new UserDzRecordInfo();
            userDzRecordInfo.setUserId(userId);
            userDzRecordInfo.setPeriods(periods);
            userDzRecordInfo.setDzMoney(record.getDzMoney());
            if(c<1 || dzInfoService.updateUsersDzInfo(userDzRecordInfo) < 1){
                throwExp("执行失败!!");
            }
        }
//        }
        String orderNo = OrderUtil.getOrder5Number();
        int b = subUserBalance(amount,userId,userCapital.getCapitalType(),userCapital.getBalance(),userCapital.getOccupyBalance(),orderNo,a,LogCapitalTypeEnum.dz_kc_reward,TableNameConstant.USER_DZ_RECORD);
        if(b<1){
            throwExp("执行失败!");
        }
        return b;
    }


    @Transactional
    public void subUserBalanceByGameCards(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.game_bet_cards, TableNameConstant.GAME_CARDS);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.game_cards_win, TableNameConstant.GAME_CARDS);
            if (b < 1) {
                throwExp("参与失败");
            }
        }
    }

    @Transactional
    public void subUserBalanceByChat(Long userId, BigDecimal amount, String orderNo, Long sourceDataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.to_lottery, TableNameConstant.CHAT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, sourceDataId, LogCapitalTypeEnum.to_lottery, TableNameConstant.CHAT);
            if (b < 1) {
                throwExp("发送失败");
            }
        }
    }


    public void subUserBalanceByShopping(Long userId, BigDecimal amount, String orderNo, Long dataId, int capitalType,LogCapitalTypeEnum em) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, em, TableNameConstant.SHOPPING_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, em, TableNameConstant.SHOPPING_RECORD);
            if (b < 1) {
                throwExp("购买道具失败");
            }

        }
    }

    @Transactional
    public void addUserBalanceByGuild(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.guild, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.guild, null);
            if (b < 1) {
                throwExp("公会拒绝失败");
            }
        }
    }

    @Transactional
    public void subUserBalanceByBuyUserNo(Long userId, BigDecimal amount) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.buy_user_no, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.buy_user_no, null);
            if (b < 1) {
                throwExp("购买靓号失败");
            }
        }
    }

    @Transactional
    public void subUserBalanceByGuild(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.guild, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.guild, null);
            if (b < 1) {
                throwExp("创建公会失败");
            }
        }
    }
    @Transactional
    public void subUserBalanceBySendGreetingCard(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.send_greeting_card, TableNameConstant.GREETING_CARD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.send_greeting_card,  TableNameConstant.GREETING_CARD);
            if (b < 1) {
                throwExp("赠送贺卡失败");
            }
        }
    }


    //扣除用户资产并清理缓存
    @Transactional
    public void subUserBalanceBySendMail(Long userId,BigDecimal amount, Integer capitalType,String orderNo,Long sourceDataId,LogCapitalTypeEnum logType) {
        // 默认资产类型为核心积分
        if (capitalType == null) {
            capitalType = UserCapitalTypeEnum.hxjf.getValue();
        }
        // 默认日志类型为send_mail
        if (logType == null) {
            logType = LogCapitalTypeEnum.send_mail;
        }
        //从缓存获取用户资产
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);

        int a = subUserBalance(
                amount,
                userId,
                capitalType,
                userCapital.getBalance(),
                userCapital.getOccupyBalance(),
                orderNo,
                sourceDataId,
                logType,
                TableNameConstant.MAIL
        );
        if (a < 1) {
            // 清理缓存后重试一次
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService
                    .getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(
                    amount,
                    userId,
                    capitalType,
                    userCapital.getBalance(),
                    userCapital.getOccupyBalance(),
                    orderNo,
                    sourceDataId,
                    logType,
                    TableNameConstant.MAIL
            );
            if (b < 1) {
                throwExp("扣除资产失败，请稍后重试");
            }
        }
    }



    @Transactional
    public void subUserBalanceByVip(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.buy_vip, TableNameConstant.R_BUY_VIP_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.buy_vip, TableNameConstant.R_BUY_VIP_RECORD);
            if (b < 1) {
                throwExp("开通失败");
            }
        }
    }
    @Transactional
    public void subUserBalanceBySweep(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.join_ancient, TableNameConstant.JOIN_ANCIENT_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.join_ancient, TableNameConstant.JOIN_ANCIENT_RECORD);
            if (b < 1) {
                throwExp("扫荡失败");
            }
        }
    }
    @Transactional
    public void subUserBalanceByRefreshShop(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.join_ancient, TableNameConstant.JOIN_ANCIENT_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.join_ancient, TableNameConstant.JOIN_ANCIENT_RECORD);
            if (b < 1) {
                throwExp("刷新失败");
            }
        }
    }
    @Transactional
    public void subUserBalanceByBuyCoin(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.buy_coin, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.buy_coin, null);
            if (b < 1) {
                throwExp("购买铜钱失败");
            }
        }
    }

    //红包炸弹扣钱
    @Transactional
    public void subUserBalanceByRedZd(Long userId, BigDecimal amount, Long dataId,String orderNo) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sub_red_zd, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sub_red_zd, null);
            if (b < 1) {
                throwExp("购买铜钱失败");
            }
        }
    }

    @Transactional
    public void subUserBalanceByBuyRed(Long userId, BigDecimal amount, Long dataId) {
        int capitalType = UserCapitalTypeEnum.currency_2.getValue();
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.send_red_package, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, dataId, LogCapitalTypeEnum.send_red_package, null);
            if (b < 1) {
                throwExp("购买铜钱失败");
            }
        }
    }





    public void subUserBalanceByUpdateIdCard(Long userId, BigDecimal amount, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.update_idCard, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.update_idCard, null);
            if (b < 1) {
                throwExp("修改实名失败，请稍后重试");
            }
        }
    }

    public void subUserBalanceByBuyHandbook(Long userId, BigDecimal amount, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.buy_handbook, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.buy_handbook, null);
            if (b < 1) {
                throwExp("购买失败，请稍后重试");
            }
        }
    }




    public void subUserBalanceByCavePrizeDraw(Long userId, BigDecimal amount, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.cave_prize_draw, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = subUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), null, null, LogCapitalTypeEnum.cave_prize_draw, null);
            if (b < 1) {
                throwExp("开启宝箱失败，请稍后重试");
            }

        }
    }


    /**
     * 提现成功 减少冻结资产
     */
    @Transactional
    public void subUserOccupyBalanceByCashSuccess(Long userId, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, BigDecimal amount, Long recordId) {
        // 减少冻结资产
        int a = subUserOccupyBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), balanceBefore, occupyBalanceBefore, orderNo, recordId, LogCapitalTypeEnum.cash_succrss, TableNameConstant.CASH_ORDER);
        // 清理缓存
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = subUserOccupyBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, recordId, LogCapitalTypeEnum.cash_succrss, TableNameConstant.CASH_ORDER);
            // 清理缓存
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
                throwExp("操作失败，请重试！");
            }
            throwExp("操作失败，请重试！");
        }
    }

    /**
     * 提现失败 恢复余额
     */
    @Transactional
    public void subUserOccupyBalanceByCashFail(BigDecimal amount, Long userId, BigDecimal balanceBefore, BigDecimal occupyBalance, String orderNo, Long dataId) {
        int a = addUserBalanceAndSubOccupyBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), balanceBefore, occupyBalance, orderNo, dataId, LogCapitalTypeEnum.cash_fail, TableNameConstant.CASH_ORDER);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = addUserBalanceAndSubOccupyBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.cash_fail, TableNameConstant.CASH_ORDER);
            if (b < 1) {
                throwExp("提现失败时恢复余额失败！");
            }
        }
        // 清理缓存

    }



    /**
     * 得到求购物品 减少冻结资产
     */
    @Transactional
    public int subUserOccupyBalanceByAskBuyItem(Long userId, Long tradId,Long itemId, BigDecimal price, int number, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo) {
        BigDecimal amount = price.multiply(new BigDecimal(number));
        // 交易记录
        Long tradingId = tradingRecordService.addTradingRecord(userId, tradId,itemId, orderNo, amount, BigDecimal.ZERO, TradingRecordTypeEnum.askbuy.getValue(), TradingRecordTypeEnum.askbuy.getName(), number, price);
        // 减少冻结资产
        int a = subUserOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), balanceBefore, occupyBalanceBefore, orderNo, tradingId, LogCapitalTypeEnum.askbuy_sucess, TableNameConstant.TRADING_RECORD);
        // 清理缓存
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, tradingId, LogCapitalTypeEnum.askbuy_sucess, TableNameConstant.TRADING_RECORD);
            if (b < 1) {
                return b;
            }
        }

        return a;
    }


    /**
     * 得到求购物品 减少冻结资产
     */
    @Transactional
    public int subUserOccupyBalanceByAskBuyGift(Long userId, Long tradId,Long itemId, BigDecimal price, int number, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo) {
        BigDecimal amount = price.multiply(new BigDecimal(number));

        Long tradingId = tradingRecordService.addTradingRecord(userId, tradId,itemId, orderNo, amount, BigDecimal.ZERO, TradingRecordTypeEnum.askbuy.getValue(), TradingRecordTypeEnum.askbuy.getName(), number, price);

        // 减少冻结资产
        int a = subUserOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), balanceBefore, occupyBalanceBefore, orderNo, tradingId, LogCapitalTypeEnum.askbuy_sucess, TableNameConstant.TRADING_RECORD);
        // 清理缓存
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = subUserOccupyBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, tradingId, LogCapitalTypeEnum.askbuy_sucess, TableNameConstant.TRADING_RECORD);
            if (b < 1) {
                return b;
            }
        }

        return a;
    }

    @Transactional
    public int subUserGift(BigDecimal amount, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, Long sourceDataId, LogCapitalTypeEnum em, String tableName) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        //数量
        params.put("amount", amount);
        params.put("occupyBalance", occupyBalanceBefore);
        int a = execute("subUserOccupyBalance", params);
        if (a >= 1) {
            userCapitalCacheService.sub(userId, capitalType, BigDecimal.ZERO, amount);
            pushLog(2, userId, capitalType, balanceBefore, occupyBalanceBefore, amount.negate(), em, orderNo, sourceDataId, tableName);
        }
        return a;
    }


    @Transactional
    public void subUserBalanceByExchange(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.exchange, TableNameConstant.EXCHANGE_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.exchange, TableNameConstant.EXCHANGE_RECORD);
            if (b < 1) {
                throwExp("扣除余额失败");
            }
        }
    }







    //试玩增加货币
    public void addUserBalanceByPlayTest(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.play_game, TableNameConstant.DUO_YOU_ORDER);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.play_game, TableNameConstant.DUO_YOU_ORDER);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
                throwExp("试玩增加灵石失败");
            }
        }
    }


    //抢红包加钱
    public void addUserBalanceByGetRed(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.lq_red_package, null);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.lq_red_package, null);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
                throwExp("操作失败");
            }
        }
    }


    public void addUserBalanceByReceiveFriend(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_income, TableNameConstant.CONVERT_INCOME_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.receive_income, TableNameConstant.CONVERT_INCOME_RECORD);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
                throwExp("领取收益失败");
            }
        }
    }
    public void addUserBalanceByFriendPlayGame(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.friend_play_game, TableNameConstant.DUO_YOU_ORDER);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());

            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.friend_play_game, TableNameConstant.DUO_YOU_ORDER);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
                throwExp("增加余额失败");
            }
        }
    }

    public void addUserBalanceByCashChannelIncome(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
        int a = addUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.cash_channel_income, TableNameConstant.CASH_CHANNEL_INCOME);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());

            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = addUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.cash_channel_income, TableNameConstant.CASH_CHANNEL_INCOME);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
                throwExp("增加余额失败");
            }
        }
    }

    // 用户完成成就奖励货币
    public void addUserBalanceByAchievement(BigDecimal amount, Long userId, String orderNo, Long dataId, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.achievement_reward, TableNameConstant.COMPLETE_ACHIEVEMENT_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);


            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.achievement_reward, TableNameConstant.COMPLETE_ACHIEVEMENT_RECORD);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
                throwExp("领取成就奖励失败");
            }
        }
    }

    // 用户完成成就奖励货币
    public void addUseeBalancePrize(BigDecimal amount, Long userId, String orderNo, Long dataId, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.dic_prize, TableNameConstant.COMPLETE_ACHIEVEMENT_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);


            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.dic_prize, TableNameConstant.COMPLETE_ACHIEVEMENT_RECORD);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
                throwExp("领取成就奖励失败");
            }
        }
    }

    // 用户出售道具给系统
    public void addUserBalanceBySellToSys(BigDecimal amount, Long userId, String orderNo, Long dataId, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sell_sys, TableNameConstant.SELL_SYS_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sell_sys, TableNameConstant.SELL_SYS_RECORD);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
                throwExp("道具出售失败");
            }
        }
    }
    public void addUserBalanceBySellToSys2(BigDecimal amount, Long userId, String orderNo, Long dataId, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sell_sys2, TableNameConstant.SELL_SYS_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sell_sys2, TableNameConstant.SELL_SYS_RECORD);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
                throwExp("道具出售失败");
            }
        }
    }
    public void addUserBalanceBySellToSys3(BigDecimal amount, Long userId, String orderNo, Long dataId, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        int a = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sell_sys3, TableNameConstant.SELL_SYS_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);

            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
            int b = addUserBalance(amount, userId, capitalType, userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sell_sys3, TableNameConstant.SELL_SYS_RECORD);
            if (b < 1) {
                userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
                throwExp("道具出售失败");
            }
        }
    }
    public void pushLog1(int type, Long userId, Integer capitalType, long number, LogCapitalTypeEnum em, String orderNo,String tableName) {
        Map<String,Object> a = new HashedMap<>();
        a.put("type", type);
        a.put("userId", userId);
        a.put("capitalType", capitalType);
        a.put("amount", number);
        a.put("em", em);
        a.put("orderNo", orderNo);
        a.put("tableName", tableName);
        Push.push(PushCode.insertLog, null, a);
    }
    public void pushLog(int type, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, BigDecimal amount, LogCapitalTypeEnum em, String orderNo, Long sourceDataId, String tableName) {
        Map<String,Object> a = new HashedMap<>();
        a.put("logType", 1);
        a.put("type", type);
        a.put("userId", userId);
        a.put("capitalType", capitalType);
        a.put("balanceBefore", balanceBefore);
        a.put("occupyBalanceBefore", occupyBalanceBefore);
        a.put("amount", amount);
        a.put("em", em);
        a.put("orderNo", orderNo);
        a.put("sourceDataId", sourceDataId);
        a.put("tableName", tableName);
        Push.push(PushCode.insertLog, null, a);
    }

    @Transactional
    public int addUserBalance(BigDecimal amount, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, Long sourceDataId, LogCapitalTypeEnum em, String tableName) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        params.put("amount", amount);
        params.put("balance", balanceBefore);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,capitalType);
        params.put("id",userCapital.getId());
        int a = execute("addUserBalance", params);
        if (a >= 1) {
            userCapitalCacheService.add(userId, capitalType, amount, BigDecimal.ZERO);
            pushLog(1, userId, capitalType, balanceBefore, occupyBalanceBefore, amount, em, orderNo, sourceDataId, tableName);
        }

        return a;
    }

    @Transactional
    public int subUserBalanceAndAddOccupyBalance(BigDecimal amount, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, Long sourceDataId, LogCapitalTypeEnum em, String tableName) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        params.put("amount", amount);
        params.put("balance", balanceBefore);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,capitalType);
        params.put("id",userCapital.getId());
        int a = execute("subUserBalanceAndAddOccupyBalance", params);
        if (a >= 1) {
            userCapitalCacheService.sub(userId, capitalType, amount, amount.negate());
        }
        if (a < 1) {

            return a;
        }
        pushLog(3, userId, capitalType, balanceBefore, occupyBalanceBefore, amount.negate(), em, orderNo, sourceDataId, tableName);

        return a;
    }


    /**
     * 扣除用户资产;
     * **/
    @Transactional
    public int subUserBalance(BigDecimal amount, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, Long sourceDataId, LogCapitalTypeEnum em, String tableName) {
        if (amount.compareTo(BigDecimal.ZERO) == -1) {
            throwExp("扣除不能小于0");
        }
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        params.put("amount", amount);
        params.put("balance", balanceBefore);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,capitalType);
        params.put("id",userCapital.getId());
        int a = execute("subUserBalance", params);
        if (a >= 1) {
            userCapitalCacheService.sub(userId, capitalType, amount, BigDecimal.ZERO);
            pushLog(1, userId, capitalType, balanceBefore, occupyBalanceBefore, amount.negate(), em, orderNo, sourceDataId, tableName);
        }
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalType);
            return a;
        }
        return a;
    }

    @Transactional
    public int subUserBalance2(BigDecimal amount, Long userId, Integer capitalType) {
        if (amount.compareTo(BigDecimal.ZERO) == -1) {
            throwExp("扣除不能小于0");
        }
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        params.put("amount", amount);
        return execute("subUserBalance2", params);
    }

    @Transactional
    private int addUserBalance2(BigDecimal amount, Long userId, Integer capitalType) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        params.put("amount", amount);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,capitalType);
        params.put("id",userCapital.getId());
        return execute("addUserBalance2", params);
    }


    @Transactional
    public int addUserBalanceAndSubOccupyBalance(BigDecimal amount, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, Long sourceDataId, LogCapitalTypeEnum em, String tableName) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        params.put("amount", amount);
        params.put("balance", balanceBefore);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,capitalType);
        params.put("id",userCapital.getId());
        int a = execute("addUserBalanceAndSubOccupyBalance", params);
        if (a >= 1) {
            userCapitalCacheService.add(userId, capitalType, amount, amount.negate());
        }
        if (a < 1) {
            return a;
        }
        if (a >= 1) {
            pushLog(3, userId, capitalType, balanceBefore, occupyBalanceBefore, amount, em, orderNo, sourceDataId, tableName);
        }
        return a;
    }

    @Transactional
    public int subUserOccupyBalance(BigDecimal amount, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, String orderNo, Long sourceDataId, LogCapitalTypeEnum em, String tableName) {
        Map<String,Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("capitalType", capitalType);
        //数量
        params.put("amount", amount);
        params.put("occupyBalance", occupyBalanceBefore);
        int a = execute("subUserOccupyBalance", params);
        if (a >= 1) {
            userCapitalCacheService.sub(userId, capitalType, BigDecimal.ZERO, amount);
            pushLog(2, userId, capitalType, balanceBefore, occupyBalanceBefore, amount.negate(), em, orderNo, sourceDataId, tableName);
        }
        return a;
    }

    @Transactional
    public void subBalanceByGift(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(), userCapital.getBalance(),
                userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.bug_role_gift, TableNameConstant.BUY_GIFT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(),
                    userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.exchange, TableNameConstant.EXCHANGE_RECORD);
            if (b < 1) {
                throwExp("扣除余额失败");
            }
        }
    }


    //TODO  hk
    @Transactional
    public void subShopManager(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.currency_2.getValue(),
                userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sub_shop_manager, TableNameConstant.BUY_GIFT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(),
                    userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sub_shop_manager, TableNameConstant.EXCHANGE_RECORD);
            if (b < 1) {
                throwExp("扣除余额失败");
            }
        }
    }

    //TODO  hk
    @Transactional
    public void subJingGangLing(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.score.getValue(), userCapital.getBalance(),
                userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sub_convert_total, TableNameConstant.BUY_GIFT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(),
                    userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.sub_convert_total, TableNameConstant.EXCHANGE_RECORD);
            if (b < 1) {
                throwExp("扣除余额失败");
            }
        }
    }

    @Transactional
    public void subFanPai(BigDecimal amount, Long userId, String orderNo, Long dataId) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.yyb.getValue());
        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.yyb.getValue(), userCapital.getBalance(),
                userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.add_reward, TableNameConstant.BUY_GIFT);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(),
                    userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.exchange, TableNameConstant.EXCHANGE_RECORD);
            if (b < 1) {
                throwExp("扣除余额失败");
            }
        }
    }

//    @Transactional
//    public void subUserBalanceByExchange(BigDecimal amount, Long userId, String orderNo, Long dataId) {
//        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
//        int a = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.exchange, TableNameConstant.EXCHANGE_RECORD);
//        if (a < 1) {
//            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
//            userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
//            int b = subUserBalance(amount, userId, UserCapitalTypeEnum.rmb.getValue(), userCapital.getBalance(), userCapital.getOccupyBalance(), orderNo, dataId, LogCapitalTypeEnum.exchange, TableNameConstant.EXCHANGE_RECORD);
//            if (b < 1) {
//                throwExp("扣除余额失败");
//            }
//        }
//    }



    @Transactional
    public void batchUpdateCoin(List<UserCapital> capitals) {
        if (capitals != null) {
            List<UserCapital> newList = new ArrayList<>();
            for (int i = 0; i < capitals.size(); i++) {
                newList.add(capitals.get(i));
                if (i % 1000 == 0) {
                    execute("batchUpdateCoin", newList);
                    newList.clear();
                }
            }
            if (!newList.isEmpty()) {
                execute("batchUpdateCoin", newList);
            }
        }
    }

    @Transactional
    public void batchUpdateByNs(JSONArray array) {
        execute("batchUpdateByNs", array);
    }

    public BigDecimal findSumBalance(int capitalType) {

        BigDecimal balance = BigDecimal.ZERO;
        Map<String,Object> params = new HashedMap<>();
        params.put("capitalType", capitalType);
        Map<String, BigDecimal> findSumBalance = (Map<String, BigDecimal>) findOne("findSumBalance", params);
        if (findSumBalance != null && findSumBalance.containsKey("allBalance")) {
            balance = balance.add(findSumBalance.get("allBalance"));
        }
        return balance;
    }
    
    public BigDecimal findNo1(int capitalType){
        BigDecimal balance = BigDecimal.ZERO;
        Map<String,Object> params = new HashedMap<>();
        params.put("capitalType", capitalType);
        UserCapital findNo1 = (UserCapital) findOne("findNo1", params);
        if (findNo1 != null ) {
            return findNo1.getBalance();
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal findSumBalance2(int capitalType) {

        BigDecimal balance = BigDecimal.ZERO;
        Map<String,Object> params = new HashedMap<>();
        params.put("capitalType", capitalType);
        Map<String, BigDecimal> findSumBalance = (Map<String, BigDecimal>) findOne("findSumBalance2", params);
        if (findSumBalance != null && findSumBalance.containsKey("allBalance")) {
            balance = balance.add(findSumBalance.get("allBalance"));
        }
        return balance;
    }
    public BigDecimal findSumBalanceNo10(int capitalType) {
        BigDecimal balance = BigDecimal.ZERO;
        Map<String,Object> params = new HashedMap<>();
        params.put("capitalType", capitalType);
        Map<String, BigDecimal> findSumBalance = (Map<String, BigDecimal>) findOne("findSumBalanceNo10", params);
        if (findSumBalance != null && findSumBalance.containsKey("allBalance")) {
            balance = balance.add(findSumBalance.get("allBalance"));
        }
        return balance;
    }


    @Transactional
    public void deletedOneMonthNoLogin(List<User> users) {
        for (User user : users) {
            Map<String,Object> params = new HashedMap<>();
            params.put("userId", user.getId());
            execute("deletedOneMonthNoLogin", params);
        }

    }
    @Transactional
    public void assetConversion(int sourceType, int targetType, BigDecimal amount, Long userId, String orderNo, Long dataId, BigDecimal targetAddBalance, LogCapitalTypeEnum em) {
        UserCapital sourceUserCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, sourceType);
        UserCapital targetUserCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, targetType);
        int a = subUserBalance(amount, userId, sourceType, sourceUserCapital.getBalance(), sourceUserCapital.getOccupyBalance(), orderNo, dataId, em, TableNameConstant.BALANCE_CONVERT_RECORD);
        if (a < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            throwExp("兑换失败，请稍后重试");
        }
        int b = addUserBalance(targetAddBalance, userId, targetType, targetUserCapital.getBalance(), targetUserCapital.getOccupyBalance(), orderNo, dataId, em, TableNameConstant.BALANCE_CONVERT_RECORD);
        if (a < 1 || b < 1) {
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.currency_2.getValue());
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.rmb.getValue());
            throwExp("兑换失败，请稍后重试");
        }
        // 清理缓存
    }




    //+++++++++++++++++++++++++++++++++++++++++++++++




}
