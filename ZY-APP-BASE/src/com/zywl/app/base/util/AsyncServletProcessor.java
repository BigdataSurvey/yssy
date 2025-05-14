package com.zywl.app.base.util;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;

public abstract class AsyncServletProcessor implements Runnable {
	protected AsyncContext asyncContext;

	public AsyncServletProcessor(HttpServletRequest request) {
		this.asyncContext = request.startAsync();
	}
}
