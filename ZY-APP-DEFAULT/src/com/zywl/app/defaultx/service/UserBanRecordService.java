package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.UserBanRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserBanRecordService extends DaoService {

    public UserBanRecordService() {
        super("UserBanRecordMapper");
    }

    public void recordInfo(Long userId, String userNo, String userName, String mark, int type, String operator, long banTime) {
        UserBanRecord record = new UserBanRecord();
        record.setUserId(userId);
        record.setUserNo(userNo);
        record.setUserName(userName);
        record.setMark(mark);
        record.setType(type);
        record.setOperator(operator);
        record.setBanTime(banTime);
        record.setRecordTime(new Date());
        save(record);
    }
}
