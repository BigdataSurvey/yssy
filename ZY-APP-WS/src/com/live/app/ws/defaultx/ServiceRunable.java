package com.live.app.ws.defaultx;

import org.apache.commons.logging.Log;

public abstract class ServiceRunable implements Runnable {

	private final Log logger;
	
	public ServiceRunable(Log logger) {
		this.logger = logger;
	}
	
	public void run() {
		try {
			service();
		} catch (Exception e) {
			logger.error(e, e);
		}
	}
	
	public abstract void service();
}
