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

    @ServiceMethod(code = "004", description = "添加公会成员")
    public Object addGuildMember(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userNo"));
        String userNo = params.getString("userNo");
        User user = userCacheService.getUserInfoByUserNo(userNo);
        params.put("userId", user.getId());
        Long createUserId = appSocket.getWsidBean().getUserId();
        GuildMember member = guildCacheService.getMemberByUserId(createUserId);
        if (member == null) {
            throwExp("您没有加入小队");
        }
        Long guildId = member.getGuildId();
        params.put("guildId", guildId);
        params.put("createUserId", createUserId);
        if (createUserId.toString().equals(params.getString("userId"))) {
            throwExp("您已是小队成员");
        }
        if (user.getRoleId() != 1) {
            throwExp("该玩家已是小队成员");
        }
        Guild guild = guildCacheService.getGuildByGuildId(guildId);
        if (!guild.getUserId().toString().equals(createUserId.toString())) {
            throwExp("您没有权限");
        }
        User my = userCacheService.getUserInfoById(createUserId);
        if (my == null) {
            throwExp("查询玩家信息失败");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018004", params).build(), new RequestManagerListener(appCommand));
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
        if (!myId.toString().equals(user.getId().toString()) && !guild.getUserId().toString().equals(myId.toString())) {
            throwExp("您无权查看");
        }
        GuildMember guildMember = guildMemberService.findByGuildIdAndUserId(guildId, userId);
        if (guildMember == null) {
            throwExp("该玩家不是您的公会成员");
        }
        List<GuildDailyStatics> statics = guildDailyStaticsService.findByUserId(userId, params.getInteger("page"), params.getInteger("num"));
        GuildDailyStaticsVo vo = guildDailyStaticsService.findStatics(userId);
        JSONObject result = new JSONObject();
        result.put("staticsList", statics);
        result.put("all", vo);
        result.put("rate", member.getProfitRate());
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
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("玩家不存在");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("018007", params).build(), new RequestManagerListener(appCommand));
        return async();
    }


}
