package com.zywl.app.defaultx;

import org.apache.logging.log4j.LogManager;

import com.live.app.ws.util.Executer;
import com.zywl.app.defaultx.util.DefaultServiceController;
import com.zywl.app.defaultx.util.SpringUtil;

public class APP {
	
	public static void run(){
		Executer.controller = DefaultServiceController.getController(SpringUtil.start("classpath:application*.xml"));
	}
	
	public static void shutdown(){
		LogManager.getLogger().info("System Shutting down..");
        LogManager.shutdown();
	}
}
