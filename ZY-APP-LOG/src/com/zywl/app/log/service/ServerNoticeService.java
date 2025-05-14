package com.zywl.app.log.service;

import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.service.NoticeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 使用该类通知会使用Socket通道以及外部离线通道一起推送
 * @author Administrator
 *
 */

public class ServerNoticeService extends BaseService {

	private static final Log logger = LogFactory.getLog(ServerNoticeService.class);
	
	private static boolean notice = false;
	
	@Autowired
	private NoticeService noticeService;
	

	

	
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
