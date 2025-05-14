package com.zywl.app.defaultx.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zywl.app.base.bean.Template;
import com.zywl.app.defaultx.dbutil.DaoService;

public class TemplateService extends DaoService {

	private final static Log logger = LogFactory.getLog(TemplateService.class);
	
	public TemplateService() {
		super("TemplateMapper");
	}
	
	public void insert(Template template){
		insert(template);
	}
	
	public void update(Template template){
		update(template);
	}
	
	public List<Template> selectAll(){
		return super.findAll();
	}

	@Override
	protected Log logger() {
		return logger;
	}

}
