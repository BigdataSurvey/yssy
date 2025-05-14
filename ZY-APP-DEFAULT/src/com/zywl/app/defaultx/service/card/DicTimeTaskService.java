package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.DicTimeTask;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicTimeTaskService extends DaoService {

    public DicTimeTaskService() {
        super("DicTimeTaskMapper");
    }

    public List<DicTimeTask> findAllTimeTask() {
        return findAll();
    }

    public List<DicTimeTask> findByGroup(int group) {
        Map<String, Object> map = new HashMap<>();
        map.put("group", group);
        return findList("findByGroup", map);
    }


}
