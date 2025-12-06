package com.zywl.app.defaultx.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.util.LockUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.Backpack;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.bean.vo.UserCapitalVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.UserCapitalService;

@Service
public class UserCapitalCacheService extends RedisService {

    private static final Log logger = LogFactory.getLog(UserCapitalCacheService.class);

    private static final Object lock = new Object();

    @Autowired
    private UserCapitalService userCapitalService;

    public static Map<String, UserCapital> userCapitals = new ConcurrentHashMap<String, UserCapital>();

    public static JSONObject data = new JSONObject();

    @PostConstruct
    public void _construct() {
        reloadCache();
    }

    public void reloadCache() {
        logger.info("删除用户资产缓存");
        del(RedisKeyConstant.APP_USER_CAPITAL + "*");
        logger.info("删除用户资产缓存结束");
    }

    public static String getKey(Long userId, int capitalType) {
        String key = userId.toString() + "-" + capitalType;
        return key;
    }


    //得到用户所有资产
    public List<UserCapitalVo> getAllUserCapitalCache(Long userId) {
        //拿到所有资产类型
        UserCapitalTypeEnum[] es = UserCapitalTypeEnum.values();
        List<UserCapitalVo> result = new ArrayList<UserCapitalVo>();
        for (UserCapitalTypeEnum userCapitalTypeEnum : es) {
            //拿每一个资产类型去看用户有没有
            UserCapital userCapital = getUserCapitalCacheByType(userId, userCapitalTypeEnum.getValue());
            if (userCapital != null) {
                UserCapitalVo vo = new UserCapitalVo();
                BeanUtils.copy(userCapital, vo);
                result.add(vo);
            }
        }
        return result;
    }

    /**
     * 按照用户、资产类型获取用户资产缓存；如果缓存没有再走DB;DB也没有就自动插入一条0越的记录之后再返回;顺便把成功获取的结果写到缓存,,
     * **/
    public UserCapital getUserCapitalCacheByType(Long userId, int capitalType) {
        UserCapitalTypeEnum e = getCapitalEnum(capitalType);
        if (e == null) {
            return null;
        }
        String key = getKey(userId, capitalType);
        UserCapital userCapital = null;
        userCapital = userCapitals.get(key);
        if (userCapital == null) {
            userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalType);
            if (userCapital != null) {
                userCapitals.put(key, userCapital);
                //需要同步
                data.clear();
                data.put("userCapitals", userCapitals);
            }
        }
        if (userCapital == null) {
            UserCapital userCapital2 = new UserCapital();
            userCapital2.setUserId(userId);
            userCapital2.setCapitalType(e.getValue());
            userCapital2.setBalance(BigDecimal.ZERO);
            userCapital2.setOccupyBalance(BigDecimal.ZERO);
            userCapitalService.insertUserCapital(userCapital2);
            userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalType);
        }
        return userCapital == null ? new UserCapital() : userCapital;
    }


    //删除用户资产缓存
    public void deltedUserCapitalCache(Long userId, Integer capitalType) {
        synchronized (LockUtil.getlock(userId.toString())) {
            String key = getKey(userId, capitalType);
            UserCapitalTypeEnum em = getCapitalEnum(capitalType);
            if (em != null && userCapitals.containsKey(key)) {
                userCapitals.remove(key);
            }
            data.clear();
            data.put("userCapitals", userCapitals);
        }
    }

    public void add(Long userId, int capitalType, BigDecimal amount, BigDecimal occupyAmount) {
        synchronized (LockUtil.getlock(userId.toString() + "money")) {
            String key = getKey(userId, capitalType);
            if (!userCapitals.containsKey(key)) {
                UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalType);
                if (userCapital != null) {
                    userCapitals.put(key, userCapital);
                }
            }
            userCapitals.get(key).setBalance(userCapitals.get(key).getBalance().add(amount));
            userCapitals.get(key).setOccupyBalance(userCapitals.get(key).getOccupyBalance().add(occupyAmount));
            data.clear();
            data.put("userCapitals", userCapitals);
        }
    }

    //在内存缓存里把用户的资产的balance / occupyBalance 做减法更新
    public void sub(Long userId, int capitalType, BigDecimal amount, BigDecimal occupyAmonut) {
        synchronized (LockUtil.getlock(userId.toString() + "money")) {
            String key = getKey(userId, capitalType);
            if (!userCapitals.containsKey(key)) {
                UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalType);
                if (userCapital != null) {
                    userCapitals.put(key, userCapital);
                }
            }
            userCapitals.get(key).setBalance(userCapitals.get(key).getBalance().subtract(amount));
            userCapitals.get(key).setOccupyBalance(userCapitals.get(key).getOccupyBalance().subtract(occupyAmonut));
            data.clear();
            data.put("userCapitals", userCapitals);
        }
    }
    public void subGift(Long userId, int capitalType, long amount, long occupyAmonut,BigDecimal price) {
        synchronized (LockUtil.getlock(userId.toString() + "money")) {
            String key = getKey(userId, capitalType);
            if (!userCapitals.containsKey(key)) {
                UserCapital userCapital = userCapitalService.findUserCapitalByUserIdAndCapitalType(userId, capitalType);
                if (userCapital != null) {
                    userCapitals.put(key, userCapital);
                }
            }
            userCapitals.get(key).setBalance(userCapitals.get(key).getBalance().subtract(price));
            data.clear();
            data.put("userCapitals", userCapitals);
        }
    }

    //删除全部资产
    public void deletedUserAllCapitalCache(Long userId) {
        UserCapitalTypeEnum[] enums = UserCapitalTypeEnum.values();
        for (UserCapitalTypeEnum userCapitalTypeEnum : enums) {
            deltedUserCapitalCache(userId, userCapitalTypeEnum.getValue());
        }
        data.clear();
        data.put("userCapitals", userCapitals);
    }

    //通过资产类型获取资产枚举
    public UserCapitalTypeEnum getCapitalEnum(int value) {
        UserCapitalTypeEnum[] enums = UserCapitalTypeEnum.values();
        for (UserCapitalTypeEnum e : enums) {
            if (e.getValue() == value) {
                return e;
            }
        }
        return null;
    }

    public void batchRemoveCache(Set<String> userIds, int capitalType) {
        for (String userId : userIds) {
            deltedUserCapitalCache(Long.parseLong(userId), capitalType);
        }
        data.clear();
        data.put("userCapitals", userCapitals);
    }

}
