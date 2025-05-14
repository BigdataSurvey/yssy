package com.zywl.app.base.util;

public class Async {
	
	public Async(long timeout) {
		this.timeout = timeout;
	}

	private long timeout = 10 * 1000;

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
