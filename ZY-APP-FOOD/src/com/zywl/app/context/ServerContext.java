package com.zywl.app.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.zywl.app.defaultx.APP;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.FoodManagerService;

/**
 * 上下文监听
 * @author FXBTG Doe.
 *
 */
public class ServerContext implements ServletContextListener {

	public void contextDestroyed(ServletContextEvent contextEvent) {
		APP.shutdown();
	}
	
	public void contextInitialized(ServletContextEvent contextEvent) {
		APP.run();
		Thread t = new Thread(() -> {
			SpringUtil.getService(FoodManagerService.class).connectManager();
		}, "connectManagerFoodServer");
		t.start();

	}
}
