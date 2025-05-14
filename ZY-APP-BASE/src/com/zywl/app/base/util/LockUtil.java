package com.zywl.app.base.util;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.GuildMember;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author ns
 * @Date 2023/3/27 19:47
 * @Version 1.0
 */
public class LockUtil {


    @PostConstruct
    public void _con() {
        new Timer("移除锁").schedule(new TimerTask() {
            public void run() {
                try {
                    Set<String> keys = LockUtil.lock.keySet();
                    for (String key : keys) {
                        Long timer = Long.parseLong(LockUtil.lock.get(key));
                        if (System.currentTimeMillis() - timer > 1000 * 30) {
                            LockUtil.lock.remove(key);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }, 0, 1000);
    }


    public static final Map<String, String> lock = new ConcurrentHashMap<>();

    public synchronized static Object getlock(String key) {
        String obj = lock.get(key);
        if (obj == null) {
            lock.put(key, obj = String.valueOf(System.currentTimeMillis()));
        }
        return obj;
    }

    public static Object getlock(Long key) {
        return getlock(key.toString());
    }


    public static void main(String[] args) {
        new Thread() {
            @Override
            public void run() {
                synchronized (getlock(1L)) {
                    System.out.println(getName());
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }.start();
        new Thread() {
            @Override
            public void run() {
                synchronized (getlock(1L)) {
                    System.out.println(getName());
                    try {
                        sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }.start();
    }
}
