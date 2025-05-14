package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserPower;
import com.zywl.app.base.bean.vo.AnimaTopVo;
import com.zywl.app.base.bean.vo.UserPowerVo;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserPowerService extends DaoService {

    @Autowired
    private UserCacheService userCacheService;
    public UserPowerService() {
        super("UserPowerMapper");
    }


    @Transactional
    public void insertUserPower(Long userId, Long power) {
        UserPower userPower = new UserPower();
        userPower.setUserId(userId);
        userPower.setPower(power);
        save(userPower);
    }

    public List<UserPower> findAllHasPower() {
        return findList("findAllHasPower",null);
    }


    public UserPower findByUserId(String userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return (UserPower) findOne("findByUserId", params);
    }

    @Transactional
    public void addPower(String userId, Long power) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", Long.parseLong(userId));
        params.put("power", power);
        execute("addPower", params);
        userCacheService.removeUserPower(userId);
    }

    public void updateOrSavePower(String userId,Long power){
        Map<String, Object> params = new HashMap<>();
        params.put("userId", Long.parseLong(userId));
        params.put("power", power);
        execute("updateOrSavePower", params);
        userCacheService.removeUserPower(userId);
        userCacheService.insertUserPowerCache(userId, power);

    }

    public List<UserPowerVo> findTop(){
        return findList("findTop",null);
    }
}
