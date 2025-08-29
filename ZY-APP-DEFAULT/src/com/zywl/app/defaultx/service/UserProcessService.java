package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.UserPower;
import com.zywl.app.base.bean.UserProcess;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserProcessService extends DaoService {


    public UserProcessService() {
        super("UserProcessMapper");
    }

    public List<UserProcess> findByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return  findList("findByUserId", params);

    }

    public Integer addUserProecss(JSONObject params) {
        UserProcess userProcess = new UserProcess();
        userProcess.setUserId( params.getInteger("userId"));
        userProcess.setCurrProcessNumber(0);
        userProcess.setHighNum(0);
        return save(userProcess);
    }
}
