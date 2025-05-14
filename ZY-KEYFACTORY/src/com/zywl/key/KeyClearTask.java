package com.zywl.key;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class KeyClearTask implements ServletContextListener{
	
    public  void task() {
        // 创建定时器
        Timer timer = new Timer();
        // 创建定时器任务
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                
                if (KeyFactoryController.map.size()>0) {
                	Set<String> keys = KeyFactoryController.map.keySet();
                    for (String key : keys) {
    					Map<String, String> map = KeyFactoryController.map.get(key);
    					 Set<String> timeKeys = map.keySet();
    					 for (String time : timeKeys) {
    						if (System.currentTimeMillis() - Long.parseLong(time)>=10000) {
    							KeyFactoryController.map.remove(key);
    							System.out.println("移除超过10秒的KEY"+key);
    						}
    					}
    				}
				}
                
            }
        };
       
        timer.scheduleAtFixedRate(timerTask, new Date(), 1000); // 每4秒执行一次
    }

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		task();
	}
}