package com.zywl.app.manager.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import com.live.app.ws.bean.Command;
import com.live.app.ws.util.CommandBuilder;
import com.zywl.app.base.util.Base64Util;
import com.zywl.app.base.util.DesUtil;
import com.zywl.app.base.util.Response;
import com.zywl.app.base.util.UID;
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.defaultx.util.SpringUtil;

@WebServlet(name = "AppUploadFileServlet", urlPatterns = "/appfile")
@MultipartConfig
public class AppUploadFileServlet extends HttpServlet {
	
	public final static Map<String, FileInfo> FILE_CACHE = new ConcurrentHashMap<String, FileInfo>();

	
	public void init() throws ServletException {
		
		new Timer("清理APP临时文件timer").schedule(new TimerTask() {
			public void run() {
				for(String key : FILE_CACHE.keySet()) {
					FileInfo fileInfo = FILE_CACHE.get(key);
					if(System.currentTimeMillis() - fileInfo.getCreateTime() > 60 * 1000) {
						FILE_CACHE.remove(key);
					}
				}
			}
		}, 3 * 1000,  3 * 1000);
	}
	
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
	
	public static String getAndRemoveBase64(String fileId) {
		FileInfo fileInfo = FILE_CACHE.remove(fileId);
		if(fileInfo != null) {
			return fileInfo.getBase64();
		}
		return null;
	}

	private static String zipBase64(String base64) throws Exception{
		if(Base64Util.getBase64ImageSize(base64) > 300 * 1024){
			return "data:image/jpg;base64," + zipBase64(Base64Util.getMinBase64(base64));
		}
		return base64;
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String wsid = request.getParameter("wsid");
		String base64 = request.getParameter("base64");
		Command command = new Command();
		if(wsid == null || base64 == null) {
			command = CommandBuilder.builder().error("参数异常").build();
		}else {
			WsidBean wsidBean =null;
			if(wsidBean == null) {
				command = CommandBuilder.builder().error("鉴权失败").build();
			}else {
				try {
					base64 = DesUtil.decrypt(base64, wsidBean.getWsPrivateKey());
					try {
						FileInfo fileInfo = new FileInfo();
						fileInfo.setBase64(zipBase64(base64));
						fileInfo.setCreateTime(System.currentTimeMillis());
						String fileId = UID.create();
						
						FILE_CACHE.put(fileId, fileInfo);
						command = CommandBuilder.builder().success(fileId).build();
					} catch (Exception e) {
						command = CommandBuilder.builder().error("文件格式错误").build();
					}
				}catch (Exception e) {
					command = CommandBuilder.builder().error("数据格式错误").build();
				}
			}
		}
		Response.doResponse(request, response, JSON.toJSONString(command));
	}
}

class FileInfo{
	
	private String base64;
	
	private Long createTime;

	public String getBase64() {
		return base64;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setBase64(String base64) {
		this.base64 = base64;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
}
