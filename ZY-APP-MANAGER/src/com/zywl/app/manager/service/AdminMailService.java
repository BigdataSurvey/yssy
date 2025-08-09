package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.TsgPayOrderVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.*;
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

import java.math.BigDecimal;
import java.util.*;

@Service
@ServiceClass(code = MessageCodeContext.ADMIN_EMAIL_SERVER)
public class AdminMailService extends BaseService {
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long fromUserId = params.getLongValue("fromUserId", 0);
        long toUserId = params.getLongValue("toUserId", 0);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);
        if (fromUserId > 0) {
            condition.put("fromUserId", fromUserId);
        }
        if (toUserId > 0) {
            condition.put("toUserId", toUserId);
        }

        Long count = mailService.count("countByConditions", condition);
        List<CashRecord> recrods = mailService.findByConditions(condition);

        JSONObject data = new JSONObject();
        data.put("list", recrods);
        data.put("count", count);
        return data;
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
        JSONObject detail = new JSONObject();
        for (int i = 0; i < itemArr.size(); i++) {
            JSONObject item = JSONObject.from(itemArr.get(i));
            int itemId = item.getIntValue("itemId");
            BigDecimal itemNum = item.getBigDecimal("itemNum");
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
            int n = mailService.save(mail);
            return new JSONObject();
        }
        for (
                Object o : userIdArr) {
            String toId = o.toString();
            Mail mail = new Mail();
            User toUser = userCacheService.getUserInfoByUserNo(toId);
            if (toUser == null) {
                continue;
            }
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
            int n = mailService.save(mail);
        }

        JSONObject content = new JSONObject();
        content.put("userIdArr", userIdArr);
        content.put("title", title);
        content.put("context", context);
        content.put("mailType", mailType);
        content.put("itemArr", itemArr);
        if (adminSocketServer != null) {
            adminLogService.addAdminLog(adminSocketServer.getAdmin(), "sendMail", content);
        }

        return new

                JSONObject();

    }

    @ServiceMethod(code = "003", description = "获取道具列表")
    public Object getItemList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        Collection<Item> items = PlayGameService.itemMap.values();
        JSONArray data = new JSONArray();
        for (Item item : items) {
            JSONObject obj = new JSONObject();
            obj.put("itemName", item.getName());
            obj.put("itemId", item.getId());
            data.add(obj);
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", 10);
        condition.put("status", 0);

        Long count = applyForService.count("countByConditions", condition);
        List<ApplyFor> recrods = applyForService.findByConditions(condition);

        JSONObject data = new JSONObject();
        data.put("list", recrods);
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);
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

    @ServiceMethod(code = "020", description = "获取公会列表")
    public Object getGuildList(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        int status = params.getIntValue("status", -1);
        long guildId = params.getLongValue("guildId", -1);
        long userId = params.getLongValue("userId", -1);
        String guildName = params.getString("guildName");

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);
        if (status >= 0) {
            condition.put("status", status);
        }
        if (guildId >= 0) {
            condition.put("guildId", guildId);
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

        Guild guild1 = guildService.findByUserId(userId);
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long guildId = params.getLongValue("guildId", -1);
        long userId = params.getLongValue("userId", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();

        condition.put("start", start);
        condition.put("limit", 10);
        if (guildId >= 0) {
            condition.put("guildId", guildId);
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
        guildMemberService.delete(delObj);
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
            guildMemberService.delete(delParam);
            guildCacheService.removeMember(member.getUserId());
        }

        JSONObject delParam = new JSONObject();
        delParam.put("id", guild.getId());
        guildService.delete(delParam);
        guildCacheService.removeGuilds();

        JSONObject content = new JSONObject();
        content.put("guildId", guildId);
        adminLogService.addAdminLog(adminSocketServer.getAdmin(), "dissGuild", content);
        return new Object();
    }

    @ServiceMethod(code = "030", description = "查询货币日志")
    public Object searchTreasureLog(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", 10);

        if (user != null) {
            condition.put("tableName", LogUserCapital.tablePrefix + user.getId().toString().charAt(user.getId().toString().length() - 1));
            condition.put("userId", user.getId());
        } else {
            condition.put("tableName", LogUserCapital.tablePrefix + 0);
        }

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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", 10);

        if (user != null) {
            condition.put("tableName", LogUserBackpack.tablePrefix + user.getId().toString().charAt(user.getId().toString().length() - 1));
            condition.put("userId", user.getId());
        } else {
            condition.put("tableName", LogUserBackpack.tablePrefix + 0);
        }

        long count = logUserBackpackService.count("dbCountByConditions", condition);
        List<LogUserBackpack> list = logUserBackpackService.findList("dbFindByConditions", condition);

        JSONArray array = new JSONArray();
        for (LogUserBackpack logUserBackpack : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(logUserBackpack);
            User user1 = userCacheService.getUserInfoById(logUserBackpack.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            Item item = itemCacheService.getItemInfoById(logUserBackpack.getItemId());
            obj.put("itemName", item.getName());
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);

        if (user != null) {
            condition.put("tableName", Backpack.tablePrefix + user.getId().toString().charAt(user.getId().toString().length() - 1));
            condition.put("userId", user.getId());
        } else {
            condition.put("tableName", Backpack.tablePrefix + 0);
        }

        long count = backpackService.count("dbCountByConditions", condition);
        List<Backpack> list = backpackService.findList("dbFindByConditions", condition);

        JSONArray array = new JSONArray();
        for (Backpack backpack : list) {
            JSONObject obj = (JSONObject) JSON.toJSON(backpack);
            User user1 = userCacheService.getUserInfoById(backpack.getUserId());
            obj.put("userName", user1 == null ? "" : user1.getName());
            array.add(obj);
        }

        JSONObject data = new JSONObject();
        data.put("list", array);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "050", description = "查询资产信息")
    public Object searchTreasureInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);

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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long capitalType = params.getLongValue("capitalType", 2);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);
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


    @ServiceMethod(code = "070", description = "查询角色信息")
    public Object searchPlayerInfo(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);

        if (user != null) {
            condition.put("userId", user.getId());
        }


        return null;
    }

    @ServiceMethod(code = "071", description = "封号解封")
    public Object banLogin(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("operation"), params.get("id"));
        checkAuth(adminSocketServer);

        long userId = params.getLongValue("id", -1);
        int status = params.getIntValue("operation", -1);//0禁登录 1解登录 2禁功能 3解功能
        String mark = params.getString("mark");
        Admin admin = adminSocketServer.getAdmin();
        return banUser(userId, status, mark, admin);
    }

    public Object banUser(Long userId, int status, String mark, Admin admin) {
        if (userId < 0 || status < 0 || status > 3 || mark == null) {
            throwExp("参数错误");
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

        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        Integer start = (page - 1) * limit;
        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", 10);

        if (userId > 0) {
            condition.put("userId", userId);
        }
        if (userNo != null && !userNo.isEmpty()) {
            condition.put("userNo", userNo);
        }
        if (userName != null && !userName.isEmpty()) {
            condition.put("userName", userName);
        }

        //记录封号或者解封原因
        Admin admin = adminSocketServer.getAdmin();
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");

        User user = findUser(userId, userNo, userName);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        String goodNo = params.getString("goodNo");

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);

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
        int page = params.getIntValue("page", 0);
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
        condition.put("limit", 10);
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
        int page = params.getIntValue("page", 0);
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
        condition.put("limit", 10);
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
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        long userId = params.getLongValue("userId", 0);
        String userNo = params.getString("userNo");
        String userName = params.getString("userName");
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);

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
            obj.remove("password");
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
        User user = userCacheService.getUserInfoById(userId);
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("userId", userId);
        queryObj.put("status", 2);
        List<Guild> rst = guildService.findByConditions(queryObj);
        if (rst.size() > 0) {
            throwExp("请耐心等待审核！");
        }
        Long dataId = guildService.applyAddGuild(user.getName(), userId, 1, BigDecimal.ZERO, 2);
        userCapitalService.subUserBalanceByGuild(userId, BigDecimal.ZERO, dataId);
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


    @ServiceMethod(code = "140", description = "查询管理员操作日志")
    public Object getAdminLog(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);

        Integer start = (page - 1) * limit;
        Integer end = page * limit;
        JSONObject condition = new JSONObject();
        condition.put("start", start);
        condition.put("limit", 10);

        long count = adminLogService.count("countByConditions", condition);
        List<AdminLog> list = adminLogService.findList("findByConditions", condition);

        JSONObject data = new JSONObject();
        data.put("list", list);
        data.put("count", count);
        return data;
    }

    @ServiceMethod(code = "150", description = "查询数美规则")
    public Object searchShuMeiRule(AdminSocketServer adminSocketServer, Command webCommand, JSONObject params) {
        checkNull(params);
        checkAuth(adminSocketServer);
        int page = params.getIntValue("page", 0);
        int limit = params.getIntValue("limit", 10);
        String model = params.getString("models");
        int status = params.getIntValue("status", -1);

        Integer start = (page - 1) * limit;
        Map<String, Object> condition = new HashMap<>();
        condition.put("start", start);
        condition.put("limit", 10);

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
}
