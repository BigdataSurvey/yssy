package com.zywl.app.base.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.AsyncContext;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class Response {
	private static final Log logger = LogFactory.getLog(Response.class);

	public static void doResponse(AsyncContext asyncContext, String message){
		PrintWriter writer = null;
		try {
			if (asyncContext == null) {
				return;
			}
			ServletResponse response = asyncContext.getResponse();
			response.setContentType("text/plain;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			writer = response.getWriter();
			writer.write(message == null ? "" : message);
			writer.flush();
		} catch (IllegalStateException ise) {
			logger.warn("AsyncContext already completed, skip response. msg=" + message);
		} catch (IOException e) {
			logger.error(e, e);
		} finally {
			if (writer != null) {
				try { writer.close(); } catch (Exception ignore) {}
			}
			try {
				asyncContext.complete();
			} catch (IllegalStateException ignore) {
			}
		}
	}


	public static void doResponse(HttpServletRequest request, HttpServletResponse response,String message){
		Writer writer = null;
		try{
			response.setContentType("text/plain;charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			writer = response.getWriter();
			writer.write(message);
		}catch(Exception e){
			logger.error(e, e);
		}finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}
}
