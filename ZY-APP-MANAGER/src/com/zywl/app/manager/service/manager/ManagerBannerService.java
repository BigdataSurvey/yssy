package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.ShopManagerProduct;
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
@ServiceClass(code = MessageCodeContext.BANNER_SERVER)
public class ManagerBannerService extends BaseService {

	@Autowired
	private ManagerConfigService managerConfigService;

	private PropertiesUtil bannerProperties;



	private String basePath = "D:\\apache-tomcat-8.5.43\\webapps\\data\\banner\\";
	
	@PostConstruct
	public void _Construct() {
		bannerProperties = new PropertiesUtil("static.properties");
	}
	
	@ServiceMethod(code="001", description = "获取店长产品图列表")
	public List<ShopManagerProduct> getBannerList(AdminSocketServer adminSocketServer){
		return getShopManagerProduct();
	}

	public List<ShopManagerProduct> getShopManagerProduct(){
		String bannerStr = managerConfigService.getString(Config.APP_SHOP_MANAGER);
		if(isNotNull(bannerStr)) {
			return JSON.parseArray(bannerStr, ShopManagerProduct.class);
		}else {
			return null;
		}
	}
	
	@Transactional
	@ServiceMethod(code="002", description = "更新banner数据")
	public void setBannerList(AdminSocketServer adminSocketServer, String bannerStr) throws IOException {
		if(isNull(bannerStr)) {
			bannerStr = "[]";
		}else {
			List<ShopManagerProduct> parseArray = JSON.parseArray(bannerStr, ShopManagerProduct.class);
			for (ShopManagerProduct banner : parseArray) {
				checkNull(banner.getImg3_1());
				if(banner.getImg3_1().length() > 255) {
					banner.setImg3_1(saveImage(banner.getImg3_1()));
				}
				banner.setUrl(banner.getImg3_1());
			}
			bannerStr = JSON.toJSONString(parseArray);
		}
		managerConfigService.updateConfigData(Config.APP_SHOP_MANAGER, bannerStr);
	}
	
	@Transactional
	@ServiceMethod(code="003", description = "删除banner")
	public void deleteBannerList(AdminSocketServer adminSocketServer, JSONObject params) {
		checkNull(params);
		checkNull(params.get("index"));
		int index = params.getInteger("index");
		List<ShopManagerProduct> parseArray = JSON.parseArray(managerConfigService.getString(Config.APP_SHOP_MANAGER), ShopManagerProduct.class);
		if(index > parseArray.size()) {
			throwExp("要删除的资源不存在");
		}
		ShopManagerProduct banner = parseArray.remove(index);
		
		String bannerStr = JSON.toJSONString(parseArray);
		managerConfigService.updateConfigData(Config.APP_SHOP_MANAGER, bannerStr);
		
		if(banner != null) {
			if(isNotNull(banner.getImg3_1())) {
				deleteImage(banner.getImg3_1());
			}
		}
	}
	
	public String saveImage(String base64) throws IOException {
		String imageWebPath = bannerProperties.get("banner.img.webPath"); //图片web访问路径
		byte[] photoByte = Base64Util.base64Str2ByteArray(base64);
		String fileName = System.currentTimeMillis() + ".png";
		String path = this.getClass().getClassLoader().getResource("../../").getPath()+"data/banner/";
		File file = new File(path + fileName);
		FileUtils.writeByteArrayToFile(file, photoByte);
		return imageWebPath + fileName;
	}
	
	public void deleteImage(String image) {
		String imagePath = this.getClass().getClassLoader().getResource("../../").getPath()+"data/banner/"; //图片文件夹路径
		String imageWebPath = bannerProperties.get("banner.img.webPath"); //图片web访问路径
		String fileName = image.replace(imageWebPath, "");
		File file = new File(imagePath + File.separator + fileName);
		if(file.exists()) {
			file.delete();
		}
	}

	public static void main(String[] args) throws IOException {
		File file = new File("D:\\apache-tomcat-8.5.43\\webapps\\data\\banner\\123.jpg");
		System.out.println(file.exists());
		if (!file.exists()){
			boolean newFile = file.createNewFile();
			if (newFile){
				System.out.println("创建成功");
			}
		}
	}
}
