package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.DicVip;
import com.zywl.app.base.bean.TVipGiftRecord;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.VipReceiveRecord;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.VipLevelTypeEnum;
import com.zywl.app.defaultx.service.MailService;
import com.zywl.app.defaultx.service.TVipGiftRecordService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.VipReceiveRecordService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Author: lzx
 * @Create: 2026-01-19
 * @Version: V1.0
 * @Description: VIP 业务
 * @Task: 9008 (MessageCodeContext.USER_VIP)
 */
@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ManagerUserVipService extends BaseService {

    /** claimStatus = 0 不可领取（未开通/过期） */
    private static final int CLAIM_STATUS_DISABLED = 0;
    /** claimStatus = 1 可领取 */
    private static final int CLAIM_STATUS_CAN_CLAIM = 1;
    /** claimStatus = 2 已领取 */
    private static final int CLAIM_STATUS_ALREADY = 2;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private VipReceiveRecordService vipReceiveRecordService;

    @Autowired
    private TVipGiftRecordService tVipGiftRecordService;

    @Autowired
    private MailService mailService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;


    /**
     * 900801
     * 获取该用户的VIP面板信息
     */
    @ServiceMethod(code = "001", description = "VIP面板信息")
    @Transactional
    public JSONObject getVipPanelInfo(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");

        synchronized (LockUtil.getlock(userId)) {
            User user = loadAndCheckUser(userId);
            long now = System.currentTimeMillis();

            DicVip vip1Cfg = getVipCfg((int) VipLevelTypeEnum.VIP1.getLevel());
            DicVip vip2Cfg = getVipCfg((int) VipLevelTypeEnum.VIP2.getLevel());

            Date vip1ExpireTime = user.getVipExpireTime();
            Date vip2ExpireTime = user.getVip2ExpireTime();

            boolean vip1Active = isActive(user.getVip1(), vip1ExpireTime, now);
            boolean vip2Active = isActive(user.getVip2(), vip2ExpireTime, now);

            int vip1CardCount = (int) gameService.getUserItemNumber(userId, String.valueOf(vip1Cfg.getCardItemId()));
            int vip2CardCount = (int) gameService.getUserItemNumber(userId, String.valueOf(vip2Cfg.getCardItemId()));

            int claimStatus = calcVip1ClaimStatus(userId, vip1Active);

            long tomorrowBegin = DateUtil.getDateBeginByDay(1).getTime();
            long nextResetTime = tomorrowBegin;
            if (vip1ExpireTime != null) {
                nextResetTime = Math.min(tomorrowBegin, vip1ExpireTime.getTime());
            }

            JSONArray vipConfigList = buildVipConfigList(vip1Cfg, vip2Cfg);

            JSONObject result = new JSONObject();
            result.put("serverTime", now);
            result.put("vipConfigList", vipConfigList);

            result.put("vip1ExpireTime", vip1ExpireTime == null ? 0 : vip1ExpireTime.getTime());
            result.put("vip2ExpireTime", vip2ExpireTime == null ? 0 : vip2ExpireTime.getTime());

            result.put("vip1Active", vip1Active ? 1 : 0);
            result.put("vip2Active", vip2Active ? 1 : 0);

            result.put("vip1CardCount", vip1CardCount);
            result.put("vip2CardCount", vip2CardCount);

            result.put("claimStatus", claimStatus);
            result.put("nextResetTime", nextResetTime);

            if (StringUtils.isNotBlank(vip1Cfg.getDailyReward())) {
                try {
                    result.put("vip1RewardPreview", JSONArray.parseArray(vip1Cfg.getDailyReward()));
                } catch (Exception e) {
                    result.put("vip1RewardPreview", new JSONArray());
                }
            } else {
                result.put("vip1RewardPreview", new JSONArray());
            }


            result.put("vipTransferEnable", user.getVipTransferEnable() == null ? 0 : user.getVipTransferEnable());

            return result;
        }
    }


    /**
     * 900802
     * 自购开通/续期（VIP1/VIP2）
     */
    @ServiceMethod(code = "002", description = "自购开通/续期VIP")
    @Transactional
    public JSONObject buyOrRenewVip(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkNull(params.get("vipType"));

        Long userId = params.getLong("userId");
        Integer vipType = params.getInteger("vipType");

        if (vipType == null || (vipType != VipLevelTypeEnum.VIP1.getLevel() && vipType != VipLevelTypeEnum.VIP2.getLevel())) {
            throwExp(String.format("vipType 参数错误, VIP类型仅支持 1/2.{用户ID: %s, 当前VIP类型: %s,}",
                    userId, vipType));
        }

        synchronized (LockUtil.getlock(userId)) {
            User user = loadAndCheckUser(userId);
            DicVip vipCfg = getVipCfg(vipType);

            BigDecimal price = vipCfg.getPrice();
            Integer capitalTypeId = vipCfg.getCapitalTypeId();

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                throwExp("VIP配置price错误");
            }
            if (capitalTypeId == null || capitalTypeId <= 0) {
                throwExp("VIP配置capitalTypeId错误");
            }

            UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalTypeId);
            if (userCapital == null || userCapital.getBalance() == null) {
                //throwExp(String.format("用户资产不足, 无法开通VIP服务.{用户ID: %s, 资产类型: %s, VIP类型: %s}",userId, capitalTypeId, vipType));
                throwExp(String.format("用户资产不足, 无法开通VIP服务!"));

            }

            BigDecimal balanceBefore = userCapital.getBalance();
            if (balanceBefore == null) {
                balanceBefore = BigDecimal.ZERO;
            }

            if (balanceBefore.compareTo(price) < 0) {
                throwExp(String.format("用户资产不足, 无法开通VIP服务!"));
            }

            String orderNo = "VIP_BUY_" + vipType + "_" + OrderUtil.getOrder32Number();
            //扣费
            userCapitalService.subUserBalanceByOpenVip(userId, price, capitalTypeId, orderNo, null, LogCapitalTypeEnum.buy_vip);

            // 清理资产缓存 + 推送资产更新
            userCapitalCacheService.deltedUserCapitalCache(userId, capitalTypeId);
            managerGameBaseService.pushCapitalUpdate(userId, capitalTypeId);

            Date oldExpire = (vipType == VipLevelTypeEnum.VIP1.getLevel()) ? user.getVipExpireTime() : user.getVip2ExpireTime();
            Date newExpire = calcRenewExpire(oldExpire, vipCfg.getDurationDays());

            if (vipType == VipLevelTypeEnum.VIP1.getLevel()) {
                //VIP 1
                userService.openWeek(userId, newExpire);
            } else {
                //VIP 2
                userService.openMonth(userId, newExpire);
            }

            UserCapital capital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalTypeId);

            JSONObject result = new JSONObject();
            result.put("vipType", vipType);
            result.put("newExpireTime", newExpire.getTime());
            result.put("serverTime", System.currentTimeMillis());
            result.put("balance", capital == null ? BigDecimal.ZERO : capital.getBalance());
            return result;
        }
    }


    /**
     * 900803
     * VIP1 每日奖励领取
     */
    @ServiceMethod(code = "003", description = "VIP1每日领取")
    @Transactional
    public JSONObject receiveVip1Daily(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");

        synchronized (LockUtil.getlock(userId)) {
            User user = loadAndCheckUser(userId);
            long now = System.currentTimeMillis();

            Date vip1Expire = user.getVipExpireTime();
            if (!isActive(user.getVip1(), vip1Expire, now)) {
                throwExp(String.format("VIP1未开通或已到期, 无法领取奖励!"));
            }

            DicVip vip1Cfg = getVipCfg((int) VipLevelTypeEnum.VIP1.getLevel());
            JSONArray rewardArr = JSONArray.parseArray(vip1Cfg.getDailyReward());
            if (rewardArr == null || rewardArr.isEmpty()) {
                throwExp(String.format("VIP1 配置项缺失.{用户ID: %s, 每日奖励配置: %s}",
                        userId, vip1Cfg.getDailyReward()));
            }

            Date today = DateUtil.getToDayDateBegin();
            // 今日是否已领
            Map<String, Object> countParams = new HashMap<>();
            countParams.put("userId", userId);
            countParams.put("vipType", VipLevelTypeEnum.VIP1.getLevel());
            countParams.put("claimDate", today);
            int cnt = vipReceiveRecordService.countByConditions(countParams);
            if (cnt > 0) {
                JSONObject result = new JSONObject();
                result.put("claimStatus", CLAIM_STATUS_ALREADY);
                return result;
            }

            // addReward 发奖
            gameService.addReward(userId, rewardArr, LogCapitalTypeEnum.VIP_RECEIVE, LogUserBackpackTypeEnum.events);

            // 领取记录
            VipReceiveRecord record = new VipReceiveRecord();
            record.setUserId(userId);
            record.setVipType((int) VipLevelTypeEnum.VIP1.getLevel());
            record.setClaimDate(today);
            record.setReward(rewardArr.toJSONString());
            record.setOrderNo("VIP1_CLAIM_" + OrderUtil.getOrder32Number());
            record.setCreateTime(new Date());
            record.setUpdateTime(new Date());
            vipReceiveRecordService.save(record);

            JSONObject result = new JSONObject();
            result.put("claimStatus", CLAIM_STATUS_ALREADY);
            result.put("rewardDetail", rewardArr);
            result.put("serverTime", System.currentTimeMillis());
            return result;
        }
    }

    /**
     * 900804
     * VIP卡转赠
     */
    @ServiceMethod(code = "004", description = "VIP卡转赠")
    @Transactional
    public JSONObject giftVipCard(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkNull(params.get("vipType"));
        checkNull(params.get("cardNum"));

        Long fromUserId = params.getLong("userId");
        Integer vipType = params.getInteger("vipType");
        Integer cardNum = params.getInteger("cardNum");

        if (vipType == null || (vipType != VipLevelTypeEnum.VIP1.getLevel() && vipType != VipLevelTypeEnum.VIP2.getLevel())) {
            throwExp(String.format("vipType 参数错误, VIP类型仅支持 1/2.{用户ID: %s, 当前VIP类型: %s,}",
                    fromUserId, vipType));
        }

        if (cardNum == null || cardNum <= 0) {
            throwExp(String.format("cardNum 参数错误, 转赠数量必须大于0.{用户ID: %s, 当前VIP类型: %s, 转赠数量: %s,}",
                    fromUserId, vipType, cardNum));
        }

        Long toUserId = params.getLong("toUserId");
        String toUserNo = params.getString("toUserNo");

        if (toUserId == null || toUserId <= 0) {
            if (StringUtils.isBlank(toUserNo)) {
                throwExp(String.format("接收方不能为空.{用户编号: %s,}",
                        toUserId, toUserNo));
            }
            User toUser = userService.findByUserNo(toUserNo);
            if (toUser == null) {
                throwExp(String.format("接收方不存在.{用户编号: %s,}",
                        toUserId, toUserNo));
            }
            toUserId = toUser.getId();
        }

        if (fromUserId.equals(toUserId)) {
            throwExp("不允许转赠给自己");
        }

        //锁
        Long lockA = Math.min(fromUserId, toUserId);
        Long lockB = Math.max(fromUserId, toUserId);

        synchronized (LockUtil.getlock(lockA)) {
            synchronized (LockUtil.getlock(lockB)) {

                User fromUser = loadAndCheckUser(fromUserId);
                User toUser = loadAndCheckUser(toUserId);

                // 权限校验
                Integer enable = fromUser.getVipTransferEnable();
                if (enable == null || enable != 1) {
                    throwExp("暂无转赠权限,请联系客服开通");
                }

                DicVip vipCfg = getVipCfg(vipType);
                String cardItemId = String.valueOf(vipCfg.getCardItemId());
                //背包道具校验
                double cardCount = gameService.getUserItemNumber(fromUserId, cardItemId);

                if (cardCount < cardNum) {
                    throwExp(String.format("转赠卡数量不足.{卡数量: %s,}",
                            fromUserId, cardCount));
                }

                //  对发送方和接收方进行背包扣卡/增卡
                gameService.updateUserBackpack(fromUserId, cardItemId, -1, LogUserBackpackTypeEnum.zsg, String.valueOf(toUserId));
                gameService.updateUserBackpack(toUserId, cardItemId, 1, LogUserBackpackTypeEnum.zs, String.valueOf(fromUserId));

                //转赠记录
                TVipGiftRecord record = new TVipGiftRecord();
                record.setGiftNo("VIP_GIFT_" + OrderUtil.getOrder32Number());
                record.setFromUserId(fromUserId);
                record.setToUserId(toUserId);
                record.setVipType(vipType);
                record.setCardItemId(Integer.parseInt(cardItemId));
                record.setCardNumber(cardNum);
                record.setRemark("VIP卡转赠");
                record.setCreateTime(new Date());
                record.setUpdateTime(new Date());
                tVipGiftRecordService.save(record);


                // 站内信通知
                String title = "收到VIP卡转赠";
                String context = "玩家【" + fromUser.getName() + "】向你转赠了【" + vipCfg.getName() + "】卡片x" + cardNum + "，请前往VIP页面确认激活。";
                mailService.sendMail(fromUserId, toUserId, title, context, "VIP", 0, new JSONArray());

                int fromRemain = (int) gameService.getUserItemNumber(fromUserId, cardItemId);

                JSONObject result = new JSONObject();
                result.put("vipType", vipType);
                result.put("fromCardRemain", fromRemain);
                result.put("giftNo", record.getGiftNo());
                result.put("serverTime", System.currentTimeMillis());

                JSONObject toUserInfo = new JSONObject();
                toUserInfo.put("userId", toUserId);
                toUserInfo.put("userNo", toUser.getUserNo());
                toUserInfo.put("name", toUser.getName());
                toUserInfo.put("headImageUrl", toUser.getHeadImageUrl());
                result.put("toUserInfo", toUserInfo);

                return result;
            }
        }
    }


    /**
     * 900805 （消耗1张卡，顺延有效期）
     * VIP卡确认激活
     */
    @ServiceMethod(code = "005", description = "VIP卡确认激活")
    @Transactional
    public JSONObject activateVipCard(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkNull(params.get("vipType"));

        Long userId = params.getLong("userId");
        Integer vipType = params.getInteger("vipType");

        if (vipType == null || (vipType != VipLevelTypeEnum.VIP1.getLevel() && vipType != VipLevelTypeEnum.VIP2.getLevel())) {
            throwExp(String.format("vipType 参数错误, VIP类型仅支持 1/2.{当前VIP类型: %s,}",
                    userId, vipType));
        }

        synchronized (LockUtil.getlock(userId)) {
            User user = loadAndCheckUser(userId);
            DicVip vipCfg = getVipCfg(vipType);

            String cardItemId = String.valueOf(vipCfg.getCardItemId());
            double cardCount = gameService.getUserItemNumber(userId, cardItemId);
            if (cardCount < 1) {
                throwExp(String.format("无可激活卡"));
            }

            //背包扣卡
            gameService.updateUserBackpack(userId, cardItemId, -1, LogUserBackpackTypeEnum.use);

            Date oldExpire = (vipType == VipLevelTypeEnum.VIP1.getLevel()) ? user.getVipExpireTime() : user.getVip2ExpireTime();
            Date newExpire = calcRenewExpire(oldExpire, vipCfg.getDurationDays());

            if (vipType == VipLevelTypeEnum.VIP1.getLevel()) {
                //VIP 1
                userService.openWeek(userId, newExpire);
            } else {
                //VIP 2
                userService.openMonth(userId, newExpire);
            }
            int remain = (int) gameService.getUserItemNumber(userId, cardItemId);

            JSONObject result = new JSONObject();
            result.put("vipType", vipType);
            result.put("newExpireTime", newExpire.getTime());
            result.put("cardRemain", remain);
            result.put("serverTime", System.currentTimeMillis());
            return result;
        }
    }

    /**
     * 900806 VIP1领取记录
     */
    @ServiceMethod(code = "006", description = "VIP1每日领取记录列表")
    @Transactional
    public JSONObject listVip1ReceiveRecords(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));

        Long userId = params.getLong("userId");
        Integer pageNo = params.getInteger("pageNo");
        Integer pageSize = params.getInteger("pageSize");

        if (pageNo == null || pageNo < 1) pageNo = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Map<String, Object> query = new HashMap<>();
        query.put("userId", userId);
        query.put("vipType", VipLevelTypeEnum.VIP1.getLevel());

        int total = vipReceiveRecordService.countByConditions(query);

        int offset = (pageNo - 1) * pageSize;
        query.put("offset", offset);
        query.put("limit", pageSize);

        List<VipReceiveRecord> list = vipReceiveRecordService.findListByConditions(query);

        JSONObject result = new JSONObject();
        result.put("total", total);
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        result.put("list", list == null ? new ArrayList<>() : list);
        return result;
    }


    /**
     * 900807 VIP卡转赠记录列表
     */
    @ServiceMethod(code = "007", description = "VIP卡转赠记录列表")
    @Transactional
    public JSONObject listVipGiftRecords(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkNull(params.get("vipType"));

        Long userId = params.getLong("userId");
        Integer vipType = params.getInteger("vipType");
        Integer direction = params.getInteger("direction");

        Integer pageNo = params.getInteger("pageNo");
        Integer pageSize = params.getInteger("pageSize");

        if (vipType == null || (vipType != VipLevelTypeEnum.VIP1.getLevel() && vipType != VipLevelTypeEnum.VIP2.getLevel())) {
            throwExp(String.format("vipType 参数错误, VIP类型仅支持 1/2.{用户ID: %s, 当前VIP类型: %s,}",
                    userId, vipType));
        }

        if (direction == null) direction = 1;
        if (pageNo == null || pageNo < 1) pageNo = 1;
        if (pageSize == null || pageSize < 1) pageSize = 10;

        Map<String, Object> query = new HashMap<>();
        query.put("vipType", vipType);

        if (direction == 1) {
            query.put("fromUserId", userId);
        } else if (direction == 2) {
            query.put("toUserId", userId);
        }

        int total = tVipGiftRecordService.countByConditions(query);

        int offset = (pageNo - 1) * pageSize;
        query.put("offset", offset);
        query.put("limit", pageSize);

        List<TVipGiftRecord> list = tVipGiftRecordService.findListByConditions(query);

        //补充用户昵称、头像
        JSONArray arr = new JSONArray();
        if (list != null && list.size() > 0) {

            Set<Long> uidSet = new HashSet<>();
            for (TVipGiftRecord r : list) {
                if (r.getFromUserId() != null && r.getFromUserId() > 0) uidSet.add(r.getFromUserId());
                if (r.getToUserId() != null && r.getToUserId() > 0) uidSet.add(r.getToUserId());
            }

            Map<Long, User> userMap = new HashMap<>();
            for (Long uid : uidSet) {
                try {
                    Map<Long, User> m = userCacheService.loadUsers(uid);
                    User u = (m != null) ? m.get(uid) : null;
                    if (u != null) {
                        userMap.put(uid, u);
                    }
                } catch (Exception e) {
                }
            }

            for (TVipGiftRecord r : list) {
                JSONObject j = new JSONObject();
                j.put("id", r.getId());
                j.put("giftNo", r.getGiftNo());
                j.put("fromUserId", r.getFromUserId());
                j.put("toUserId", r.getToUserId());
                j.put("vipType", r.getVipType());
                j.put("cardItemId", r.getCardItemId());
                j.put("cardNumber", r.getCardNumber());
                j.put("remark", r.getRemark());
                j.put("createTime", r.getCreateTime());
                j.put("updateTime", r.getUpdateTime());

                User fromU = userMap.get(r.getFromUserId());
                if (fromU != null) {
                    j.put("fromUserName", fromU.getName());
                    j.put("fromHeadImageUrl", fromU.getHeadImageUrl());
                    j.put("fromUserNo", fromU.getUserNo());
                }

                User toU = userMap.get(r.getToUserId());
                if (toU != null) {
                    j.put("toUserName", toU.getName());
                    j.put("toHeadImageUrl", toU.getHeadImageUrl());
                    j.put("toUserNo", toU.getUserNo());
                }
                arr.add(j);
            }
        }

        JSONObject result = new JSONObject();
        result.put("total", total);
        result.put("pageNo", pageNo);
        result.put("pageSize", pageSize);
        result.put("list", arr);
        return result;
    }



    /**
     * 用户校验
     * **/
    private User loadAndCheckUser(Long userId) {
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = (users != null) ? users.get(userId) : null;
        if (user == null) {
            throwExp("用户不存在");
        }
        return user;
    }

    /**
     * 读取VIP配置
     * **/
    private DicVip getVipCfg(int vipType) {
        DicVip cfg = PlayGameService.DIC_VIP_MAP.get(String.valueOf(vipType));
        if (cfg == null) {
            throwExp(String.format("VIP配置缺失.{ vipType: %s }",
                    vipType));
        }
        if (cfg.getStatus() != null && cfg.getStatus() != 1) {
            throwExp(String.format("VIP配置已禁用.{ vipType: %s ,status: %s  }",
                    vipType, cfg.getStatus()));
        }
        if (cfg.getDurationDays() == null || cfg.getDurationDays() <= 0) {
            throwExp(String.format("VIP配置持续天数错误.{ vipType: %s ,durationDays: %s  }",
                    vipType, cfg.getDurationDays()));
        }
        if (cfg.getCardItemId() == null || cfg.getCardItemId() <= 0) {
            throwExp(String.format("VIP配置VIP卡ID错误.{ vipType: %s ,cardItemId: %s  }",
                    vipType, cfg.getCardItemId()));
        }
        return cfg;
    }

    private boolean isActive(Integer vipFlag, Date expireTime, long now) {
        return vipFlag != null && vipFlag == 1
                && expireTime != null
                && expireTime.getTime() > now;
    }

    /** 续期统一规则：expire<=now/空 -> now+duration；expire>now -> expire+duration */
    private Date calcRenewExpire(Date oldExpire, Integer durationDays) {
        if (durationDays == null || durationDays <= 0) {
            throwExp("durationDays参数错误");
        }
        long now = System.currentTimeMillis();
        long base = (oldExpire == null || oldExpire.getTime() <= now) ? now : oldExpire.getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(base);
        cal.add(Calendar.DAY_OF_YEAR, durationDays);
        return cal.getTime();
    }

    /** 计算VIP1每日领取claimStatus */
    private int calcVip1ClaimStatus(Long userId, boolean vip1Active) {
        if (!vip1Active) return CLAIM_STATUS_DISABLED;

        Date today = DateUtil.getToDayDateBegin();
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("vipType", VipLevelTypeEnum.VIP1.getLevel());
        params.put("claimDate", today);
        int cnt = vipReceiveRecordService.countByConditions(params);
        return cnt > 0 ? CLAIM_STATUS_ALREADY : CLAIM_STATUS_CAN_CLAIM;
    }

    /** dic_vip 下发给客户端展示用 */
    private JSONArray buildVipConfigList(DicVip vip1Cfg, DicVip vip2Cfg) {
        List<DicVip> list = new ArrayList<>();
        list.add(vip1Cfg);
        list.add(vip2Cfg);
        list.sort(Comparator.comparingInt(DicVip::getVipType));

        JSONArray arr = new JSONArray();
        for (DicVip cfg : list) {
            JSONObject j = new JSONObject();
            j.put("id", cfg.getVipType());
            j.put("vipType", cfg.getVipType());
            j.put("name", cfg.getName());
            j.put("durationDays", cfg.getDurationDays());
            j.put("price", cfg.getPrice());
            j.put("capitalTypeId", cfg.getCapitalTypeId());
            j.put("benefitText", cfg.getBenefitText());
            j.put("dailyReward", cfg.getDailyReward());
            j.put("cardItemId", cfg.getCardItemId());
            j.put("status", cfg.getStatus());
            j.put("updateTime", cfg.getUpdateTime() == null ? 0 : cfg.getUpdateTime().getTime());
            arr.add(j);
        }
        return arr;
    }


    /**
     * VIP信息登录下发
     * **/
    public JSONObject buildVipInfoForLogin(Long userId) {
        User user = loadAndCheckUser(userId);

        long now = System.currentTimeMillis();
        DicVip vip1Cfg = getVipCfg((int) VipLevelTypeEnum.VIP1.getLevel());
        DicVip vip2Cfg = getVipCfg((int) VipLevelTypeEnum.VIP2.getLevel());

        Date vip1ExpireTime = user.getVipExpireTime();
        Date vip2ExpireTime = user.getVip2ExpireTime();

        boolean vip1Active = user.getVip1() == 1
                && vip1ExpireTime != null
                && vip1ExpireTime.getTime() > now;

        boolean vip2Active = user.getVip2() == 1
                && vip2ExpireTime != null
                && vip2ExpireTime.getTime() > now;
        int vip1CardCount = (int) gameService.getUserItemNumber(userId, String.valueOf(vip1Cfg.getCardItemId()));
        int vip2CardCount = (int) gameService.getUserItemNumber(userId, String.valueOf(vip2Cfg.getCardItemId()));

        int claimStatus = calcVip1ClaimStatus(userId, vip1Active);

        JSONObject vipInfo = new JSONObject();
        vipInfo.put("vip1ExpireTime", vip1ExpireTime == null ? 0 : vip1ExpireTime.getTime());
        vipInfo.put("vip2ExpireTime", vip2ExpireTime == null ? 0 : vip2ExpireTime.getTime());
        vipInfo.put("vip1Active", vip1Active ? 1 : 0);
        vipInfo.put("vip2Active", vip2Active ? 1 : 0);
        vipInfo.put("vip1CardCount", vip1CardCount);
        vipInfo.put("vip2CardCount", vip2CardCount);
        vipInfo.put("claimStatus", claimStatus);
        vipInfo.put("vipTransferEnable", user.getVipTransferEnable() == null ? 0 : user.getVipTransferEnable());

        return vipInfo;
    }


}
