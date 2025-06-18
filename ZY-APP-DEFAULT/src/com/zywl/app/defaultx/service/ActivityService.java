package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Achievement;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService extends DaoService {

    public ActivityService() {
        super("ActivityMapper");
    }

    public List<Activity> findAllActivity() {
        return findAll();
    }


    public Activity findActivityByTime() {
        Map<String, Object> params = new HashMap<>();
        params.put("time", new Date());
        return (Activity) findOne("findByTime", params);
    }

    public Activity findById(Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return (Activity) findOne("findById",params);
    }
}
