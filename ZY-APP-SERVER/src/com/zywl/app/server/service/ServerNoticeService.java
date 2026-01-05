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

	@ServiceMethod(code = "001", description = "查看公告列表")
	public Object getNoticeHistory(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		return noticeService.findHistoryNotice();
	}

	/**
	 * 新增查看公告详情 不然公告多了列表返回很大 前端渲染就慢
	 * **/
	@ServiceMethod(code = "002", description = "查看公告详情")
	public Object getNoticeInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		Long noticeId = params.getLong("noticeId");
		if (noticeId == null || noticeId <= 0) {
			throwExp("参数错误");
		}
		Object notice = noticeService.getNoticeById(noticeId);
		if (notice == null) {
			throwExp("公告不存在");
		}
		return notice;

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

