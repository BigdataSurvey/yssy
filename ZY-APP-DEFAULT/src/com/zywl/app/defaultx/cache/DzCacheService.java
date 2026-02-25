package com.zywl.app.defaultx.cache;

import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserDzPeriods;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.DzPeriodsService;
import com.zywl.app.defaultx.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DzCacheService extends RedisService {

    @Autowired
    private DzPeriodsService dzPeriodsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCacheService userCacheService;

    //缓存数据，上一期的期数详情，有效期为24小时，因为这数据不会变，所以留存即可，定时的时候 会清理这部分数据
    public UserDzPeriods getDzInitInfo(){
            //查数据库，将上一期的数据放入其中
            UserDzPeriods userDzPeriods = dzPeriodsService.findOne();
            if(null!= userDzPeriods){
                //同时，根据幸运儿userId 查出其名称，头像信息
                User user = userCacheService.getUserInfoById(userDzPeriods.getUserId());
                if(null!=user){
                    userDzPeriods.setUserName(user.getName());
                    userDzPeriods.setUserImage(user.getHeadImageUrl());
                }
            }else {
                //第一期
                userDzPeriods = new UserDzPeriods();
                userDzPeriods.setPeriods(0);
                userDzPeriods.setUserImage("");
                userDzPeriods.setUserId(0L);
                userDzPeriods.setUserName("");
                userDzPeriods.setCuMoney(BigDecimal.ZERO);
                userDzPeriods.setUserDkNum(0);
                userDzPeriods.setUserDZNum(0);
            }

        return userDzPeriods;
    }
    //玩家是否待领取
    public Boolean getUserLqInfo(String userId){
        String key = RedisKeyConstant.APP_USER_DZ_LQ_USERID_NOW;
        List<String> userIdsRedis = getList(key,String.class);
        if(null == userIdsRedis){
            return false;
        }
        return userIdsRedis.contains(userId);
    }
    //查看玩家是否打卡
    public Boolean getUserDkInfo(long userId){
        String key = RedisKeyConstant.APP_USER_DZ_DK_USERID;
        List<String> lists = getList(key,String.class);
        if(null == lists){
            return false;
        }
        return lists.contains(String.valueOf(userId));
//        String userIdsRedis = get(key);
//        if(StringUtils.isEmpty(userIdsRedis)){
//            return false;
//        }
//        return userIdsRedis.contains(userId+"");
    }
    //玩家是否报名
    public Boolean getUserBmInfo(long userId){
        String key = RedisKeyConstant.APP_USER_DZ_BM_USERID_NOW;
        List<String> userIdsRedis = getList(key,String.class);
        if(null == userIdsRedis){
            return false;
        }
        return userIdsRedis.contains(userId+"");
    }
    public List<String> getUserImageUrl(){
        List<String> userIds = getList(RedisKeyConstant.APP_USER_DZ_BM_USERID_NOW,String.class);
        List<String> imageUrls = new ArrayList<>();
        if(null == userIds){
            return imageUrls;
        }
        //展示最新的10个人的头像 ，所以不用缓存了，直接找出来返回去
            if(userIds.size()>10){
                for(int i = userIds.size();i>(userIds.size()-10);i--){
                    User user = userCacheService.getUserInfoById(Long.parseLong(userIds.get(i-1)));
                    if(null != user){
                        imageUrls.add(user.getHeadImageUrl());
                    }
                }
            }else {
                for(String userImage : userIds) {
                    User user = userCacheService.getUserInfoById(Long.parseLong(userImage));
                    if(null!=user){
                        imageUrls.add(user.getHeadImageUrl());
                    }
                }
            }
        return imageUrls;
    }
    //取
    public String getUsersMoney(){
        String key =  RedisKeyConstant.APP_USER_DZ_MONEY;
        return get(key);
    }

    public String getUsersNum(){
        String key = RedisKeyConstant.APP_USER_DZ_PERIODS_NUM;
        return get(key);
    }


    //打卡过的userId ,丢入数据库，有效期为24小时 ，这个跑批的时候，会定时清理redis的数据
    public Boolean setUserIdDkIntoCache(long userId){
        String key = RedisKeyConstant.APP_USER_DZ_DK_USERID;
        List<String> lists  = getList(key,String.class);
        if(null == lists){
            lists = new ArrayList<>();
        }
        lists.add(String.valueOf(userId));
        return set(RedisKeyConstant.APP_USER_DZ_DK_USERID,lists);
    }

    //报名过的userId ,丢入数据库
    public Boolean setUserIdBmIntoCache(String userId){
        String key = RedisKeyConstant.APP_USER_DZ_BM_USERID_NOW;
        List<String> userIdsRedis = getList(key,String.class);
        int num = 0;
        String usersNum = get(RedisKeyConstant.APP_USER_DZ_PERIODS_NUM);
        if(null == userIdsRedis){
            userIdsRedis =new ArrayList<>();
        }else if(userIdsRedis.contains(userId)){
            return true;
        }
        userIdsRedis.add(userId);
        if(StringUtils.isEmpty(usersNum)){
            num = 1;
        }else {
            num = Integer.parseInt(usersNum) +1;
        }
        set(RedisKeyConstant.APP_USER_DZ_BM_USERID_NOW,userIdsRedis);
        stringSet(RedisKeyConstant.APP_USER_DZ_PERIODS_NUM,num);
        return true;
    }

    //待领取玩家集合
    public Boolean setUserIdLqIntoCache(List<String> nowSet){
        String key = RedisKeyConstant.APP_USER_DZ_LQ_USERID_NOW;
        List<String>  userIdRedis =  getList(key,String.class);
        if(null == userIdRedis){
            userIdRedis = nowSet;
        }else {
            userIdRedis.addAll(nowSet);
        }

        return set(RedisKeyConstant.APP_USER_DZ_LQ_USERID_NOW,userIdRedis);
    }
    //当期打坐用户灵石总数量
    public boolean setUsersMoney(BigDecimal money){
        String key =  RedisKeyConstant.APP_USER_DZ_MONEY;
        String monyes = get(key);
        if(StringUtils.isEmpty(monyes)){
            return stringSet(RedisKeyConstant.APP_USER_DZ_MONEY,money);
        }
        return stringSet(RedisKeyConstant.APP_USER_DZ_MONEY,new BigDecimal(monyes).add(money));
    }

    //清楚打坐上期游戏
    public void removeDzInfo(){
        String key = RedisKeyConstant.APP_USER_DZ_BEAN + "DzCacheBean";
        del(key);
        del(RedisKeyConstant.APP_USER_DZ_DK_USERID);
        del(RedisKeyConstant.APP_USER_DZ_BM_USERID_NOW);
        del(RedisKeyConstant.APP_USER_DZ_MONEY);
        del(RedisKeyConstant.APP_USER_DZ_PERIODS_NUM);
        for (int i = 0; i < 10; i++) {
            del(RedisKeyConstant.APP_USER_DZ_BM_IMAGE+i);
        }
    }
    //清除已经领取过的userID
    public void removeLqUserId(String userId){
        String key = RedisKeyConstant.APP_USER_DZ_LQ_USERID_NOW;
        List<String> userIds = getList(key,String.class);
        if(null!=userIds){
            //这里校验
            userIds.remove(userId);
            set(RedisKeyConstant.APP_USER_DZ_LQ_USERID_NOW,userIds);
        }

    }

}
