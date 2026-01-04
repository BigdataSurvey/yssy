package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.GuildMemberVo;
import com.zywl.app.base.bean.vo.GuildVo;
import com.zywl.app.base.bean.vo.UserVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GuildCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@ServiceClass(code = MessageCodeContext.GUILD_SERVER)
public class ManagerGuildService extends BaseService {


    @Autowired
    private GuildService guildService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private GuildDailyStaticsService guildDailyStaticsService;

    @Autowired
    private GuildMemberService guildMemberService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private ManagerSocketService managerSocketService;
    @Autowired
    private ConfigService configService;

    @Autowired
    private GuildGrantRecordService guildGrantRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private GuildCacheService guildCacheService;

    private BigDecimal GUILD_FEE;

    private BigDecimal GUILD_MEMBER_FEE;

    private BigDecimal GUILD_CREATE_FEE_RATE;


    @PostConstruct
    public void _construct() {
        Config config = configService.getConfigByKey(Config.GUILD_FEE);
        if (config != null) {
            GUILD_FEE = new BigDecimal(config.getValue());
        } else {
            GUILD_FEE = new BigDecimal("100000");
        }

        Config config2 = configService.getConfigByKey(Config.GUILD_MEMBER_FEE);
        if (config2 != null) {
            GUILD_MEMBER_FEE = new BigDecimal(config2.getValue());
        } else {
            GUILD_MEMBER_FEE = new BigDecimal("5000");
        }

        // 创建公会手续费比例
        Config config3 = configService.getConfigByKey(Config.GUILD_CREATE_FEE_RATE);
        if (config3 != null && config3.getValue() != null && !config3.getValue().trim().isEmpty()) {
            BigDecimal rate = new BigDecimal(config3.getValue().trim());
            if (rate.compareTo(BigDecimal.ONE) > 0) {
                rate = rate.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
            }
            GUILD_CREATE_FEE_RATE = rate;
        } else {
            GUILD_CREATE_FEE_RATE = BigDecimal.ZERO;
        }

        //初始化每日报表
        List<GuildMember> allGuildMember = guildMemberService.findAllGuildMember();
        for (GuildMember member : allGuildMember) {
            List<GuildDailyStatics> staticsByYmd = guildDailyStaticsService.findStaticsByYmd(member.getUserId(), DateUtil.format9(new Date()));
            if (staticsByYmd == null || staticsByYmd.size() == 0) {
                guildDailyStaticsService.addStatics(DateUtil.format9(new Date()), member.getUserId(), member.getGuildId());
            }
        }
    }

    @Transactional
    public void initStatics(Long userId, Long guildId) {
        String ymd = DateUtil.format9(new Date());
        guildDailyStaticsService.addStatics(ymd, userId, guildId);
    }

    @Transactional
    @ServiceMethod(code = "001", description = "获取公会列表")
    public JSONObject getGuilds(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Long userId = data.getLong("userId");
        List<Guild> guilds = guildCacheService.getGuilds();
        List<GuildVo> vos = new ArrayList<>();
        for (Guild guild : guilds) {
            GuildVo guildVo = new GuildVo();
            User user = userCacheService.getUserInfoById(guild.getUserId());
            BeanUtils.copy(guild, guildVo);
            if (user != null) {
                guildVo.setUserNo(user.getUserNo());
                guildVo.setName(user.getName());
                guildVo.setHeadImageUrl(user.getHeadImageUrl());
            }
            vos.add(guildVo);
        }
        JSONObject result = new JSONObject();
        User my = userCacheService.getUserInfoById(userId);
        result.put("roleId", my.getRoleId());
        Guild applyGuild = null;
        Map<String, Object> applyQuery = new HashMap<>();
        applyQuery.put("userId", userId);
        applyQuery.put("status", 2);
        List<Guild> applyList = guildService.findByConditions(applyQuery);
        if (applyList != null && !applyList.isEmpty()) {
            applyGuild = applyList.get(0);
        }
        if (applyGuild != null) {
            JSONObject applyInfo = new JSONObject();
            applyInfo.put("guildId", applyGuild.getId());
            applyInfo.put("status", applyGuild.getStatus());
            applyInfo.put("guildName", applyGuild.getGuildName());
            applyInfo.put("needMemberNumber", applyGuild.getNeedMemberNumber());
            applyInfo.put("unitPrice", GUILD_FEE);
            applyInfo.put("bailAmount", applyGuild.getBailAmount());
            applyInfo.put("feeRate", applyGuild.getFeeRate() == null ? GUILD_CREATE_FEE_RATE : applyGuild.getFeeRate());
            applyInfo.put("feeAmount", applyGuild.getFeeAmount());
            applyInfo.put("payAmount", applyGuild.getPayAmount());
            applyInfo.put("applyTime", applyGuild.getApplyTime());
            result.put("applyInfo", applyInfo);
        }
        result.put("guilds", vos);
        result.put("guildUnitPrice", GUILD_FEE);
        result.put("createFeeRate", GUILD_CREATE_FEE_RATE);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "002", description = "创建公会（需后台审核，状态=2审核中）")
    public JSONObject createGuild(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        checkNull(data.get("guildName"));
        checkNull(data.get("needMemberNumber"));

        Long userId = data.getLong("userId");
        String guildName = data.getString("guildName");
        Integer needMemberNumber = data.getInteger("needMemberNumber");

        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("查询玩家信息失败");
        }

        if (guildName == null) {
            throwExp("公会名称不能为空");
        }
        guildName = guildName.trim();
        if (guildName.isEmpty()) {
            guildName = user.getName();
        }

        if (guildName.codePointCount(0, guildName.length()) > 10) {
            throwExp("公会名称最多10个字");
        }
        if (needMemberNumber == null || needMemberNumber <= 0) {
            throwExp("needMemberNumber参数错误");
        }

        if (user.getRoleId() != null && user.getRoleId() != 1) {
            throwExp("已加入公会，无法重复创建");
        }

        // 若已存在审核中的申请，直接返回申请信息
        Map<String, Object> queryObj = new HashMap<>();
        queryObj.put("userId", userId);
        queryObj.put("status", 2);
        List<Guild> rst = guildService.findByConditions(queryObj);
        if (rst != null && rst.size() > 0) {
            Guild applyGuild = rst.get(0);
            JSONObject r = new JSONObject();
            r.put("guildId", applyGuild.getId());
            r.put("status", applyGuild.getStatus());
            r.put("guildName", applyGuild.getGuildName());
            r.put("needMemberNumber", applyGuild.getNeedMemberNumber());
            r.put("unitPrice", GUILD_FEE);
            r.put("bailAmount", applyGuild.getBailAmount());
            r.put("feeRate", applyGuild.getFeeRate() == null ? GUILD_CREATE_FEE_RATE : applyGuild.getFeeRate());
            r.put("feeAmount", applyGuild.getFeeAmount());
            r.put("payAmount", applyGuild.getPayAmount());
            r.put("applyTime", applyGuild.getApplyTime());
            return r;
        }

        // 单价来自 Config.GUILD_FEE；质押=needMemberNumber*单价；手续费=质押*费率
        BigDecimal unitPrice = GUILD_FEE;
        BigDecimal bailAmount = unitPrice.multiply(new BigDecimal(needMemberNumber));
        BigDecimal feeRate = (GUILD_CREATE_FEE_RATE == null ? BigDecimal.ZERO : GUILD_CREATE_FEE_RATE);

        // 手续费向上取整
        BigDecimal feeAmount = bailAmount.multiply(feeRate).setScale(0, RoundingMode.UP);
        BigDecimal payAmount = bailAmount.add(feeAmount).setScale(0, RoundingMode.UP);

        // 校验核心积分余额
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.hxjf.getValue());
        if (userCapital == null || userCapital.getBalance() == null || userCapital.getBalance().compareTo(payAmount) < 0) {
            throwExp(UserCapitalTypeEnum.hxjf.getName() + "不足");
        }

        // 先创建申请记录后扣款；拒绝时仅退还质押 手续费不退
        int freeNum = Math.max(needMemberNumber - 1, 0);
        Long guildId = guildService.applyAddGuild(
                guildName,
                userId,
                1,
                bailAmount,
                freeNum,
                needMemberNumber,
                feeRate,
                feeAmount,
                payAmount,
                2
        );

        userCapitalService.subUserBalanceByGuild(userId, payAmount, guildId, UserCapitalTypeEnum.hxjf.getValue());
        managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.hxjf.getValue());

        JSONObject r = new JSONObject();
        r.put("guildId", guildId);
        r.put("status", 2);
        r.put("guildName", guildName);
        r.put("needMemberNumber", needMemberNumber);
        r.put("unitPrice", unitPrice);
        r.put("bailAmount", bailAmount);
        r.put("feeRate", feeRate);
        r.put("feeAmount", feeAmount);
        r.put("payAmount", payAmount);
        r.put("applyTime", new Date());
        return r;
    }

    //发送成为会长通知
    private void sendBecomeGuildMasterNotification(Long userId, Long guildId, String guildName) {
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("guildId", guildId);
        pushData.put("guildName", guildName);
        pushData.put("role", "guild_master");
        pushData.put("timestamp", new Date());
        Push.push(PushCode.BECOME_GUILD_MASTER, managerSocketService.getServerIdByUserId(userId), pushData);
    }


    public void passApplyGuild(long dataId, long userId) {
        Guild guild = guildService.findById(dataId);
        if (guild == null) {
            throwExp("公会申请不存在");
        }
        if (guild.getStatus() == null || guild.getStatus() != 2) {
            throwExp("申请状态异常");
        }

        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("查询玩家信息失败");
        }
        if (user.getRoleId() != 1) {
            throwExp("身份异常");
        }

        // 审核通过：先更新状态，再建立会长关系
        guildService.passGuildApply(dataId);

        BigDecimal profitRate = new BigDecimal("8");
        BigDecimal bailAmount = guild.getBailAmount() == null ? BigDecimal.ZERO : guild.getBailAmount();
        guildMemberService.addGuildMember(dataId, userId, profitRate, 3, userId, bailAmount);

        initStatics(userId, dataId);
        userService.updateUserRoleId(userId, 3);
        // 成为会长通知
        sendBecomeGuildMasterNotification(userId, dataId, guild.getGuildName());
    }
    public void refuseApplyGuild(long dataId, long userId) {
        Guild guild = guildService.findById(dataId);
        if (guild == null) {
            throwExp("公会申请不存在");
        }
        if (guild.getStatus() == null || guild.getStatus() != 2) {
            throwExp("申请状态异常");
        }

        // 退款只退还质押手续费不退
        BigDecimal refundAmount = guild.getBailAmount() == null ? BigDecimal.ZERO : guild.getBailAmount();
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            userCapitalService.addUserBalanceByGuild(userId, refundAmount, dataId);
            userCapitalCacheService.deltedUserCapitalCache(userId, UserCapitalTypeEnum.hxjf.getValue());
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.hxjf.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", userId);
            pushData.put("capitalType", UserCapitalTypeEnum.hxjf.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        }

        // 删除申请记录
        guildService.delete(dataId);
        guildCacheService.removeGuilds();

        // 重置身份
        userService.updateUserRoleId(userId, 1);

        JSONObject obj = new JSONObject();
        obj.put("code", "deleteGuild");
        Push.push(PushCode.deleteGuild, managerSocketService.getServerIdByUserId(userId), obj);
    }



    @Transactional
    @ServiceMethod(code = "003", description = "查询公会详情")
    public Object getGuildInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("guildId"), data.get("userId"));
        Long userId = data.getLong("userId");
        loadAndCheckUser(userId);

        Long guildId = data.getLong("guildId");
        // 查询公会成员列表
        List<GuildMember> guildMembers = guildMemberService.findByGuildId(guildId);
        if (guildMembers == null || guildMembers.isEmpty()) {
            logger.error("暂无工会");
            return new ArrayList<>();
        }

        List<UserVo> list = new ArrayList<>();
        for (GuildMember guildMember : guildMembers) {
            User user = userCacheService.getUserInfoById(guildMember.getUserId());
            if (user == null) {
                logger.error("工会用户不存在");
                continue;
            }
            UserVo vo = new UserVo();
            try {
                BeanUtils.copy(user, vo);
            }catch (Exception e){
                vo.setHeadImageUrl(user.getHeadImageUrl());
                vo.setName(user.getName());
                vo.setUserNo(user.getUserNo());
                vo.setId(user.getId());
                vo.setAuthentication(user.getAuthentication());
                vo.setIdCard(user.getIdCard());
                vo.setIsCash(user.getIsCash());
                vo.setPhone(user.getPhone());
                vo.setQq(user.getQq());
                vo.setRealName(user.getRealName());
                vo.setRisk(user.getRisk());
                vo.setVipExpireTime(user.getVipExpireTime());
                vo.setVip1(user.getVip1());
                vo.setVip2(user.getVip2());
                vo.setVip2ExpireTime(user.getVip2ExpireTime());
                vo.setRoleId(user.getRoleId());
                vo.setWechatId(user.getWechatId());
                vo.setGameToken(null);
            }

            list.add(vo);
        }
        return list;
    }


    @Transactional
    @ServiceMethod(code = "004", description = "添加工会成员（会长可邀请副会长/成员；副会长可邀请成员）")
    public JSONObject addGuildMember(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("guildId"),  data.get("createUserId"));
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId.toString())) {
            Long guildId = data.getLong("guildId");
            Long createUserId = data.getLong("createUserId");
            // memberRoleId：2=成员(默认)，4=副会长
            Integer memberRoleId = data.getInteger("memberRoleId");
            if (memberRoleId == null) {
                memberRoleId = 2;
            }
            if (memberRoleId != 2 && memberRoleId != 4) {
                throwExp("memberRoleId错误");
            }
            Guild guild = guildCacheService.getGuildByGuildId(guildId);
            GuildMember member = guildMemberService.findByUserId(userId);
            if (member != null) {
                throwExp("该玩家已加入公会");
            }

            Integer freeNum = guild.getFreeNum();
            if (freeNum == null) {
                freeNum = 0;
            }
            if (freeNum <= 0) {
                //需要收费
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(createUserId, UserCapitalTypeEnum.hxjf.getValue());
                if (userCapital == null || userCapital.getBalance() == null || userCapital.getBalance().compareTo(GUILD_MEMBER_FEE) < 0) {
                    throwExp(UserCapitalTypeEnum.hxjf.getName() + "不足");
                }
                userCapitalService.subUserBalanceByGuild(createUserId, GUILD_MEMBER_FEE, guildId, UserCapitalTypeEnum.hxjf.getValue());
                managerGameBaseService.pushCapitalUpdate(createUserId, UserCapitalTypeEnum.hxjf.getValue());
                guildService.updateGuildBailAmount(GUILD_MEMBER_FEE, guildId);
            }
            guildMemberService.addGuildMember(guildId, userId, new BigDecimal("8"), memberRoleId, createUserId, guild.getMemberNumber() > 0 ? GUILD_MEMBER_FEE : BigDecimal.ZERO);
            initStatics(userId, guildId);
            userService.updateUserRoleId(userId, memberRoleId);
            guildService.updateGuildMemberNumber(guildId, 1, freeNum > 0 ? -1 : 0);
            return new JSONObject();
        }
    }

    @Transactional
    @ServiceMethod(code = "005", description = "我的公会")
    public JSONObject myGuild(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("guildId"), data.get("userId"));
        String userId = data.getString("userId");
        Long guildId = data.getLong("guildId");
        Guild guild = guildCacheService.getGuildByGuildId(guildId);
        JSONObject result = new JSONObject();
        List<GuildMember> guildMembers = guildMemberService.findByGuildId(guildId);
        List<GuildMemberVo> vos = new ArrayList<>();
        for (GuildMember member : guildMembers) {
            GuildMemberVo vo = new GuildMemberVo();
            User user = userCacheService.getUserInfoById(member.getUserId());
            BeanUtils.copy(member, vo);
            if (user != null) {
                vo.setUserNo(user.getUserNo());
                vo.setName(user.getName());
                vo.setHeadImageUrl(user.getHeadImageUrl());
            }
            if (!guild.getUserId().toString().equals(userId)){
                vo.setProfitBalance(BigDecimal.ZERO);
            }
            vos.add(vo);
        }
        result.put("members", vos);

        if (guild != null && userId.equals(guild.getUserId().toString())) {
            //是本人的
            result.put("freeNumber", guild.getFreeNum());
        }
        result.put("addFee", GUILD_MEMBER_FEE);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "007", description = "发放佣金")
    public Object receive(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("guildId"), data.get("userId"), data.get("operatorUserId"));
        Long guildId = data.getLong("guildId");
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId.toString())) {
            User user = userCacheService.getUserInfoById(userId);
            Long operatorUserId = data.getLong("operatorUserId");
            GuildMember member = guildMemberService.findByGuildIdAndUserId(guildId, userId);
            if (member == null) {
                throwExp("领取失败");
            }
            if (member.getProfitBalance().compareTo(BigDecimal.ZERO) == 0) {
                throwExp("当前暂无可领取的收益");
            }
            BigDecimal amount = member.getProfitBalance();
            //可领取设置为0
            guildMemberService.receiveProfitBalance(guildId, userId);
            //添加领取记录
            String orderNo = OrderUtil.getOrder5Number();
            Long dataId = guildGrantRecordService.addRecord(guildId, userId, orderNo, operatorUserId, member.getProfitBalance(), member.getProfitRate(), amount, amount, user.getUserNo());
            userCapitalService.addUserBalanceByReceiveGuild(operatorUserId, amount, orderNo, dataId);
            managerGameBaseService.pushCapitalUpdate(operatorUserId,UserCapitalTypeEnum.hxjf.getValue());
            JSONObject result = new JSONObject();
            result.put("memberAmount", amount);
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "009", description = "修改比例")
    public JSONObject updateRate(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("rate"));
        Long userId = data.getLong("userId");
        guildMemberService.updateRate(userId, data.getBigDecimal("rate"));
        return data;
    }

    @Transactional
    @ServiceMethod(code = "011", description = "获取公会配置（创建公会单价/手续费比例/成员费用）")
    public JSONObject getGuildConfig(ManagerSocketServer adminSocketServer, JSONObject data) {
        JSONObject result = new JSONObject();
        // 单价
        result.put("guildUnitPrice", GUILD_FEE);
        // 手续费比例
        result.put("createFeeRate", GUILD_CREATE_FEE_RATE);
        // 添加成员费用
        result.put("guildMemberFee", GUILD_MEMBER_FEE);
        return result;
    }

    /**
     * 加载并校验用户
     */
    private User loadAndCheckUser(Long userId) {
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = (users != null) ? users.get(userId) : null;
        if (user == null) {
            throwExp("用户不存在");
        }
        return user;
    }
}
