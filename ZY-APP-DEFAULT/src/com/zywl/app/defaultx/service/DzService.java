package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserDzPeriods;
import com.zywl.app.base.bean.UserDzRecord;
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
public class DzService extends DaoService {

    public DzService() {
        super("UserDzRecordMapper");
    }

    // 玩家报名 打坐
    @Transactional
    public long addUserRecord(Long userId,BigDecimal dzMoney ,Integer periods) {
        //初始化信息
        UserDzRecord userDzRecord = new UserDzRecord();
        userDzRecord.setUserId(userId);
        userDzRecord.setDzMoney(dzMoney);
        userDzRecord.setStatus(DzStatus.BM_STATUS.getValue());
        userDzRecord.setLuckUserStatus(LuckUserEnum.LUCK_USER_NO_STATUS.getValue());
        userDzRecord.setPeriods(periods);
        userDzRecord.setCreateTime(new Date());
        save(userDzRecord);
        return userDzRecord.getId();
    }

    @Transactional
    public long updateUsersDzMoney(UserDzRecord userDzRecord){
        return execute("updateUsersDzMoney",userDzRecord);
    }

    public UserDzRecord findByUserIdAndPeriods(Long userId,Integer periods) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        params.put("periods", periods);
        return findOne(params);
    }

    public List<UserDzRecord> getUsersDz16(){
        List<UserDzRecord> userDzRecords = findList("selectPeriods16Data",null);
        return userDzRecords;
    }

    @Transactional
    public int batchUpdateRecord(List<UserDzRecord> userDzRecords){
        int a = execute("batchUpdateRecord",userDzRecords);
        if(a<1){
            throwExp("更新失败");
        }
        return a;
    }

    public UserDzRecord selectByUserIdAndStatus(Long userId,Integer status,Integer periods){
        UserDzRecord userDzRecord;
        synchronized (LockUtil.getlock(userId+"")) {
            Map<String, Object> params = new HashedMap<>();
            params.put("userId", userId);
            params.put("status",status);
            params.put("periods",periods);
            userDzRecord = (UserDzRecord)findOne("selectByUserIdAndStatus",params);
        }
        return userDzRecord;
    }

    @Transactional
    public int updateStatusTo3(UserDzRecord userDzRecord){
        int a = execute("updateStatusTo3",userDzRecord);
        if(a<1){
            throwExp("更新失败");
        }
        return a;
    }

    public Map<String,Object> selectPeriodsSum(Integer periods){
        List<Map<String,Object>> maps = findList("selectPeriodsSum",periods);
        return maps.get(0);
    }

    public List<UserDzRecord> findAllRecord(UserDzPeriods userDzPeriods){
        //查出当期期数，这个期数依据periods表中为准
        int periods = userDzPeriods.getPeriods();
        List<UserDzRecord> userDzRecords = findList("selectYesTodayAllData",periods+1);
        return userDzRecords;
    }

    @Transactional
    public int updateByUserIdAndPeriods(Long userId,Integer periods){
        UserDzRecord userDzRecord = new UserDzRecord();
        userDzRecord.setUserId(userId);
        userDzRecord.setPeriods(periods);
        userDzRecord.setStatus(DzStatus.DK_STATUS.getValue());
        userDzRecord.setClockTime(new Date());
        int a = execute("updateUserInfo",userDzRecord);
        if(a<1){
            throwExp("签到失败");
        }
        return a;
    }

    public UserDzRecord selectByUserIdAndPeriods(Long userId,Integer periods){
        UserDzRecord userDzRecord = new UserDzRecord();
        userDzRecord.setUserId(userId);
        userDzRecord.setPeriods(periods);
        return (UserDzRecord)findOne("selectUserRecode",userDzRecord);
    }

    public List<UserDzRecordVO> queryOrderByPeriods(Map<String, Object> par) {
        return findList("queryOrderByPeriods", par);
    }

    public UserDzRecordVO queryOrderByUserId(Map<String, Object> par) {
        return (UserDzRecordVO) findOne("queryOrderByUserId", par);
    }

    public List<UserDzRecord> selectByStatusAndPeriods(){
        return findList("selectByStatusAndPeriods",null);
    }

    public List<UserDzRecord> selectByStatusUserId(Long userId){
        Map<String, Object> params = new HashedMap<>();
        params.put("userId",userId);
         return findList("selectByStatusUserId",params);
    }


    public List<UserDzRecord> selectNoDkUserIds(int status,int periods){
        Map<String, Object> params = new HashedMap<>();
        params.put("status",status);
        params.put("periods",periods);
        return findList("selectNoDkUserIds",params);
    }

}
