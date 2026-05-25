package com.zywl.app.manager.service;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.base.bean.shoop.ShopManager;
import com.zywl.app.base.bean.vo.BackpackVo;
import com.zywl.app.base.bean.vo.TsgPayOrderVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.*;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.defaultx.service.card.UserMineService;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerGuildService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerTradingService;
import com.zywl.app.manager.socket.AdminSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: lzx
 * @Description: 后台管理; 并非只用于邮件管理；所有需要走后台的业务都走这里
 * @Task: 021 (MessageCodeContext.ADMIN_EMAIL_SERVER)
 */
@Service
@ServiceClass(code = MessageCodeContext.ADMIN_EMAIL_SERVER)
public class AdminMailService extends BaseService {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private RoleService roleService;
    @Autowired
    private MailService mailService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemCacheService itemCacheService;
    @Autowired
    private ApplyForService applyForService;
    @Autowired
    private GameCacheService gameCacheService;
    @Autowired
    private PlayGameService gameService;
    @Autowired
    private GameService miniGameService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private GuildCacheService guildCacheService;
    @Autowired
    private GuildMemberService guildMemberService;
    @Autowired
    private UserService userService;
    @Autowired
    private AppConfigCacheService appConfigCacheService;
    @Autowired
    private GuildService guildService;
    @Autowired
    private ManagerGuildService managerGuildService;
    @Autowired
    private LogUserCapitalService logUserCapitalService;
    @Autowired
    private LogUserBackpackService logUserBackpackService;
    @Autowired
    private BackpackService backpackService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private TradingService tradingService;
    @Autowired
    private GoodNoService goodNoService;
    @Autowired
    private ManagerTradingService managerTradingService;
    @Autowired
    private ManagerSocketService managerSocketService;
    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private CashRecordService cashRecordService;
    @Autowired
    private RechargeOrderService rechargeOrderService;
    @Autowired
    private AdminLogService adminLogService;
    @Autowired
    private TsgPayOrderService tsgPayOrderService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private DeviceRiskService deviceRiskService;
    @Autowired
    private LoginService loginService;
    @Autowired
    private UserBanRecordService userBanRecordService;
    @Autowired
    private CashCacheService cashCacheService;
    @Autowired
    private UserMineService userMineService;
    @Autowired
    private ShopManagerService shopManagerService;
    /*公告*/
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private DicVipService dicVipService;
    @Autowired
    private VipReceiveRecordService vipReceiveRecordService;


    private void checkAuth(AdminSocketServer adminSocketServer) {
        if (!roleService.isAdmin(adminSocketServer.getAdmin())) {
            throwExp("权限不足");
        }
    }

    private User findUser(long userId, String userNo, String userName) {
        if (userId > 0) {
            return userCacheService.getUserInfoById(userId);
        } else if (userNo != null && !userNo.isEmpty()) {
            return userCacheService.getUserInfoByUserNo(userNo);
        } else if (userName != null && !userName.isEmpty()) {
            JSONObject obj = new JSONObject();
            obj.put("userName", userName);
            return (User) userService.findOne("findByConditions", obj);
        }
        return null;
    }

    @ServiceMethod(code = "001")
    public Object getEmailList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long fromUserId = params.getLongValue("fromUserId", 0);
        long toUserId = params.getLongValue("toUserId", 0);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        if (fromUserId > 0) {
            condition.put("fromUserId", fromUserId);
        }
        if (toUserId > 0) {
            condition.put("toUserId", toUserId);
        }

        Long count = mailService.count("countByConditions", condition);
        List<Mail> records = mailService.findByConditions(condition);

        JSONArray array = new JSONArray();
        for (Mail mail : records) {
            JSONObject obj = (JSONObject) JSON.toJSON(mail);
            obj.put("fromUserName", mail.getFromUserName() == null ? "" : mail.getFromUserName());
            obj.put("toUserName", mail.getToUserName() == null ? "" : mail.getToUserName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "004", description = "查询用户(发送邮件前验证)")
    public Object searchUserForMail(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        String userNo = params.getString("userNo");

        logger().info("[查询用户] userId=" + userId + ", userNo=" + userNo);

        User user = null;
        if (userId != null && userId > 0) {
            user = userCacheService.getUserInfoById(userId);
            logger().info("[查询用户] 按userId查询结果: " + (user != null ? "找到, id=" + user.getId() + ", name=" + user.getName() : "未找到"));
        }
        if (user == null && userNo != null && !userNo.trim().isEmpty()) {
            user = userCacheService.getUserInfoByUserNo(userNo.trim());
            logger().info("[查询用户] 按userNo查询结果: " + (user != null ? "找到, id=" + user.getId() + ", name=" + user.getName() : "未找到"));
        }
        if (user == null) {
            logger().warn("[查询用户] 两种查询均未找到用户");
            return null;
        }
        JSONObject result = new JSONObject();
        result.put("id", user.getId());
        result.put("userNo", user.getUserNo());
        result.put("name", user.getName());
        result.put("headImageUrl", user.getHeadImageUrl());
        return result;
    }

    @ServiceMethod(code = "002", description = "发送邮件")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SYS_MAIL, sendParams = true)
    public Object sendMail(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkNull(params);
        if (adminSocketServer != null) checkAuth(adminSocketServer);
        JSONArray userIdArr = (JSONArray) params.get("userArr");
        String title = params.getString("title");
        String context = params.getString("context");
        int mailType = params.getIntValue("mailType");
        JSONArray itemArr = (JSONArray) params.get("itemArr");

        JSONArray detailArr = new JSONArray();
        for (int i = 0; i < itemArr.size(); i++) {
            JSONObject item = JSONObject.from(itemArr.get(i));
            int itemId = item.getIntValue("itemId");
            BigDecimal itemNum = item.getBigDecimal("itemNum");
            JSONObject detail = new JSONObject();
            detail.put("type", 1);
            detail.put("id", itemId);
            detail.put("number", itemNum);
            detailArr.add(detail);
        }

        int isAttachments = itemArr.size() > 0 ? 1 : 0;
        int time = Integer.parseInt(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_MAIL_VALIDITY, Config.MAIL_VALIDITY));
        if (mailType == 2) {
            Mail mail = new Mail();
            mail.setFromUserId(-1L);
            mail.setToUserId(0L);
            mail.setFromUserNo("");
            mail.setFromUserName("");
            mail.setFromUserHeadImg("");
            mail.setToUserNo("");
            mail.setToUserName("");
            mail.setToUserHeadImg("");
            mail.setType(mailType);
            mail.setSendTime(new Date());
            mail.setExpirationTime(DateUtil.getTimeByDay(time));
            mail.setContext(context);
            mail.setTitle(title);
            mail.setIsAttachments(isAttachments);
            mail.setAttachmentsDetails(detailArr);
            mail.setStatus(1);
            mail.setIsRead(0);
            mailService.save(mail);
            return new JSONObject();
        }

        JSONArray successUsers = new JSONArray();
        JSONArray failedUsers = new JSONArray();
        for (Object o : userIdArr) {
            String toIdStr = o.toString().trim();
            User toUser = null;
            try {
                long toId = Long.parseLong(toIdStr);
                if (toId > 0) {
                    toUser = userCacheService.getUserInfoById(toId);
                }
            } catch (NumberFormatException ignored) {}
            if (toUser == null) {
                toUser = userCacheService.getUserInfoByUserNo(toIdStr);
            }
            if (toUser == null) {
                failedUsers.add(toIdStr);
                continue;
            }
            Mail mail = new Mail();
            mail.setFromUserId(-1L);
            mail.setToUserId(toUser.getId());
            mail.setFromUserNo("");
            mail.setFromUserName("");
            mail.setFromUserHeadImg("");
            mail.setToUserNo(toUser.getUserNo());
            mail.setToUserName(toUser.getName());
            mail.setToUserHeadImg(toUser.getHeadImageUrl());
            mail.setType(mailType);
            mail.setSendTime(new Date());
            mail.setExpirationTime(DateUtil.getTimeByDay(time));
            mail.setContext(context);
            mail.setTitle(title);
            mail.setIsAttachments(isAttachments);
            mail.setAttachmentsDetails(detailArr);
            mail.setStatus(1);
            mail.setIsRead(0);
            mailService.save(mail);
            successUsers.add(toUser.getUserNo());
        }

        JSONObject result = new JSONObject();
        result.put("successCount", successUsers.size());
        result.put("failedCount", failedUsers.size());
        result.put("failedUsers", failedUsers);

        JSONObject content = new JSONObject();
        content.put("userIdArr", userIdArr);
        content.put("title", title);
        content.put("context", context);
        content.put("mailType", mailType);
        content.put("itemArr", itemArr);
        if (adminSocketServer != null) {
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "sendMail", content);
        }
        return result;
    }

    @ServiceMethod(code = "003", description = "获取道具列表")
    public Object getItemList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        Collection<Item> items = PlayGameService.itemMap.values();
        JSONArray data = new JSONArray();
        for (Item item : items) {
            if (item.getId() != null && item.getId() >= 1000) {
                JSONObject obj = new JSONObject();
                obj.put("itemName", item.getName());
                obj.put("itemId", item.getId());
                data.add(obj);
            }
        }
        return data;
    }

    /**
     * 获取渠道申请列表
     *
     * @return
     */
    @ServiceMethod(code = "010", description = "获取渠道申请列表")
    public Object getChannelApplyList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", limit);
        condition.put("status", 0);

        Long count = applyForService.count("countByConditions", condition);
        List<ApplyFor> recrods = applyForService.findByConditions(condition);

        JSONObject data = new JSONObject();
        data.put("list", recrods);
        data.put("count", count);
        return data;
    }

    /**
     * 获取店长列表
     *
     * @return
     */
    @ServiceMethod(code = "026", description = "获取店长列表")
    public Object getShopList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        if (status >= 0) {
            condition.put("status", status);
        }
        Long count = shopManagerService.count("countByConditions", condition);
        List<ShopManager> list = shopManagerService.findByConditions(condition);

        JSONArray array = new JSONArray();
        for (ShopManager shopManager : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(shopManager);
            User user1 = userCacheService.getUserInfoById(shopManager.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    /**
     * 获取渠道列表
     *
     * @return
     */
    @ServiceMethod(code = "011", description = "获取渠道列表")
    public Object getChannelList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        if (status >= 0) {
            condition.put("status", status);
        }
        Long count = applyForService.count("countByConditions", condition);
        List<ApplyFor> list = applyForService.findByConditions(condition);

        JSONArray array = new JSONArray();
        for (ApplyFor applyFor : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(applyFor);
            User user1 = userCacheService.getUserInfoById(applyFor.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    /**
     * 渠道审核
     *
     * @return
     */
    @ServiceMethod(code = "012", description = "渠道审核")
    public Object modifyChannelApply(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("status"));
        checkAuth(adminSocketServer);

        Long userId = params.getLongValue("userId", -1);
        int status = params.getIntValue("status", -1);

        Map<String, Object> findCondition = new HashMap<>();
        findCondition.put("userId", userId);
        findCondition.put("status", 0);
        try {
            if (applyForService.findOne(findCondition) == null) {
                throwExp("未找到数据！");
            }

            Map<String, Object> condition = new HashMap();
            condition.put("userId", userId);
            condition.put("status", status);
            applyForService.execute("pass", condition);
            Map<String, Object> upCondition = new HashMap<>();
            upCondition.put("userId", userId);
            userService.execute("updateChannelInfo", upCondition);
            userCacheService.removeUserInfoCache(userId);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "modifyChannelApply", new JSONObject(condition));
        } catch (Exception e) {
            throwExp("执行出错！" + e.toString());
        }
        return new JSONObject();
    }

    /**
     * 店长审核
     *
     * @return
     */
    @ServiceMethod(code = "025", description = "店长审核")
    public Object modifyShopManager(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("status"));
        checkAuth(adminSocketServer);
        Long userId = params.getLongValue("userId", -1);
        int status = params.getIntValue("status", -1);

        Map<String, Object> findCondition = new HashMap<>();
        findCondition.put("userId", userId);
        findCondition.put("status", 2);
        try {
            if (shopManagerService.findOne(findCondition) == null) {
                throwExp("未找到数据！");
            }
            Map<String, Object> condition = new HashMap();
            condition.put("userId", userId);
            condition.put("status", status);
            shopManagerService.execute("pass", condition);

            //赠送100张卡
            JSONArray reward = JSONArray.parseArray(managerConfigService.getString(Config.SHOP_MANAGER_REWARD));
            gameService.addReward(userId,reward,null,null);
            //Map<String, Object> upCondition = new HashMap<>();
            //upCondition.put("userId", userId);
            //shopManagerService.execute("updateShopManagerInfo", upCondition);
            //userCacheService.removeUserInfoCache(userId);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "modifyChannelApply", new JSONObject(condition));
        } catch (Exception e) {
            throwExp("执行出错！" + e.toString());
        }
        return new JSONObject();
    }



    @ServiceMethod(code = "020", description = "获取公会列表")
    public Object getGuildList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        int status = params.getIntValue("status", -1);
        long guildId = params.getLongValue("guildId", -1);
        long userId = params.getLongValue("userId", -1);
        String guildName = params.getString("guildName");

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        if (status >= 0) {
            condition.put("status", status);
        }
        if (guildId >= 0) {
            condition.put("id", guildId);
        }
        if (userId >= 0) {
            User user = userCacheService.getUserInfoById(userId);
            if (user != null) {
                condition.put("userId", user.getId());
            }
        }
        if (guildName != null && !guildName.isEmpty()) {
            condition.put("guildName", guildName);
        }

        long count = guildService.count("countByConditions", condition);
        List<Guild> list = guildService.findByConditions(condition);

        JSONArray array = new JSONArray();
        for (Guild guild : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(guild);
            User user1 = userCacheService.getUserInfoById(guild.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "021", description = "公会审核/解散")
    public Object passRefuseGuild(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("status"), params.get("userId"));
        checkAuth(adminSocketServer);

        int action = params.getIntValue("status", 0);
        long userId = params.getLong("userId");
        Guild guild = new Guild();
        guild.setUserId(userId);
        guild.setStatus(action);

        Guild guild1 = null;
        Map<String, Object> q = new HashMap<>();
        q.put("userId", userId);
        q.put("status", 2);
        List<Guild> pending = guildService.findByConditions(q);
        if (pending != null && !pending.isEmpty()) {
            guild1 = pending.get(0);
        }
        if (guild1 == null) {
            throwExp("未找到待审核公会申请");
        }
        if (action == 1) {
            //同意
            managerGuildService.passApplyGuild(guild1.getId(), userId);
        } else if (action == 2) {
            //拒绝
            managerGuildService.refuseApplyGuild(guild1.getId(), userId);
        }

        JSONObject content = new JSONObject();
        content.put("userId", userId);
        content.put("action", action);
        content.put("guilId", guild.getId());
        adminLogService.addAdminLog(adminSocketServer.getAdmin(), "passRefuseGuild", content);
        return new JSONObject();
    }

    @ServiceMethod(code = "022", description = "获取公会成员列表")
    public Object searchGuilMember(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long guildId = params.getLongValue("guildId", -1);
        long userId = params.getLongValue("userId", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();

        condition.put("start", start);
        condition.put("limit", limit);
        if (guildId >= 0) {
            condition.put("id", guildId);
        }
        if (userId >= 0) {
            User user = userCacheService.getUserInfoById(userId);
            if (user != null) {
                condition.put("userId", user.getId());
            }
        }

        long count = guildMemberService.count("countByConditions", condition);
        List<GuildMember> list = guildMemberService.findByConditions(condition);

        JSONArray array = new JSONArray();
        for (GuildMember guildMember : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(guildMember);
            User user1 = userCacheService.getUserInfoById(guildMember.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "023", description = "踢出公会")
    public Object removeUserFromGuild(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkAuth(adminSocketServer);

        long userId = params.getLong("userId");
        GuildMember member = guildMemberService.findByUserId(userId);
        if (member == null) {
            throwExp("未找到该公会玩家");
        }
        if (member.getRoleId() == 3) {
            throwExp("不能踢出会长");
        }
        JSONObject delObj = new JSONObject();
        delObj.put("id", member.getId());
        guildMemberService.delete(new HashMap<>(delObj));
        guildCacheService.removeMember(userId);

        long guildId = member.getGuildId();
        guildService.updateGuildMemberNumber(guildId, -1, 1);
        userService.updateUserRoleId(userId, 1);

        JSONObject content = new JSONObject();
        content.put("userId", userId);
        adminLogService.addAdminLog(adminSocketServer.getAdmin(), "removeUserFromGuild", content);
        return new Object();
    }


    @ServiceMethod(code = "024", description = "解散公会")
    public Object dissGuild(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("guildId"));
        checkAuth(adminSocketServer);

        long guildId = params.getLong("guildId");
        Guild guild = guildService.findById(guildId);
        userService.updateUserRoleId(guild.getUserId(), 1);
        List<GuildMember> members = guildMemberService.findByGuildId(guildId);
        for (GuildMember member : members) {
            userService.updateUserRoleId(member.getUserId(), 1);
            JSONObject delParam = new JSONObject();
            delParam.put("id", member.getId());
            guildMemberService.delete(new HashMap<>(delParam));
            guildCacheService.removeMember(member.getUserId());
        }

        JSONObject delParam = new JSONObject();
        delParam.put("id", guild.getId());
        guildService.delete(new HashMap<>(delParam));
        guildCacheService.removeGuilds();

        JSONObject content = new JSONObject();
        content.put("guildId", guildId);
        adminLogService.addAdminLog(adminSocketServer.getAdmin(), "dissGuild", content);
        return new Object();
    }

    @ServiceMethod(code = "030", description = "查询货币日志")
    public Object searchTreasureLog(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);

        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }

        int limit = params.getIntValue("limit", 10);
        if (limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }

        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        int start = (page - 1) * limit;

        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", limit);

        int tableIndex = 0;
        if (user != null && user.getId() != null) {
            tableIndex = (int) (user.getId() % 10);
            condition.put("userId", user.getId());
        }
        condition.put("tableName", LogUserCapital.tablePrefix + tableIndex);

        long count = logUserCapitalService.count("dbCountByConditions", condition);
        List<LogUserCapital> list = logUserCapitalService.findList("dbFindByConditions", condition);

        JSONArray array = new JSONArray();
        for (LogUserCapital logUserCapital : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(logUserCapital);
            User user1 = userCacheService.getUserInfoById(logUserCapital.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "040", description = "查询背包日志")
    public Object searchBackpackLog(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);

        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }

        int limit = params.getIntValue("limit", 10);
        if (limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }

        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        int start = (page - 1) * limit;

        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", limit);

        int tableIndex = 0;
        if (user != null && user.getId() != null) {
            tableIndex = (int) (user.getId() % 10);
            condition.put("userId", user.getId());
        }
        condition.put("tableName", LogUserBackpack.tablePrefix + tableIndex);

        long count = logUserBackpackService.count("dbCountByConditions", condition);
        List<LogUserBackpack> list = logUserBackpackService.findList("dbFindByConditions", condition);

        JSONArray array = new JSONArray();
        for (LogUserBackpack logUserBackpack : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(logUserBackpack);
            User user1 = userCacheService.getUserInfoById(logUserBackpack.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            Item item = itemCacheService.getItemInfoById(logUserBackpack.getItemId());
            obj.put("itemName", item == null ? "" : item.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "041", description = "查询背包详情")
    public Object searchBackpackInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;

        JSONArray array = new JSONArray();
        long totalCount = 0;

        if (user != null) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("start", start);
            condition.put("limit", limit);
            condition.put("tableName", Backpack.tablePrefix + user.getId().toString().charAt(user.getId().toString().length() - 1));
            condition.put("userId", user.getId());

            long count = backpackService.count("dbCountByConditions", condition);
            List<Backpack> list = backpackService.findList("dbFindByConditions", condition);
            for (Backpack backpack : list) {
                JSONObject obj = (JSONObject) JSON.toJSON(backpack);
                obj.put("userName", user.getName());
                Item item = itemCacheService.getItemInfoById(backpack.getItemId());
                obj.put("itemName", item == null ? "" : item.getName());
                array.add(obj);
            }
            totalCount = count;
        } else {
            for (int i = 0; i < 10; i++) {
                Map<String, Object> condition = new HashMap<>();
                condition.put("tableName", Backpack.tablePrefix + i);

                long count = backpackService.count("dbCountByConditions", condition);
                totalCount += count;
            }

            for (int i = 0; i < 10; i++) {
                Map<String, Object> condition = new HashMap<>();
                condition.put("start", start);
                condition.put("limit", limit);
                condition.put("tableName", Backpack.tablePrefix + i);

                List<Backpack> list = backpackService.findList("dbFindByConditions", condition);
                for (Backpack backpack : list) {
                    JSONObject obj = (JSONObject) JSON.toJSON(backpack);
                    User user1 = userCacheService.getUserInfoById(backpack.getUserId());
                    obj.put("userName", user1 == null ? "" : user1.getName());
                    Item item = itemCacheService.getItemInfoById(backpack.getItemId());
                    obj.put("itemName", item == null ? "" : item.getName());
                    array.add(obj);
                }
            }

            if (array.size() > limit) {
                array = new JSONArray(array.subList(0, limit));
            }
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", totalCount);
        return data;
    }

    @ServiceMethod(code = "050", description = "查询资产信息")
    public Object searchTreasureInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        condition.put("capitalTypes", Arrays.asList(
                UserCapitalTypeEnum.hxjf.getValue(),
                UserCapitalTypeEnum.xxxhhb.getValue(),
                UserCapitalTypeEnum.ejjf.getValue()
        ));

        if (user != null) {
            condition.put("userId", user.getId());
        }

        long count = userCapitalService.count("countByConditions", condition);
        List<UserCapital> list = userCapitalService.findList("findByConditions", condition);

        JSONArray array = new JSONArray();
        for (UserCapital userCapital : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(userCapital);
            User user1 = userCacheService.getUserInfoById(userCapital.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            obj.put("userNo", user1 == null ? "" : user1.getUserNo());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "051", description = "查询资产排行信息")
    public Object searchTreasureRankInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long reqCapitalType = params.getLongValue("capitalType", UserCapitalTypeEnum.hxjf.getValue());
        Set<Long> allowTypes = new HashSet<>();
        allowTypes.add((long) UserCapitalTypeEnum.hxjf.getValue());
        allowTypes.add((long) UserCapitalTypeEnum.xxxhhb.getValue());
        allowTypes.add((long) UserCapitalTypeEnum.ejjf.getValue());
        long capitalType = allowTypes.contains(reqCapitalType) ? reqCapitalType : UserCapitalTypeEnum.hxjf.getValue();

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        condition.put("capitalType", capitalType);
        long count = userCapitalService.count("countRank", condition);
        List<UserCapital> list = userCapitalService.findList("findRank", condition);

        JSONArray array = new JSONArray();
        for (UserCapital userCapital : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(userCapital);
            User user1 = userCacheService.getUserInfoById(userCapital.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    /**
     * 使用Java8的Stream API实现分页
     *
     * @param list     入参集合
     * @param pageSize 每页显示条数
     * @param pageNum  当前页码
     * @return 分页结果集合
     */
    private List<?> subListJava8(List<?> list, int pageSize, int pageNum) {
        int count = list.size(); // 总记录数
        // 计算总页数
        int pages = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
        // 起始位置
        int start = pageNum <= 0 ? 0 : (pageNum > pages ? (pages - 1) * pageSize : (pageNum - 1) * pageSize);
        // 终止位置
        int end = pageSize;
        return list.stream().skip(start).limit(pageSize).collect(Collectors.toList());
    }

    @ServiceMethod(code = "052", description = "查询文房排行信息")
    public Object searchItemRankInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        String itemId = null;
        List<BackpackVo> backpackTopList = backpackService.getBackpackTopList(itemId);
        backpackTopList.sort(((o1, o2) -> (o1.getNumber() - o2.getNumber()) < 0 ? 1 : -1));
        List<?> objects = subListJava8(backpackTopList, limit, page);
        JSONObject data = new JSONObject();
        data.put("list", objects);
        data.put("count", backpackTopList.size());
        return data;
    }


    @ServiceMethod(code = "070", description = "查询角色信息")
    public Object searchPlayerInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);

        if (user != null) {
            condition.put("userId", user.getId());
        }


        return null;
    }

    @ServiceMethod(code = "071", description = "封号解封")
    public Object banLogin(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        if (adminSocketServer == null || adminSocketServer.getAdmin() == null) {
            throwExp("登录状态失效，请重新登录");
        }

        long userId = params.getLongValue("id", params.getLongValue("userId", -1));
        int status = params.containsKey("operation")
                ? params.getIntValue("operation", -1)
                : params.getIntValue("status", -1);//0禁登录 1解登录 2禁功能 3解功能
        String mark = params.getString("mark");
        Admin admin = adminSocketServer.getAdmin();
        return banUser(userId, status, mark, admin);
    }

    public Object banUser(Long userId, int status, String mark, Admin admin) {
        if (userId < 0 || status < 0 || status > 3) {
            throwExp("参数错误");
        }
        if (mark == null || mark.trim().isEmpty()) {
            mark = "后台封禁操作";
        }

        User user = userService.findByIdAllStatus(userId);
        if (user == null) {
            throwExp("找不到该玩家");
        }
        if (status == 0 || status == 1) {
            if (userService.updateStatus(userId, status == 0 ? 2 : 1) < 0) {
                throwExp("操作失败");
            }
        } else {
            if (userService.updateRiskPlus(userId, status == 2 ? 1 : 0) < 0) {
                throwExp("操作失败");
            }
        }

        //封禁登陆时踢下线
        if (status == 0) {
            managerSocketService.kickPlayer(String.valueOf(userId), "");
        }

        long dt = 0;
        if (status == 0 || status == 2) {
            Date date = DateUtil.getDate("2027-12-31 00:00:00", 1);
            dt = date.getTime();
        }

        //记录封号或者解封原因

        userBanRecordService.recordInfo(user.getId(), user.getUserNo(), user.getName(), mark, status, admin.getUsername(), dt);
        JSONObject content = new JSONObject();
        content.put("userId", userId);
        content.put("status", status);
        adminLogService.addAdminLog(admin, "banLogin", content);
        return new Object();
    }


    @ServiceMethod(code = "072", description = "查询封号记录")
    public Object searchBanLogin(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);

        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        Integer start = (page - 1) * limit;

        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);

        if (userId > 0) {
            condition.put("userId", userId);
        }
        if (userNo != null && !userNo.isEmpty()) {
            condition.put("userNo", userNo);
        }
        if (userName != null && !userName.isEmpty()) {
            condition.put("userName", userName);
        }

        long count = userBanRecordService.count("countByConditions", condition);
        List<UserBanRecord> records = userBanRecordService.findByConditions(condition);

        JSONObject data = new JSONObject();
        data.put("list", records);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "080", description = "查询交易行信息")
    public Object searchTransactionInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        condition.put("status", 1);

        if (user != null) {
            condition.put("userId", user.getId());
        }

        long count = tradingService.count("countByConditions", condition);
        List<Trading> list = tradingService.findList("findByConditions", condition);
        JSONArray arr = new JSONArray();
        for (Trading trading : list) {
            JSONObject obj = JSONObject.from(trading);
            Item item = PlayGameService.itemMap.get(String.valueOf(trading.getItemId()));
            obj.put("itemName", item.getName());
            User user1 = userCacheService.getUserInfoById(trading.getUserId());
            obj.put("itemName", item.getName());
            obj.put("userName", user1 == null ? "" : user1.getName());
            arr.add(obj);
        }
        JSONObject data = new JSONObject();
        data.put("list", arr);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "081", description = "生成交易行订单")
    public Object makeOrder(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("orderList"));
        checkAuth(adminSocketServer);

        JSONArray arr = JSONArray.from(params.getJSONArray("orderList"));
        for (Object o : arr) {
            JSONObject order = JSONObject.from(o);
            long itemId = order.getLong("itemId");
            int itemNum = order.getIntValue("itemNum");
            if (itemNum < 0 || itemNum > 99999) {
                throwExp("道具数量错误");
            }
            BigDecimal itemPrice = order.getBigDecimal("itemPrice");
            int orderType = order.getIntValue("orderType");
            if (orderType != 0 && orderType != 1) {
                throwExp("订单类型错误");
            }
            managerTradingService.sysAddOrder(itemId, itemNum, itemPrice, orderType);
        }

        adminLogService.addAdminLog(adminSocketServer.getAdmin(), "makeOrder", new JSONObject());
        return new Object();
    }


    @ServiceMethod(code = "061", description = "矿产分析")
    public Object getPetAnalysis(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        JSONObject condition = new JSONObject();
        condition.put("start", 0);
        condition.put("limit", 80);

        List<PetAnalysis> list = userMineService.findList("findAnalysis", condition);
        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", list.size());
        return data;
    }

    @ServiceMethod(code = "100", description = "查询靓号信息")
    public Object searchGoodNo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        String goodNo = params.getString("goodNo");

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);

        if (goodNo != null && !goodNo.isEmpty()) {
            condition.put("goodNo", goodNo);
        }

        long count = goodNoService.count("countByConditions", condition);
        List<GoodNo> list = goodNoService.findList("findByConditions", condition);
        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "101", description = "上架靓号")
    public Object addGoodNo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("goodNo"), params.get("price"));
        checkAuth(adminSocketServer);

        String goodNo = params.getString("goodNo");
        BigDecimal price = params.getBigDecimal("price");

        GoodNo goodNo1 = goodNoService.findByNo(goodNo);
        if (goodNo1 != null) {
            throwExp("号码：" + goodNo + " 已存在！");
        }
        User user = userCacheService.getUserInfoByUserNo(goodNo);
        if (user != null) {
            throwExp("号码：" + goodNo + " 已存在！");
        }

        goodNoService.addGoodNo(goodNo, price, 0);

        JSONObject content = new JSONObject();
        content.put("goodNo", goodNo);
        content.put("price", price);
        adminLogService.addAdminLog(adminSocketServer.getAdmin(), "addGoodNo", content);
        return new Object();
    }

    @ServiceMethod(code = "102", description = "修改靓号信息")
    public Object modifyGoodNo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"));
        checkAuth(adminSocketServer);

        long id = params.getLongValue("id", -1);
        if (id < 0) {
            throwExp("参数错误");
        }
        GoodNo goodNo = new GoodNo();
        goodNo.setId(id);
        JSONObject content = new JSONObject();
        content.put("id", id);
        if (params.containsKey("price")) {
            BigDecimal price = params.getBigDecimal("price");
            goodNo.setPrice(price);
            content.put("price", price);
        }
        if (params.containsKey("status")) {
            int status = params.getIntValue("status", -1);
            if (status >= 0) {
                goodNo.setStatus(status);
                content.put("status", status);
            }
        }

        goodNoService.execute("updateGoodNo", goodNo);
        adminLogService.addAdminLog(adminSocketServer.getAdmin(), "modifyGoodNo", content);
        return new Object();
    }

    @ServiceMethod(code = "111", description = "获取提现数据列表")
    public Object getCashData(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        int status = params.getIntValue("status", -1);

        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        if (status >= 0) {
            condition.put("status", status);
        }
        if (user != null) {
            condition.put("userId", user.getId());
        }

        Long count = cashRecordService.count("countByConditions", condition);
        List<CashRecord> recrods = cashRecordService.findByConditions(condition);

        JSONObject data = new JSONObject();
        data.put("list", recrods);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "112", description = "修改提现数据")
    public Object modifyCashData(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);

        int id = params.getIntValue("id", -1);
        int action = params.getIntValue("action", -1);
        if (id < 0 || action < 0) {
            throwExp("参数错误!");
        }

        String strlck = "cash_" + id;
        synchronized (strlck) {

            Map<String, Object> obj = new HashMap<>();
            obj.put("id", id);
            CashRecord cashRecord = cashRecordService.findOne(obj);
            if (cashRecord == null) {
                throwExp("未找到数据");
            }

            String limitTips = managerConfigService.getString(Config.CASH_LIMIT_TIPS);
            BigDecimal cashLimit = managerConfigService.getBigDecimal(Config.CASH_LIMIT_DAY);
            String strCnt = cashCacheService.getTodayCashCount();
            BigDecimal cashCount = new BigDecimal(strCnt);

            int status = action == 1 ? 1 : 4;
            String mark = action == 1 ? null : limitTips;
            if (cashCount.add(cashRecord.getAmount()).compareTo(cashLimit) > 0) {
                status = 4;
                mark = limitTips;
            }

            cashRecordService.updateStatus(id, status, mark);

            //提现失败
            if (status == 4) {
                UserCapital capital = userCapitalCacheService.getUserCapitalCacheByType(cashRecord.getUserId(),
                        UserCapitalTypeEnum.rmb.getValue());
                userCapitalService.subUserOccupyBalanceByCashFail(cashRecord.getAmount(), cashRecord.getUserId(),
                        capital.getBalance(), capital.getOccupyBalance(), cashRecord.getOrderNo(), cashRecord.getId());
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(cashRecord.getUserId(), UserCapitalTypeEnum.rmb.getValue());
                JSONObject pushData = new JSONObject();
                pushData.put("userId", cashRecord.getUserId());
                pushData.put("capitalType", UserCapitalTypeEnum.rmb.getValue());
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(cashRecord.getUserId()), pushData);
            } else {
                Double amount = cashRecord.getAmount().doubleValue();
                cashCacheService.sumTodayCash(amount);
            }

            JSONObject content = new JSONObject();
            content.put("id", id);
            content.put("status", status);
            content.put("mark", mark);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "modifyCashData", content);
        }
        return new Object();
    }

    @ServiceMethod(code = "113", description = "获取充值数据")
    public Object getOrderList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        int status = params.getIntValue("status", -1);

        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        if (status >= 0) {
            condition.put("status", status);
        }
        if (user != null) {
            condition.put("userId", user.getId());
        }

        Long count = tsgPayOrderService.count("countByConditions", condition);
        List<TsgPayOrder> list = tsgPayOrderService.findByConditions(condition);
        List<TsgPayOrderVo> list1 = new ArrayList<>();
        for (TsgPayOrder tsgPayOrder : list) {
            TsgPayOrderVo vo = new TsgPayOrderVo();
            BeanUtils.copy(tsgPayOrder, vo);
            Long userId1 = tsgPayOrder.getUserId();
            User userInfo = userCacheService.getUserInfoById(userId1);
            if (userInfo != null) {
                vo.setUserName(userInfo.getName());
                vo.setRealName(userInfo.getRealName());
                vo.setIdCard(userInfo.getIdCard());
            }
            if (tsgPayOrder.getProductId() == 1) {
                vo.setProduct("单角色礼包");
            } else {
                vo.setProduct("全角色礼包");
            }
            if (tsgPayOrder.getStatus() != 3) {
                vo.setStatusInfo("支付失败");
            }
            if (tsgPayOrder.getStatus() == 3) {
                vo.setStatusInfo("支付成功");
            }
            list1.add(vo);
        }
        List<RechargeOrder> list2 = rechargeOrderService.findByConditions(condition);
        JSONArray array = new JSONArray();
        JSONObject data = new JSONObject();
        data.put("list", list1);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "120", description = "查询用户信息")
    public Object searchUserInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);

        if (userId > 0) {
            condition.put("userId", userId);
        }
        if (userNo != null && !userNo.isEmpty()) {
            condition.put("userNo", userNo);
        }
        if (userName != null && !userName.isEmpty()) {
            condition.put("userName", userName);
        }
        if (status >= 0) {
            condition.put("status", status);
        }

        long count = userService.count("countByConditions", condition);
        List<User> list = userService.findList("findByConditions", condition);

        JSONArray array = new JSONArray();
        for (User user : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(user);
            obj.put("online", managerSocketService.getUserOnlineInfo(user.getId().toString()) != null);
            if (user.getParentId() != null) {
                User parent = userCacheService.getUserInfoById(user.getParentId());
                obj.put("parentNo", parent == null ? "" : parent.getUserNo());
            } else {
                obj.put("parentNo", "");
            }
            obj.remove("password");
            for (int i = 1; i <= 3; i++) {
                Activity activity = getActivity(i);
                Double score;
                if (i == 1) {
                    if (activity!=null){
                        score = gameCacheService.getUserTopScore(String.valueOf(user.getId()), activity.getId());
                    }else {
                        score=0.0;
                    }
                } else if (i == 2) {
                    if (activity!=null){
                        score = gameCacheService.getUserTopScore2(String.valueOf(user.getId()), activity.getId());
                    }else {
                        score=0.0;
                    }
                } else {
                    if (activity!=null){
                        score = gameCacheService.getUserTopScore3(String.valueOf(user.getId()), activity.getId());
                    }else {
                        score=0.0;
                    }
                }
                obj.put("score"+i,score==null?0.0:score);
            }
            array.add(obj);
        }


        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "121", description = "修改用户信息")
    public Object modifyUserInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkAuth(adminSocketServer);

        long userId = params.getLongValue("userId", 0);
        String newParentNo = params.getString("cno");

        User user = userCacheService.getUserInfoById(userId);
        User parent = userCacheService.getUserInfoByUserNo(newParentNo);
        if (parent == null) {
            throwExp("新的上级不存在");
        }
        userService.updateUserParent(userId, parent.getId(), parent.getParentId());
        /*Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("userId", userId);
        queryObj.put("status", 2);
        List<Guild> rst = guildService.findByConditions(queryObj);
        if (rst.size() > 0) {
            throwExp("请耐心等待审核！");
        }
        Long dataId = guildService.applyAddGuild(user.getName(), userId, 1, BigDecimal.ZERO, 2);
        userCapitalService.subUserBalanceByGuild(userId, BigDecimal.ZERO, dataId);*/
        return new JSONObject();
    }

    @ServiceMethod(code = "122", description = "修改用户信息渠道状态")
    public Object modifyUserChannel(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkAuth(adminSocketServer);
        Long userId = params.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        JSONObject content = new JSONObject();
        content.put("id", user.getId());
        userService.updateIsChannel(userId);
        return new JSONObject();
    }

    public Activity getActivity(int activeNo) {
        if (activeNo == 1) {
            return gameCacheService.getActivity();
        } else if (activeNo == 2) {
            return gameCacheService.getActivity2();
        } else {
            return gameCacheService.getActivity3();
        }
    }

    @ServiceMethod(code = "123", description = "修改用户活动积分")
    public Object updateUserActiveScore(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        checkAuth(adminSocketServer);
        Long userId = params.getLong("userId");
        int activeNo = params.getIntValue("activeNo");
        int score = params.getIntValue("score");
        User user = userCacheService.getUserInfoById(userId);
        if (activeNo == 1) {
            gameCacheService.addPointMySelf(userId, score);
        } else if (activeNo == 2) {
            gameCacheService.addPoint2MySelf(userId, score);
        } else {
            gameCacheService.addPointMySelf3(userId, score);
        }
        JSONObject content = new JSONObject();
        content.put("id", user.getId());
        return content;
    }


    @ServiceMethod(code = "140", description = "查询管理员操作日志")
    public Object getAdminLog(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);

        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }

        int limit = params.getIntValue("limit", 10);
        if (limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }

        int start = (page - 1) * limit;

        Map<String, Object> condition = new HashMap<>(8);
        condition.put("start", start);
        condition.put("limit", limit);

        String adminName = params.getString("adminName");
        if (adminName != null && !adminName.trim().isEmpty()) {
            condition.put("adminName", adminName.trim());
        }

        String adminAccount = params.getString("adminAccount");
        if (adminAccount != null && !adminAccount.trim().isEmpty()) {
            condition.put("adminAccount", adminAccount.trim());
        }

        long count = adminLogService.count("countByConditions", condition);
        List<AdminLog> list = Collections.emptyList();
        if (count > 0) {
            list = adminLogService.findList("findByConditions", condition);
        }

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("page", page);
        data.put("limit", limit);
        return data;
    }

    @ServiceMethod(code = "150", description = "查询数美规则")
    public Object searchShuMeiRule(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 10);
        String model = params.getString("models");
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);

        if (model != null && !model.isEmpty()) {
            condition.put("models", model);
        }

        if (status >= 0) {
            condition.put("status", status);
        }

        long count = deviceRiskService.count("countByConditions", condition);
        List<DeviceRisk> list = deviceRiskService.findList("findByConditions", condition);

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "151", description = "查询管理员操作日志")
    public Object modifyShuMeiRule(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"));
        checkAuth(adminSocketServer);
        long id = params.getIntValue("id", -1);
        if (id < 0) {
            throwExp("参数错误");
        }

        DeviceRisk deviceRisk = deviceRiskService.findById(id);
        if (deviceRisk == null) {
            throwExp("未找到数据");
        }
        int newStatus = deviceRisk.getStatus() == 1 ? 0 : 1;

        deviceRiskService.updateStatus(newStatus, id);
        if (newStatus == 1) {
            loginService.addShuMeiModel(deviceRisk.getModels());
        } else {
            loginService.removeShuMeiModel(deviceRisk.getModels());
        }
        logger.info(loginService.getShuMeiModels());
        return new Object();
    }

    /**
     * 公告-历史列表
     * 在 014001 ServerNoticeService 中同样查询了历史公告,通过 AppSocket 直接对接客户端；这里用AdminSocketServer给管理员；
     * **/
    @ServiceMethod(code = "160", description = "公告-历史列表")
    public Object getNoticeHistory(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);

        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 20);
        if (limit <= 0) {
            limit = 20;
        }

        List<Notice> all = noticeService.findAll();
        long count = all == null ? 0L : all.size();

        List<Notice> list = new ArrayList<>();
        if (all != null && !all.isEmpty()) {
            int start = (page - 1) * limit;
            if (start < all.size()) {
                int end = Math.min(start + limit, all.size());
                list = all.subList(start, end);
            }
        }

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        return data;
    }

    /**
     * 公告-新增
     * 在 ManagerNoticeService 中同样有该接口,只不过现在adminSocketServer是后台入口,所以Manager端暂时没用
     * **/
    @ServiceMethod(code = "161", description = "公告-新增")
    public Object addNotice(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);

        String title = params.getString("title");
        String context = params.getString("context");
        Integer type = params.getInteger("type");
        Integer push = params.getInteger("push");

        if (title == null || title.trim().isEmpty()) {
            throwExp("公告标题不能为空");
        }
        if (context == null || context.trim().isEmpty()) {
            throwExp("公告内容不能为空");
        }
        if (type == null) {
            type = 1;
        }

        Notice notice = new Notice();
        notice.setTitle(title.trim());
        notice.setContext(context.trim());
        notice.setType(type);
        notice.setCreateTime(new Date());

        noticeService.insert(notice);
        // 新增后立即推送给在线用户
        if (push != null && push == 1) {
            JSONObject data = new JSONObject();
            data.put("notice", notice.getContext());
            Push.push(PushCode.sendNotice, null, data);
        }

        JSONObject resp = new JSONObject();
        resp.put("notice", notice);
        return resp;
    }

    /**
     * 公告-编辑
     * 在 ManagerNoticeService 中同样有该接口,只不过现在adminSocketServer是后台入口,所以Manager端暂时没用
     * **/
    @ServiceMethod(code = "162", description = "公告-编辑")
    public Object updateNotice(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);

        Long id = params.getLong("id");
        if (id == null || id <= 0) {
            throwExp("id不能为空");
        }

        String title = params.getString("title");
        String context = params.getString("context");
        Integer type = params.getInteger("type");
        Integer push = params.getInteger("push");

        if (title == null || title.trim().isEmpty()) {
            throwExp("title不能为空");
        }
        if (context == null || context.trim().isEmpty()) {
            throwExp("context不能为空");
        }
        if (type == null) {
            type = 1;
        }

        Notice notice = new Notice();
        notice.setId(id);
        notice.setTitle(title.trim());
        notice.setContext(context.trim());
        notice.setType(type);

        int rows = noticeService.update(notice);
        // 编辑后立即推送给在线用户
        if (push != null && push == 1) {
            JSONObject data = new JSONObject();
            data.put("notice", context.trim());
            Push.push(PushCode.sendNotice, null, data);
        }

        JSONObject resp = new JSONObject();
        resp.put("rows", rows);
        return resp;
    }

    /**
     * 公告-删除
     * 在 ManagerNoticeService 中同样有该接口,只不过现在adminSocketServer是后台入口,所以Manager端暂时没用
     * **/
    @ServiceMethod(code = "163", description = "公告-删除")
    public Object deleteNotice(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);

        Long id = params.getLong("id");
        if (id == null || id <= 0) {
            throwExp("id不能为空");
        }

        JSONObject condition = new JSONObject();
        condition.put("id", id);
        int rows = noticeService.execute("deleteNoticeById", condition);

        JSONObject resp = new JSONObject();
        resp.put("rows", rows);
        return resp;
    }

    /**
     * 公告-推送
     * 在 ManagerNoticeService 中同样有该接口,只不过现在adminSocketServer是后台入口,所以Manager端暂时没用
     * **/
    @ServiceMethod(code = "164", description = "公告-推送")
    public Object pushNotice(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);

        Long id = params.getLong("id");
        String noticeText = params.getString("notice");

        if ((noticeText == null || noticeText.trim().isEmpty()) && id != null && id > 0) {
            JSONObject p = new JSONObject();
            p.put("id", id);
            Notice notice = noticeService.findOne(p);
            if (notice == null) {
                throwExp("公告不存在");
            }
            noticeText = notice.getContext();
        }

        if (noticeText == null || noticeText.trim().isEmpty()) {
            throwExp("notice不能为空");
        }
        JSONObject data = new JSONObject();
        data.put("notice", noticeText.trim());
        Push.push(PushCode.sendNotice, null, data);

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    /**
     * VIP卡转赠权限名单管理（V1.0）
     * vipTransferEnable: 0无权限 1允许
     */
    @ServiceMethod(code = "170", description = "VIP卡转赠权限-查询")
    public Object getVipTransferEnable(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkNull(params);
        if (adminSocketServer != null) checkAuth(adminSocketServer);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");

        User user = findUser(userId, userNo, null);
        if (user == null) {
            throwExp("用户不存在");
        }

        JSONObject result = new JSONObject();
        result.put("userId", user.getId());
        result.put("userNo", user.getUserNo());
        result.put("userName", user.getName());
        result.put("vipTransferEnable", user.getVipTransferEnable() == null ? 0 : user.getVipTransferEnable());
        return result;
    }


    /**
     * VIP卡转赠权限名单管理（V1.0）
     * vipTransferEnable: 0无权限 1允许
     */
    @ServiceMethod(code = "171", description = "VIP卡转赠权限-设置")
    public Object setVipTransferEnable(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkNull(params);
        if (adminSocketServer != null) checkAuth(adminSocketServer);

        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        Integer enable = params.getInteger("vipTransferEnable");

        if (enable == null || (enable != 0 && enable != 1)) {
            throwExp("vipTransferEnable 参数错误，仅支持 0/1");
        }

        User user = findUser(userId, userNo, null);
        if (user == null) {
            throwExp("用户不存在");
        }

        int n = userService.updateUserVipTransfer(user.getId(),enable);
        if (n < 1) {
            throwExp("设置失败，请稍后重试");
        }

        // 清理用户缓存
        userCacheService.removeUserInfoCache(user.getId());

        // 管理员操作日志
        JSONObject content = new JSONObject();
        content.put("userId", user.getId());
        content.put("userNo", user.getUserNo());
        content.put("vipTransferEnable", enable);
        if (adminSocketServer != null) {
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "setVipTransferEnable", content);
        }

        JSONObject result = new JSONObject();
        result.put("userId", user.getId());
        result.put("userNo", user.getUserNo());
        result.put("vipTransferEnable", enable);
        return result;
    }

    @ServiceMethod(code = "172", description = "弹珠消耗统计-主列表")
    public Object searchMarbleCostStats(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 20);
        if (limit <= 0) {
            limit = 20;
        }

        String keyword = params.getString("keyword");
        String userName = params.getString("userName");

        Integer start = (page - 1) * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", limit);
        if (keyword != null && !keyword.trim().isEmpty()) {
            condition.put("keyword", keyword.trim());
        }
        if (userName != null && !userName.trim().isEmpty()) {
            condition.put("userName", userName.trim());
        }

        long count = userService.count("countMarbleCostByConditions", condition);
        List<Map<String, Object>> list = userService.findList("findMarbleCostByConditions", condition);

        JSONArray dataList = new JSONArray();
        if (list != null) {
            for (Map<String, Object> row : list) {
                JSONObject obj = new JSONObject();
                obj.put("userId", row.get("userId"));
                obj.put("userNo", row.get("userNo"));
                obj.put("userName", row.get("userName"));
                obj.put("parentId", row.get("parentId"));
                obj.put("sonCount", row.get("sonCount"));
                obj.put("lionCost", row.get("lionCost"));
                obj.put("lionGain", row.get("lionGain"));
                obj.put("rabbitCost", row.get("rabbitCost"));
                obj.put("rabbitGain", row.get("rabbitGain"));
                obj.put("flipCost", row.get("flipCost"));
                obj.put("flipGain", row.get("flipGain"));
                obj.put("totalCost", row.get("totalCost"));
                obj.put("totalGain", row.get("totalGain"));
                dataList.add(obj);
            }
        }

        JSONObject data = new JSONObject();
        data.put("list", dataList);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "173", description = "弹珠消耗统计-下级列表")
    public Object searchMarbleCostChildren(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        long parentId = params.getLongValue("parentId", -1);
        if (parentId <= 0) {
            throwExp("上级ID不能为空");
        }

        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 20);
        if (limit <= 0) {
            limit = 20;
        }

        String keyword = params.getString("keyword");
        String userName = params.getString("userName");

        Integer start = (page - 1) * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("parentId", parentId);
        condition.put("start", start);
        condition.put("limit", limit);
        if (keyword != null && !keyword.trim().isEmpty()) {
            condition.put("keyword", keyword.trim());
        }
        if (userName != null && !userName.trim().isEmpty()) {
            condition.put("userName", userName.trim());
        }

        long count = userService.count("countMarbleCostByParent", condition);
        List<Map<String, Object>> list = userService.findList("findMarbleCostByParent", condition);

        JSONArray dataList = new JSONArray();
        if (list != null) {
            for (Map<String, Object> row : list) {
                JSONObject obj = new JSONObject();
                obj.put("userId", row.get("userId"));
                obj.put("userNo", row.get("userNo"));
                obj.put("userName", row.get("userName"));
                obj.put("parentId", row.get("parentId"));
                obj.put("lionCost", row.get("lionCost"));
                obj.put("lionGain", row.get("lionGain"));
                obj.put("rabbitCost", row.get("rabbitCost"));
                obj.put("rabbitGain", row.get("rabbitGain"));
                obj.put("flipCost", row.get("flipCost"));
                obj.put("flipGain", row.get("flipGain"));
                obj.put("totalCost", row.get("totalCost"));
                obj.put("totalGain", row.get("totalGain"));
                dataList.add(obj);
            }
        }

        // 下级汇总（全部下级的总消耗和总获得）
        Map<String, Object> sumCondition = new HashMap<>();
        sumCondition.put("parentId", parentId);
        Map<String, Object> summary = (Map<String, Object>) userService.findOne("sumMarbleCostByParent", sumCondition);

        JSONObject data = new JSONObject();
        data.put("list", dataList);
        data.put("count", count);
        if (summary != null) {
            data.put("summary", summary);
        }
        return data;
    }

    /**
     * VIP配置管理 - 获取配置列表
     * 展示dic_vip全部数据，将daily_reward和cardItemId解析为道具名称
     */
    @ServiceMethod(code = "180", description = "VIP配置-获取列表")
    public Object getVipConfigList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);

        List<DicVip> vipList = dicVipService.findList("findAllForAdmin", null);
        JSONArray array = new JSONArray();
        for (DicVip vip : vipList) {
            JSONObject obj = new JSONObject();
            obj.put("vipType", vip.getVipType());
            obj.put("name", vip.getName());
            obj.put("durationDays", vip.getDurationDays());
            obj.put("price", vip.getPrice());
            obj.put("capitalTypeId", vip.getCapitalTypeId());
            obj.put("capitalTypeName", UserCapitalTypeEnum.getName(vip.getCapitalTypeId()));
            obj.put("benefitText", vip.getBenefitText() == null ? "" : vip.getBenefitText());
            obj.put("status", vip.getStatus());
            obj.put("createTime", vip.getCreateTime());
            obj.put("updateTime", vip.getUpdateTime());

            // 解析卡道具
            Item cardItem = itemCacheService.getItemInfoById(vip.getCardItemId());
            obj.put("cardItemId", vip.getCardItemId());
            obj.put("cardItemName", cardItem == null ? "" : cardItem.getName());

            // 解析每日奖励JSON
            JSONArray rewardItems = new JSONArray();
            if (vip.getDailyReward() != null && !vip.getDailyReward().trim().isEmpty()) {
                try {
                    JSONArray rewards = JSON.parseArray(vip.getDailyReward());
                    for (int i = 0; i < rewards.size(); i++) {
                        JSONObject r = rewards.getJSONObject(i);
                        int itemId = r.getIntValue("id");
                        int number = r.getIntValue("number");
                        int type = r.getIntValue("type");
                        Item item = itemCacheService.getItemInfoById((long) itemId);
                        JSONObject rewardObj = new JSONObject();
                        rewardObj.put("type", type);
                        rewardObj.put("id", itemId);
                        rewardObj.put("itemName", item == null ? "未知道具(ID:" + itemId + ")" : item.getName());
                        rewardObj.put("number", number);
                        rewardItems.add(rewardObj);
                    }
                } catch (Exception e) {
                    logger.warn("解析VIP" + vip.getVipType() + "daily_reward失败: " + vip.getDailyReward());
                }
            }
            obj.put("dailyRewardRaw", vip.getDailyReward());
            obj.put("dailyRewardItems", rewardItems);

            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", array.size());
        return data;
    }

    /**
     * VIP配置管理 - 更新配置
     * 支持修改name、durationDays、price、capitalTypeId、benefitText、dailyReward、cardItemId、status
     */
    @ServiceMethod(code = "181", description = "VIP配置-更新")
    public Object updateVipConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkNull(params);
        checkNull(params.get("vipType"));
        checkAuth(adminSocketServer);

        int vipType = params.getIntValue("vipType", 0);
        if (vipType != 1 && vipType != 2) {
            throwExp("VIP类型参数错误");
        }

        DicVip existing = (DicVip) dicVipService.findOne("selectByPrimaryKey", (long) vipType);
        if (existing == null) {
            throwExp("VIP配置不存在");
        }

        DicVip updateObj = new DicVip();
        updateObj.setVipType(vipType);
        updateObj.setUpdateTime(new Date());

        if (params.containsKey("name")) {
            String name = params.getString("name");
            if (name == null || name.trim().isEmpty()) {
                throwExp("VIP名称不能为空");
            }
            updateObj.setName(name.trim());
        }
        if (params.containsKey("durationDays")) {
            int days = params.getIntValue("durationDays");
            if (days <= 0) {
                throwExp("时长必须大于0");
            }
            updateObj.setDurationDays(days);
        }
        if (params.containsKey("price")) {
            BigDecimal price = params.getBigDecimal("price");
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                throwExp("价格不能为负数");
            }
            updateObj.setPrice(price);
        }
        if (params.containsKey("capitalTypeId")) {
            updateObj.setCapitalTypeId(params.getIntValue("capitalTypeId"));
        }
        if (params.containsKey("benefitText")) {
            updateObj.setBenefitText(params.getString("benefitText"));
        }
        if (params.containsKey("dailyReward")) {
            String dailyReward = params.getString("dailyReward");
            if (dailyReward != null && !dailyReward.trim().isEmpty()) {
                try {
                    JSON.parseArray(dailyReward);
                } catch (Exception e) {
                    throwExp("奖励JSON格式错误");
                }
            }
            updateObj.setDailyReward(dailyReward);
        }
        if (params.containsKey("cardItemId")) {
            Long cardItemId = params.getLong("cardItemId");
            Item item = itemCacheService.getItemInfoById((long) cardItemId);
            if (item == null) {
                throwExp("卡道具不存在");
            }
            updateObj.setCardItemId(cardItemId);
        }
        if (params.containsKey("status")) {
            int status = params.getIntValue("status");
            if (status != 0 && status != 1) {
                throwExp("状态参数错误");
            }
            updateObj.setStatus(status);
        }

        int rows = dicVipService.execute("updateVip", updateObj);
        if (rows <= 0) {
            throwExp("更新失败");
        }

        // 递增版本号并统一走 ManagerConfigService.updateGameKey 热更链路。
        hotReloadTableVersion(Config.VIP_TABLE_VERSION);

        JSONObject content = new JSONObject();
        content.put("vipType", vipType);
        if (adminSocketServer != null) {
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateVipConfig", content);
        }

        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    @ServiceMethod(code = "197", description = "每日任务配置-获取")
    public Object getDailyTaskConfig(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        JSONArray tasks = queryDailyTaskConfigRows();
        JSONArray boxes = parseDailyTaskBoxes(managerConfigService.getString(Config.DAILY_TASK_BOX_CONFIG));

        JSONObject data = new JSONObject();
        data.put("tasks", tasks);
        data.put("boxes", boxes);
        data.put("count", tasks.size());
        data.put("boxCount", boxes.size());
        return data;
    }

    @ServiceMethod(code = "198", description = "每日任务配置-更新")
    public Object updateDailyTaskConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        String mode = trimToNull(params.getString("mode"));
        if ("box".equals(mode)) {
            updateDailyTaskBoxConfig(params);
        } else {
            updateDailyTaskRow(params);
            managerConfigService.updateGameKey(Config.DAILY_TASK_BOX_CONFIG, managerConfigService.getString(Config.DAILY_TASK_BOX_CONFIG));
        }

        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("mode", mode == null ? "task" : mode);
            content.put("id", params.get("id"));
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateDailyTaskConfig", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    @ServiceMethod(code = "199", description = "养宠配置-获取")
    public Object getPetConfig(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        JSONArray list = queryPetConfigRows();
        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", list.size());
        return data;
    }

    @ServiceMethod(code = "200", description = "养宠配置-更新")
    public Object updatePetConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        Integer id = params.getInteger("id");
        if (id == null || id <= 0) {
            throwExp("养宠配置ID不能为空");
        }
        int status = params.getIntValue("status", 1);
        if (status != 0 && status != 1) {
            throwExp("状态参数错误");
        }
        JSONArray profitFixed = normalizeProfitFixed(params.get("profitFixedList"));
        JSONArray yieldCurve = normalizeYieldCurve(params.get("yieldCurveList"));

        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(status);
        sqlParams.add(emptyIfNull(params.getString("remark")));
        sqlParams.add(requirePositiveInt(params, "settleIntervalMin", "结算周期"));
        sqlParams.add(requirePositiveInt(params, "hungerMaxDays", "饱腹上限天数"));
        sqlParams.add(requirePositiveInt(params, "feedAddHours", "单次喂养增加小时"));
        sqlParams.add(requirePositiveInt(params, "buyCostCapitalType", "购买消耗资产类型"));
        sqlParams.add(requireNonNegativeDecimal(params, "buyCostAmount", "购买单只狮子价格"));
        sqlParams.add(requirePositiveInt(params, "feedCostCapitalType", "喂养消耗资产类型"));
        sqlParams.add(requireNonNegativeDecimal(params, "feedCostAmount", "单次喂养价格"));
        sqlParams.add(requirePositiveInt(params, "yieldCapitalType", "产出资产类型"));
        sqlParams.add(requirePositiveInt(params, "dividendCapitalType", "分润资产类型"));
        sqlParams.add(requirePositiveInt(params, "unlockContribCapitalType", "解锁贡献统计币种"));
        sqlParams.add(requireNonNegativeInt(params, "unlockDirectLv3", "3代直推人数"));
        sqlParams.add(requireNonNegativeInt(params, "unlockDirectLv4", "4代直推人数"));
        sqlParams.add(requireNonNegativeInt(params, "unlockDirectLv5", "5代直推人数"));
        sqlParams.add(requireNonNegativeDecimal(params, "unlockContribLv3", "3代贡献阈值"));
        sqlParams.add(requireNonNegativeDecimal(params, "unlockContribLv4", "4代贡献阈值"));
        sqlParams.add(requireNonNegativeDecimal(params, "unlockContribLv5", "5代贡献阈值"));
        sqlParams.add(JSON.toJSONString(profitFixed));
        sqlParams.add(JSON.toJSONString(yieldCurve));
        sqlParams.add(id);

        int rows = executeUpdate("UPDATE dic_pet SET status = ?, remark = ?, settle_interval_min = ?, hunger_max_days = ?, feed_add_hours = ?, " +
                "buy_cost_capital_type = ?, buy_cost_amount = ?, feed_cost_capital_type = ?, feed_cost_amount = ?, yield_capital_type = ?, " +
                "dividend_capital_type = ?, unlock_contrib_capital_type = ?, unlock_direct_lv3 = ?, unlock_direct_lv4 = ?, unlock_direct_lv5 = ?, " +
                "unlock_contrib_lv3 = ?, unlock_contrib_lv4 = ?, unlock_contrib_lv5 = ?, profit_fixed_json = ?, yield_curve_json = ?, update_time = NOW() WHERE id = ?", sqlParams);
        if (rows <= 0) {
            throwExp("养宠配置不存在或更新失败");
        }
        hotReloadTableVersion(Config.PET_TABLE_VERSION);

        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("id", id);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updatePetConfig", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    @ServiceMethod(code = "201", description = "道具配置-获取")
    public Object getItemConfig(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }
        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = normalizeLimit(params.getIntValue("limit", 20));
        int start = (page - 1) * limit;

        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();
        Long itemId = parseLongOrNull(trimToNull(params.getString("itemId")));
        if (itemId != null && itemId > 0) {
            whereSql.append(" AND id = ?");
            whereParams.add(itemId);
        }
        String itemName = trimToNull(params.getString("itemName"));
        if (itemName != null) {
            whereSql.append(" AND name LIKE ?");
            whereParams.add("%" + itemName + "%");
        }
        Integer type = params.getInteger("type");
        if (type != null && type >= 0) {
            whereSql.append(" AND type = ?");
            whereParams.add(type);
        }
        Integer status = params.getInteger("status");
        if (status != null && status >= 0) {
            whereSql.append(" AND status = ?");
            whereParams.add(status);
        }
        String onlyRecycle = trimToNull(params.getString("onlyRecycle"));
        if ("1".equals(onlyRecycle)) {
            whereSql.append(" AND price IS NOT NULL AND price > 0");
        }
        String onlyTrading = trimToNull(params.getString("onlyTrading"));
        if ("1".equals(onlyTrading)) {
            whereSql.append(" AND is_trading = 1");
        }

        long count = queryLong("SELECT COUNT(1) FROM dic_item" + whereSql, whereParams);
        List<Object> listParams = new ArrayList<>(whereParams);
        listParams.add(start);
        listParams.add(limit);
        JSONArray list = queryItemConfigRows("SELECT * FROM dic_item" + whereSql + " ORDER BY type ASC, id ASC LIMIT ?, ?", listParams);

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("settings", queryItemConfigSettings());
        return data;
    }

    @ServiceMethod(code = "202", description = "ItemConfig-update")
    public Object updateItemConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        Long id = params.getLong("id");
        if (id == null || id <= 0) {
            throwExp("itemId cannot be empty");
        }
        String name = trimToNull(params.getString("name"));
        if (name == null) {
            throwExp("itemName cannot be empty");
        }

        JSONArray synUse = normalizeIdArray(params.get("synUseList"));
        int canSyn = params.getIntValue("canSyn", 0);
        if (canSyn != 0 && canSyn != 1) {
            throwExp("canSyn is invalid");
        }

        int isTrading = requireSwitch(params, "isTrading", "isTrading");
        BigDecimal tradPrice = isTrading == 1 ? optionalNonNegativeDecimal(params, "tradPrice", "tradPrice") : BigDecimal.ZERO;
        int synNumber = canSyn == 1 ? requireNonNegativeInt(params, "synNumber", "synNumber") : 0;
        String synRate = canSyn == 1 ? emptyIfNull(params.getString("synRate")) : "";
        String synResultId = canSyn == 1 ? emptyIfNull(params.getString("synResultId")) : "";

        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(name);
        sqlParams.add(params.getInteger("number"));
        sqlParams.add(emptyIfNull(params.getString("context")));
        sqlParams.add(requireNonNegativeInt(params, "quality", "quality"));
        sqlParams.add(isTrading);
        sqlParams.add(canSyn);
        sqlParams.add(canSyn == 1 ? JSON.toJSONString(synUse) : null);
        sqlParams.add(synNumber);
        sqlParams.add(synRate);
        sqlParams.add(synResultId);
        sqlParams.add(requireNonNegativeInt(params, "durationDays", "durationDays"));
        sqlParams.add(requireNonNegativeDecimal(params, "price", "price"));
        sqlParams.add(tradPrice);
        sqlParams.add(optionalNonNegativeDecimal(params, "shopPrice", "shopPrice"));
        sqlParams.add(requireNonNegativeInt(params, "type", "type"));
        sqlParams.add(requireSwitch(params, "status", "status"));
        sqlParams.add(id);

        int rows = executeUpdate("UPDATE dic_item SET name = ?, number = ?, context = ?, quality = ?, is_trading = ?, can_syn = ?, syn_use = ?, " +
                "syn_number = ?, syn_rate = ?, syn_result_id = ?, duration_days = ?, price = ?, trad_price = ?, shop_price = ?, type = ?, " +
                "status = ?, update_time = NOW() WHERE id = ?", sqlParams);
        if (rows <= 0) {
            throwExp("item not found or update failed");
        }
        hotReloadTableVersion(Config.ITEM_VERSION);

        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("id", id);
            content.put("name", name);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateItemConfig", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }


    @ServiceMethod(code = "203", description = "ItemConfig-settings-update")
    public Object updateItemConfigSettings(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        String mode = trimToNull(params.getString("mode"));
        if ("convertRate".equals(mode)) {
            BigDecimal rate = requirePositiveDecimal(params, "convertRate", "convertRate");
            updateConfigAndNotify(Config.CONVERT_RATE, rate.stripTrailingZeros().toPlainString());
        } else if ("seedExchange".equals(mode)) {
            JSONObject cfg = normalizeSeedExchangeConfig(params);
            updateConfigAndNotify(Config.SEED_EXCHANGE_CONFIG, JSON.toJSONString(cfg));
        } else if ("seedSyn".equals(mode)) {
            updateConfigAndNotify(Config.SEED_SYN_DARK_SWITCH, String.valueOf(requireSwitch(params, "darkSwitch", "darkSwitch")));
            updateConfigAndNotify(Config.SEED_SYN_DARK_RATE_LV2, requireNonNegativeDecimal(params, "darkRateLv2", "darkRateLv2").stripTrailingZeros().toPlainString());
            updateConfigAndNotify(Config.SEED_SYN_DARK_RATE_LV3, requireNonNegativeDecimal(params, "darkRateLv3", "darkRateLv3").stripTrailingZeros().toPlainString());
            updateConfigAndNotify(Config.SEED_SYN_DARK_RATE_LV4, requireNonNegativeDecimal(params, "darkRateLv4", "darkRateLv4").stripTrailingZeros().toPlainString());
            updateConfigAndNotify(Config.SEED_SYN_DARK_RATE_LV5, requireNonNegativeDecimal(params, "darkRateLv5", "darkRateLv5").stripTrailingZeros().toPlainString());
            updateConfigAndNotify(Config.SEED_SYN_FAIL_POOL_RATE, requireNonNegativeDecimal(params, "failPoolRate", "failPoolRate").stripTrailingZeros().toPlainString());
        } else {
            throwExp("mode is invalid");
        }

        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("mode", mode);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateItemConfigSettings", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    @ServiceMethod(code = "204", description = "BountyConfig-get")
    public Object getBountyConfig(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }
        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = normalizeLimit(params.getIntValue("limit", 20));
        int start = (page - 1) * limit;

        String baseSql = " FROM t_bounty_task bt LEFT JOIN t_user u ON u.id = bt.user_id";
        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();

        appendOpsUserWhere(whereSql, whereParams, "bt.user_id", params);
        String keyword = trimToNull(params.getString("keyword"));
        if (keyword != null) {
            whereSql.append(" AND (u.name LIKE ? OR u.user_no = ? OR CAST(bt.user_id AS CHAR) = ?)");
            whereParams.add("%" + keyword + "%");
            whereParams.add(keyword);
            whereParams.add(keyword);
        }
        Integer status = params.getInteger("status");
        if (status != null && status >= 0) {
            whereSql.append(" AND bt.status = ?");
            whereParams.add(status);
        }
        String taskName = trimToNull(params.getString("taskName"));
        if (taskName != null) {
            whereSql.append(" AND (bt.task_name LIKE ? OR bt.task_title LIKE ?)");
            whereParams.add("%" + taskName + "%");
            whereParams.add("%" + taskName + "%");
        }
        appendDateWhere(whereSql, whereParams, "bt.create_time", params);

        long count = queryLong("SELECT COUNT(1) " + baseSql + whereSql, whereParams);
        List<Object> listParams = new ArrayList<>(whereParams);
        listParams.add(start);
        listParams.add(limit);
        JSONArray tasks = queryBountyRows("SELECT bt.*, u.user_no, u.name AS user_name " + baseSql + whereSql +
                " ORDER BY bt.create_time DESC, bt.id DESC LIMIT ?, ?", listParams);

        JSONObject data = new JSONObject();
        String feeRateValue = trimToNull(managerConfigService.getString(Config.BOUNTY_FEE_RATE));
        BigDecimal feeRate = new BigDecimal(feeRateValue == null ? "0" : feeRateValue);
        long poolCents = queryLong("SELECT COALESCE(pool_cents, 0) FROM t_bounty_fee_pool WHERE id = 1");
        BigDecimal poolAmount = new BigDecimal(poolCents).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        JSONObject config = new JSONObject();
        config.put("feeRate", feeRate.stripTrailingZeros().toPlainString());
        config.put("feeRatePercent", feeRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
        config.put("poolAmount", money(poolAmount));
        config.put("poolCents", poolCents);
        config.put("activeTaskCount", queryLong("SELECT COUNT(1) FROM t_bounty_task WHERE status = 1"));
        config.put("cancelTaskCount", queryLong("SELECT COUNT(1) FROM t_bounty_task WHERE status = 2"));
        config.put("finishedTaskCount", queryLong("SELECT COUNT(1) FROM t_bounty_task WHERE status = 3"));
        config.put("pendingAuditCount", queryLong("SELECT COUNT(1) FROM t_bounty_task_order WHERE status = 1"));
        config.put("pendingAppealCount", queryLong("SELECT COUNT(1) FROM t_bounty_task_order WHERE appeal_status = 1"));
        config.put("todayTaskCount", queryLong("SELECT COUNT(1) FROM t_bounty_task WHERE create_time >= CURDATE()"));
        config.put("todayFeeAmount", money(queryDecimal("SELECT COALESCE(SUM(fee_amount), 0) FROM t_bounty_task WHERE create_time >= CURDATE()")));
        config.put("totalFeeAmount", money(queryDecimal("SELECT COALESCE(SUM(fee_amount), 0) FROM t_bounty_task")));
        config.put("activeEscrowAmount", money(queryDecimal("SELECT COALESCE(SUM(escrow_amount), 0) FROM t_bounty_task WHERE status = 1")));
        data.put("config", config);
        data.put("tasks", tasks);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "205", description = "BountyConfig-update")
    public Object updateBountyConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        BigDecimal feeRate = requireNonNegativeDecimal(params, "feeRate", "feeRate");
        if (feeRate.compareTo(BigDecimal.ONE) > 0) {
            throwExp("feeRate cannot be greater than 1");
        }
        updateConfigAndNotify(Config.BOUNTY_FEE_RATE, feeRate.stripTrailingZeros().toPlainString());
        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("feeRate", feeRate);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateBountyConfig", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    @ServiceMethod(code = "206", description = "FarmConfig-get")
    public Object getFarmConfig(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        JSONArray list = queryFarmConfigRows();
        long enabledCount = 0;
        int minGrowSeconds = 0;
        int maxGrowSeconds = 0;
        for (int i = 0; i < list.size(); i++) {
            JSONObject row = list.getJSONObject(i);
            if (row.getIntValue("status") == 1) {
                enabledCount++;
            }
            int growSeconds = row.getIntValue("growSeconds");
            if (growSeconds > 0 && (minGrowSeconds == 0 || growSeconds < minGrowSeconds)) {
                minGrowSeconds = growSeconds;
            }
            if (growSeconds > maxGrowSeconds) {
                maxGrowSeconds = growSeconds;
            }
        }

        JSONObject summary = new JSONObject();
        summary.put("totalCount", list.size());
        summary.put("enabledCount", enabledCount);
        summary.put("disabledCount", list.size() - enabledCount);
        summary.put("minGrowText", farmGrowText(minGrowSeconds));
        summary.put("maxGrowText", farmGrowText(maxGrowSeconds));

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("summary", summary);
        data.put("count", list.size());
        return data;
    }

    @ServiceMethod(code = "207", description = "FarmConfig-update")
    public Object updateFarmConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        Long id = params.getLong("id");
        if (id == null || id <= 0) {
            throwExp("种地配置ID不能为空");
        }
        int growSeconds = requirePositiveInt(params, "growSeconds", "成长时间");
        int status = requireSwitch(params, "status", "状态");
        JSONArray rewards = normalizeFarmRewardArray(params.get("rewardItems"));

        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(JSON.toJSONString(rewards));
        sqlParams.add(growSeconds);
        sqlParams.add(status);
        sqlParams.add(id);
        int rows = executeUpdate("UPDATE dic_farm SET reward = ?, grow_seconds = ?, status = ?, update_time = NOW() WHERE id = ?", sqlParams);
        if (rows <= 0) {
            throwExp("种地配置不存在或更新失败");
        }
        String version = hotReloadTableVersion(Config.FARM_TABLE_VERSION);

        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("id", id);
            content.put("version", version);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateFarmConfig", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("version", version);
        return resp;
    }

    @ServiceMethod(code = "208", description = "JoyConfig-get")
    public Object getJoyConfig(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        return queryJoyConfig();
    }

    @ServiceMethod(code = "209", description = "JoyConfig-update")
    public Object updateJoyConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        BigDecimal exchangeRate = requirePositiveDecimal(params, "exchangeRate", "欢乐值兑换比例");
        JSONObject baseJoy = normalizeJoyBaseConfig(params.get("baseJoyList"));
        JSONObject percent = normalizeJoyPercentConfig(params.get("percentList"));

        updateConfigAndNotify(Config.JOY_EXCHANGE_RATE, exchangeRate.stripTrailingZeros().toPlainString());
        updateConfigAndNotify(Config.JOY_PER_LEVEL, JSON.toJSONString(baseJoy));
        updateConfigAndNotify(Config.JOY_LEVEL_PERCENT, JSON.toJSONString(percent));

        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("exchangeRate", exchangeRate);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateJoyConfig", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    @ServiceMethod(code = "210", description = "GuildConfig-get")
    public Object getGuildConfigForAdmin(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        JSONObject data = new JSONObject();
        data.put("config", queryGuildConfigValues());
        data.put("summary", queryGuildConfigSummary());
        data.put("list", queryGuildConfigRows());
        return data;
    }

    @ServiceMethod(code = "211", description = "GuildConfig-update")
    public Object updateGuildConfigForAdmin(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        BigDecimal guildUnitPrice = requireNonNegativeDecimal(params, "guildUnitPrice", "开通公会质押单价");
        BigDecimal createFeeRate = requireNonNegativeDecimal(params, "createFeeRate", "创建公会手续费率");
        if (createFeeRate.compareTo(BigDecimal.ONE) > 0) {
            if (createFeeRate.compareTo(new BigDecimal("100")) > 0) {
                throwExp("创建公会手续费率不能超过100%");
            }
            createFeeRate = createFeeRate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        }
        BigDecimal guildMemberFee = requireNonNegativeDecimal(params, "guildMemberFee", "添加成员费用");

        updateConfigAndNotify(Config.GUILD_FEE, guildUnitPrice.stripTrailingZeros().toPlainString());
        updateConfigAndNotify(Config.GUILD_CREATE_FEE_RATE, createFeeRate.stripTrailingZeros().toPlainString());
        updateConfigAndNotify(Config.GUILD_MEMBER_FEE, guildMemberFee.stripTrailingZeros().toPlainString());

        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("guildUnitPrice", guildUnitPrice);
            content.put("createFeeRate", createFeeRate);
            content.put("guildMemberFee", guildMemberFee);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateGuildConfigForAdmin", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        return resp;
    }

    @ServiceMethod(code = "212", description = "MiniGameConfig-get")
    public Object getMiniGameConfig(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        JSONObject data = new JSONObject();
        data.put("version", emptyIfNull(managerConfigService.getString(Config.GAME_TABLE_VERSION)));
        JSONArray games = new JSONArray();
        games.add(queryMiniGameConfigRow("lion", 7L, "疯狂的狮子", Config.DTS_STATUS));
        games.add(queryMiniGameConfigRow("rabbit", 1L, "消失的兔子", Config.DTS3_STATUS));
        games.add(queryMiniGameConfigRow("flip", 12L, "翻牌牌", Config.DTS2_STATUS));
        data.put("games", games);
        return data;
    }

    @ServiceMethod(code = "213", description = "MiniGameConfig-update")
    public Object updateMiniGameConfig(AdminSocketServer adminSocketServer, JSONObject params, Command webCommand) {
        checkAuth(adminSocketServer);
        checkNull(params);
        String gameCode = trimToNull(params.getString("gameCode"));
        String mode = trimToNull(params.getString("mode"));
        if (!"lion".equals(gameCode) && !"rabbit".equals(gameCode) && !"flip".equals(gameCode)) {
            throwExp("小游戏类型不正确");
        }
        String version = emptyIfNull(managerConfigService.getString(Config.GAME_TABLE_VERSION));
        if ("setting".equals(mode)) {
            version = updateMiniGameSetting(gameCode, params);
        } else if ("runtime".equals(mode)) {
            version = updateMiniGameRuntime(gameCode, params);
        } else {
            throwExp("保存类型不正确");
        }
        if (adminSocketServer != null) {
            JSONObject content = new JSONObject();
            content.put("gameCode", gameCode);
            content.put("mode", mode);
            content.put("version", version);
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "updateMiniGameConfig", content);
        }
        JSONObject resp = new JSONObject();
        resp.put("success", true);
        resp.put("version", version);
        return resp;
    }

    /**
     * VIP领取记录 - 列表查询
     * 平铺reward JSON，展示道具名称；关联用户名称
     */
    @ServiceMethod(code = "182", description = "VIP领取-获取列表")
    public Object getVipReceiveRecordList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);

        int page = params.getIntValue("page", 1);
        if (page <= 0) {
            page = 1;
        }
        int limit = params.getIntValue("limit", 20);
        if (limit <= 0) {
            limit = 20;
        }
        if (limit > 100) {
            limit = 100;
        }

        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");
        Integer vipType = params.getInteger("vipType");

        int start = (page - 1) * limit;
        Map<String, Object> condition = new HashMap<>();
        if (userId > 0) {
            condition.put("userId", userId);
        }
        if (userNo != null && !userNo.trim().isEmpty()) {
            condition.put("userNo", userNo.trim());
        }
        if (userName != null && !userName.trim().isEmpty()) {
            condition.put("userName", userName.trim());
        }
        if (vipType != null) {
            condition.put("vipType", vipType);
        }
        condition.put("start", start);
        condition.put("limit", limit);

        long count = vipReceiveRecordService.count("countForAdmin", condition);
        List<Map<String, Object>> list = vipReceiveRecordService.findList("findListForAdmin", condition);

        JSONArray array = new JSONArray();
        for (Map<String, Object> row : list) {
            JSONObject obj = new JSONObject();
            obj.put("id", row.get("id"));
            obj.put("userId", row.get("userId"));
            obj.put("userName", row.get("userName") == null ? "" : row.get("userName"));
            obj.put("userNo", row.get("userNo") == null ? "" : row.get("userNo"));
            obj.put("vipType", row.get("vipType"));
            obj.put("vipTypeName", Integer.valueOf(1).equals(row.get("vipType")) ? "VIP1" : "VIP2");
            obj.put("claimDate", row.get("claimDate"));
            obj.put("orderNo", row.get("orderNo") == null ? "" : row.get("orderNo"));
            obj.put("createTime", row.get("createTime"));

            // 解析reward JSON
            String rewardStr = row.get("reward") != null ? row.get("reward").toString() : "";
            JSONArray rewardItems = new JSONArray();
            if (!rewardStr.isEmpty()) {
                try {
                    JSONArray rewards = JSON.parseArray(rewardStr);
                    for (int i = 0; i < rewards.size(); i++) {
                        JSONObject r = rewards.getJSONObject(i);
                        int itemId = r.getIntValue("id");
                        int number = r.getIntValue("number");
                        int type = r.getIntValue("type");
                        Item item = itemCacheService.getItemInfoById((long) itemId);
                        JSONObject rewardObj = new JSONObject();
                        rewardObj.put("type", type);
                        rewardObj.put("id", itemId);
                        rewardObj.put("itemName", item == null ? "未知道具(ID:" + itemId + ")" : item.getName());
                        rewardObj.put("number", number);
                        rewardItems.add(rewardObj);
                    }
                } catch (Exception e) {
                    logger.warn("解析reward失败: " + rewardStr);
                }
            }
            obj.put("rewardItems", rewardItems);
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "060", description = "养兽详情")
    public Object searchPetInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }
        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = normalizeLimit(params.getIntValue("limit", 20));
        int start = (page - 1) * limit;

        String baseSql = " FROM (SELECT user_id FROM t_user_pet_user UNION SELECT user_id FROM t_user_pet WHERE status = 1) pu " +
                "LEFT JOIN t_user u ON u.id = pu.user_id " +
                "LEFT JOIN t_user_pet_user ppu ON ppu.user_id = pu.user_id " +
                "LEFT JOIN (SELECT user_id, COUNT(1) pet_count, MIN(buy_time) first_buy_time, MAX(buy_time) last_buy_time FROM t_user_pet WHERE status = 1 GROUP BY user_id) pc ON pc.user_id = pu.user_id";
        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();
        appendOpsUserWhere(whereSql, whereParams, "pu.user_id", params);

        String hasPet = trimToNull(params.getString("hasPet"));
        if ("1".equals(hasPet)) {
            whereSql.append(" AND COALESCE(pc.pet_count, 0) > 0");
        } else if ("0".equals(hasPet)) {
            whereSql.append(" AND COALESCE(pc.pet_count, 0) = 0");
        }
        String hasPending = trimToNull(params.getString("hasPending"));
        if ("1".equals(hasPending)) {
            whereSql.append(" AND COALESCE(ppu.pending_yield_amount, 0) > 0");
        } else if ("0".equals(hasPending)) {
            whereSql.append(" AND COALESCE(ppu.pending_yield_amount, 0) = 0");
        }
        String isFed = trimToNull(params.getString("isFed"));
        if ("1".equals(isFed)) {
            whereSql.append(" AND COALESCE(ppu.hunger_hours, 0) > 0");
        } else if ("0".equals(isFed)) {
            whereSql.append(" AND COALESCE(ppu.hunger_hours, 0) <= 0");
        }

        long count = queryLong("SELECT COUNT(1) " + baseSql + whereSql, whereParams);
        List<Object> listParams = new ArrayList<>(whereParams);
        listParams.add(start);
        listParams.add(limit);
        JSONArray list = queryPetInfoRows("SELECT pu.user_id, u.user_no, u.name AS user_name, COALESCE(pc.pet_count, 0) AS pet_count, " +
                "COALESCE(ppu.hunger_hours, 0) AS hunger_hours, COALESCE(ppu.pending_yield_amount, 0) AS pending_yield_amount, " +
                "COALESCE(ppu.total_yield_amount, 0) AS total_yield_amount, COALESCE(ppu.today_dividend_amount, 0) AS today_dividend_amount, " +
                "COALESCE(ppu.total_dividend_amount, 0) AS total_dividend_amount, COALESCE(ppu.unlock_lv3, 0) AS unlock_lv3, " +
                "COALESCE(ppu.unlock_lv4, 0) AS unlock_lv4, COALESCE(ppu.unlock_lv5, 0) AS unlock_lv5, ppu.last_settle_time, " +
                "pc.first_buy_time, pc.last_buy_time, ppu.update_time " + baseSql + whereSql +
                " ORDER BY COALESCE(pc.pet_count, 0) DESC, pu.user_id DESC LIMIT ?, ?", listParams);

        JSONObject summary = new JSONObject();
        summary.put("totalPetCount", queryLong("SELECT COUNT(1) FROM t_user_pet WHERE status = 1"));
        summary.put("todayBuyCount", queryLong("SELECT COUNT(1) FROM t_user_pet WHERE status = 1 AND buy_time >= CURDATE()"));
        summary.put("todayFeedTimes", queryLong("SELECT COUNT(1) FROM t_user_pet_record WHERE record_type = 4 AND status = 1 AND create_time >= CURDATE()"));
        summary.put("todayYield", money(queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM t_user_pet_record WHERE record_type = 1 AND status = 1 AND create_time >= CURDATE()")));
        summary.put("todayDividend", money(queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM t_user_pet_record WHERE record_type = 2 AND status = 1 AND create_time >= CURDATE()")));
        summary.put("pendingYieldTotal", money(queryDecimal("SELECT COALESCE(SUM(pending_yield_amount), 0) FROM t_user_pet_user")));

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("summary", summary);
        return data;
    }

    @ServiceMethod(code = "193", description = "农场运营")
    public Object searchFarmOps(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }
        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = normalizeLimit(params.getIntValue("limit", 20));
        int start = (page - 1) * limit;

        String baseSql = " FROM t_user_farm_land fl LEFT JOIN t_user u ON u.id = fl.user_id LEFT JOIN dic_item di ON di.id = fl.seed_item_id";
        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();
        appendOpsUserWhere(whereSql, whereParams, "fl.user_id", params);
        Integer status = params.getInteger("status");
        if (status != null && status >= 0) {
            whereSql.append(" AND fl.status = ?");
            whereParams.add(status);
        }
        Integer seedItemId = params.getInteger("seedItemId");
        if (seedItemId != null && seedItemId > 0) {
            whereSql.append(" AND fl.seed_item_id = ?");
            whereParams.add(seedItemId);
        }
        String canHarvest = trimToNull(params.getString("canHarvest"));
        if ("1".equals(canHarvest)) {
            whereSql.append(" AND fl.seed_item_id IS NOT NULL AND fl.seed_item_id > 0 AND fl.end_time IS NOT NULL AND fl.end_time <= NOW() AND fl.status IN (1, 2)");
        } else if ("0".equals(canHarvest)) {
            whereSql.append(" AND NOT (fl.seed_item_id IS NOT NULL AND fl.seed_item_id > 0 AND fl.end_time IS NOT NULL AND fl.end_time <= NOW() AND fl.status IN (1, 2))");
        }

        long count = queryLong("SELECT COUNT(1) " + baseSql + whereSql, whereParams);
        List<Object> listParams = new ArrayList<>(whereParams);
        listParams.add(start);
        listParams.add(limit);
        JSONArray list = queryFarmRows("SELECT fl.id, fl.user_id, u.user_no, u.name AS user_name, fl.land_index, fl.seed_item_id, di.name AS seed_name, " +
                "fl.status, fl.start_time, fl.end_time, fl.last_harvest_time, fl.create_time, fl.update_time, " +
                "CASE WHEN fl.seed_item_id IS NOT NULL AND fl.seed_item_id > 0 AND fl.end_time IS NOT NULL AND fl.end_time <= NOW() AND fl.status IN (1, 2) THEN 1 ELSE 0 END AS can_harvest, " +
                "CASE WHEN fl.end_time IS NULL THEN 0 ELSE GREATEST(TIMESTAMPDIFF(SECOND, NOW(), fl.end_time), 0) END AS remain_seconds " +
                baseSql + whereSql + " ORDER BY fl.update_time DESC, fl.id DESC LIMIT ?, ?", listParams);

        JSONObject summary = new JSONObject();
        summary.put("todayPlantCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE seed_item_id IS NOT NULL AND seed_item_id > 0 AND start_time >= CURDATE()"));
        summary.put("todayHarvestCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE last_harvest_time >= CURDATE()"));
        summary.put("canHarvestLandCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE seed_item_id IS NOT NULL AND seed_item_id > 0 AND end_time IS NOT NULL AND end_time <= NOW() AND status IN (1, 2)"));
        summary.put("growingCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE seed_item_id IS NOT NULL AND seed_item_id > 0 AND status = 1 AND (end_time IS NULL OR end_time > NOW())"));
        summary.put("emptyCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE seed_item_id IS NULL OR seed_item_id = 0 OR status = 0"));
        summary.put("activeUserCount", queryLong("SELECT COUNT(DISTINCT user_id) FROM t_user_farm_land WHERE seed_item_id IS NOT NULL AND seed_item_id > 0"));

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("summary", summary);
        return data;
    }

    @ServiceMethod(code = "194", description = "气球树运营")
    public Object searchJoyOps(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }
        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = normalizeLimit(params.getIntValue("limit", 20));
        int start = (page - 1) * limit;

        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();
        appendJoyWhere(whereSql, whereParams, params);

        long count = 0L;
        JSONArray list = new JSONArray();
        if (tableExists(null, "t_user_joy_event")) {
            String baseSql = " FROM t_user_joy_event e LEFT JOIN t_user ru ON ru.id = e.receiver_user_id LEFT JOIN t_user fu ON fu.id = e.from_user_id";
            count = queryLong("SELECT COUNT(1) " + baseSql + whereSql, whereParams);
            List<Object> listParams = new ArrayList<>(whereParams);
            listParams.add(start);
            listParams.add(limit);
            list = queryJoyRows("SELECT e.id, e.event_id, e.receiver_user_id, ru.user_no AS receiver_user_no, ru.name AS receiver_user_name, " +
                    "e.from_user_id, fu.user_no AS from_user_no, fu.name AS from_user_name, e.source_type, e.level, e.item_quality, " +
                    "e.base_joy, e.percent, e.joy_amount, e.calc_desc, e.create_time " + baseSql + whereSql +
                    " ORDER BY e.create_time DESC, e.id DESC LIMIT ?, ?", listParams);
        }

        JSONObject summary = new JSONObject();
        int todayInt = Integer.parseInt(new java.text.SimpleDateFormat("yyyyMMdd").format(new Date()));
        summary.put("todayJoy", money(queryDecimal("SELECT COALESCE(SUM(today_joy), 0) FROM t_user_joy WHERE today_date = " + todayInt)));
        summary.put("totalJoy", money(queryDecimal("SELECT COALESCE(SUM(total_joy), 0) FROM t_user_joy")));
        summary.put("availableJoy", money(queryDecimal("SELECT COALESCE(SUM(available_joy), 0) FROM t_user_joy")));
        summary.put("todayBalloonExchange", sumTodayBackpackLogByType(1013).longValue());
        summary.put("contribUserCount", tableExists(null, "t_user_joy_event") ? queryLong("SELECT COUNT(DISTINCT from_user_id) FROM t_user_joy_event WHERE create_time >= CURDATE()") : 0L);

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("summary", summary);
        return data;
    }

    @ServiceMethod(code = "195", description = "悬赏任务运营")
    public Object searchBountyTasks(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }
        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = normalizeLimit(params.getIntValue("limit", 20));
        int start = (page - 1) * limit;

        String baseSql = " FROM t_bounty_task bt LEFT JOIN t_user u ON u.id = bt.user_id";
        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();
        appendOpsUserWhere(whereSql, whereParams, "bt.user_id", params);
        Integer status = params.getInteger("status");
        if (status != null && status >= 0) {
            whereSql.append(" AND bt.status = ?");
            whereParams.add(status);
        }
        String taskName = trimToNull(params.getString("taskName"));
        if (taskName != null) {
            whereSql.append(" AND (bt.task_name LIKE ? OR bt.task_title LIKE ?)");
            whereParams.add("%" + taskName + "%");
            whereParams.add("%" + taskName + "%");
        }
        appendDateWhere(whereSql, whereParams, "bt.create_time", params);

        long count = queryLong("SELECT COUNT(1) " + baseSql + whereSql, whereParams);
        List<Object> listParams = new ArrayList<>(whereParams);
        listParams.add(start);
        listParams.add(limit);
        JSONArray list = queryBountyRows("SELECT bt.*, u.user_no, u.name AS user_name " + baseSql + whereSql +
                " ORDER BY bt.create_time DESC, bt.id DESC LIMIT ?, ?", listParams);

        JSONObject summary = new JSONObject();
        summary.put("activeTaskCount", queryLong("SELECT COUNT(1) FROM t_bounty_task WHERE status = 1"));
        summary.put("pendingAuditCount", queryLong("SELECT COUNT(1) FROM t_bounty_task_order WHERE status = 1"));
        summary.put("pendingAppealCount", queryLong("SELECT COUNT(1) FROM t_bounty_task_order WHERE appeal_status = 1"));
        summary.put("todayTaskCount", queryLong("SELECT COUNT(1) FROM t_bounty_task WHERE create_time >= CURDATE()"));
        summary.put("todayFee", money(queryDecimal("SELECT COALESCE(SUM(fee_amount), 0) FROM t_bounty_task WHERE create_time >= CURDATE()")));
        summary.put("escrowAmount", money(queryDecimal("SELECT COALESCE(SUM(escrow_amount), 0) FROM t_bounty_task WHERE status = 1")));
        // 全服配置：手续费奖池 + 手续费率
        BigDecimal feeRate = new BigDecimal(managerConfigService.getString(Config.BOUNTY_FEE_RATE));
        summary.put("feeRate", feeRate.stripTrailingZeros().toPlainString());
        long poolCents = queryLong("SELECT COALESCE(pool_cents, 0) FROM t_bounty_fee_pool WHERE id = 1");
        BigDecimal poolAmount = new BigDecimal(poolCents).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        summary.put("feePoolAmount", money(poolAmount));
        summary.put("feePoolCents", poolCents);

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("summary", summary);
        return data;
    }

    @ServiceMethod(code = "196", description = "交易行运营增强")
    public Object searchTradingOps(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }
        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = normalizeLimit(params.getIntValue("limit", 20));
        int start = (page - 1) * limit;

        String baseSql = " FROM t_trading tr LEFT JOIN t_user u ON u.id = tr.user_id LEFT JOIN dic_item di ON di.id = tr.item_id";
        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();
        appendOpsUserWhere(whereSql, whereParams, "tr.user_id", params);
        Integer type = params.getInteger("type");
        if (type != null && type >= 0) {
            whereSql.append(" AND tr.type = ?");
            whereParams.add(type);
        }
        Integer status = params.getInteger("status");
        if (status != null && status >= 0) {
            whereSql.append(" AND tr.status = ?");
            whereParams.add(status);
        }
        Integer itemId = params.getInteger("itemId");
        if (itemId != null && itemId > 0) {
            whereSql.append(" AND tr.item_id = ?");
            whereParams.add(itemId);
        }
        appendDateWhere(whereSql, whereParams, "tr.create_time", params);

        long count = queryLong("SELECT COUNT(1) " + baseSql + whereSql, whereParams);
        List<Object> listParams = new ArrayList<>(whereParams);
        listParams.add(start);
        listParams.add(limit);
        JSONArray list = queryTradingRows("SELECT tr.*, u.user_no, u.name AS user_name, di.name AS item_name " + baseSql + whereSql +
                " ORDER BY tr.create_time DESC, tr.id DESC LIMIT ?, ?", listParams);

        JSONObject summary = new JSONObject();
        summary.put("currentSellCount", queryLong("SELECT COUNT(1) FROM t_trading WHERE type = 0 AND status = 1"));
        summary.put("currentAskBuyCount", queryLong("SELECT COUNT(1) FROM t_trading WHERE type = 1 AND status = 1"));
        summary.put("todayDealCount", queryLong("SELECT COUNT(DISTINCT order_no) FROM r_trading_record WHERE create_time >= CURDATE()"));
        summary.put("todayAmount", money(queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM r_trading_record WHERE type = 1 AND create_time >= CURDATE()")));
        summary.put("todayFee", money(queryDecimal("SELECT COALESCE(SUM(fee), 0) FROM r_trading_record WHERE type = 2 AND create_time >= CURDATE()")));
        summary.put("highPriceCount", queryLong("SELECT COUNT(1) FROM t_trading WHERE status = 1 AND item_price >= 10000"));

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("summary", summary);
        return data;
    }

    @ServiceMethod(code = "130", description = "小游戏日志")
    public Object searchDtsLog(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);
        if (params == null) {
            params = new JSONObject();
        }

        int page = Math.max(1, params.getIntValue("page", 1));
        int limit = params.getIntValue("limit", 20);
        if (limit <= 0) {
            limit = 20;
        }
        limit = Math.min(limit, 200);
        int start = (page - 1) * limit;

        String baseSql = dtsLogBaseSql();
        StringBuilder whereSql = new StringBuilder(" WHERE 1 = 1");
        List<Object> whereParams = new ArrayList<>();
        appendDtsLogWhere(whereSql, whereParams, params);

        long count = queryLong("SELECT COUNT(1) " + baseSql + whereSql, whereParams);

        List<Object> listParams = new ArrayList<>(whereParams);
        listParams.add(start);
        listParams.add(limit);
        JSONArray list = queryDtsLogRows("SELECT r.game_id, r.game_name, r.record_id, r.user_id, r.order_no, r.periods_num, " +
                "r.bet_info, r.bet_amount, r.profit, r.lottery_result, r.win_or_lose, r.status, r.create_time, r.update_time, " +
                "u.user_no, u.name AS user_name " + baseSql + whereSql +
                " ORDER BY r.create_time DESC, r.record_id DESC LIMIT ?, ?", listParams);

        JSONObject summary = queryDtsLogSummary(baseSql, whereSql.toString(), whereParams);

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        data.put("summary", summary);
        return data;
    }

    @ServiceMethod(code = "190", description = "首页运营驾驶舱统计")
    public Object getDashboardOpsStats(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkAuth(adminSocketServer);

        JSONObject data = new JSONObject();

        JSONObject relation = new JSONObject();
        relation.put("todayJoy", money(queryDecimal("SELECT COALESCE(SUM(today_joy), 0) FROM t_user_joy WHERE today_date = CAST(DATE_FORMAT(CURDATE(), '%Y%m%d') AS UNSIGNED)")));
        relation.put("todayPetDividend", money(queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM t_user_pet_record WHERE record_type = 2 AND status = 1 AND create_time >= CURDATE()")));
        relation.put("todayNewValidChildren", queryLong("SELECT COUNT(1) FROM t_user WHERE parent_id IS NOT NULL AND parent_id > 0 AND regist_time >= CURDATE()"));
        data.put("relation", relation);

        JSONObject pet = new JSONObject();
        pet.put("todayBuyCount", queryLong("SELECT COUNT(1) FROM t_user_pet WHERE status = 1 AND buy_time >= CURDATE()"));
        pet.put("todayFeedTimes", queryLong("SELECT COUNT(1) FROM t_user_pet_record WHERE record_type = 4 AND status = 1 AND create_time >= CURDATE()"));
        pet.put("todayYield", money(queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM t_user_pet_record WHERE record_type = 1 AND status = 1 AND create_time >= CURDATE()")));
        pet.put("todayClaim", money(queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM t_user_pet_record WHERE record_type = 5 AND status = 1 AND create_time >= CURDATE()")));
        pet.put("todayDividend", relation.getString("todayPetDividend"));
        pet.put("pendingYieldTotal", money(queryDecimal("SELECT COALESCE(SUM(pending_yield_amount), 0) FROM t_user_pet_user")));
        data.put("pet", pet);

        JSONObject farmJoy = new JSONObject();
        farmJoy.put("todayPlantCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE seed_item_id IS NOT NULL AND seed_item_id > 0 AND start_time >= CURDATE()"));
        farmJoy.put("todayHarvestCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE last_harvest_time >= CURDATE()"));
        farmJoy.put("todayJoy", relation.getString("todayJoy"));
        farmJoy.put("todayBalloonExchange", sumTodayBackpackLogByType(1013).longValue());
        farmJoy.put("canHarvestLandCount", queryLong("SELECT COUNT(1) FROM t_user_farm_land WHERE seed_item_id IS NOT NULL AND seed_item_id > 0 AND end_time IS NOT NULL AND end_time <= NOW() AND status IN (1, 2)"));
        data.put("farmJoy", farmJoy);

        JSONObject lionGame = queryMiniGameStats("r_battle_royale_record");
        JSONObject flipGame = queryMiniGameStats("r_battle_royale2_record");
        JSONObject rabbitGame = queryMiniGameStats("r_battle_royale3_record");

        BigDecimal todayBet = decimalFromJson(lionGame, "bet")
                .add(decimalFromJson(flipGame, "bet"))
                .add(decimalFromJson(rabbitGame, "bet"));
        BigDecimal platformProfit = decimalFromJson(lionGame, "platformProfit")
                .add(decimalFromJson(flipGame, "platformProfit"))
                .add(decimalFromJson(rabbitGame, "platformProfit"));
        long abnormalRoundCount = lionGame.getLongValue("abnormalRoundCount")
                + flipGame.getLongValue("abnormalRoundCount")
                + rabbitGame.getLongValue("abnormalRoundCount");

        JSONObject miniGame = new JSONObject();
        miniGame.put("lion", lionGame);
        miniGame.put("flipCard", flipGame);
        miniGame.put("rabbit", rabbitGame);
        miniGame.put("dts1Name", "疯狂的狮子");
        miniGame.put("dts1Bet", lionGame.getString("bet"));
        miniGame.put("dts1Reward", lionGame.getString("reward"));
        miniGame.put("dts1Profit", lionGame.getString("platformProfit"));
        miniGame.put("dts2Name", "翻牌牌");
        miniGame.put("dts2Bet", flipGame.getString("bet"));
        miniGame.put("dts2Reward", flipGame.getString("reward"));
        miniGame.put("dts2Profit", flipGame.getString("platformProfit"));
        miniGame.put("dts3Name", "消失的兔子");
        miniGame.put("dts3Bet", rabbitGame.getString("bet"));
        miniGame.put("dts3Reward", rabbitGame.getString("reward"));
        miniGame.put("dts3Profit", rabbitGame.getString("platformProfit"));
        miniGame.put("todayBet", money(todayBet));
        miniGame.put("platformProfit", money(platformProfit));
        miniGame.put("abnormalRoundCount", abnormalRoundCount);
        data.put("miniGame", miniGame);

        JSONObject bounty = new JSONObject();
        bounty.put("pendingAuditCount", queryLong("SELECT COUNT(1) FROM t_bounty_task_order WHERE status = 1"));
        bounty.put("pendingAppealCount", queryLong("SELECT COUNT(1) FROM t_bounty_task_order WHERE appeal_status = 1"));
        bounty.put("activeTaskCount", queryLong("SELECT COUNT(1) FROM t_bounty_task WHERE status = 1"));
        bounty.put("todayFee", money(queryDecimal("SELECT COALESCE(SUM(fee_amount), 0) FROM t_bounty_task WHERE create_time >= CURDATE()")));
        data.put("bounty", bounty);

        JSONObject trading = new JSONObject();
        trading.put("todayAmount", money(queryDecimal("SELECT COALESCE(SUM(amount), 0) FROM r_trading_record WHERE type = 1 AND create_time >= CURDATE()")));
        trading.put("todayFee", money(queryDecimal("SELECT COALESCE(SUM(fee), 0) FROM r_trading_record WHERE type = 2 AND create_time >= CURDATE()")));
        trading.put("todayOrderCount", queryLong("SELECT COUNT(DISTINCT order_no) FROM r_trading_record WHERE create_time >= CURDATE()"));
        data.put("trading", trading);

        return data;
    }

    private BigDecimal queryDecimal(String sql) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                BigDecimal value = resultSet.getBigDecimal(1);
                return value == null ? BigDecimal.ZERO : value;
            }
        } catch (Exception e) {
            logger().warn("[DashboardOps] query failed: " + sql, e);
        }
        return BigDecimal.ZERO;
    }

    private long queryLong(String sql) {
        return queryDecimal(sql).longValue();
    }

    private long queryLong(String sql, List<Object> params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
        } catch (Exception e) {
            logger().warn("[DTSLog] query count failed: " + sql, e);
        }
        return 0L;
    }

    private BigDecimal queryDecimal(String sql, List<Object> params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    BigDecimal value = resultSet.getBigDecimal(1);
                    return value == null ? BigDecimal.ZERO : value;
                }
            }
        } catch (Exception e) {
            logger().warn("[DTSLog] query decimal failed: " + sql, e);
        }
        return BigDecimal.ZERO;
    }

    private void fillStatement(PreparedStatement statement, List<Object> params) throws Exception {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }

    private String dtsLogBaseSql() {
        return " FROM (" +
                " SELECT 7 AS game_id, '疯狂的狮子' AS game_name, id AS record_id, user_id, order_no, periods_num, bet_info, bet_amount, profit, lottery_result, win_or_lose, status, create_time, update_time FROM r_battle_royale_record" +
                " UNION ALL " +
                " SELECT 12 AS game_id, '翻牌牌' AS game_name, id AS record_id, user_id, order_no, periods_num, bet_info, bet_amount, profit, lottery_result, win_or_lose, status, create_time, update_time FROM r_battle_royale2_record" +
                " UNION ALL " +
                " SELECT 1 AS game_id, '消失的兔子' AS game_name, id AS record_id, user_id, order_no, periods_num, bet_info, bet_amount, profit, lottery_result, win_or_lose, status, create_time, update_time FROM r_battle_royale3_record" +
                " ) r LEFT JOIN t_user u ON u.id = r.user_id";
    }

    private void appendDtsLogWhere(StringBuilder whereSql, List<Object> params, JSONObject request) {
        Integer gameId = request.getInteger("gameId");
        if (gameId != null && (gameId == 1 || gameId == 7 || gameId == 12)) {
            whereSql.append(" AND r.game_id = ?");
            params.add(gameId);
        }

        Long userId = parseLongOrNull(trimToNull(request.getString("userId")));
        if (userId != null && userId > 0) {
            whereSql.append(" AND r.user_id = ?");
            params.add(userId);
        }

        String userNo = trimToNull(request.getString("userNo"));
        if (userNo != null) {
            whereSql.append(" AND (u.user_no = ? OR CAST(r.user_id AS CHAR) = ?)");
            params.add(userNo);
            params.add(userNo);
        }

        String userName = trimToNull(request.getString("userName"));
        if (userName != null) {
            whereSql.append(" AND u.name LIKE ?");
            params.add("%" + userName + "%");
        }

        String periodsNum = trimToNull(request.getString("periodsNum"));
        if (periodsNum != null) {
            whereSql.append(" AND r.periods_num = ?");
            params.add(periodsNum);
        }

        String orderNo = trimToNull(request.getString("orderNo"));
        if (orderNo != null) {
            whereSql.append(" AND r.order_no = ?");
            params.add(orderNo);
        }

        Integer status = request.getInteger("status");
        if (status != null && status >= 0) {
            whereSql.append(" AND r.status = ?");
            params.add(status);
        }

        Integer winOrLose = request.getInteger("winOrLose");
        if (winOrLose != null && winOrLose >= 0) {
            whereSql.append(" AND r.win_or_lose = ?");
            params.add(winOrLose);
        }

        String startDate = normalizeStartDate(trimToNull(request.getString("startDate")));
        if (startDate != null) {
            whereSql.append(" AND r.create_time >= ?");
            params.add(startDate);
        }

        String endDate = normalizeEndDate(trimToNull(request.getString("endDate")));
        if (endDate != null) {
            whereSql.append(" AND r.create_time <= ?");
            params.add(endDate);
        }
    }

    private JSONArray queryDtsLogRows(String sql, List<Object> params) {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int gameId = resultSet.getInt("game_id");
                    long recordId = resultSet.getLong("record_id");
                    String betInfo = resultSet.getString("bet_info");
                    String lotteryResult = resultSet.getString("lottery_result");
                    int status = resultSet.getInt("status");
                    Object winOrLoseObject = resultSet.getObject("win_or_lose");
                    Integer winOrLose = winOrLoseObject == null ? null : resultSet.getInt("win_or_lose");

                    JSONObject row = new JSONObject();
                    row.put("id", gameId + "-" + recordId);
                    row.put("recordId", recordId);
                    row.put("gameId", gameId);
                    row.put("gameName", resultSet.getString("game_name"));
                    row.put("userId", resultSet.getLong("user_id"));
                    row.put("userNo", emptyIfNull(resultSet.getString("user_no")));
                    row.put("userName", emptyIfNull(resultSet.getString("user_name")));
                    row.put("orderNo", emptyIfNull(resultSet.getString("order_no")));
                    row.put("periodsNum", emptyIfNull(resultSet.getString("periods_num")));
                    row.put("betInfo", emptyIfNull(betInfo));
                    row.put("betInfoText", formatDtsBetInfo(gameId, betInfo));
                    row.put("betAmount", money(resultSet.getBigDecimal("bet_amount")));
                    row.put("profit", status == 1 ? money(resultSet.getBigDecimal("profit")) : "--");
                    row.put("platformProfit", status == 1 ? money(BigDecimal.ZERO.subtract(nullToZero(resultSet.getBigDecimal("profit")))) : "--");
                    row.put("lotteryResult", emptyIfNull(lotteryResult));
                    row.put("lotteryResultText", status == 1 ? formatDtsLotteryResult(gameId, lotteryResult) : "--");
                    row.put("winOrLose", winOrLose);
                    row.put("winOrLoseName", formatWinOrLose(status, winOrLose));
                    row.put("status", status);
                    row.put("statusName", status == 1 ? "已结算" : "未结算");
                    row.put("createTime", resultSet.getTimestamp("create_time"));
                    row.put("updateTime", resultSet.getTimestamp("update_time"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[DTSLog] query rows failed: " + sql, e);
        }
        return list;
    }

    private JSONObject queryDtsLogSummary(String baseSql, String whereSql, List<Object> whereParams) {
        JSONObject summary = new JSONObject();
        summary.put("betAmount", money(queryDecimal("SELECT COALESCE(SUM(r.bet_amount), 0) " + baseSql + whereSql, whereParams)));
        summary.put("profit", money(queryDecimal("SELECT COALESCE(SUM(CASE WHEN r.status = 1 THEN r.profit ELSE 0 END), 0) " + baseSql + whereSql, whereParams)));
        summary.put("platformProfit", money(queryDecimal("SELECT COALESCE(SUM(CASE WHEN r.status = 1 THEN -r.profit ELSE 0 END), 0) " + baseSql + whereSql, whereParams)));
        return summary;
    }

    private String formatDtsBetInfo(int gameId, String betInfo) {
        String value = trimToNull(betInfo);
        if (value == null) {
            return "--";
        }
        if (gameId == 12 && value.startsWith("{")) {
            try {
                JSONObject object = JSON.parseObject(value);
                List<String> items = new ArrayList<>();
                for (String key : object.keySet()) {
                    items.add(formatDtsOption(gameId, key) + ":" + object.getString(key));
                }
                return items.isEmpty() ? "--" : String.join("，", items);
            } catch (Exception ignored) {
                return value;
            }
        }
        return formatDtsOption(gameId, value);
    }

    private String formatDtsLotteryResult(int gameId, String lotteryResult) {
        String value = trimToNull(lotteryResult);
        if (value == null) {
            return "--";
        }
        if (value.startsWith("[") && value.endsWith("]")) {
            try {
                JSONArray array = JSON.parseArray(value);
                List<String> items = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    items.add(formatDtsOption(gameId, String.valueOf(array.get(i))));
                }
                return items.isEmpty() ? "--" : String.join("，", items);
            } catch (Exception ignored) {
                return value;
            }
        }
        return formatDtsOption(gameId, value);
    }

    private String formatDtsOption(int gameId, String raw) {
        String value = trimToNull(raw);
        if (value == null) {
            return "--";
        }
        if (gameId == 12) {
            return "元素" + value;
        }
        return value + "号";
    }

    private String formatWinOrLose(int status, Integer winOrLose) {
        if (status != 1 || winOrLose == null) {
            return "未结算";
        }
        return winOrLose == 1 ? "胜利" : "失败";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String result = value.trim();
        return result.isEmpty() ? null : result;
    }

    private String normalizeStartDate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() == 10 ? value + " 00:00:00" : value;
    }

    private String normalizeEndDate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() == 10 ? value + " 23:59:59" : value;
    }

    private Long parseLongOrNull(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal nullToZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String emptyIfNull(String value) {
        return value == null ? "" : value;
    }

    private int executeUpdate(String sql, List<Object> params) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            return statement.executeUpdate();
        } catch (Exception e) {
            logger().warn("[AdminConfig] update failed: " + sql, e);
            throwExp("保存失败");
            return 0;
        }
    }

    private String hotReloadTableVersion(String versionKey) {
        Config versionConfig = configService.getConfigByKey(versionKey);
        if (versionConfig == null) {
            throwExp("缺少版本配置：" + versionKey);
        }
        String newVersion = nextConfigVersion(versionConfig.getValue());
        updateConfigAndNotify(versionKey, newVersion);
        return newVersion;
    }


    private void updateConfigAndNotify(String key, String value) {
        managerConfigService.updateConfigData(key, value);
        managerConfigService.updateGameKey(key, value);
    }

    private void upsertConfigAndNotify(String key, String value) {
        managerConfigService.upsertConfigData(key, value);
        managerConfigService.updateGameKey(key, value);
    }

    private String nextConfigVersion(String currentVersion) {
        if (currentVersion == null || currentVersion.trim().isEmpty()) {
            return String.valueOf(System.currentTimeMillis());
        }
        String value = currentVersion.trim();
        if (value.matches("\\d+")) {
            return String.valueOf(Long.parseLong(value) + 1);
        }
        int dotIndex = value.lastIndexOf('.');
        String prefix = dotIndex >= 0 ? value.substring(0, dotIndex + 1) : "";
        String tail = dotIndex >= 0 ? value.substring(dotIndex + 1) : value;
        if (tail.matches("\\d+")) {
            return prefix + (Long.parseLong(tail) + 1);
        }
        return value + "." + System.currentTimeMillis();
    }

    private boolean columnExists(String tableName, String columnName) {
        String sql = "SELECT COUNT(1) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        List<Object> params = new ArrayList<>();
        params.add(tableName);
        params.add(columnName);
        return queryLong(sql, params) > 0;
    }

    private JSONArray queryDailyTaskConfigRows() {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM dic_daily_task ORDER BY sort ASC, id ASC");
             ResultSet rs = statement.executeQuery()) {
            Set<String> columns = resultColumns(rs);
            while (rs.next()) {
                JSONObject row = new JSONObject();
                long id = rs.getLong("id");
                String rewardRaw = getStringIfExists(rs, columns, "reward");
                JSONArray rewards = parseRewardItems(rewardRaw);
                row.put("id", id);
                row.put("context", emptyIfNull(getStringIfExists(rs, columns, "context")));
                row.put("condition", getIntIfExists(rs, columns, "condition"));
                row.put("sort", getIntIfExists(rs, columns, "sort"));
                row.put("type", getIntIfExists(rs, columns, "type"));
                row.put("category", emptyIfNull(getStringIfExists(rs, columns, "category")));
                row.put("expand", getIntIfExists(rs, columns, "expand"));
                row.put("rewardRaw", rewardRaw);
                row.put("rewardItems", rewards);
                row.put("rewardText", rewardText(rewards));
                list.add(row);
            }
        } catch (Exception e) {
            logger().warn("[DailyTaskConfig] query failed", e);
        }
        return list;
    }

    private void updateDailyTaskRow(JSONObject params) {
        Long id = params.getLong("id");
        if (id == null || id <= 0) {
            throwExp("任务ID不能为空");
        }
        String context = trimToNull(params.getString("context"));
        if (context == null) {
            throwExp("任务说明不能为空");
        }
        JSONArray reward = normalizeRewardArray(params.get("rewardItems"));
        int condition = requireNonNegativeInt(params, "condition", "完成条件");
        int sort = requireNonNegativeInt(params, "sort", "排序");

        StringBuilder sql = new StringBuilder("UPDATE dic_daily_task SET context = ?, `condition` = ?, reward = ?, sort = ?");
        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(context);
        sqlParams.add(condition);
        sqlParams.add(JSON.toJSONString(reward));
        sqlParams.add(sort);
        if (columnExists("dic_daily_task", "type")) {
            sql.append(", type = ?");
            sqlParams.add(requireNonNegativeInt(params, "type", "任务类型"));
        }
        if (columnExists("dic_daily_task", "category")) {
            sql.append(", category = ?");
            sqlParams.add(emptyIfNull(params.getString("category")));
        }
        if (columnExists("dic_daily_task", "expand")) {
            sql.append(", expand = ?");
            sqlParams.add(requireNonNegativeInt(params, "expand", "扩展值"));
        }
        sql.append(" WHERE id = ?");
        sqlParams.add(id);

        int rows = executeUpdate(sql.toString(), sqlParams);
        if (rows <= 0) {
            throwExp("每日任务不存在或更新失败");
        }
    }

    private JSONArray parseDailyTaskBoxes(String value) {
        JSONArray boxes = new JSONArray();
        if (value == null || value.trim().isEmpty()) {
            return boxes;
        }
        try {
            JSONArray raw = JSON.parseArray(value);
            for (int i = 0; i < raw.size(); i++) {
                JSONObject box = raw.getJSONObject(i);
                JSONArray rewards = box.containsKey("rewardRandomOne")
                        ? parseRewardItems(box.get("rewardRandomOne"))
                        : parseRewardItems(box.get("reward"));
                JSONObject row = new JSONObject();
                row.put("id", box.getString("id"));
                row.put("condition", box.getIntValue("condition"));
                row.put("rewardMode", box.containsKey("rewardRandomOne") ? "randomOne" : "fixed");
                row.put("rewardItems", rewards);
                row.put("rewardText", rewardText(rewards));
                row.put("raw", box);
                boxes.add(row);
            }
        } catch (Exception e) {
            logger().warn("[DailyTaskConfig] parse box config failed: " + value, e);
        }
        return boxes;
    }

    private void updateDailyTaskBoxConfig(JSONObject params) {
        String id = trimToNull(params.getString("id"));
        if (id == null) {
            throwExp("宝箱ID不能为空");
        }
        int condition = requirePositiveInt(params, "condition", "领取门槛");
        String rewardMode = trimToNull(params.getString("rewardMode"));
        if (!"randomOne".equals(rewardMode)) {
            rewardMode = "fixed";
        }
        JSONArray reward = normalizeRewardArray(params.get("rewardItems"));
        JSONArray boxes = JSON.parseArray(managerConfigService.getString(Config.DAILY_TASK_BOX_CONFIG));
        boolean updated = false;
        for (int i = 0; i < boxes.size(); i++) {
            JSONObject box = boxes.getJSONObject(i);
            if (id.equals(box.getString("id"))) {
                box.put("condition", condition);
                box.remove("reward");
                box.remove("rewardRandomOne");
                if ("randomOne".equals(rewardMode)) {
                    box.put("rewardRandomOne", reward);
                } else {
                    box.put("reward", reward);
                }
                updated = true;
                break;
            }
        }
        if (!updated) {
            JSONObject box = new JSONObject();
            box.put("id", id);
            box.put("condition", condition);
            if ("randomOne".equals(rewardMode)) {
                box.put("rewardRandomOne", reward);
            } else {
                box.put("reward", reward);
            }
            boxes.add(box);
        }
        updateConfigAndNotify(Config.DAILY_TASK_BOX_CONFIG, JSON.toJSONString(boxes));
    }

    private JSONArray queryFarmConfigRows() {
        JSONArray list = new JSONArray();
        String sql = "SELECT f.*, seed.name AS seed_name, seed.quality AS seed_quality, seed.type AS seed_type, seed.context AS seed_context " +
                "FROM dic_farm f LEFT JOIN dic_item seed ON seed.id = f.seed_item_id ORDER BY f.seed_item_id ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                JSONObject row = new JSONObject();
                int seedItemId = rs.getInt("seed_item_id");
                int growSeconds = rs.getInt("grow_seconds");
                JSONArray rewards = parseRewardItems(rs.getString("reward"));
                row.put("id", rs.getLong("id"));
                row.put("seedItemId", seedItemId);
                row.put("seedName", emptyIfNull(rs.getString("seed_name")).isEmpty() ? itemName(seedItemId) : rs.getString("seed_name"));
                row.put("seedQuality", rs.getInt("seed_quality"));
                row.put("seedQualityName", qualityName(rs.getInt("seed_quality")));
                row.put("seedType", rs.getInt("seed_type"));
                row.put("seedTypeName", itemTypeName(rs.getInt("seed_type")));
                row.put("seedContext", emptyIfNull(rs.getString("seed_context")));
                row.put("rewardItems", rewards);
                row.put("rewardText", rewardText(rewards));
                row.put("growSeconds", growSeconds);
                row.put("growHours", new BigDecimal(growSeconds).divide(new BigDecimal("3600"), 2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                row.put("growText", farmGrowText(growSeconds));
                row.put("status", rs.getInt("status"));
                row.put("statusName", rs.getInt("status") == 1 ? "启用" : "停用");
                row.put("createTime", rs.getTimestamp("create_time"));
                row.put("updateTime", rs.getTimestamp("update_time"));
                list.add(row);
            }
        } catch (Exception e) {
            logger().warn("[FarmConfig] query failed", e);
        }
        return list;
    }

    private JSONObject queryJoyConfig() {
        JSONObject data = new JSONObject();
        String exchangeRate = trimToNull(managerConfigService.getString(Config.JOY_EXCHANGE_RATE));
        JSONObject perLevel = parseJsonObjectSafe(managerConfigService.getString(Config.JOY_PER_LEVEL));
        JSONObject percent = parseJsonObjectSafe(managerConfigService.getString(Config.JOY_LEVEL_PERCENT));

        JSONArray baseJoyList = new JSONArray();
        for (int i = 1; i <= 5; i++) {
            BigDecimal value = perLevel.getBigDecimal(String.valueOf(i));
            JSONObject row = new JSONObject();
            row.put("level", i);
            row.put("levelName", i + "阶种子");
            row.put("baseJoy", value == null ? BigDecimal.ZERO : value);
            baseJoyList.add(row);
        }

        JSONArray percentList = new JSONArray();
        BigDecimal percentTotal = BigDecimal.ZERO;
        for (int i = 1; i <= 5; i++) {
            BigDecimal value = percent.getBigDecimal(String.valueOf(i));
            if (value == null) {
                value = BigDecimal.ZERO;
            }
            percentTotal = percentTotal.add(value);
            JSONObject row = new JSONObject();
            row.put("level", i);
            row.put("levelName", i + "代上级");
            row.put("percent", value);
            percentList.add(row);
        }

        JSONObject config = new JSONObject();
        config.put("exchangeRate", exchangeRate == null ? "0" : exchangeRate);
        config.put("exchangeText", "每 " + (exchangeRate == null ? "0" : exchangeRate) + " 欢乐值兑换 1 组气球");
        config.put("baseJoyList", baseJoyList);
        config.put("percentList", percentList);
        config.put("percentTotal", percentTotal.stripTrailingZeros().toPlainString());
        config.put("percentValid", percentTotal.compareTo(new BigDecimal("100")) <= 0);
        data.put("config", config);
        return data;
    }

    private JSONObject queryGuildConfigValues() {
        BigDecimal guildFee = configDecimal(Config.GUILD_FEE);
        BigDecimal createFeeRate = configDecimal(Config.GUILD_CREATE_FEE_RATE);
        BigDecimal guildMemberFee = configDecimal(Config.GUILD_MEMBER_FEE);
        JSONObject config = new JSONObject();
        config.put("guildUnitPrice", guildFee.stripTrailingZeros().toPlainString());
        config.put("createFeeRate", createFeeRate.stripTrailingZeros().toPlainString());
        config.put("createFeeRatePercent", createFeeRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
        config.put("guildMemberFee", guildMemberFee.stripTrailingZeros().toPlainString());
        config.put("capitalName", capitalName(UserCapitalTypeEnum.hxjf.getValue()));
        return config;
    }

    private JSONObject queryMiniGameConfigRow(String code, long gameId, String displayName, String statusKey) {
        Game game = miniGameService.findGameById(gameId);
        if (game == null) {
            throwExp("缺少小游戏配置：" + displayName);
        }
        JSONObject setting = parseJsonObjectSafe(game.getGameSetting());
        int capitalType = setting.getIntValue("capitalType");
        JSONObject runtime = new JSONObject();
        runtime.put("status", configInteger(statusKey, game.getStatus() == null ? 0 : game.getStatus()));
        runtime.put("statusKey", statusKey);
        if ("lion".equals(code)) {
            runtime.put("killRate", configString(Config.DTS_KILL_RATE, "0"));
            runtime.put("killRateKey", Config.DTS_KILL_RATE);
            runtime.put("botProbability", configString(Config.GAME_DTS_NEED_BOT, "0"));
            runtime.put("botProbabilityKey", Config.GAME_DTS_NEED_BOT);
            runtime.put("botMoney", configString(Config.DTS_BOT_MONEY, ""));
            runtime.put("botMoneyKey", Config.DTS_BOT_MONEY);
            runtime.put("feeText", "无独立可调手续费");
        } else if ("rabbit".equals(code)) {
            runtime.put("killRate", configString(Config.DTS3_KILL_RATE, "0"));
            runtime.put("killRateKey", Config.DTS3_KILL_RATE);
            runtime.put("botProbability", configString(Config.GAME_DTS3_NEED_BOT, "0"));
            runtime.put("botProbabilityKey", Config.GAME_DTS3_NEED_BOT);
            runtime.put("botMoney", configString(Config.DTS3_BOT_MONEY, ""));
            runtime.put("botMoneyKey", Config.DTS3_BOT_MONEY);
            runtime.put("killDistribution", configString(Config.SZHT_RATE, ""));
            runtime.put("killDistributionKey", Config.SZHT_RATE);
            runtime.put("feeText", "无独立可调手续费");
        } else {
            runtime.put("botProbability", configString(Config.GAME_DTS2_NEED_BOT, "0"));
            runtime.put("botProbabilityKey", Config.GAME_DTS2_NEED_BOT);
            BigDecimal fee = setting.getBigDecimal("feeRate");
            runtime.put("feeRatePercent", fee == null ? "0" :
                    fee.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
        }
        JSONObject row = new JSONObject();
        row.put("code", code);
        row.put("id", gameId);
        row.put("name", displayName);
        row.put("dbName", emptyIfNull(game.getName()));
        row.put("setting", setting);
        row.put("runtime", runtime);
        row.put("capitalType", capitalType);
        row.put("capitalName", capitalName(capitalType));
        return row;
    }

    private String updateMiniGameSetting(String code, JSONObject params) {
        long gameId = "lion".equals(code) ? 7L : ("rabbit".equals(code) ? 1L : 12L);
        Game game = miniGameService.findGameById(gameId);
        if (game == null) {
            throwExp("小游戏配置不存在");
        }
        JSONObject setting = parseJsonObjectSafe(game.getGameSetting());
        if ("flip".equals(code)) {
            int time = requirePositiveInt(params, "time", "每局时长");
            int settleSec = requirePositiveInt(params, "settleSec", "结算展示时长");
            BigDecimal feeRatePercent = requirePercentDecimal(params, "feeRatePercent", "平台手续费率");
            BigDecimal triple = requirePositiveDecimal(params, "triple", "三同倍数");
            BigDecimal twoSame = requirePositiveDecimal(params, "double", "两同倍数");
            BigDecimal allDiff = requirePositiveDecimal(params, "allDiff", "三不同倍数");
            setting.put("time", String.valueOf(time));
            setting.put("settleSec", String.valueOf(settleSec));
            setting.put("feeRate", feeRatePercent.divide(new BigDecimal("100"), 8, RoundingMode.HALF_UP)
                    .stripTrailingZeros().toPlainString());
            JSONObject multipliers = setting.getJSONObject("multipliers");
            if (multipliers == null) {
                multipliers = new JSONObject();
                setting.put("multipliers", multipliers);
            }
            multipliers.put("triple", triple.stripTrailingZeros().toPlainString());
            multipliers.put("double", twoSame.stripTrailingZeros().toPlainString());
            multipliers.put("allDiff", allDiff.stripTrailingZeros().toPlainString());
        } else {
            int peopleNum = requirePositiveInt(params, "peopleNum", "开局最低人数");
            BigDecimal minBet = requirePositiveDecimal(params, "minBet", "下注最低金额");
            BigDecimal maxBet = requirePositiveDecimal(params, "maxBet", "下注最高金额");
            int time = requirePositiveInt(params, "time", "每局时长");
            if (maxBet.compareTo(minBet) < 0) {
                throwExp("下注最高金额不能小于最低金额");
            }
            setting.put("peopleNum", String.valueOf(peopleNum));
            setting.put("minBet", minBet.stripTrailingZeros().toPlainString());
            setting.put("maxBet", maxBet.stripTrailingZeros().toPlainString());
            setting.put("time", String.valueOf(time));
        }
        List<Object> sqlParams = new ArrayList<>();
        sqlParams.add(JSON.toJSONString(setting));
        sqlParams.add(gameId);
        if (executeUpdate("UPDATE l_game SET game_setting = ? WHERE id = ?", sqlParams) <= 0) {
            throwExp("小游戏规则保存失败");
        }
        return hotReloadMiniGameVersion();
    }

    private String updateMiniGameRuntime(String code, JSONObject params) {
        int status = requireSwitch(params, "status", "游戏开关");
        int botProbability = requirePercentInt(params, "botProbability", "人机下注概率");
        Integer killRate = null;
        String botMoney = null;
        if (!"flip".equals(code)) {
            killRate = requirePercentInt(params, "killRate", "控杀概率");
            botMoney = requireMoneyList(params, "botMoney", "人机下注金额");
        }
        String version = synchronizeMiniGameStatus(code, status);
        if ("lion".equals(code)) {
            upsertConfigAndNotify(Config.DTS_STATUS, String.valueOf(status));
            upsertConfigAndNotify(Config.DTS_KILL_RATE, String.valueOf(killRate));
            upsertConfigAndNotify(Config.GAME_DTS_NEED_BOT, String.valueOf(botProbability));
            upsertConfigAndNotify(Config.DTS_BOT_MONEY, botMoney);
        } else if ("rabbit".equals(code)) {
            upsertConfigAndNotify(Config.DTS3_STATUS, String.valueOf(status));
            upsertConfigAndNotify(Config.DTS3_KILL_RATE, String.valueOf(killRate));
            upsertConfigAndNotify(Config.GAME_DTS3_NEED_BOT, String.valueOf(botProbability));
            upsertConfigAndNotify(Config.DTS3_BOT_MONEY, botMoney);
        } else {
            upsertConfigAndNotify(Config.DTS2_STATUS, String.valueOf(status));
            upsertConfigAndNotify(Config.GAME_DTS2_NEED_BOT, String.valueOf(botProbability));
        }
        return version;
    }

    private String synchronizeMiniGameStatus(String code, int status) {
        long gameId = "lion".equals(code) ? 7L : ("rabbit".equals(code) ? 1L : 12L);
        Game game = miniGameService.findGameById(gameId);
        if (game == null) {
            throwExp("小游戏配置不存在");
        }
        if (game.getStatus() != null && game.getStatus() == status) {
            return emptyIfNull(managerConfigService.getString(Config.GAME_TABLE_VERSION));
        }
        List<Object> params = new ArrayList<>();
        params.add(status);
        params.add(gameId);
        if (executeUpdate("UPDATE l_game SET status = ? WHERE id = ?", params) <= 0) {
            throwExp("游戏状态保存失败");
        }
        return hotReloadMiniGameVersion();
    }

    private String hotReloadMiniGameVersion() {
        String current = trimToNull(managerConfigService.getString(Config.GAME_TABLE_VERSION));
        String version = current == null ? "v1.0.0" : nextConfigVersion(current);
        upsertConfigAndNotify(Config.GAME_TABLE_VERSION, version);
        return version;
    }

    private String configString(String key, String defaultValue) {
        String value = trimToNull(managerConfigService.getString(key));
        return value == null ? defaultValue : value;
    }

    private int configInteger(String key, int defaultValue) {
        String value = trimToNull(managerConfigService.getString(key));
        try {
            return value == null ? defaultValue : Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private JSONObject queryGuildConfigSummary() {
        JSONObject summary = new JSONObject();
        summary.put("totalCount", queryLong("SELECT COUNT(1) FROM t_guild"));
        summary.put("activeCount", queryLong("SELECT COUNT(1) FROM t_guild WHERE status = 1"));
        summary.put("pendingCount", queryLong("SELECT COUNT(1) FROM t_guild WHERE status = 2"));
        summary.put("disabledCount", queryLong("SELECT COUNT(1) FROM t_guild WHERE status = 0"));
        summary.put("totalMemberNumber", queryLong("SELECT COALESCE(SUM(member_number), 0) FROM t_guild WHERE status = 1"));
        summary.put("totalBailAmount", money(queryDecimal("SELECT COALESCE(SUM(bail_amount), 0) FROM t_guild WHERE status = 1")));
        summary.put("pendingBailAmount", money(queryDecimal("SELECT COALESCE(SUM(bail_amount), 0) FROM t_guild WHERE status = 2")));
        return summary;
    }

    private JSONArray queryGuildConfigRows() {
        JSONArray list = new JSONArray();
        String sql = "SELECT g.*, u.user_no, u.name AS user_name FROM t_guild g LEFT JOIN t_user u ON u.id = g.user_id " +
                "ORDER BY COALESCE(g.apply_time, g.create_time) DESC, g.id DESC LIMIT 30";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                JSONObject row = new JSONObject();
                BigDecimal feeRate = nullToZero(rs.getBigDecimal("fee_rate"));
                row.put("id", rs.getLong("id"));
                row.put("guildName", emptyIfNull(rs.getString("guild_name")));
                row.put("userId", rs.getLong("user_id"));
                row.put("userNo", emptyIfNull(rs.getString("user_no")));
                row.put("userName", emptyIfNull(rs.getString("user_name")));
                row.put("memberNumber", rs.getInt("member_number"));
                row.put("needMemberNumber", rs.getInt("need_member_number"));
                row.put("freeNum", rs.getInt("free_num"));
                row.put("bailAmount", money(rs.getBigDecimal("bail_amount")));
                row.put("feeRate", feeRate.stripTrailingZeros().toPlainString());
                row.put("feeRatePercent", feeRate.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString());
                row.put("feeAmount", money(rs.getBigDecimal("fee_amount")));
                row.put("payAmount", money(rs.getBigDecimal("pay_amount")));
                row.put("type", rs.getInt("type"));
                row.put("typeName", rs.getInt("type") == 1 ? "活动赠送" : "质押开通");
                row.put("status", rs.getInt("status"));
                row.put("statusName", guildStatusName(rs.getInt("status")));
                row.put("remark", emptyIfNull(rs.getString("remark")));
                row.put("createTime", rs.getTimestamp("create_time"));
                row.put("applyTime", rs.getTimestamp("apply_time"));
                list.add(row);
            }
        } catch (Exception e) {
            logger().warn("[GuildConfig] query rows failed", e);
        }
        return list;
    }

    private JSONArray queryPetConfigRows() {
        JSONArray list = new JSONArray();
        String sql = "SELECT * FROM dic_pet ORDER BY id ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                JSONObject row = new JSONObject();
                row.put("id", rs.getInt("id"));
                row.put("status", rs.getInt("status"));
                row.put("remark", emptyIfNull(rs.getString("remark")));
                row.put("settleIntervalMin", rs.getInt("settle_interval_min"));
                row.put("hungerMaxDays", rs.getInt("hunger_max_days"));
                row.put("hungerMaxHours", rs.getInt("hunger_max_days") * 24);
                row.put("feedAddHours", rs.getInt("feed_add_hours"));
                row.put("buyCostCapitalType", rs.getInt("buy_cost_capital_type"));
                row.put("buyCostCapitalName", capitalName(rs.getInt("buy_cost_capital_type")));
                row.put("buyCostAmount", money(rs.getBigDecimal("buy_cost_amount")));
                row.put("feedCostCapitalType", rs.getInt("feed_cost_capital_type"));
                row.put("feedCostCapitalName", capitalName(rs.getInt("feed_cost_capital_type")));
                row.put("feedCostAmount", money(rs.getBigDecimal("feed_cost_amount")));
                row.put("yieldCapitalType", rs.getInt("yield_capital_type"));
                row.put("yieldCapitalName", capitalName(rs.getInt("yield_capital_type")));
                row.put("dividendCapitalType", rs.getInt("dividend_capital_type"));
                row.put("dividendCapitalName", capitalName(rs.getInt("dividend_capital_type")));
                row.put("unlockContribCapitalType", rs.getInt("unlock_contrib_capital_type"));
                row.put("unlockContribCapitalName", capitalName(rs.getInt("unlock_contrib_capital_type")));
                row.put("unlockDirectLv3", rs.getInt("unlock_direct_lv3"));
                row.put("unlockDirectLv4", rs.getInt("unlock_direct_lv4"));
                row.put("unlockDirectLv5", rs.getInt("unlock_direct_lv5"));
                row.put("unlockContribLv3", money(rs.getBigDecimal("unlock_contrib_lv3")));
                row.put("unlockContribLv4", money(rs.getBigDecimal("unlock_contrib_lv4")));
                row.put("unlockContribLv5", money(rs.getBigDecimal("unlock_contrib_lv5")));
                JSONArray profitFixed = parseJsonArraySafe(rs.getString("profit_fixed_json"));
                JSONArray yieldCurve = parseJsonArraySafe(rs.getString("yield_curve_json"));
                row.put("profitFixedList", profitFixed);
                row.put("yieldCurveList", yieldCurve);
                row.put("profitFixedText", profitFixedText(profitFixed));
                row.put("yieldCurveText", yieldCurveText(yieldCurve));
                row.put("createTime", rs.getTimestamp("create_time"));
                row.put("updateTime", rs.getTimestamp("update_time"));
                list.add(row);
            }
        } catch (Exception e) {
            logger().warn("[PetConfig] query failed", e);
        }
        return list;
    }

    private JSONArray queryItemConfigRows(String sql, List<Object> params) {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    long id = rs.getLong("id");
                    row.put("id", id);
                    row.put("name", emptyIfNull(rs.getString("name")));
                    row.put("number", rs.getObject("number"));
                    row.put("context", emptyIfNull(rs.getString("context")));
                    row.put("quality", rs.getInt("quality"));
                    row.put("qualityName", qualityName(rs.getInt("quality")));
                    row.put("isUse", rs.getInt("is_use"));
                    row.put("isOverlap", rs.getInt("is_overlap"));
                    row.put("isTrading", rs.getInt("is_trading"));
                    row.put("isSell", rs.getInt("is_sell"));
                    row.put("isSend", rs.getInt("is_send"));
                    row.put("canSyn", rs.getInt("can_syn"));
                    JSONArray synUse = parseJsonArraySafe(rs.getString("syn_use"));
                    row.put("synUseList", synUse);
                    row.put("synUseText", itemIdArrayText(synUse));
                    row.put("synNumber", rs.getInt("syn_number"));
                    row.put("synRate", emptyIfNull(rs.getString("syn_rate")));
                    row.put("synResultId", emptyIfNull(rs.getString("syn_result_id")));
                    row.put("durationDays", rs.getInt("duration_days"));
                    row.put("price", money(rs.getBigDecimal("price")));
                    row.put("tradPrice", money(rs.getBigDecimal("trad_price")));
                    row.put("shopPrice", money(rs.getBigDecimal("shop_price")));
                    row.put("type", rs.getInt("type"));
                    row.put("typeName", itemTypeName(rs.getInt("type")));
                    row.put("positon", rs.getInt("positon"));
                    row.put("positionName", rs.getInt("positon") == 1 ? "货币栏" : "道具背包");
                    row.put("getWay", emptyIfNull(rs.getString("get_way")));
                    row.put("icon", emptyIfNull(rs.getString("icon")));
                    row.put("power", rs.getInt("power"));
                    row.put("status", rs.getInt("status"));
                    row.put("statusName", rs.getInt("status") == 1 ? "有效" : "下架");
                    JSONArray shops = queryItemShopRows(id);
                    row.put("shopList", shops);
                    row.put("shopCount", shops.size());
                    row.put("createTime", rs.getTimestamp("create_time"));
                    row.put("updateTime", rs.getTimestamp("update_time"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[ItemConfig] query rows failed: " + sql, e);
        }
        return list;
    }

    private JSONObject queryItemConfigSettings() {
        JSONObject data = new JSONObject();
        String convertRate = managerConfigService.getString(Config.CONVERT_RATE);
        data.put("convertRate", convertRate);
        data.put("convertText", "1 个弹珠(1002) = " + convertRate + " 个小丑币(1001)");

        JSONObject exchangeConfig = parseJsonObjectSafe(managerConfigService.getString(Config.SEED_EXCHANGE_CONFIG));
        JSONArray rules = exchangeConfig.getJSONArray("rules");
        JSONArray exchangeRules = new JSONArray();
        if (rules != null) {
            for (int i = 0; i < rules.size(); i++) {
                JSONObject rule = rules.getJSONObject(i);
                JSONObject row = new JSONObject();
                int seedItemId = rule.getIntValue("seedItemId");
                row.put("seedItemId", seedItemId);
                row.put("seedName", itemName(seedItemId));
                row.put("cost", rule.getBigDecimal("cost") == null ? BigDecimal.ZERO : rule.getBigDecimal("cost"));
                row.put("fee", rule.getBigDecimal("fee") == null ? BigDecimal.ZERO : rule.getBigDecimal("fee"));
                exchangeRules.add(row);
            }
        }
        exchangeConfig.put("rules", exchangeRules);
        exchangeConfig.put("capitalTypeName", capitalName(exchangeConfig.getIntValue("capitalTypeId")));
        data.put("seedExchange", exchangeConfig);

        JSONObject seedSyn = new JSONObject();
        seedSyn.put("darkSwitch", managerConfigService.getString(Config.SEED_SYN_DARK_SWITCH));
        seedSyn.put("darkRateLv2", managerConfigService.getString(Config.SEED_SYN_DARK_RATE_LV2));
        seedSyn.put("darkRateLv3", managerConfigService.getString(Config.SEED_SYN_DARK_RATE_LV3));
        seedSyn.put("darkRateLv4", managerConfigService.getString(Config.SEED_SYN_DARK_RATE_LV4));
        seedSyn.put("darkRateLv5", managerConfigService.getString(Config.SEED_SYN_DARK_RATE_LV5));
        seedSyn.put("failPoolRate", managerConfigService.getString(Config.SEED_SYN_FAIL_POOL_RATE));
        data.put("seedSyn", seedSyn);
        return data;
    }

    private JSONArray queryItemShopRows(long itemId) {
        JSONArray list = new JSONArray();
        String sql = "SELECT ds.id, ds.item_id, ds.use_item_id, ds.shop_type, ds.number, ds.price, ds.is_show, ds.can_buy, " +
                "pay.name pay_item_name FROM dic_shop ds LEFT JOIN dic_item pay ON pay.id = ds.use_item_id WHERE ds.item_id = ? ORDER BY ds.shop_type ASC, ds.id ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, itemId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    row.put("shopId", rs.getLong("id"));
                    row.put("shopType", rs.getInt("shop_type"));
                    row.put("number", rs.getInt("number"));
                    row.put("price", money(rs.getBigDecimal("price")));
                    row.put("useItemId", rs.getInt("use_item_id"));
                    row.put("useItemName", emptyIfNull(rs.getString("pay_item_name")));
                    row.put("isShow", rs.getInt("is_show"));
                    row.put("canBuy", rs.getInt("can_buy"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[ItemConfig] query shop rows failed, itemId=" + itemId, e);
        }
        return list;
    }

    private Set<String> resultColumns(ResultSet rs) throws Exception {
        Set<String> columns = new HashSet<>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            columns.add(metaData.getColumnLabel(i).toLowerCase());
        }
        return columns;
    }

    private String getStringIfExists(ResultSet rs, Set<String> columns, String column) throws Exception {
        return columns.contains(column.toLowerCase()) ? rs.getString(column) : null;
    }

    private int getIntIfExists(ResultSet rs, Set<String> columns, String column) throws Exception {
        return columns.contains(column.toLowerCase()) ? rs.getInt(column) : 0;
    }

    private JSONArray parseRewardItems(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONArray result = new JSONArray();
        for (int i = 0; i < raw.size(); i++) {
            JSONObject item = raw.getJSONObject(i);
            int itemId = item.getIntValue("id");
            JSONObject row = new JSONObject();
            row.put("type", item.getIntValue("type", 1));
            row.put("id", itemId);
            row.put("itemName", itemName(itemId));
            row.put("number", item.getBigDecimal("number") == null ? BigDecimal.ZERO : item.getBigDecimal("number"));
            result.add(row);
        }
        return result;
    }

    private JSONArray parseJsonArraySafe(Object value) {
        if (value == null) {
            return new JSONArray();
        }
        if (value instanceof JSONArray) {
            return (JSONArray) value;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return new JSONArray();
        }
        try {
            return JSON.parseArray(text);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private JSONObject parseJsonObjectSafe(Object value) {
        if (value == null) {
            return new JSONObject();
        }
        if (value instanceof JSONObject) {
            return (JSONObject) value;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return new JSONObject();
        }
        try {
            return JSON.parseObject(text);
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private JSONArray normalizeRewardArray(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONArray result = new JSONArray();
        for (int i = 0; i < raw.size(); i++) {
            JSONObject input = raw.getJSONObject(i);
            int itemId = input.getIntValue("id");
            BigDecimal number = input.getBigDecimal("number");
            if (itemId <= 0 || number == null || number.compareTo(BigDecimal.ZERO) <= 0) {
                throwExp("奖励道具和数量必须有效");
            }
            JSONObject row = new JSONObject();
            row.put("type", input.getIntValue("type", 1));
            row.put("id", String.valueOf(itemId));
            row.put("number", number);
            result.add(row);
        }
        if (result.isEmpty()) {
            throwExp("至少配置一个奖励");
        }
        return result;
    }

    private JSONArray normalizeFarmRewardArray(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONArray result = new JSONArray();
        for (int i = 0; i < raw.size(); i++) {
            JSONObject input = raw.getJSONObject(i);
            int itemId = input.getIntValue("id");
            BigDecimal number = input.getBigDecimal("number");
            if (itemId <= 0 || number == null || number.compareTo(BigDecimal.ZERO) <= 0) {
                throwExp("收获产出道具和数量必须有效");
            }
            JSONObject row = new JSONObject();
            row.put("type", input.getIntValue("type", 1));
            row.put("id", itemId);
            row.put("number", number.stripTrailingZeros());
            result.add(row);
        }
        if (result.isEmpty()) {
            throwExp("至少配置一个收获产出");
        }
        return result;
    }

    private JSONObject normalizeJoyBaseConfig(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONObject result = new JSONObject();
        for (int i = 0; i < raw.size(); i++) {
            JSONObject input = raw.getJSONObject(i);
            int level = input.getIntValue("level");
            BigDecimal baseJoy = input.getBigDecimal("baseJoy");
            if (level <= 0 || baseJoy == null || baseJoy.compareTo(BigDecimal.ZERO) < 0) {
                throwExp("种子基础欢乐值配置不合法");
            }
            result.put(String.valueOf(level), baseJoy.stripTrailingZeros());
        }
        if (result.isEmpty()) {
            throwExp("基础欢乐值配置不能为空");
        }
        return result;
    }

    private JSONObject normalizeJoyPercentConfig(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONObject result = new JSONObject();
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < raw.size(); i++) {
            JSONObject input = raw.getJSONObject(i);
            int level = input.getIntValue("level");
            BigDecimal percent = input.getBigDecimal("percent");
            if (level <= 0 || level > 5 || percent == null || percent.compareTo(BigDecimal.ZERO) < 0 || percent.compareTo(new BigDecimal("100")) > 0) {
                throwExp("代数分成比例必须在0~100之间");
            }
            total = total.add(percent);
            result.put(String.valueOf(level), percent.stripTrailingZeros());
        }
        if (result.isEmpty()) {
            throwExp("代数分成比例不能为空");
        }
        if (total.compareTo(new BigDecimal("100")) > 0) {
            throwExp("1~5代分成比例总和不能超过100%");
        }
        return result;
    }

    private JSONArray normalizeIdArray(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONArray result = new JSONArray();
        for (int i = 0; i < raw.size(); i++) {
            Long id;
            Object item = raw.get(i);
            if (item instanceof JSONObject) {
                id = ((JSONObject) item).getLong("id");
            } else {
                id = parseLongOrNull(String.valueOf(item));
            }
            if (id != null && id > 0) {
                result.add(id);
            }
        }
        return result;
    }

    private JSONObject normalizeSeedExchangeConfig(JSONObject params) {
        JSONObject cfg = new JSONObject();
        cfg.put("capitalTypeId", requirePositiveInt(params, "capitalTypeId", "capitalTypeId"));
        cfg.put("feeSwitch", requireSwitch(params, "feeSwitch", "feeSwitch"));
        JSONArray raw = parseJsonArraySafe(params.get("rules"));
        JSONArray rules = new JSONArray();
        for (int i = 0; i < raw.size(); i++) {
            JSONObject input = raw.getJSONObject(i);
            int seedItemId = input.getIntValue("seedItemId");
            BigDecimal cost = input.getBigDecimal("cost");
            BigDecimal fee = input.getBigDecimal("fee");
            if (seedItemId <= 0 || cost == null || cost.compareTo(BigDecimal.ZERO) < 0 || fee == null || fee.compareTo(BigDecimal.ZERO) < 0) {
                throwExp("seed exchange rule is invalid");
            }
            JSONObject row = new JSONObject();
            row.put("seedItemId", seedItemId);
            row.put("cost", cost.stripTrailingZeros().toPlainString());
            row.put("fee", fee.stripTrailingZeros().toPlainString());
            rules.add(row);
        }
        if (rules.isEmpty()) {
            throwExp("seed exchange rules cannot be empty");
        }
        cfg.put("rules", rules);
        return cfg;
    }

    private JSONArray normalizeProfitFixed(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONArray result = new JSONArray();
        for (int i = 0; i < raw.size(); i++) {
            JSONObject input = raw.getJSONObject(i);
            int level = input.getIntValue("level");
            BigDecimal amount = input.getBigDecimal("amountPerHour");
            if (level <= 0 || amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                throwExp("固定分润配置不合法");
            }
            JSONObject row = new JSONObject();
            row.put("level", level);
            row.put("amountPerHour", amount.toPlainString());
            result.add(row);
        }
        if (result.isEmpty()) {
            throwExp("固定分润不能为空");
        }
        return result;
    }

    private JSONArray normalizeYieldCurve(Object value) {
        JSONArray raw = parseJsonArraySafe(value);
        JSONArray result = new JSONArray();
        for (int i = 0; i < raw.size(); i++) {
            JSONObject input = raw.getJSONObject(i);
            int dayStart = input.getIntValue("dayStart");
            int dayEnd = input.getIntValue("dayEnd");
            BigDecimal amount = input.getBigDecimal("amountPerHour");
            if (dayStart <= 0 || dayEnd < dayStart || amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
                throwExp("收益曲线配置不合法");
            }
            JSONObject row = new JSONObject();
            row.put("dayStart", dayStart);
            row.put("dayEnd", dayEnd);
            row.put("amountPerHour", amount.toPlainString());
            result.add(row);
        }
        if (result.isEmpty()) {
            throwExp("收益曲线不能为空");
        }
        return result;
    }

    private String rewardText(JSONArray rewards) {
        if (rewards == null || rewards.isEmpty()) {
            return "-";
        }
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < rewards.size(); i++) {
            JSONObject reward = rewards.getJSONObject(i);
            texts.add(reward.getString("itemName") + " x" + reward.getString("number"));
        }
        return String.join("，", texts);
    }

    private String profitFixedText(JSONArray list) {
        if (list == null || list.isEmpty()) {
            return "-";
        }
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            JSONObject row = list.getJSONObject(i);
            texts.add(row.getIntValue("level") + "代 " + row.getString("amountPerHour") + "/小时");
        }
        return String.join("，", texts);
    }

    private String yieldCurveText(JSONArray list) {
        if (list == null || list.isEmpty()) {
            return "-";
        }
        JSONObject first = list.getJSONObject(0);
        JSONObject last = list.getJSONObject(list.size() - 1);
        return "共" + list.size() + "段，Day" + first.getString("dayStart") + "=" + first.getString("amountPerHour")
                + "/小时，末段Day" + last.getString("dayStart") + "-" + last.getString("dayEnd") + "=" + last.getString("amountPerHour") + "/小时";
    }

    private String itemIdArrayText(JSONArray ids) {
        if (ids == null || ids.isEmpty()) {
            return "-";
        }
        List<String> texts = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            int id = ids.getIntValue(i);
            texts.add(itemName(id) + "(" + id + ")");
        }
        return String.join("，", texts);
    }

    private String itemName(int itemId) {
        Item item = itemCacheService.getItemInfoById((long) itemId);
        if (item == null) {
            item = PlayGameService.itemMap.get(String.valueOf(itemId));
        }
        return item == null ? "未知道具" : item.getName();
    }

    private BigDecimal configDecimal(String key) {
        String value = trimToNull(managerConfigService.getString(key));
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private String farmGrowText(int seconds) {
        if (seconds <= 0) {
            return "-";
        }
        int days = seconds / 86400;
        int remain = seconds % 86400;
        int hours = remain / 3600;
        int minutes = (remain % 3600) / 60;
        List<String> parts = new ArrayList<>();
        if (days > 0) {
            parts.add(days + "天");
        }
        if (hours > 0) {
            parts.add(hours + "小时");
        }
        if (minutes > 0 || parts.isEmpty()) {
            parts.add(minutes + "分钟");
        }
        return String.join("", parts);
    }

    private String guildStatusName(int status) {
        if (status == 1) return "已通过";
        if (status == 2) return "申请中";
        if (status == 0) return "已拒绝/停用";
        return "未知状态";
    }

    private String capitalName(int capitalType) {
        String name = UserCapitalTypeEnum.getName(capitalType);
        return name == null ? String.valueOf(capitalType) : name + "(" + capitalType + ")";
    }

    private String qualityName(int quality) {
        if (quality == 1) return "白";
        if (quality == 2) return "绿";
        if (quality == 3) return "蓝";
        if (quality == 4) return "紫";
        if (quality == 5) return "橙";
        return "未知";
    }

    private String itemTypeName(int type) {
        if (type == 1) return "果实/材料";
        if (type == 2) return "种子/基础道具";
        if (type == 3) return "功能道具";
        if (type == 4) return "资产货币";
        if (type == 5) return "礼包";
        return "未知类型";
    }

    private int requireSwitch(JSONObject params, String key, String label) {
        int value = requireNonNegativeInt(params, key, label);
        if (value != 0 && value != 1) {
            throwExp(label + "只能为启用或禁用");
        }
        return value;
    }

    private int requirePositiveInt(JSONObject params, String key, String label) {
        int value = params.getIntValue(key);
        if (value <= 0) {
            throwExp(label + "必须大于0");
        }
        return value;
    }

    private int requireNonNegativeInt(JSONObject params, String key, String label) {
        Integer value = params.getInteger(key);
        if (value == null) {
            String text = trimToNull(params.getString(key));
            value = text == null ? 0 : Integer.parseInt(text);
        }
        if (value < 0) {
            throwExp(label + "不能小于0");
        }
        return value;
    }

    private BigDecimal requireNonNegativeDecimal(JSONObject params, String key, String label) {
        BigDecimal value = optionalNonNegativeDecimal(params, key, label);
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal requirePositiveDecimal(JSONObject params, String key, String label) {
        BigDecimal value = optionalNonNegativeDecimal(params, key, label);
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throwExp(label + " must be greater than 0");
        }
        return value;
    }

    private int requirePercentInt(JSONObject params, String key, String label) {
        int value = requireNonNegativeInt(params, key, label);
        if (value > 100) {
            throwExp(label + "必须在 0 到 100 之间");
        }
        return value;
    }

    private BigDecimal requirePercentDecimal(JSONObject params, String key, String label) {
        BigDecimal value = requireNonNegativeDecimal(params, key, label);
        if (value.compareTo(new BigDecimal("100")) > 0) {
            throwExp(label + "必须在 0 到 100% 之间");
        }
        return value;
    }

    private String requireMoneyList(JSONObject params, String key, String label) {
        String text = trimToNull(params.getString(key));
        if (text == null) {
            throwExp(label + "不能为空");
        }
        List<String> values = new ArrayList<>();
        for (String item : text.split(",")) {
            BigDecimal value;
            try {
                value = new BigDecimal(item.trim());
            } catch (Exception e) {
                throwExp(label + "必须是以英文逗号分隔的数字");
                return "";
            }
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                throwExp(label + "必须全部大于0");
            }
            values.add(value.stripTrailingZeros().toPlainString());
        }
        return String.join(",", values);
    }

    private BigDecimal optionalNonNegativeDecimal(JSONObject params, String key, String label) {
        String text = trimToNull(params.getString(key));
        BigDecimal value = text == null ? null : new BigDecimal(text);
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throwExp(label + "不能小于0");
        }
        return value;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            limit = 20;
        }
        return Math.min(limit, 200);
    }

    private void appendOpsUserWhere(StringBuilder whereSql, List<Object> params, String userIdColumn, JSONObject request) {
        Long userId = parseLongOrNull(trimToNull(request.getString("userId")));
        if (userId != null && userId > 0) {
            whereSql.append(" AND ").append(userIdColumn).append(" = ?");
            params.add(userId);
        }

        String userNo = trimToNull(request.getString("userNo"));
        if (userNo != null) {
            whereSql.append(" AND (u.user_no = ? OR CAST(").append(userIdColumn).append(" AS CHAR) = ?)");
            params.add(userNo);
            params.add(userNo);
        }

        String userName = trimToNull(request.getString("userName"));
        if (userName != null) {
            whereSql.append(" AND u.name LIKE ?");
            params.add("%" + userName + "%");
        }
    }

    private void appendDateWhere(StringBuilder whereSql, List<Object> params, String columnName, JSONObject request) {
        String startDate = normalizeStartDate(trimToNull(request.getString("startDate")));
        if (startDate != null) {
            whereSql.append(" AND ").append(columnName).append(" >= ?");
            params.add(startDate);
        }
        String endDate = normalizeEndDate(trimToNull(request.getString("endDate")));
        if (endDate != null) {
            whereSql.append(" AND ").append(columnName).append(" <= ?");
            params.add(endDate);
        }
    }

    private void appendJoyWhere(StringBuilder whereSql, List<Object> params, JSONObject request) {
        Long receiverUserId = parseLongOrNull(trimToNull(request.getString("receiverUserId")));
        Long userId = parseLongOrNull(trimToNull(request.getString("userId")));
        if (receiverUserId == null) {
            receiverUserId = userId;
        }
        if (receiverUserId != null && receiverUserId > 0) {
            whereSql.append(" AND e.receiver_user_id = ?");
            params.add(receiverUserId);
        }

        Long fromUserId = parseLongOrNull(trimToNull(request.getString("fromUserId")));
        if (fromUserId != null && fromUserId > 0) {
            whereSql.append(" AND e.from_user_id = ?");
            params.add(fromUserId);
        }

        String userNo = trimToNull(request.getString("userNo"));
        if (userNo != null) {
            whereSql.append(" AND (ru.user_no = ? OR fu.user_no = ? OR CAST(e.receiver_user_id AS CHAR) = ? OR CAST(e.from_user_id AS CHAR) = ?)");
            params.add(userNo);
            params.add(userNo);
            params.add(userNo);
            params.add(userNo);
        }

        String userName = trimToNull(request.getString("userName"));
        if (userName != null) {
            whereSql.append(" AND (ru.name LIKE ? OR fu.name LIKE ?)");
            params.add("%" + userName + "%");
            params.add("%" + userName + "%");
        }

        String sourceType = trimToNull(request.getString("sourceType"));
        if (sourceType != null) {
            whereSql.append(" AND e.source_type = ?");
            params.add(sourceType);
        }

        Integer level = request.getInteger("level");
        if (level != null && level > 0) {
            whereSql.append(" AND e.level = ?");
            params.add(level);
        }

        appendDateWhere(whereSql, params, "e.create_time", request);
    }

    private JSONArray queryPetInfoRows(String sql, List<Object> params) {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    long userId = rs.getLong("user_id");
                    row.put("id", userId);
                    row.put("userId", userId);
                    row.put("userNo", emptyIfNull(rs.getString("user_no")));
                    row.put("userName", emptyIfNull(rs.getString("user_name")));
                    row.put("petCount", rs.getLong("pet_count"));
                    row.put("hungerHours", rs.getInt("hunger_hours"));
                    row.put("pendingYieldAmount", money(rs.getBigDecimal("pending_yield_amount")));
                    row.put("totalYieldAmount", money(rs.getBigDecimal("total_yield_amount")));
                    row.put("todayDividendAmount", money(rs.getBigDecimal("today_dividend_amount")));
                    row.put("totalDividendAmount", money(rs.getBigDecimal("total_dividend_amount")));
                    row.put("unlockLv3", rs.getInt("unlock_lv3"));
                    row.put("unlockLv4", rs.getInt("unlock_lv4"));
                    row.put("unlockLv5", rs.getInt("unlock_lv5"));
                    row.put("unlockText", "3代:" + (rs.getInt("unlock_lv3") == 1 ? "已解锁" : "未解锁")
                            + " / 4代:" + (rs.getInt("unlock_lv4") == 1 ? "已解锁" : "未解锁")
                            + " / 5代:" + (rs.getInt("unlock_lv5") == 1 ? "已解锁" : "未解锁"));
                    row.put("firstBuyTime", rs.getTimestamp("first_buy_time"));
                    row.put("lastBuyTime", rs.getTimestamp("last_buy_time"));
                    row.put("lastSettleTime", rs.getTimestamp("last_settle_time"));
                    row.put("updateTime", rs.getTimestamp("update_time"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[PetOps] query rows failed: " + sql, e);
        }
        return list;
    }

    private JSONArray queryFarmRows(String sql, List<Object> params) {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    row.put("id", rs.getLong("id"));
                    row.put("userId", rs.getLong("user_id"));
                    row.put("userNo", emptyIfNull(rs.getString("user_no")));
                    row.put("userName", emptyIfNull(rs.getString("user_name")));
                    row.put("landIndex", rs.getInt("land_index"));
                    row.put("seedItemId", rs.getInt("seed_item_id"));
                    row.put("seedName", emptyIfNull(rs.getString("seed_name")));
                    int status = rs.getInt("status");
                    row.put("status", status);
                    row.put("statusName", farmStatusName(status, rs.getInt("can_harvest")));
                    row.put("canHarvest", rs.getInt("can_harvest"));
                    row.put("canHarvestName", rs.getInt("can_harvest") == 1 ? "可收割" : "不可收割");
                    row.put("remainSeconds", rs.getLong("remain_seconds"));
                    row.put("startTime", rs.getTimestamp("start_time"));
                    row.put("endTime", rs.getTimestamp("end_time"));
                    row.put("lastHarvestTime", rs.getTimestamp("last_harvest_time"));
                    row.put("createTime", rs.getTimestamp("create_time"));
                    row.put("updateTime", rs.getTimestamp("update_time"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[FarmOps] query rows failed: " + sql, e);
        }
        return list;
    }

    private String farmStatusName(int status, int canHarvest) {
        if (canHarvest == 1) {
            return "可收割";
        }
        if (status == 0) {
            return "空地";
        }
        if (status == 1) {
            return "成长中";
        }
        if (status == 2) {
            return "已成熟";
        }
        return String.valueOf(status);
    }

    private JSONArray queryJoyRows(String sql, List<Object> params) {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    row.put("id", rs.getLong("id"));
                    row.put("eventId", emptyIfNull(rs.getString("event_id")));
                    row.put("receiverUserId", rs.getLong("receiver_user_id"));
                    row.put("receiverUserNo", emptyIfNull(rs.getString("receiver_user_no")));
                    row.put("receiverUserName", emptyIfNull(rs.getString("receiver_user_name")));
                    row.put("fromUserId", rs.getLong("from_user_id"));
                    row.put("fromUserNo", emptyIfNull(rs.getString("from_user_no")));
                    row.put("fromUserName", emptyIfNull(rs.getString("from_user_name")));
                    row.put("sourceType", emptyIfNull(rs.getString("source_type")));
                    row.put("level", rs.getInt("level"));
                    row.put("itemQuality", rs.getInt("item_quality"));
                    row.put("baseJoy", money(rs.getBigDecimal("base_joy")));
                    row.put("percent", rs.getInt("percent"));
                    row.put("joyAmount", money(rs.getBigDecimal("joy_amount")));
                    row.put("calcDesc", emptyIfNull(rs.getString("calc_desc")));
                    row.put("createTime", rs.getTimestamp("create_time"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[JoyOps] query rows failed: " + sql, e);
        }
        return list;
    }

    private JSONArray queryBountyRows(String sql, List<Object> params) {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    row.put("id", rs.getLong("id"));
                    row.put("userId", rs.getLong("user_id"));
                    row.put("userNo", emptyIfNull(rs.getString("user_no")));
                    row.put("userName", emptyIfNull(rs.getString("user_name")));
                    row.put("taskName", emptyIfNull(rs.getString("task_name")));
                    row.put("taskTitle", emptyIfNull(rs.getString("task_title")));
                    row.put("unitPrice", money(rs.getBigDecimal("unit_price")));
                    row.put("quotaTotal", rs.getInt("quota_total"));
                    row.put("quotaRemain", rs.getInt("quota_remain"));
                    row.put("joinCount", rs.getInt("join_count"));
                    row.put("finishCount", rs.getInt("finish_count"));
                    row.put("feeAmount", money(rs.getBigDecimal("fee_amount")));
                    row.put("escrowAmount", money(rs.getBigDecimal("escrow_amount")));
                    int status = rs.getInt("status");
                    row.put("status", status);
                    row.put("statusName", bountyTaskStatusName(status));
                    row.put("createTime", rs.getTimestamp("create_time"));
                    row.put("updateTime", rs.getTimestamp("update_time"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[BountyOps] query rows failed: " + sql, e);
        }
        return list;
    }

    private String bountyTaskStatusName(int status) {
        if (status == 1) {
            return "上架";
        }
        if (status == 2) {
            return "已取消";
        }
        if (status == 3) {
            return "已结束";
        }
        return String.valueOf(status);
    }

    private JSONArray queryTradingRows(String sql, List<Object> params) {
        JSONArray list = new JSONArray();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            fillStatement(statement, params);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    JSONObject row = new JSONObject();
                    row.put("id", rs.getLong("id"));
                    row.put("userId", rs.getLong("user_id"));
                    row.put("userNo", emptyIfNull(rs.getString("user_no")));
                    row.put("userName", emptyIfNull(rs.getString("user_name")));
                    row.put("itemId", rs.getInt("item_id"));
                    row.put("itemName", emptyIfNull(rs.getString("item_name")));
                    row.put("itemType", rs.getInt("item_type"));
                    row.put("itemNumber", rs.getInt("item_number"));
                    row.put("itemAllNumber", rs.getInt("item_all_number"));
                    row.put("itemPrice", money(rs.getBigDecimal("item_price")));
                    int type = rs.getInt("type");
                    int status = rs.getInt("status");
                    row.put("type", type);
                    row.put("typeName", type == 0 ? "出售" : "求购");
                    row.put("status", status);
                    row.put("statusName", tradingStatusName(status));
                    row.put("createTime", rs.getTimestamp("create_time"));
                    row.put("updateTime", rs.getTimestamp("update_time"));
                    list.add(row);
                }
            }
        } catch (Exception e) {
            logger().warn("[TradingOps] query rows failed: " + sql, e);
        }
        return list;
    }

    private String tradingStatusName(int status) {
        if (status == 1) {
            return "上架中";
        }
        if (status == 2) {
            return "已取消";
        }
        if (status == 0) {
            return "已完成";
        }
        return String.valueOf(status);
    }

    private BigDecimal sumTodayBackpackLogByType(int type) {
        BigDecimal currentSchemaTotal = BigDecimal.ZERO;
        boolean currentSchemaFound = false;
        for (int i = 0; i <= 9; i++) {
            String tableName = LogUserBackpack.tablePrefix + i;
            if (!tableExists(null, tableName)) {
                continue;
            }
            currentSchemaFound = true;
            currentSchemaTotal = currentSchemaTotal.add(queryDecimal("SELECT COALESCE(SUM(number), 0) FROM `" + tableName + "` WHERE type = " + type + " AND number > 0 AND create_time >= CURDATE()"));
        }
        if (currentSchemaFound) {
            return currentSchemaTotal;
        }

        BigDecimal logSchemaTotal = BigDecimal.ZERO;
        for (int i = 0; i <= 9; i++) {
            String tableName = LogUserBackpack.tablePrefix + i;
            if (!tableExists("tsg-log", tableName)) {
                continue;
            }
            logSchemaTotal = logSchemaTotal.add(queryDecimal("SELECT COALESCE(SUM(number), 0) FROM `tsg-log`.`" + tableName + "` WHERE type = " + type + " AND number > 0 AND create_time >= CURDATE()"));
        }
        return logSchemaTotal;
    }

    private boolean tableExists(String schemaName, String tableName) {
        String sql = schemaName == null
                ? "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?"
                : "SELECT COUNT(1) FROM information_schema.tables WHERE table_schema = ? AND table_name = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (schemaName == null) {
                statement.setString(1, tableName);
            } else {
                statement.setString(1, schemaName);
                statement.setString(2, tableName);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getLong(1) > 0;
            }
        } catch (Exception e) {
            logger().warn("[DashboardOps] check table failed: " + (schemaName == null ? "" : schemaName + ".") + tableName, e);
            return false;
        }
    }

    private JSONObject queryMiniGameStats(String tableName) {
        JSONObject stats = new JSONObject();
        BigDecimal bet = queryDecimal("SELECT COALESCE(SUM(bet_amount), 0) FROM " + tableName + " WHERE create_time >= CURDATE()");
        BigDecimal reward = queryDecimal("SELECT COALESCE(SUM(CASE WHEN profit > 0 THEN profit ELSE 0 END), 0) FROM " + tableName + " WHERE status = 1 AND update_time >= CURDATE()");
        BigDecimal platformProfit = queryDecimal("SELECT COALESCE(SUM(CASE WHEN profit IS NULL THEN 0 ELSE -profit END), 0) FROM " + tableName + " WHERE status = 1 AND update_time >= CURDATE()");
        long settledCount = queryLong("SELECT COUNT(1) FROM " + tableName + " WHERE status = 1 AND update_time >= CURDATE()");
        long abnormalRoundCount = queryLong("SELECT COUNT(DISTINCT periods_num) FROM " + tableName + " WHERE status = 0 AND create_time < DATE_SUB(NOW(), INTERVAL 2 MINUTE)");

        stats.put("bet", money(bet));
        stats.put("reward", money(reward));
        stats.put("platformProfit", money(platformProfit));
        stats.put("settledCount", settledCount);
        stats.put("abnormalRoundCount", abnormalRoundCount);
        return stats;
    }

    private BigDecimal decimalFromJson(JSONObject object, String key) {
        if (object == null) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(String.valueOf(object.get(key)));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private String money(BigDecimal value) {
        if (value == null) {
            value = BigDecimal.ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

}
