package com.zywl.app.manager.service.manager;

import java.io.File;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Admin;
import com.zywl.app.base.bean.Role;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Base64Util;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.base.util.UID;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.AdminService;
import com.zywl.app.defaultx.service.RoleService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.AdminSocketServer;

@Service
@ServiceClass(code = MessageCodeContext.ADMIN_SERVER)
public class ManagerAdminService extends BaseService {

	private static final Log logger = LogFactory.getLog(ManagerAdminService.class);

	@Autowired
	private AdminService adminService;
	
	@Autowired
	private RoleService roleService;

	private PropertiesUtil adminProperties;
	
	@PostConstruct
	public void _construct(){
		adminProperties = new PropertiesUtil("static.properties");
	}
	
	@ServiceMethod(code = "001", description = "获取员工列表")
	public JSONObject getAdminList(AdminSocketServer adminSocketServer){
		if(!roleService.isAdmin(adminSocketServer.getAdmin())){
			throwExp("权限不足");
		}
		JSONObject result = new JSONObject();
		List<Admin> list = adminService.getAdminList();
		result.put("admins", list);
		result.put("roles", Role.ROLES);
		return result; 
	}

	@Transactional
	@ServiceMethod(code = "002", description = "新增员工")
	public Admin addAdmin(AdminSocketServer adminSocketServer, Admin admin) throws Exception{
		checkNull(admin);
		checkNull(admin.getName(), admin.getUsername(), admin.getRoleId());
		if(!roleService.isAdmin(adminSocketServer.getAdmin())){
			throwExp("权限不足");
		}
		if(!Role.ROLES.containsKey(admin.getRoleId())){
			throwExp("该角色不存在");
		}
		Admin adminByUsername = adminService.getAdminByUsername(admin.getUsername());
		if(adminByUsername != null){
			throwExp("员工信息已存在");
		}
		admin.setId(UID.create());
		if(admin.getPhoto() != null && admin.getPhoto().length() > 255) {
			saveAdminPhoto(admin.getPhoto(), admin.getId());
		}
		if(isNull(admin.getPassword())) {
			admin.setPassword("123456a");
		}
		admin.setState(Admin.ENABLE_STATE);
		adminService.addAdmin(admin);
		return admin;
	}

	@Transactional
	@ServiceMethod(code = "003", description = "修改员工信息")
	public void updateAdmin(AdminSocketServer adminSocketServer, Admin admin) throws Exception{
		checkNull(admin);
		checkNull(admin.getId());
		if(!roleService.isAdmin(adminSocketServer.getAdmin())){
			throwExp("权限不足");
		}
		if(admin.getState() != null && admin.getState() != Admin.DISABLE_STATE && admin.getState() != Admin.ENABLE_STATE){
			throwExp("未知的账户状态");
		}
		Admin adminById = adminService.getAdminById(admin.getId());
		if(adminById == null){
			throwExp("员工信息不存在");
		}
		if(admin.getUsername() != null) {
			Admin adminByUsername = adminService.getAdminByUsername(admin.getUsername());
			if(adminByUsername != null && !eq(adminById.getId(), adminByUsername.getId())){
				throwExp("用户名不可重复");
			}
		}
		if(admin.getPhoto() != null && admin.getPhoto().length() > 255) {
			admin.setPhoto(saveAdminPhoto(admin.getPhoto(), admin.getId()));
		}
		adminService.updateAdmin(admin);
	}
	
	private String saveAdminPhoto(String photoBase64, String id) throws Exception{
		if(Base64Util.getBase64ImageSize(photoBase64) > 600 * 1024) {
			throwExp("最大允许600KB图片上传");
		}
		if(Base64Util.getBase64ImageSize(photoBase64) > 300 * 1024) {
			photoBase64 = Base64Util.getMinBase64(photoBase64);
		}
		String imagePath = adminProperties.get("admin.photo.path"); //图片文件夹路径
		String imageWebPath = adminProperties.get("admin.photo.webPath"); //图片web访问路径
		byte[] photoByte = Base64Util.base64Str2ByteArray(photoBase64);
		String fileName = id + ".jpg";
		File photoFile = new File(imagePath + File.separator + fileName);
		FileUtils.writeByteArrayToFile(photoFile, photoByte);
		return imageWebPath + fileName + "?" + System.currentTimeMillis();
	}
	
	public List<Admin> getKefuList(){
		return adminService.selectAdminItemByRoleId(Role.KEFU);
	}
	
	public List<Admin> getAdminRoleList(){
		return adminService.selectAdminItemByRoleId(Role.ADMIN);
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
	
}
