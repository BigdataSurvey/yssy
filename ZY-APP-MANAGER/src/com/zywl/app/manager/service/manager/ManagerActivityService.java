package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Activity;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserAchievement;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.ActivityService;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@ServiceClass(code = MessageCodeContext.Activity)
public class ManagerActivityService  extends BaseService {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private CashRecordService cashRecordService;


    @Transactional
    @ServiceMethod(code = "001", description = "获取活动信息")
    public JSONObject getAchievementInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        synchronized (LockUtil.getlock(userId)){
            JSONObject result = new JSONObject();
            List<Activity> activityList = activityService.findAllActivity();
            result.put("activityList", activityList);
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "002", description = "获取榜单信息")
    public JSONObject getTopListInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
            JSONObject result = new JSONObject();
            redisTemplate.opsForValue().get(RedisKeyConstant.APP_TOP_lIST+  DateUtil.format2(new Date()));
            List<JSONObject> array = JSONArray.parseArray(redisService.get(RedisKeyConstant.APP_TOP_lIST+  DateUtil.format2(new Date())), JSONObject.class);
            for (JSONObject jsonObject : array) {
                String orderNo = OrderUtil.getOrder5Number();
                Double point = (Double) jsonObject.get("point");
                String openId = jsonObject.getString("openId");
                Long userId = jsonObject.getLong("userId");
                String userNo = jsonObject.getString("userNo");
                String userName =  jsonObject.getString("userName");
                String realName =  jsonObject.getString("realName");
                String tel =  jsonObject.getString("tel");
                Double score = redisTemplate.opsForZSet().score(redisService.get(RedisKeyConstant.APP_TOP_lIST + DateUtil.format2(new Date())), userId);
                BigDecimal  RawrdsAmont ;
                if(score==1){
                    RawrdsAmont =  BigDecimal.valueOf(point).multiply(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(10));
                }else if(score==2){
                    RawrdsAmont =  BigDecimal.valueOf(point).multiply(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(5));
                }else{
                    RawrdsAmont = BigDecimal.valueOf(point).multiply(BigDecimal.valueOf(50));
                }
                cashRecordService.addCashOrder(openId, userId, userNo, userName, realName, orderNo,RawrdsAmont , 2,tel );
                if(point<3){
                    array.remove(jsonObject);
                }
            }
            result.put("array",array);
            return result;
        }
    @Transactional
    @ServiceMethod(code = "003", description = "获取领奖记录")
    public JSONObject getRawrdsRecord(ManagerSocketServer adminSocketServer, JSONObject data) {
        JSONObject result = new JSONObject();
        checkNull(data);
        checkNull(data.get("page"), data.get("num"));
        Long userId = data.getLong("userId");
        List<CashRecord> cashRecord= cashRecordService.findCashRecordByUserId(userId, data.getIntValue("page"), data.getIntValue("num"));
        result.put("cashRecord",cashRecord);
        return result;
    }
    @Transactional
    @ServiceMethod(code = "004", description = "上期榜单")
    public JSONObject getLastTopList(ManagerSocketServer adminSocketServer, JSONObject data) {
        JSONObject result = new JSONObject();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -1); // 减去一天
        Date yesterday = calendar.getTime();
        redisTemplate.opsForValue().get(RedisKeyConstant.APP_TOP_lIST+yesterday);
        List<JSONObject> array = JSONArray.parseArray(redisService.get(RedisKeyConstant.APP_TOP_lIST+ yesterday), JSONObject.class);
        //todo 红包金额 积分*50 如果第一名或者第二名会有额外的红包奖励
        for (JSONObject jsonObject : array) {
            String orderNo = OrderUtil.getOrder5Number();
            Double point = (Double) jsonObject.get("point");
            String openId = jsonObject.getString("openId");
            Long userId = jsonObject.getLong("userId");
            String userNo = jsonObject.getString("userNo");
            String userName =  jsonObject.getString("userName");
            String realName =  jsonObject.getString("realName");
            String tel =  jsonObject.getString("tel");
            Double score = redisTemplate.opsForZSet().score(redisService.get(RedisKeyConstant.APP_TOP_lIST + DateUtil.format2(new Date())), userId);
            BigDecimal  RawrdsAmont ;
            if(score==1){
                RawrdsAmont =  BigDecimal.valueOf(point).multiply(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(10));
            }else if(score==2){
                RawrdsAmont =  BigDecimal.valueOf(point).multiply(BigDecimal.valueOf(50)).add(BigDecimal.valueOf(5));
            }else{
                RawrdsAmont = BigDecimal.valueOf(point).multiply(BigDecimal.valueOf(50));
            }
            cashRecordService.addCashOrder(openId, userId, userNo, userName, realName, orderNo,RawrdsAmont , 2,tel);
            if(point<3){
                array.remove(jsonObject);
            }
        }
        result.put("array",array);
        return result;
    }
    @Transactional
    @ServiceMethod(code = "005", description = "获取领奖记录")
    public JSONObject judgeBind(ManagerSocketServer adminSocketServer, JSONObject data) {
        JSONObject result = new JSONObject();
        checkNull(data);
        Long userId = data.getLong("userId");
        User user = userService.findById(userId);
        if(null == user.getAlipayId()){
            throwExp("您没有绑定提现方式，请绑定后重试");
        }
        return result;
    }




}
