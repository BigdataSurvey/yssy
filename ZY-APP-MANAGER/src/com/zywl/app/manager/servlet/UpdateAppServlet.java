package com.zywl.app.manager.servlet;

import java.io.InputStream;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zywl.app.base.bean.Version;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.ReleaseAppService;

@SuppressWarnings("serial")
@WebServlet(name = "UpdateAppServlet", urlPatterns = "/updateApp")
@MultipartConfig
public class UpdateAppServlet extends BaseServlet {

	private static final Log logger = LogFactory.getLog(UpdateAppServlet.class);
	
	private ReleaseAppService releaseAppService;
	
	public UpdateAppServlet(){
		releaseAppService = SpringUtil.getService(ReleaseAppService.class);
	}
	
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception{
		HttpSession session = request.getSession();
		if(session.getAttribute("Admin") == null){
			throw new AppException("请先登录");
		}
		if(isNull(request.getParameter("id")) || isNull(request.getParameter("description")) || isNull(request.getParameter("versionName")) || isNull(request.getParameter("versionNo"))){
			throw new AppException("参数异常");
		}
		String id = request.getParameter("id");
		String description = request.getParameter("description");
		String versionName = request.getParameter("versionName");
		int versionNo = Integer.parseInt(request.getParameter("versionNo"));
		String _type = request.getParameter("type");
		checkNull(_type);
		byte[] bytes = null;
		Part part = request.getPart("file");
		if (part != null && part.getSize() > 0) {
			String suffix = part.getSubmittedFileName();
			suffix = suffix.substring(suffix.lastIndexOf(".") + 1);
			if(Version.APP_ANDROID.equals(suffix.toLowerCase()) || Version.APP_IOS.equals(suffix.toLowerCase())){
				InputStream inputStream = part.getInputStream();
				bytes = new byte[inputStream.available()];
				inputStream.read(bytes);
			} else {
				throw new AppException("请上传正确的安装包");
			}
		}
		releaseAppService.updateVersion(id, bytes, description, versionName, versionNo, null, null, Integer.parseInt(_type));
		return null;
	}

	@Override
	protected Log logger() {
		return logger;
	}
}
