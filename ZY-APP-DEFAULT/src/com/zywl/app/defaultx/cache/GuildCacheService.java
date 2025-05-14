package com.zywl.app.defaultx.cache;

import com.zywl.app.base.bean.Guild;
import com.zywl.app.base.bean.GuildMember;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.GuildMemberService;
import com.zywl.app.defaultx.service.GuildService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuildCacheService extends RedisService {



    @Autowired
    private GuildService guildService;

    @Autowired
    private GuildMemberService guildMemberService;

    public List<Guild> getGuilds(){
        String key = RedisKeyConstant.APP_GUILD;
        List<Guild> guilds = getList(key, Guild.class);
        if (guilds==null){
            guilds = guildService.findAllGuild();
            if (guilds!=null){
                set(key,guilds);
            }
        }
        return guilds;
    }

    public void removeGuilds(){
        String key = RedisKeyConstant.APP_GUILD;
        del(key);
    }

    public Guild getGuildByGuildId(Long guildId){
        List<Guild> guilds = getGuilds();
        for (Guild guild : guilds) {
            if (guildId==guild.getId()){
                return guild;
            }
        }
        removeGuilds();
        return guildService.findById(guildId);
    }

    public GuildMember getMemberByUserId(Long userId){
        String key = RedisKeyConstant.APP_GUILD_MEMBER+userId+"-";
        GuildMember member = get(key,GuildMember.class);
        if (member==null){
            member=guildMemberService.findByUserId(userId);
            if (member!=null){
                set(key,member,1440*60L*7);
            }
        }
        return member;
    }

    public void removeMember(Long userId){
        String key = RedisKeyConstant.APP_GUILD_MEMBER+userId+"-";
        del(key);
    }
}
