package com.zywl.app.manager.service.manager;


import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
//import com.sun.org.apache.regexp.internal.RE;
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
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
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
        result.put("guilds", vos);
        result.put("createFee", GUILD_FEE);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "002", description = "创建公会")
    public JSONObject createGuild(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);

        Map<String,Object> queryObj = new HashMap<>();
        queryObj.put("userId", userId);
        queryObj.put("status", 2);
        List<Guild> rst = guildService.findByConditions(queryObj);
        List<Guild> allGuild = guildService.findAllGuild();
        if (rst.size() > 0) {
            throwExp("请耐心等待审核！");
        }
        BigDecimal createAmount = GUILD_FEE;

        Map<String, Backpack> userBackpack = gameService.getUserBackpack(userId.toString());
        if (allGuild.size()==0){
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.5"));
        } else if (allGuild.size()==1) {
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.6"));
        }else if (allGuild.size()==2) {
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.7"));
        }else if (allGuild.size()==3) {
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.8"));
        }else if (allGuild.size()==4) {
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.9"));
        }
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        if (userCapital.getBalance().compareTo(createAmount) < 0) {
            throwExp(UserCapitalTypeEnum.currency_2.getName()+"不足");
        }
        if (user.getRoleId() == 2 || user.getRoleId() == 3) {
            throwExp("您已成为王者之师，无需再次购买");
        }
        Long dataId = guildService.applyAddGuild(user.getName(), userId, 1, createAmount, 2);
        //guildMemberService.addGuildMember(dataId, userId, new BigDecimal("8"), 3, userId, GUILD_FEE);
        //initStatics(userId, dataId);
        userCapitalService.subUserBalanceByGuild(userId, createAmount, dataId);
        managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
        //userService.updateUserRoleId(userId, 3);
        return new JSONObject();
    }

    public void passApplyGuild(long dataId, long userId) {
        User user = userCacheService.getUserInfoById(userId);
        if (user.getRoleId() != 1) {
            throwExp("身份异常");
        }
        guildMemberService.addGuildMember(dataId, userId, new BigDecimal("8"), 3, userId, GUILD_FEE);
        initStatics(userId, dataId);
        guildService.passGuildApply(dataId);
        userService.updateUserRoleId(userId, 3);
    }

    public void refuseApplyGuild(long dataId, long userId) {
        //退款
        userCapitalService.addUserBalanceByGuild(userId, GUILD_FEE.add(GUILD_FEE.multiply(new BigDecimal("0.08"))), dataId);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId,UserCapitalTypeEnum.currency_2.getValue());
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
        pushData.put("balance", userCapital.getBalance());
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
        JSONObject obj = new JSONObject();
        obj.put("id", dataId);
        guildService.delete(obj);
    }

    @Transactional
    @ServiceMethod(code = "003", description = "查询公会详情")
    public Object getGuildInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("guildId"), data.get("userId"));
        String userId = data.getString("userId");
        Long guildId = data.getLong("guildId");
        List<GuildMember> guildMembers = guildMemberService.findByGuildId(guildId);

        List<UserVo> list = new ArrayList<>();
        for (GuildMember guildMember : guildMembers) {
            User user = userCacheService.getUserInfoById(guildMember.getUserId());
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
    @ServiceMethod(code = "004", description = "添加工会成员")
    public JSONObject addGuildMember(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("guildId"),  data.get("createUserId"));
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId.toString())) {
            Long guildId = data.getLong("guildId");
            Long createUserId = data.getLong("createUserId");
            Guild guild = guildCacheService.getGuildByGuildId(guildId);
            GuildMember member = guildMemberService.findByUserId(userId);
            if (member != null) {
                throwExp("该玩家已是人气之王");
            }

            Integer freeNum = guild.getFreeNum();
            if (freeNum <= 0) {
                //需要收费
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(createUserId, UserCapitalTypeEnum.currency_2.getValue());
                if (userCapital.getBalance().compareTo(GUILD_MEMBER_FEE) == -1) {
                    throwExp("金币不足");
                }
                userCapitalService.subUserBalanceByGuild(createUserId, GUILD_MEMBER_FEE, guildId);
                UserCapital createUserCapital = userCapitalCacheService.getUserCapitalCacheByType(createUserId,UserCapitalTypeEnum.currency_2.getValue());
                JSONObject pushData = new JSONObject();
                pushData.put("userId", createUserId);
                pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(createUserId), pushData);
                guildService.updateGuildBailAmount(GUILD_MEMBER_FEE, guildId);
            }
            guildMemberService.addGuildMember(guildId, userId, new BigDecimal("8"), 2, createUserId, guild.getMemberNumber() > 0 ? GUILD_MEMBER_FEE : BigDecimal.ZERO);
            initStatics(userId, guildId);
            userService.updateUserRoleId(userId, 2);
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
            managerGameBaseService.pushCapitalUpdate(operatorUserId,UserCapitalTypeEnum.currency_2.getValue());
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
    @ServiceMethod(code = "011", description = "获取工会配置")
    public JSONObject getGuildConfig(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        Map<String, Backpack> userBackpack = gameService.getUserBackpack(String.valueOf(userId));
        BigDecimal createAmount = GUILD_FEE;
        BigDecimal fee = createAmount.multiply(new BigDecimal("0.08"));
        if (userBackpack != null && userBackpack.containsKey("50")) {
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.6"));
        }
        if (userBackpack != null && userBackpack.containsKey("51")) {
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.7"));
        }
        if (userBackpack != null && userBackpack.containsKey("52")) {
            createAmount = GUILD_FEE.multiply(new BigDecimal("0.8"));
        }
        JSONObject result = new JSONObject();
        result.put("baseAmount", GUILD_FEE);
        result.put("createAmount", createAmount);
        result.put("fee", fee);
        return result;
    }

}
