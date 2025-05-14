package com.zywl.app.manager.service.manager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Banner;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Base64Util;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.AdminSocketServer;

@Service
@ServiceClass(code = MessageCodeContext.BANNER_SERVER)
public class ManagerBannerServicee extends BaseService {

	@Autowired
	private ManagerConfigService managerConfigService;

	private PropertiesUtil bannerProperties;
	
	@PostConstruct
	public void _Construct() {
		bannerProperties = new PropertiesUtil("static.properties");
	}
	
	@ServiceMethod(code="001", description = "获取banner图列表")
	public List<Banner> getBannerList(AdminSocketServer adminSocketServer){
		return null;
	}
	
	@Transactional
	@ServiceMethod(code="002", description = "更新banner数据")
	public void setBannerList(AdminSocketServer adminSocketServer, String bannerStr) throws IOException {
	}
	
	@Transactional
	@ServiceMethod(code="003", description = "删除banner")
	public void deleteBannerList(AdminSocketServer adminSocketServer, JSONObject params) {
		
	}
	
	public String saveImage(String base64) throws IOException {
		String imagePath = bannerProperties.get("banner.img.path"); //图片文件夹路径
		String imageWebPath = bannerProperties.get("banner.img.webPath"); //图片web访问路径
		byte[] photoByte = Base64Util.base64Str2ByteArray(base64);
		String fileName = System.currentTimeMillis() + ".jpg";
		File file = new File(imagePath + File.separator + fileName);
		FileUtils.writeByteArrayToFile(file, photoByte);
		return imageWebPath + fileName;
	}
	
	public void deleteImage(String image) {
		String imagePath = bannerProperties.get("banner.img.path"); //图片文件夹路径
		String imageWebPath = bannerProperties.get("banner.img.webPath"); //图片web访问路径
		
		String fileName = image.replace(imageWebPath, "");
		File file = new File(imagePath + File.separator + fileName);
		if(file.exists()) {
			file.delete();
		}
	}
}
