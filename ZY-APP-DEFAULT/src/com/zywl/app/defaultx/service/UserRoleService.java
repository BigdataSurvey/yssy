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
    public UserRole addUserRole(Long userId,Long roleId,int days) {
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRole.setEndTime(DateUtil.getDateByDay(days));
        userRole.setHp(100);
        userRole.setMaxHp(100);
        userRole.setUnReceive(new JSONArray());
        userRole.setLastLookTime(new Date());
        userRole.setLastReceiveTime(new Date());
        userRole.setCreateTime(new Date());
        //0 未使用   1 使用中   -1已到期
        userRole.setStatus(0);
        save(userRole);
        return userRole;
    }


    public UserRole findByUserIdAndRoleId(Long userId,int roleId){
        Map<String,Object> params = new HashMap<>();
        params.put("userId",userId);
        params.put("roleId",roleId);
        return (UserRole) findOne("findByUserIdAndRoleId",params);
    }


    public List<UserRole> findByUserId(Long userId){
        Map<String,Object> params = new HashMap<>();
        params.put("userId",userId);
        return findList("findByUserId",params);
    }

    public UserRole findByIndex(Long userId,int index){
        Map<String,Object> params = new HashMap<>();
        params.put("userId",userId);
        params.put("index",index);
        return (UserRole) findOne("findByIndex",params);
    }


    public UserRole findByUserRoleId(Long userRoleId){
        Map<String,Object> params = new HashMap<>();
        params.put("id",userRoleId);
        return (UserRole) findOne("findByUserRoleId",params);
    }

    public List<UserRole> findWorkingRoles(Long userId){
        Map<String,Object> params = new HashMap<>();
        params.put("userId",userId);
        return findList("findWorkingRoles",params);
    }

    public List<UserRole> findNoWorkingRolesByIndex(Long userId,int index){
        Map<String,Object> params = new HashMap<>();
        int skill ;
        if (index==1 || index==2){
            skill=1;
        } else if (index==3 || index==4) {
            skill=2;
        }else {
            skill=3;
        }
        params.put("userId",userId);
        params.put("skill",skill);
        return findList("findNoWorkingRolesByIndex",params);
    }


    @Transactional
    public void batchUpdateUserRole(List<UserRole> userRoles){
        if (userRoles.size()>0){
            execute("batchUpdateUserRole",userRoles);
        }
    }

    @Transactional
    public void updateUserRole(UserRole userRole){
        int updateUserRole = execute("updateUserRole", userRole);
        if (updateUserRole<1){
            throwExp("角色信息更新失败，请稍后重试");
        }
    }


}