package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.NoticeService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.stereotype.Service;

@Service
@ServiceClass(code = MessageCodeContext.NOTIC_SERVER)
public class ManagerNoticeService extends BaseService {
	

	private NoticeService noticeService;

	@ServiceMethod(code = "001", description = "获取历史公告")
	public Object broadcastPush(ManagerSocketServer managerSocketServer, JSONObject params){
		return noticeService.findHistoryNotice();
	}
	

}
