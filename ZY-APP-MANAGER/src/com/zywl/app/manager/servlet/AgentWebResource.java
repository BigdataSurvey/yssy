package com.zywl.app.manager.servlet;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zywl.app.base.util.PropertiesUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

@WebServlet(name = "AgentModalLoader", urlPatterns = "/AgentModal")
public class AgentWebResource extends HttpServlet{

	private static final long serialVersionUID = 3505059656700635796L;
	
	private static final Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
	
	private static final String LOGIN_TPL_URL = "Login.ftl";
    
    private static final String DESKTOP_TPL_URL = "Desktop.ftl";
    
    private static PropertiesUtil propertiesUtil;
    
	@Override
	public void init(ServletConfig config) throws ServletException {
		try{
			cfg.setDirectoryForTemplateLoading(new File(this.getClass().getResource("../webpages_agent").getPath()));
			cfg.setDefaultEncoding("UTF-8");
			cfg.setLocalizedLookup(false);
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setLogTemplateExceptions(true);
		}catch(Exception e){
			e.printStackTrace();
		}
		propertiesUtil = new PropertiesUtil("global.properties");
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Writer out = null;
		try{
		    response.setCharacterEncoding("UTF-8");
		    out = response.getWriter();
		    boolean hasLogin = false;
		    Object user = request.getSession().getAttribute("Agent");
		    if(user != null) {
		        hasLogin = true;
		    }
			String target = request.getParameter("target");
			Map<Object, Object> paramMap = new HashMap<Object, Object>(propertiesUtil.getProperties());
	        if(isNotEmpty(target)) {
	            if(!hasLogin) {
	                target = LOGIN_TPL_URL;
	            } else {
	                paramMap.put("menuId", target);
	                target = target + ".ftl";
	            }
	        } else {
	            target = hasLogin ? DESKTOP_TPL_URL : LOGIN_TPL_URL;
	        }
	        if(isEmpty(target)) {
	            target = "error/404.ftl";
	        }
	        if(hasLogin) {
	            paramMap.put("User", user);
	        }
			Template template = cfg.getTemplate(target,"UTF-8");
			template.process(paramMap, out);
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			if(out != null){
				out.close();
			}
		}
	}
	  /**
     * 是空的字符串
     * @param param
     * @return
     */
    protected boolean isEmpty(String param){
        return null == param || "".equals(param) || param.equals("null");
    }
    
    /**
     * 不是空的字符串
     * @param param
     * @return
     */
    protected boolean isNotEmpty(String param){
        return null != param && !"".equals(param) && !param.equals("null");
    }
}
