package com.zywl.app.defaultx.service;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.Admin;
import com.zywl.app.base.util.UID;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class AdminService extends DaoService {
	private static final Log logger = LogFactory.getLog(AdminService.class);
	
	public AdminService(){
		super("AdminMapper");
	}

	public List<Admin> selectAdminItemByRoleId(String roleId){
		Admin parameters = new Admin();
		parameters.setRoleId(roleId);
		return findByConditions(parameters);
	}
	
	public Admin getAdminById(String id){
		return findOne(id);
	}
	
	public List<Admin> getAdminList(){
		return findAll();
	}
	
	@Transactional
	public void addAdmin(Admin admin){
		if(admin.getId() == null) {
			admin.setId(UID.create());
		}
		save(admin);
	}
	
	@Transactional
	public void updateAdmin(Admin admin){
		update(admin);
	}
	
	public Admin getAdminByUsername(String username){
		Admin parameters = new Admin();
		parameters.setUsername(username);
		List<Admin> list = findByConditions(parameters);
		if(list == null || list.isEmpty()){
			return null;
		}
		return list.get(0);
	}

	protected Log logger() {
		return logger;
	}
}
