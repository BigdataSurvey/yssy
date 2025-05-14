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
@WebServlet(name = "ReleaseAppServlet", urlPatterns = "/releaseApp")
@MultipartConfig
public class ReleaseAppServlet extends BaseServlet {

	private static final Log logger = LogFactory.getLog(ReleaseAppServlet.class);
	
	private ReleaseAppService releaseAppService;
	
	public ReleaseAppServlet(){
		releaseAppService = SpringUtil.getService(ReleaseAppService.class);
	}
	
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception{
		HttpSession session = request.getSession();
		if(session.getAttribute("Admin") == null){
			throw new AppException("请先登录");
		}
		if(isNull("id") || isNull(request.getParameter("description")) || isNull(request.getParameter("versionName")) || isNull(request.getParameter("versionNo"))){
			throw new AppException("参数异常");
		}
		String id = request.getParameter("id");
		String description = request.getParameter("description");
		String versionName = request.getParameter("versionName");
		int versionNo = Integer.parseInt(request.getParameter("versionNo"));
		int fc = Version.NORMAL_UPDATE;		//默认普通上传
		int release = Version.RELEASE_DISABLE;	//默认不发布
		String _type = request.getParameter("type");
		checkNull(_type);
		Part part = request.getPart("file");
		if (part != null && part.getSize() > 1) {
			String suffix = part.getSubmittedFileName();
			suffix = suffix.substring(suffix.lastIndexOf(".") + 1);
			if(Version.APP_ANDROID.equals(suffix.toLowerCase()) || Version.APP_IOS.equals(suffix.toLowerCase())){
				InputStream inputStream = part.getInputStream();
				byte[] bytes = new byte[inputStream.available()];
				inputStream.read(bytes);
				releaseAppService.addVersion(id, bytes, description, versionName, versionNo, fc, release, Integer.parseInt(_type));
				return null;
			} else {
				throw new AppException("请上传正确的安装包");
			}
		} else {
			throw new AppException("请上传安装程序");
		}
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
}
