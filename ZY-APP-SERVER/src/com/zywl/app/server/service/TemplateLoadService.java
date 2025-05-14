package com.zywl.app.server.service;

import java.io.File;
import java.io.StringWriter;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.PropertiesUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;

@Service
public class TemplateLoadService extends BaseService {

	private static final Log logger = LogFactory.getLog(TemplateLoadService.class);
	
	private static final Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
	
	private static JSONObject COST = new JSONObject();
	
	public static String staticWebUrl = "";

	public static String managerWebUrl = "";
	
	@PostConstruct
	public void _construct(){
		PropertiesUtil propertiesUtil = new PropertiesUtil("config.properties");
		Properties properties = propertiesUtil.getProperties();
		for (Object object : properties.keySet()) {
			String key = object.toString();
			if(key.startsWith("template.")){
				COST.put(key.replaceFirst("template.", ""), properties.get(object));
			}
		}
		try{
			cfg.setDirectoryForTemplateLoading(new File(this.getClass().getResource("../template").getPath()));
			cfg.setDefaultEncoding("UTF-8");
			cfg.setLocalizedLookup(false);
			cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			cfg.setLogTemplateExceptions(true);
		}catch(Exception e){
			logger.error(e, e);
		}
	}

	public String loadTemplate(String templateName){
		return loadTemplate(templateName, null);
	}
	
	public String loadTemplate(String templateName, JSONObject params){
		if(params == null){
			params = new JSONObject();
		}
		StringWriter stringWriter = new StringWriter();
		try{
			Template template = cfg.getTemplate(templateName);
			if(isNotNull(template)){
				params.put("staticWebUrl", staticWebUrl);
				params.put("managerWebUrl", managerWebUrl);
				params.putAll(COST);
				template.process(params, stringWriter);
			}
		}catch (Exception e) {
			logger.error("获取模板失败：" + e, e);
		}
		return stringWriter.toString();
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
}
