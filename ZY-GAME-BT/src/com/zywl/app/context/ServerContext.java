package com.zywl.app.context;

import com.zywl.app.defaultx.APP;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.LoginManagerService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
			SpringUtil.getService(LoginManagerService.class).connectManager();
		}, "connectManagerSGServer");
		t.start();
		Thread t2 = new Thread(() -> {
			SpringUtil.getService(LoginManagerService.class).connectLog();
		}, "connectLogServer");
		t2.start();
	}
}
