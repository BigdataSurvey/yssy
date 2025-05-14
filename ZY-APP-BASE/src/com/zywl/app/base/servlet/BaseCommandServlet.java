package com.zywl.app.base.servlet;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.filter.SimplePropertyPreFilter;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.util.AsyncServletProcessor;
import com.zywl.app.base.util.Response;
import net.rubyeye.xmemcached.command.Command;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@SuppressWarnings("serial")
public abstract class BaseCommandServlet extends BaseServlet{

	public static SimplePropertyPreFilter filter = new SimplePropertyPreFilter();

	protected final Log logger = LogFactory.getLog(getClass());

	public BaseCommandServlet(){}
	
	public String getRequestStreamString(HttpServletRequest request) throws IOException {
		byte[] bytes = new byte[1024 * 1024];
		InputStream is = request.getInputStream();
		int nRead = 1;
		int nTotalRead = 0;
		while (nRead > 0) {
			nRead = is.read(bytes, nTotalRead, bytes.length - nTotalRead);
			if (nRead > 0)
				nTotalRead = nTotalRead + nRead;
		}
		return new String(bytes, 0, nTotalRead, "utf-8");
	}

	protected void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		response.setCharacterEncoding("UTF-8");
		request.setCharacterEncoding("UTF-8");
		
		String ipAddress = (String)request.getSession().getAttribute("ip");
		String requestStreamString = getRequestStreamString(request);
		if(requestStreamString == null) {
			throwExp("参数异常");
		}
//		Command command = JSON.parseObject(requestStreamString, Command.class);
		
		logger().debug("["+getRequestUID(request)+"]来自" + ipAddress + "的请求：" + JSON.toJSONString(request.getParameterMap()));
		
		String webResponse = "";
		try{
			Object doProcess = doProcess(request, response, ipAddress, null);
			if(doProcess instanceof AsyncServletProcessor){
				asyncServletExecutor.execute((AsyncServletProcessor)doProcess);
				return;
			}else{
				webResponse = String.valueOf(doProcess);
			}
		}catch(AppException e){
			logger().warn("执行异常：" + e);
			webResponse = e.getMessage();
		}catch(Exception e){
			logger().error("未知异常", e);
			webResponse = "服务端异常";
		}
		Response.doResponse(request, response, webResponse);
	}
	
	@Override
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp) throws AppException, Exception {
		return null;
	}
	
	public abstract Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp, Command command) throws AppException, Exception;
}
