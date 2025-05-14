package com.zywl.app.defaultx.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.zywl.app.base.bean.Admin;
import com.zywl.app.base.bean.Role;
import com.zywl.app.base.service.BaseService;

@Service
public class RoleService extends BaseService {

	private static final Log logger = LogFactory.getLog(RoleService.class);
	
	public boolean isAdmin(Admin admin){
		return isAdmin(admin.getRoleId());
	}
	
	public boolean isAdmin(String roleId){
		return eq(Role.ADMIN, roleId);
	}

	@Override
	protected Log logger() {
		return logger;
	}
}
