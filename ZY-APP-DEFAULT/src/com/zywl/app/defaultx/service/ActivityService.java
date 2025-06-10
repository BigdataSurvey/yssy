package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Achievement;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService extends DaoService {

    public ActivityService() {
        super("ActivityMapper");
    }

    public List<Activity> findAllActivity() {
        return findAll();
    }



}
