package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.DzCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DzPeriodsService extends DaoService {

    private static final Log logger = LogFactory.getLog(DzPeriodsService.class);

    public DzPeriodsService() {
        super("UserDzPeriodsMapper");
    }

    @Autowired
    private DzCacheService dzCacheService;

    @Autowired
    private DzService dzService;

    @Autowired
    private DzInfoService dzInfoService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserCacheService userCacheService;

    public UserDzPeriods findByPeriods(Integer periods) {
        if (isNull(periods)) {
            return null;
        }
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("periods", periods);
        return findOne(params);
    }

    //查找最新的一期汇总数据
    public UserDzPeriods findOne() {
        return findOne(null);
    }

    public UserDzPeriods findPeriodsNew() {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("periods", 1);
        List<UserDzPeriods> list = findList("findPeriodsNew", null);
        logger.info(list.get(0).getPeriods() + "====================================");
        return list.get(0);
    }

    //定时处理 昨日打坐游戏数据
    @Transactional
    public void yesTodayTask(List<UserDzRecord> userDzRecords) {
        int userDzNum = 0;
        int userDkNum = 0;
        if (null == userDzRecords) {
            //当期直接结束
            return;
        }
        //报名的灵石总数量
        BigDecimal userDzMoneyAll = BigDecimal.ZERO;
        //瓜分灵石得总数量
        BigDecimal userDzMoneyReturn = BigDecimal.ZERO;
        //打卡签到的人数总灵石数量
        BigDecimal dkMoney = BigDecimal.ZERO;
        //随机一个幸运儿
        long userId = getLuckPerson(userDzRecords);
        //幸运儿瓜分的灵石数量
        BigDecimal luckCuMoney = BigDecimal.ZERO;
        //待领取玩家集合
        List<String> lists = new ArrayList<>();
        //退回的灵石
        BigDecimal returnMoney = BigDecimal.ZERO;
        UserDzPeriods userDzPeriodsCache = dzCacheService.getDzInitInfo();
        int periods = userDzPeriodsCache.getPeriods()+1;
        List<UserDzRecordInfo> userDzRecordInfoList = new ArrayList<>();
        for (UserDzRecord userDzRecord : userDzRecords) {
            periods = userDzRecord.getPeriods();
            userDzNum = userDzNum + 1;
            userDkNum = userDzRecord.getStatus() == 1 ? userDkNum + 1 : userDkNum;
            userDzMoneyAll = userDzMoneyAll.add(userDzRecord.getDzMoney());
            userDzMoneyReturn = userDzRecord.getStatus() == 1 ? userDzMoneyReturn.add(userDzRecord.getDzMoney()) : userDzMoneyReturn;
            returnMoney = userDzRecord.getStatus() == 1 ? returnMoney : returnMoney.add(userDzRecord.getDzMoney());
            userDzRecord.setUpdateTime(new Date());
            userDzRecord.setReturnMoney(userDzRecord.getStatus() == 1 ? BigDecimal.ZERO : userDzRecord.getDzMoney());
            // 更新操作记录
            UserDzRecordInfo userDzRecordInfo = dzInfoService.selectList(userDzRecord.getUserId(), periods).get(0);
            userDzRecordInfo.setReturnMoney(userDzRecord.getReturnMoney());
            userDzRecordInfoList.add(userDzRecordInfo);
            if (userDzRecord.getUserId() == userId) {
                userDzRecord.setLuckUserStatus(2);
            }
            dkMoney = userDzRecord.getStatus() == 1 ? dkMoney : dkMoney.add(userDzRecord.getDzMoney());
        }
        luckCuMoney = userDzMoneyReturn.multiply(new BigDecimal("0.1"));
        if (userDzMoneyReturn.compareTo(BigDecimal.ZERO) == 0) {
            //都报名了
            luckCuMoney = BigDecimal.ZERO;
        }
        userDzMoneyReturn = userDzMoneyReturn.subtract(luckCuMoney);
        UserDzPeriods userDzPeriods = getUserDzPeriods(userId, periods, userDzNum, userDkNum, userDzMoneyAll, userDzMoneyReturn, luckCuMoney, returnMoney);
        save(userDzPeriods);
        //更新record表 瓜分的灵石
        //1.先补充表信息 有点问题
        for (UserDzRecord userDzRecord : userDzRecords) {
            if (userDzRecord.getStatus() != 1) {
                if (userDzRecord.getLuckUserStatus() == 2) {
                    userDzRecord.setCuMoney(luckCuMoney);
                } else {
                    userDzRecord.setCuMoney(userDzRecord.getDzMoney().divide(dkMoney, 6, BigDecimal.ROUND_DOWN).multiply(userDzMoneyReturn));
                }
                lists.add(String.valueOf(userDzRecord.getUserId()));
                // 更新操作记录
                UserDzRecordInfo userDzRecordInfo = dzInfoService.selectList(userDzRecord.getUserId(), periods).get(0);
                userDzRecordInfo.setCuMoney(userDzRecord.getCuMoney());
                userDzRecordInfoList.add(userDzRecordInfo);
            }
        }
        logger.info("----------------" + userDzRecords.isEmpty());
        if(!userDzRecords.isEmpty()){
            dzService.batchUpdateRecord(userDzRecords);
            for (int i = 0; i < userDzRecordInfoList.size(); i++) {
                dzInfoService.updateUsersDzInfo(userDzRecordInfoList.get(i));
            }
        }
        //更新redis中的信息,直接清除上一期所有数据即可 除了领取
        dzCacheService.removeDzInfo();
        //

        dzCacheService.setUserIdLqIntoCache(lists);
    }

    public long getLuckPerson(List<UserDzRecord> userDzRecords) {
        Config config = configService.getConfigByKey(Config.DZ_LUCK_USER);
        if(null!=config){
            String userId = config.getValue();
            //格式为日期+userId,例如：20200101:1910001
            //先判断库里有没有预定的userId ,有效期为一天
            if(!StringUtils.isEmpty(userId)){
                String[] a = userId.split(":");
                String nowDate =  DateUtil.format9(new Date());
                if(nowDate.equals(a[0])){
                    //说明幸运儿id今日有效，做一步校验，判断userId 是否在库里
                    User user = userCacheService.getUserInfoByUserNo(a[1]);
                    if(null!=user){
                        return Long.parseLong(a[1]);
                    }
                }
            }
        }
        List<Long> list = new ArrayList<>();
        for (UserDzRecord record : userDzRecords) {
            if (record.getStatus() == 2) {
                list.add(record.getUserId());
            }
        }
        long luckUserId = 0;
        if (list.isEmpty()) {
            //没人签到
            return -1;
        } else {
            Random random = new Random();
            luckUserId = random.nextInt(list.size());

        }
        return list.get(Integer.parseInt(luckUserId + ""));
    }

    public UserDzPeriods getUserDzPeriods(long UserId, Integer periods, int userDzNum, int userDkNum, BigDecimal userDzMoneyAll, BigDecimal userDzMoneyReturn
            , BigDecimal luckCuMoney, BigDecimal returnMoney) {
        UserDzPeriods userDzPeriods = new UserDzPeriods();
        userDzPeriods.setUserId(UserId);
        userDzPeriods.setPeriods(periods);
        userDzPeriods.setUserDZNum(userDzNum);
        userDzPeriods.setUserDkNum(userDkNum);
        userDzPeriods.setDzMoneyAll(userDzMoneyAll);
        userDzPeriods.setDzMoneyReturn(userDzMoneyReturn);
        userDzPeriods.setCuMoney(luckCuMoney);
        userDzPeriods.setReturnMoney(returnMoney);
        userDzPeriods.setCreateTime(new Date());
        userDzPeriods.setUpdateTime(new Date());
        return userDzPeriods;
    }


    //修复线上缓存
    public void updateOnlineRedisData() {

        //清除头像redis
        dzCacheService.del(RedisKeyConstant.APP_USER_DZ_BM_USERID);


//        List<UserDzRecord> userDzRecords = dzService.selectByStatusAndPeriods();
//        List<String> strLq = new ArrayList<>();
//
//        if (null != userDzRecords) {
//            for (UserDzRecord record : userDzRecords) {
//                strLq.add(String.valueOf(record.getUserId()));
//            }
//        }
//
//        dzCacheService.set("t:app:dz:lq:userId:now", strLq);


//        String bmKey = RedisKeyConstant.APP_USER_DZ_BM_USERID;
//        String lqKey = RedisKeyConstant.APP_USER_DZ_LQ_USERID;
//
//        String bmRedis = dzCacheService.get(bmKey);
//
//        String lqRedis = dzCacheService.get(lqKey);
//        if(!StringUtils.isEmpty(bmRedis)){
//            String[] bm  = bmRedis.split("DZ",0);
//            List<String> strBm = new ArrayList<>();
//            for (String userId : bm){
//                if(!StringUtils.isEmpty(userId)){
//                    strBm.add(userId);
//                }
//            }
//            dzCacheService.set("t:app:dz:bm:userId:now",strBm);
//        }


//        if(!StringUtils.isEmpty(lqRedis)){
//            String[] lq  = lqRedis.split("DZ",0);
//            List<String> strLq = new ArrayList<>();
//            for (String userId : lq){
//                if(!StringUtils.isEmpty(userId)){
//                    strLq.add(userId);
//                }
//            }


    }

    public static void main(String[] args) {

        String s = "20200101:1910001";
        String[] a = s.split(":");
        System.out.println(a[0]);
        System.out.println(a[1]);
    }


}
