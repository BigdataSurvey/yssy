package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Achievement;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ActivityService extends DaoService {

    public ActivityService() {
        super("ActivityMapper");
    }

    public List<Activity> findAllActivity() {
        return findAll();
    }


    public List<Activity> findActivityByTime(String date) {
        return findList("findByTime",  date);
    }
}
