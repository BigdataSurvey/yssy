package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Admin;
import com.zywl.app.base.bean.AdminLog;
import com.zywl.app.base.util.UID;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AdminLogService extends DaoService {
	private static final Log logger = LogFactory.getLog(AdminLogService.class);

	public AdminLogService(){
		super("AdminLogMapper");
	}
	
	public AdminLog getAdminById(long id){
		return findOne(id);
	}
	
	public List<AdminLog> getAdminList(){
		return findAll();
	}
	
	@Transactional
	public void addAdminLog(Admin admin, String action, JSONObject content){
		AdminLog adminLog = new AdminLog();
		adminLog.setAdminAccount(admin.getUsername());
		adminLog.setAdminName(admin.getName());
		adminLog.setAction(action);
		adminLog.setContent(content);
		adminLog.setRecordTime(new Date());
		save(adminLog);
	}

	protected Log logger() {
		return logger;
	}
}
