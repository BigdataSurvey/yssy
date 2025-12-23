package com.zywl.app.context;

import com.zywl.app.defaultx.APP;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.PbxManagerService;
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
		System.out.println(2222222);
		Thread t = new Thread(() -> {
			SpringUtil.getService(PbxManagerService.class).connectManager();
		}, "connectManagerDgsServer");
		t.start();
	}
}
