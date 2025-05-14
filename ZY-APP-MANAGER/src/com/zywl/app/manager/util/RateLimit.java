package com.zywl.app.manager.util;

import java.util.concurrent.atomic.AtomicLong;

public class RateLimit {
    private final static int max = 1000;
    private final AtomicLong lastTime;
    private long delayTime;

    public RateLimit(int rate) {
        lastTime = new AtomicLong(0);
        delayTime = (max * 100 / rate) / 100;
    }

    public synchronized boolean check() {
        long interval = System.currentTimeMillis() - lastTime.get();
        if(interval < delayTime) {
            return false;
        }
        lastTime.set(System.currentTimeMillis());
        return true;
    }
}
