package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
//import com.sun.org.apache.regexp.internal.RE;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.GuildDailyStaticsVo;
import com.zywl.app.base.bean.vo.UserVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.GuildCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.GuildDailyStaticsService;
import com.zywl.app.defaultx.service.GuildGrantRecordService;
import com.zywl.app.defaultx.service.GuildMemberService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.GUILD_SERVER)
public class ServerGuildService extends BaseService {


    @Autowired
    private GuildCacheService guildCacheService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private GuildMemberService guildMemberService;

    @Autowired
    private GuildDailyStaticsService guildDailyStaticsService;

    @Autowired
    private GuildGrantRecordService guildGrantRecordService;

    @ServiceMethod(code = "001", description = "查看工会列表")
    public Object getGuilds(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId  );
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "002", description = "创建公会")
    public Object createGuilds(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);

        // 前置校验（减少无效跨服 RPC）
        checkNull(params.get("guildName"));
        checkNull(params.get("needMemberNumber"));
        String guildName = params.getString("guildName");
        if (guildName != null && guildName.trim().codePointCount(0, guildName.trim().length()) > 10) {
            throwExp("公会名称最多10个字");
        }
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("查询玩家信息失败");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "003", description = "查看公会详情")
    public Object getGuildInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("guildId"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("查询玩家信息失败");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018003", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "004", description = "添加公会成员（会长可邀请副会长/成员；副会长可邀请成员）")
    public Object addGuildMember(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userNo"));
        String userNo = params.getString("userNo");

        User targetUser = userCacheService.getUserInfoByUserNo(userNo);
        if (targetUser == null) {
            throwExp("玩家不存在");
        }

        Long operatorUserId = appSocket.getWsidBean().getUserId();
        GuildMember operatorMember = guildCacheService.getMemberByUserId(operatorUserId);
        if (operatorMember == null) {
            throwExp("您没有加入公会");
        }

        Long guildId = operatorMember.getGuildId();
        Guild guild = guildCacheService.getGuildByGuildId(guildId);
        if (guild == null) {
            throwExp("公会不存在");
        }
        if (guild.getStatus() == null || guild.getStatus() != 1) {
            throwExp("公会审核中，暂不可邀请成员");
        }

        // memberRoleId：2=成员(默认)，4=副会长
        Integer memberRoleId = params.getInteger("memberRoleId");
        if (memberRoleId == null) {
            memberRoleId = 2;
        }
        if (memberRoleId != 2 && memberRoleId != 4) {
            throwExp("memberRoleId错误");
        }

        // 邀请权限校验
        Integer operatorRoleId = operatorMember.getRoleId();
        if (memberRoleId == 4) {
            if (operatorRoleId == null || operatorRoleId != 3) {
                throwExp("仅会长可邀请副会长");
            }
        } else {
            if (operatorRoleId == null || (operatorRoleId != 3 && operatorRoleId != 4)) {
                throwExp("仅会长/副会长可邀请成员");
            }
        }

        if (operatorUserId.toString().equals(targetUser.getId().toString())) {
            throwExp("不能添加自己");
        }
        if (targetUser.getRoleId() != 1) {
            throwExp("该玩家已是公会成员");
        }

        params.put("userId", targetUser.getId());
        params.put("guildId", guildId);
        params.put("createUserId", operatorUserId);
        params.put("memberRoleId", memberRoleId);

        Executer.request(TargetSocketType.manager,
                CommandBuilder.builder().request("018004", params).build(),
                new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "005", description = "查看公会成员列表")
    public Object getMemberList(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        GuildMember member = guildCacheService.getMemberByUserId(userId);
        if (member == null) {
            throwExp("您没有加入公会");
        }
        Long guildId = member.getGuildId();
        params.put("guildId", guildId);
        Guild guild = guildCacheService.getGuildByGuildId(guildId);
        List<GuildMember> guildMembers = guildMemberService.findByGuildId(guildId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018005", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "006", description = "查看指定公会成员详情")
    public Object getMemberInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("page"), params.get("num"));
        Long userId = params.getLong("userId");

        GuildMember targetMemberCache = guildCacheService.getMemberByUserId(userId);
        if (targetMemberCache == null) {
            throwExp("该玩家未加入公会");
        }
        Long guildId = targetMemberCache.getGuildId();

        User targetUser = userCacheService.getUserInfoById(userId);
        if (targetUser == null) {
            throwExp("玩家不存在");
        }

        Guild guild = guildCacheService.getGuildByGuildId(guildId);
        if (guild == null) {
            throwExp("公会不存在");
        }

        Long myId = appSocket.getWsidBean().getUserId();
        GuildMember myMember = guildCacheService.getMemberByUserId(myId);
        if (myMember == null || !guildId.toString().equals(myMember.getGuildId().toString())) {
            throwExp("您没有加入该公会");
        }

        GuildMember guildMember = guildMemberService.findByGuildIdAndUserId(guildId, userId);
        if (guildMember == null) {
            throwExp("该玩家不是您的公会成员");
        }

        // 权限：本人 / 会长 / 副会长(仅可查看自己直邀的成员)
        boolean allow = myId.toString().equals(userId.toString());
        if (!allow) {
            Integer myRoleId = myMember.getRoleId();
            if (myRoleId != null && myRoleId == 3) {
                allow = true;
            } else if (myRoleId != null && myRoleId == 4
                    && guildMember.getCreateUserId() != null
                    && guildMember.getCreateUserId().toString().equals(myId.toString())) {
                allow = true;
            } else if (guild.getUserId() != null && guild.getUserId().toString().equals(myId.toString())) {
                // 兼容：历史逻辑以 guild.userId 作为会长判断
                allow = true;
            }
        }
        if (!allow) {
            throwExp("您无权查看");
        }

        List<GuildDailyStatics> statics = guildDailyStaticsService.findByUserId(
                userId, params.getInteger("page"), params.getInteger("num"));
        GuildDailyStaticsVo vo = guildDailyStaticsService.findStatics(userId);

        JSONObject result = new JSONObject();
        result.put("staticsList", statics);
        result.put("all", vo);
        result.put("rate", guildMember.getProfitRate());
        return result;
    }

    @ServiceMethod(code = "007", description = "发放佣金")
    public Object receive(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        GuildMember member = guildCacheService.getMemberByUserId(userId);
        if (member == null) {
            throwExp("您没有加入公会");
        }
        Long guildId = member.getGuildId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("玩家不存在");
        }
        Guild guild = guildCacheService.getGuildByGuildId(guildId);
        Long myId = appSocket.getWsidBean().getUserId();
        if (guild == null) {
            throwExp("公会不存在");
        }
        params.put("guildId", guild.getId());
        Long operatorUserId = appSocket.getWsidBean().getUserId();
        params.put("operatorUserId", operatorUserId);
        if (!guild.getUserId().toString().equals(myId.toString())) {
            throwExp("您无权操作");
        }
        GuildMember guildMember = guildMemberService.findByGuildIdAndUserId(guildId, userId);
        if (guildMember == null) {
            throwExp("该玩家不是您的公会成员");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018007", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "008", description = "查看发放佣金记录")
    public Object getLog(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("page"), params.get("num"));
        Long userId = appSocket.getWsidBean().getUserId();
        GuildMember member = guildCacheService.getMemberByUserId(userId);
        if (member == null) {
            throwExp("查询记录失败");
        }
        List<GuildGrantRecord> records;
        if (member.getRoleId() == 3) {
            records = guildGrantRecordService.findByOperatorUserId(userId, params.getInteger("page"), params.getInteger("num"));
        } else {
            records = guildGrantRecordService.findByUserId(userId, params.getInteger("page"), params.getInteger("num"));
        }
        return records;
    }

    @ServiceMethod(code = "009", description = "修改比例")
    public Object updateRate(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("rate"));
        Long myId = appSocket.getWsidBean().getUserId();
        Long userId = params.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        if (user.getRoleId() != 2) {
            throwExp("非公会成员不能修改");
        }
        GuildMember member = guildCacheService.getMemberByUserId(userId);
        if (member == null) {
            throwExp("查询公会成员信息失败");
        }
        Guild guild = guildCacheService.getGuildByGuildId(member.getGuildId());
        if (guild == null) {
            throwExp("查询公会信息失败");
        }
        if (!myId.toString().equals(guild.getUserId().toString())) {
            throwExp("您无权操作");

        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018009", params).build(), new RequestManagerListener(appCommand));
        return async();
    }



    @ServiceMethod(code = "010", description = "查询某个日期的")
    public Object getStaticsByYmd(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        GuildMember member = guildCacheService.getMemberByUserId(userId);
        if (member == null) {
            throwExp("查询记录失败");
        }
        Long guildId = member.getGuildId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("玩家不存在");
        }
        Guild guild = guildCacheService.getGuildByGuildId(guildId);
        Long myId = appSocket.getWsidBean().getUserId();
        if (guild == null) {
            throwExp("公会不存在");
        }
        if (!myId.toString().equals(userId.toString()) && !guild.getUserId().toString().equals(myId.toString())) {
            throwExp("您无权查看");
        }
        GuildMember guildMember = guildMemberService.findByGuildIdAndUserId(guildId, userId);
        if (guildMember == null) {
            throwExp("该玩家不是您的公会成员");
        }
        List<GuildDailyStatics> ymd = guildDailyStaticsService.findStaticsByYmd(userId, params.getString("ymd"));
        if (ymd==null){
            throwExp("未查询到数据");
        }
        return ymd;
    }


    @ServiceMethod(code = "011", description = "获取公会配置")
    public Object getGuildConfig(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("玩家不存在");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018011", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "012", description = "领取收益")
    public Object receiveSelf(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        GuildMember member = guildCacheService.getMemberByUserId(userId);
        if (member == null) {
            throwExp("查询记录失败");
        }
        Long guildId = member.getGuildId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("玩家不存在");
        }
        Guild guild = guildCacheService.getGuildByGuildId(guildId);

        if (guild == null) {
            throwExp("公会不存在");
        }
        params.put("guildId", guildId);
        params.put("operatorUserId", params.get("operatorUserId"));
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018007", params).build(), new RequestManagerListener(appCommand));
        return async();
    }


}
