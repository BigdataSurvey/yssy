package com.zywl.app.base.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * <h1>参考{@link Executors}-DefaultThreadFactory</h1>
 * The default thread factory
 * @author FXBTG Doe.
 *
 */
public class AppDefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private String threadMark;

	/**
	 * 
	 * <h1>参考{@link Executors}-DefaultThreadFactory</h1>
	 * The default thread factory
	 * @author FXBTG Doe.
	 *
	 */
    public AppDefaultThreadFactory(String threadMark) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                      poolNumber.getAndIncrement() +
                     "-thread-";
        this.threadMark = threadMark;
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement()+"-"+threadMark,
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}