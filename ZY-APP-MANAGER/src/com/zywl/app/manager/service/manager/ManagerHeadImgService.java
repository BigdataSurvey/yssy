package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.HeadImg;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Base64Util;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.AdminSocketServer;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;


@Service
@ServiceClass(code = MessageCodeContext.HEAD_IMG_SERVER)
public class ManagerHeadImgService extends BaseService {

	@Autowired
	private ManagerConfigService managerConfigService;

	private PropertiesUtil bannerProperties;



	@PostConstruct
	public void _Construct() {
		bannerProperties = new PropertiesUtil("static.properties");
	}
	
	@ServiceMethod(code="001", description = "获取头像图列表")
	public List<HeadImg> getBannerList(){
		return getHeadImg();
	}

	public List<HeadImg> getHeadImg(){
		String headImgStr = managerConfigService.getString(Config.APP_HOME_HEAD_IMG);
		if(isNotNull(headImgStr)) {
			return JSON.parseArray(headImgStr, HeadImg.class);
		}else {
			return null;
		}
	}
	
	@Transactional
	@ServiceMethod(code="002", description = "更新头像数据")
	public void setHeadImgList(AdminSocketServer adminSocketServer, String headImgStr) throws IOException {
		if(isNull(headImgStr)) {
			headImgStr = "[]";
		}else {
			List<HeadImg> parseArray = JSON.parseArray(headImgStr, HeadImg.class);
			for (HeadImg headImg : parseArray) {
				checkNull(headImg.getImg3_1());
				if(headImg.getImg3_1().length() > 255) {
					headImg.setImg3_1(saveImage(headImg.getImg3_1()));
				}
				headImg.setUrl(headImg.getImg3_1());
			}
			headImgStr = JSON.toJSONString(parseArray);
		}
		managerConfigService.updateConfigData(Config.APP_HOME_HEAD_IMG, headImgStr);
		managerConfigService.updateGameKey(Config.APP_HOME_HEAD_IMG, headImgStr);
	}
	
	@Transactional
	@ServiceMethod(code="003", description = "删除头像")
	public void deleteBannerList(AdminSocketServer adminSocketServer, JSONObject params) {
		checkNull(params);
		checkNull(params.get("index"));
		int index = params.getInteger("index");
		List<HeadImg> parseArray = JSON.parseArray(managerConfigService.getString(Config.APP_HOME_HEAD_IMG), HeadImg.class);
		if(index > parseArray.size()) {
			throwExp("要删除的资源不存在");
		}
		HeadImg headImg = parseArray.remove(index);
		
		String headImgStr = JSON.toJSONString(parseArray);
		managerConfigService.updateConfigData(Config.APP_HOME_HEAD_IMG, headImgStr);
		managerConfigService.updateGameKey(Config.APP_HOME_HEAD_IMG, headImgStr);
		
		if(headImg != null) {
			if(isNotNull(headImg.getImg3_1())) {
				deleteImage(headImg.getImg3_1());
			}
		}
	}
	
	public String saveImage(String base64) throws IOException {
		String imageWebPath = bannerProperties.get("head.img.webPath"); //图片web访问路径
		byte[] photoByte = Base64Util.base64Str2ByteArray(base64);
		String fileName = System.currentTimeMillis() + ".png";
		String path = this.getClass().getClassLoader().getResource("../../").getPath()+"data/head/";
		File file = new File(path + fileName);
		FileUtils.writeByteArrayToFile(file, photoByte);
		return imageWebPath + fileName;
	}
	
	public void deleteImage(String image) {
		String imagePath = this.getClass().getClassLoader().getResource("../../").getPath()+"data/head/"; //图片文件夹路径
		String imageWebPath = bannerProperties.get("head.img.webPath"); //图片web访问路径
		String fileName = image.replace(imageWebPath, "");
		File file = new File(imagePath + File.separator + fileName);
		if(file.exists()) {
			file.delete();
		}
	}

}
