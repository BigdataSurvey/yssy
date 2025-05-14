package com.zywl.app.manager.service;

import com.zywl.app.base.service.BaseService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

/**
 * 使用该类通知会使用Socket通道以及外部离线通道一起推送
 * @author Administrator
 *
 */
@Service
public class ServerNoticeService extends BaseService {

	private static final Log logger = LogFactory.getLog(ServerNoticeService.class);
	
	private static boolean notice = false;
	


	
	





	public static void setOpenNotice(boolean open){
		if(open){
			notice = true;
			logger.info("打开通知开关");
		}else{
			notice = false;
			logger.info("关闭通知开关");
		}
	}

	public static boolean isOpenNotice(){
		return notice;
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
}
