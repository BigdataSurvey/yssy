package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.UserRole;
import com.zywl.app.base.bean.card.UserMine;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserRoleService extends DaoService {




    public UserRoleService() {
        super("UserRoleMapper");
    }

    @Transactional
    public UserRole addUserRole(Long userId,Long roleId,int days,int index,int oneReward) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setEndTime(DateUtil.getDateByDay(days));
        userRole.setHp(100);
        userRole.setUnReceive(new JSONArray());
        userRole.setLastLookTime(new Date());
        userRole.setLastReceiveTime(new Date());
        userRole.setCreateTime(new Date());
        //0 未使用   1 使用中   -1已到期
        userRole.setStatus(0);
        save(userRole);
        return userRole;
    }


}