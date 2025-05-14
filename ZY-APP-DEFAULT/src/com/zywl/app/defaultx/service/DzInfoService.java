package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserDzPeriods;
import com.zywl.app.base.bean.UserDzRecord;
import com.zywl.app.base.bean.UserDzRecordInfo;
import com.zywl.app.base.bean.vo.UserDzRecordVO;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.DzStatus;
import com.zywl.app.defaultx.enmus.LuckUserEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DzInfoService extends DaoService {

    public DzInfoService() {
        super("UserDzRecordInfoMapper");
    }

    // 玩家报名 打坐
    @Transactional
    public long addUserRecordInfo(Long userId, Integer periods, BigDecimal dzMoney) {
        UserDzRecordInfo userDzRecordInfo = new UserDzRecordInfo();
        userDzRecordInfo.setUserId(userId);
        userDzRecordInfo.setPeriods(periods);
        userDzRecordInfo.setDzMoney(dzMoney);
//        userDzRecordInfo.setCuMoney(cuMoney);
//        userDzRecordInfo.setReturnMoney(returnMoney);
        userDzRecordInfo.setCreateTime(new Date());
        save(userDzRecordInfo);
        return userDzRecordInfo.getId();
    }

    @Transactional
    public long updateUsersDzInfo(UserDzRecordInfo userDzRecordInfo) {
        return execute("update", userDzRecordInfo);
    }

    public List<UserDzRecordInfo> selectList(Long userId, Integer periods) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        params.put("periods", periods);
        return findList("selectList", params);
    }

}
