package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.NoticeService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 使用该类通知会使用Socket通道以及外部离线通道一起推送
 * @author Administrator
 *
 */
@Service
@ServiceClass(code = MessageCodeContext.NOTICE_SERVER)
public class ServerNoticeService extends BaseService {

	private static final Log logger = LogFactory.getLog(ServerNoticeService.class);
	
	private static boolean notice = false;
	
	@Autowired
	private NoticeService noticeService;




	@ServiceMethod(code = "001", description = "查看邮件列表")
	public Object getMailInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
		return noticeService.findHistoryNotice();
	}





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
