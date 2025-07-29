package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.Activity2;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class Activity3Service extends DaoService {

    public Activity3Service() {
        super("Activity3Mapper");
    }

    public List<Activity2> findAllActivity2() {
        return findAll();
    }


    public Activity findActivity3ByTime() {
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
