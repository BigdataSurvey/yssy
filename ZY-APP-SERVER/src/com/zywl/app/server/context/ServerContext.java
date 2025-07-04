package com.zywl.app.server.context;

import com.zywl.app.defaultx.APP;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.ServerManagerService;

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
			SpringUtil.getService(ServerManagerService.class).connectManager();
		}, "connectManagerServer");
		t.start();
		Thread t2 = new Thread(() -> {
			SpringUtil.getService(ServerManagerService.class).connectLogServer();
		}, "connectLogServer");
		t2.start();
		Thread t6 = new Thread(() -> {
			SpringUtil.getService(ServerManagerService.class).connectLHDServer();
		}, "connectLhdServer");
		t6.start();
		Thread t7 = new Thread(() -> {
			SpringUtil.getService(ServerManagerService.class).connectDts2Server();
		}, "connectDts2Server");
		t7.start();
		/*Thread t8 = new Thread(() -> {
			SpringUtil.getService(ServerManagerService.class).connectDgsServer();
		}, "connectDgsServer");
		t8.start();*/
		/*Thread t8 = new Thread(() -> {
			SpringUtil.getService(ServerManagerService.class).connectSgServer();
		}, "connectSgServer");
		t8.start();*/

		/*Thread t9 = new Thread(() -> {
			SpringUtil.getService(ServerManagerService.class).connectBattleRoyaleServer();
		}, "connectBattleRoyaleServer");
		t9.start();*/
	}
}
