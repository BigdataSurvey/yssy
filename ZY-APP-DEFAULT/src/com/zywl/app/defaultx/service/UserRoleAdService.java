package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.UserRoleAd;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserRoleAdService extends DaoService {

    public UserRoleAdService() {
        super("UserRoleAdMapper");
    }


    @Transactional
    public UserRoleAd addUserRoleAd(Long userId, int canLook, Date lastTime) {
        UserRoleAd userRoleAd = new UserRoleAd();
        userRoleAd.setYmd(DateUtil.getCurrent9());
        userRoleAd.setUserId(userId);
        userRoleAd.setCanLook(canLook);
        userRoleAd.setLook(0);
        userRoleAd.setLastTime(lastTime);
        save(userRoleAd);
        return userRoleAd;
    }


    public UserRoleAd  findByUserIdAndYmd(Long userId) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("ymd",DateUtil.getCurrent9());
        List<UserRoleAd> byConditions = findByConditions(map);
        UserRoleAd userRoleAd ;
        if (byConditions==null || byConditions.size()==0){
            userRoleAd = addUserRoleAd(userId,0,DateUtil.getToDayDateBegin());
        }else{
            userRoleAd = byConditions.get(0);
        }
        return userRoleAd;
    }

    public List<UserRoleAd> findByUserId(Long userId) {
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        return findByConditions( map);
    }

}
