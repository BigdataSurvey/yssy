package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.DicTimeTask;
import com.zywl.app.base.bean.card.DicTimeTaskGroup;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DicTimeTaskGroupService extends DaoService {

    public DicTimeTaskGroupService() {
        super("DicTimeTaskGroupMapper");
    }

    public List<DicTimeTaskGroup> findAllTimeTaskGroup() {
        return findAll();
    }

    public DicTimeTaskGroup findGroup() {
        Map<String, Object> map = new HashMap<>();
        map.put("nowTime", new Date());
        DicTimeTaskGroup dicTimeTaskGroup = (DicTimeTaskGroup) findOne("findByGroup",map);
        return dicTimeTaskGroup;
    }



}
