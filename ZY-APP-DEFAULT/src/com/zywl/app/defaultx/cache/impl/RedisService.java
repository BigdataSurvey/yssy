
package com.zywl.app.defaultx.cache.impl;


import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.service.BaseService;

@Component
public class RedisService extends BaseService {

    //锁名称
    public static final String LOCK_PREFIX = "redis_lock";
    //加锁失效时间，毫秒
    public static final int LOCK_EXPIRE = 300; // ms
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private String service;

    @PostConstruct
    public void _construct() {
        redisTemplate.setValueSerializer(new StringRedisSerializer());
    }

    public RedisService(){
        this.service = getClass().getName();
    }


    public boolean lock(String key){
        String lock = LOCK_PREFIX + key;
        // 利用lambda表达式
        return (Boolean) redisTemplate.execute((RedisCallback) connection -> {

            long expireAt = System.currentTimeMillis() + LOCK_EXPIRE + 1;
            Boolean acquire = connection.setNX(lock.getBytes(), String.valueOf(expireAt).getBytes());


            if (acquire) {
                return true;
            } else {

                byte[] value = connection.get(lock.getBytes());

                if (Objects.nonNull(value) && value.length > 0) {

                    long expireTime = Long.parseLong(new String(value));
                    // 如果锁已经过期
                    if (expireTime < System.currentTimeMillis()) {
                        // 重新加锁，防止死锁
                        byte[] oldValue = connection.getSet(lock.getBytes(), String.valueOf(System.currentTimeMillis() + LOCK_EXPIRE + 1).getBytes());
                        return Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
                    }
                }
            }
            return false;
        });
    }

    /**
     * 删除锁
     *
     * @param key
     */
    public void deleteLock(String key) {
        redisTemplate.delete(key);
    }



    public boolean stringSet(String key, Object value) {
        boolean var4;
        try {
            this.stringRedisTemplate.opsForValue().set(key, value.toString());
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }


    public long getExpire(String key) {
        return this.redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    public boolean hasKey(String key) {
        boolean var3;
        try {
            boolean var2 = this.redisTemplate.hasKey(key);
            return var2;
        } catch (Exception var7) {
            logger.error("", var7);
            var3 = false;
        } finally {
            this.close();
        }

        return var3;
    }

    public void del(String... key) {
        try {
            if (key != null && key.length > 0) {
                if (key.length == 1) {
                    this.redisTemplate.delete(key[0]);
                } else {
                    this.redisTemplate.delete(CollectionUtils.arrayToList(key));
                }
            }
        } catch (Exception var6) {
            logger.error("", var6);
        } finally {
            this.close();
        }

    }



    public <T> T get(String key) {
        Object var3;
        try {
            Object var2 = key == null ? null :  this.redisTemplate.opsForValue().get(key);
            return (T)var2;
        } catch (Exception var7) {
            logger.error("", var7);
            var3 = null;
        } finally {
            this.close();
        }

        return (T)var3;
    }


    public <T> T get(String key,Class<T> class1) {
        Object var3;
        try {
            Object var2 = key == null ? null :  this.redisTemplate.opsForValue().get(key);
            return (T)JSONObject.parseObject(get(key), class1);
        } catch (Exception var7) {
            logger.error("", var7);
            var3 = null;
        } finally {
            this.close();
        }

        return null;
    }

    public <T> List  getList(String key,Class<T> class1) {
        Object var3;
        try {
            Object var2 = key == null ? null :  this.redisTemplate.opsForValue().get(key);
            return JSONArray.parseArray(get(key), class1);
        } catch (Exception var7) {
            logger.error("", var7);
            var3 = null;
        } finally {
            this.close();
        }

        return null;
    }

    public boolean setNumber(String key, Object value) {
        boolean var4;
        try {
            this.redisTemplate.opsForValue().set(key,value);
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }
    public boolean setNumber(String key, Object value, long time) {
        boolean var4;
        try {
            this.redisTemplate.opsForValue().set(key, JSON.toJSONString(value), time, TimeUnit.SECONDS);
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }

    public boolean set(String key, Object value) {
        boolean var4;
        try {
            String strValue = JSON.toJSONString(value);
            this.redisTemplate.opsForValue().set(key,strValue  );
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }

    public boolean set(String key, Object value, long time) {
        boolean var6;
        try {
            if (time > 0L) {
                String strValue = JSON.toJSONString(value);
                this.redisTemplate.opsForValue().set(key, strValue, time, TimeUnit.SECONDS);
            } else {
                this.set(key, value);
            }

            boolean var5 = true;
            return var5;
        } catch (Exception var10) {
            logger.error("", var10);
            var6 = false;
        } finally {
            this.close();
        }

        return var6;
    }

    public long incr(String key, Long delta) {
        long var5;
        try {
            if (delta < 0L) {
                throw new RuntimeException("递增因子必须大于0");
            }
            long var4 = this.redisTemplate.opsForValue().increment(key, delta);
            return var4;
        } catch (Exception var10) {
            logger.error("", var10);
            var5 = 0L;
        } finally {
            this.close();
        }

        return var5;
    }

    public double incrDouble(String key, Double delta) {
        double var5;
        try {
            if (delta < 0L) {
                throw new RuntimeException("递增因子必须大于0");
            }
            double var4 = this.redisTemplate.opsForValue().increment(key, delta);
            return var4;
        } catch (Exception var10) {
            logger.error("", var10);
            var5 = 0L;
        } finally {
            this.close();
        }

        return var5;
    }

    public long decr(String key, Long delta) {
        long var5;
        try {
            if (delta < 0L) {
                throw new RuntimeException("递减因子必须大于0");
            }

            long var4 = this.redisTemplate.opsForValue().increment(key, -delta);
            return var4;
        } catch (Exception var10) {
            logger.error("", var10);
            var5 = 0L;
        } finally {
            this.close();
        }

        return var5;
    }

    public Object hget(String key, String item) {
        Object var4;
        try {
            Object var3 = this.redisTemplate.opsForHash().get(key, item);
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = null;
        } finally {
            this.close();
        }

        return var4;
    }

    public Map<String, Object> hmget(String key) {
        Object var3;
        try {
            Map var2 = this.redisTemplate.opsForHash().entries(key);
            return var2;
        } catch (Exception var7) {
            logger.error("", var7);
            var3 = null;
        } finally {
            this.close();
        }

        return (Map)var3;
    }

    public boolean hmset(String key, Map<Object, Object> map) {
        boolean var4;
        try {
            this.redisTemplate.opsForHash().putAll(key, map);
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }

    public boolean hmset(String key, Map<String, Object> map, long time) {
        boolean var6;
        try {
            this.redisTemplate.opsForHash().putAll(key, map);
            if (time > 0L) {
                this.expire(key, time);
            }

            boolean var5 = true;
            return var5;
        } catch (Exception var10) {
            logger.error("", var10);
            var6 = false;
        } finally {
            this.close();
        }

        return var6;
    }

    public boolean hset(String key, String item, Object value) {
        boolean var5;
        try {
            this.redisTemplate.opsForHash().put(key, item, value);
            boolean var4 = true;
            return var4;
        } catch (Exception var9) {
            logger.error("向Redis中添加数据发生异常key=[" + key + "]", var9);
            var5 = false;
        } finally {
            this.close();
        }

        return var5;
    }

    public boolean hset(String key, String item, Object value, long time) {
        boolean var7;
        try {
            this.redisTemplate.opsForHash().put(key, item, value);
            if (time > 0L) {
                this.expire(key, time);
            }

            boolean var6 = true;
            return var6;
        } catch (Exception var11) {
            logger.error("", var11);
            var7 = false;
        } finally {
            this.close();
        }

        return var7;
    }

    public Set hAllKeys(String key) {
        Set keys = null;
        try {
            keys = this.redisTemplate.opsForHash().keys(key);
        } catch (Exception var7) {
            logger.error("", var7);
        } finally {
            this.close();
        }
        return keys;
    }

    public void hdel(String key, Object... item) {
        try {
            this.redisTemplate.opsForHash().delete(key, item);
        } catch (Exception var7) {
            logger.error("", var7);
        } finally {
            this.close();
        }

    }

    public boolean hHasKey(String key, String item) {
        boolean var4;
        try {
            boolean var3 = this.redisTemplate.opsForHash().hasKey(key, item);
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }
    /**
     * ZSet: 增加分数
     */
    public Double zincrby(String key, String value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * ZSet: 获取分数
     */
    public Double zscore(String key, String value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * ZSet: 获取倒序排名
     */
    public Long zrevrank(String key, String value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }
    public double hincr(String key, String item, double by) {
        double var6;
        try {
            double var5 = this.redisTemplate.opsForHash().increment(key, item, by);
            return var5;
        } catch (Exception var11) {
            logger.error("", var11);
            var6 = 0.0D;
        } finally {
            this.close();
        }

        return var6;
    }

    public double hdecr(String key, String item, double by) {
        double var6;
        try {
            double var5 = this.redisTemplate.opsForHash().increment(key, item, -by);
            return var5;
        } catch (Exception var11) {
            logger.error("", var11);
            var6 = 0.0D;
        } finally {
            this.close();
        }

        return var6;
    }

    public Set<Object> sGet(String key) {
        Object var3;
        try {
            Set var2 = this.redisTemplate.opsForSet().members(key);
            return var2;
        } catch (Exception var7) {
            logger.error("", var7);
            var3 = null;
        } finally {
            this.close();
        }

        return (Set)var3;
    }

    public boolean sHasKey(String key, Object value) {
        boolean var4;
        try {
            boolean var3 = this.redisTemplate.opsForSet().isMember(key, value);
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }

    public long sSet(String key, Object... values) {
        long var4;
        try {
            long var3 = this.redisTemplate.opsForSet().add(key, values);
            return var3;
        } catch (Exception var9) {
            logger.error("", var9);
            var4 = 0L;
        } finally {
            this.close();
        }

        return var4;
    }

    public long sSetAndTime(String key, long time, Object... values) {
        long var6;
        try {
            Long count = this.redisTemplate.opsForSet().add(key, values);
            if (time > 0L) {
                this.expire(key, time);
            }

            var6 = count;
            return var6;
        } catch (Exception var11) {
            logger.error("", var11);
            var6 = 0L;
        } finally {
            this.close();
        }

        return var6;
    }

    public long sGetSetSize(String key) {
        long var3;
        try {
            long var2 = this.redisTemplate.opsForSet().size(key);
            return var2;
        } catch (Exception var8) {
            logger.error("", var8);
            var3 = 0L;
        } finally {
            this.close();
        }

        return var3;
    }

    public long setRemove(String key, Object... values) {
        long var4;
        try {
            Long count = this.redisTemplate.opsForSet().remove(key, values);
            var4 = count;
            return var4;
        } catch (Exception var9) {
            logger.error("", var9);
            var4 = 0L;
        } finally {
            this.close();
        }

        return var4;
    }

    public List<Object> lGet(String key, long start, long end) {
        Object var7;
        try {
            List var6 = this.redisTemplate.opsForList().range(key, start, end);
            return var6;
        } catch (Exception var11) {
            logger.error("", var11);
            var7 = null;
        } finally {
            this.close();
        }

        return (List)var7;
    }

    public long lGetListSize(String key) {
        long var3;
        try {
            long var2 = this.redisTemplate.opsForList().size(key);
            return var2;
        } catch (Exception var8) {
            logger.error("从Redis中获取指定key数据key=[" + key + "]", var8);
            var3 = 0L;
        } finally {
            this.close();
        }

        return var3;
    }

    public Object lGetIndex(String key, long index) {
        Object var5;
        try {
            Object var4 = this.redisTemplate.opsForList().index(key, index);
            return var4;
        } catch (Exception var9) {
            logger.error("", var9);
            var5 = null;
        } finally {
            this.close();
        }

        return var5;
    }

    public boolean lSet(String key, Object value) {
        boolean var4;
        try {
            this.redisTemplate.opsForList().rightPush(key, value);
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }

    public boolean leftPushAll(String key, Object value) {
        boolean var4;
        try {
            this.redisTemplate.opsForList().leftPushAll(key, value);
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }

    public Object rightPop(String key){
        Object s = null;
        try {
            s = this.redisTemplate.opsForList().leftPop(key);
            return s;
        } catch (Exception var10) {
            logger.error("向Redis中添加数据发生异常key=[" + key + "]", var10);
        } finally {
            this.close();
        }
        return s;
    }

    public Boolean setIfAbsent(String key,String redUserKey){
        Boolean s;
        try {
            s = this.redisTemplate.opsForValue().setIfAbsent(key,redUserKey);
            return s;
        } catch (Exception var10) {
            logger.error("向Redis中添加数据发生异常key=[" + key + "]", var10);
        } finally {
            this.close();
            s = false;
        }
        return s;
    }



    public boolean lSet(String key, Object value, long time) {
        boolean var6;
        try {
            this.redisTemplate.opsForList().rightPush(key, value);
            if (time > 0L) {
                this.expire(key, time);
            }

            boolean var5 = true;
            return var5;
        } catch (Exception var10) {
            logger.error("向Redis中添加数据发生异常key=[" + key + "]", var10);
            var6 = false;
        } finally {
            this.close();
        }

        return var6;
    }

    public boolean lSet(String key, List<Object> value) {
        boolean var4;
        try {
            this.redisTemplate.opsForList().rightPushAll(key, value);
            boolean var3 = true;
            return var3;
        } catch (Exception var8) {
            logger.error("", var8);
            var4 = false;
        } finally {
            this.close();
        }

        return var4;
    }

    public boolean lSet(String key, List<Object> value, long time) {
        boolean var6;
        try {
            this.redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0L) {
                this.expire(key, time);
            }

            boolean var5 = true;
            return var5;
        } catch (Exception var10) {
            logger.error("", var10);
            var6 = false;
        } finally {
            this.close();
        }

        return var6;
    }

    public boolean lUpdateIndex(String key, long index, Object value) {
        boolean var6;
        try {
            this.redisTemplate.opsForList().set(key, index, value);
            boolean var5 = true;
            return var5;
        } catch (Exception var10) {
            logger.error("", var10);
            var6 = false;
        } finally {
            this.close();
        }

        return var6;
    }

    public long lRemove(String key, long count, Object value) {
        long var6;
        try {
            Long remove = this.redisTemplate.opsForList().remove(key, count, value);
            var6 = remove;
            return var6;
        } catch (Exception var11) {
            logger.error("", var11);
            var6 = 0L;
        } finally {
            this.close();
        }

        return var6;
    }

    public void insertKey(String key, String value, Long timeout) {
        try {
            if (null != timeout) {
                this.redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MINUTES);
            } else {
                this.redisTemplate.opsForValue().set(key, value);
            }
        } catch (Exception var8) {
            logger.error("向Redis中添加数据发生异常key=[" + key + "]", var8);
        } finally {
            this.close();
        }

    }


    public void deleteByKey(String key) {
        try {
            Boolean exists = this.redisTemplate.hasKey(key);
            if (exists) {
                this.redisTemplate.delete(key);
                return;
            }
        } catch (Exception var6) {
            logger.error("从Redis中删除指定key数据key=[" + key + "],发生异常", var6);
            return;
        } finally {
            this.close();
        }

    }



    public Set<String> getKeys(String key){
        return  this.redisTemplate.keys(key);
    }

    public void deleteByLikeKey(String key) {
        try {
            Set<String> keys = this.redisTemplate.keys(key);
            Iterator var3 = keys.iterator();

            while(var3.hasNext()) {
                String s = (String)var3.next();
                this.redisTemplate.delete(s);
            }
        } catch (Exception var8) {
            logger.error("从Redis中删除指定key数据key=[" + key + "],发生异常", var8);
        } finally {
            this.close();
        }

    }

    public Object queryValueByKey(String key) {
        Object var3;
        try {
            Boolean exists = this.redisTemplate.hasKey(key);
            if (!exists) {
                var3 = null;
                return var3;
            }

            var3 = this.redisTemplate.opsForValue().get(key);
        } catch (Exception var7) {
            logger.error("从Redis中获取指定key数据key=[" + key + "]发生异常", var7);
            var3 = null;
            return var3;
        } finally {
            this.close();
        }

        return var3;
    }

    public boolean expire(String key, long seconds) {
        try {
            if (seconds > 0L) {
                this.redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
            }

            boolean var4 = true;
            return var4;
        } catch (Exception var8) {
            logger.info("对指定的key=[" + key + "]设置时间[" + seconds + "s]发生异常", var8);
        } finally {
            this.close();
        }

        return false;
    }

    public void addZset(String key, String value,Double score){
        Double oldScore = redisTemplate.opsForZSet().score(key, value);
        if (oldScore==null){
            oldScore=0.0;
        }
        redisTemplate.opsForZSet().add(key,value,score+oldScore);
    }
    public void addForZset(String key, String value,Double score){
        redisTemplate.opsForZSet().add(key,value,score);
    }



    public  Set<ZSetOperations.TypedTuple<String>> getZset(String key, int limit){
        // Set<String> userIds = redisTemplate.opsForZSet().reverseRangeByScore(key, 0.0, Double.MAX_VALUE, 0, limit);
        Set set = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, 0.0, Double.MAX_VALUE, 0, limit);
        return set;
    }

    public  Set<ZSetOperations.TypedTuple<String>> getZset(String key, double minScore){
        // Set<String> userIds = redisTemplate.opsForZSet().reverseRangeByScore(key, 0.0, Double.MAX_VALUE, 0, limit);
        Set set = redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, minScore, Double.MAX_VALUE, 0, 9999);
        return set;
    }

    public Set<ZSetOperations.TypedTuple<String>> getZset(String key, long start, long end) {
        try {
            return this.redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        } catch (Exception e) {
            logger.error("从Redis ZSet获取指定范围数据异常 key=[" + key + "]", e);
            return null;
        }
    }

    public void removeZsetKey(String key,String... var){
        redisTemplate.opsForZSet().remove(key,var);
    }
    public Long getZsetRank(String value,String key){

        return redisTemplate.opsForZSet().reverseRank(key,value);
    }

    public Set<String> getZsetRange(String key,int start,int end){
       return redisTemplate.opsForZSet().reverseRange(key,start,end);
    }

    public Double getZsetScore(String key ,String value){
        return redisTemplate.opsForZSet().score(key, value);
    }

    private void close() {
        RedisConnectionUtils.unbindConnection(this.redisTemplate.getConnectionFactory());
    }
}
