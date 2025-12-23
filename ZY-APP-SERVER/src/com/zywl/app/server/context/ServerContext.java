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

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		APP.shutdown();
	}

	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		APP.run();

		Thread t1 = new Thread(() -> SpringUtil.getService(ServerManagerService.class).connectManager(),
				"connectManagerServer");
		t1.start();

		Thread t2 = new Thread(() -> SpringUtil.getService(ServerManagerService.class).connectLogServer(),
				"connectLogServer");
		t2.start();

		Thread t3 = new Thread(() -> SpringUtil.getService(ServerManagerService.class).connectDts2Server(),
				"connectDts2Server");
		t3.start();

		//
		// new Thread(() -> SpringUtil.getService(ServerManagerService.class).connectDgsServer(), "connectDgsServer").start();
		// new Thread(() -> SpringUtil.getService(ServerManagerService.class).connectBattleRoyaleServer(), "connectBattleRoyaleServer").start();
		// new Thread(() -> SpringUtil.getService(ServerManagerService.class).connectLHDServer(), "connectLhdServer").start();
		// new Thread(() -> SpringUtil.getService(ServerManagerService.class).connectSgServer(), "connectSgServer").start();
	}
}
