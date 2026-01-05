package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.socket.BaseServerSocket;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Notice;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.NoticeService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: lzx
 * @Create: 2026/1/5
 * @Version: V2.0
 * @Description: 公告 Manager 优化
 * @Task: NOTIC_SERVER 016
 */

@Service
@ServiceClass(code = MessageCodeContext.NOTIC_SERVER)
public class ManagerNoticeService extends BaseService {
	@Autowired
	private NoticeService noticeService;

	@ServiceMethod(code = "001", description = "获取历史公告")
	public Object broadcastPush(ManagerSocketServer socket, JSONObject params){
		return noticeService.findHistoryNotice();
	}

	@ServiceMethod(code = "002", description = "公告-新增")
	@Transactional
	public Object addNotice(ManagerSocketServer socket, JSONObject params) {
		checkNull(params);

		String title = params.getString("title");
		String context = params.getString("context");
		Integer type = params.getInteger("type");

		if (title == null || title.trim().isEmpty()) {
			throwExp("公告标题不能为空");
		}
		if (context == null || context.trim().isEmpty()) {
			throwExp("公告内容不能为空");
		}
		if (type == null) {
			type = 1;
		}

		Notice notice = noticeService.addNotice(title.trim(), context.trim(), type);

		// 新增后立即推送给在线用户
		Integer push = params.getInteger("push");
		if (push != null && push == 1) {
			pushNoticeText(notice.getContext());
		}

		JSONObject resp = new JSONObject();
		resp.put("notice", notice);
		return resp;
	}

	@ServiceMethod(code = "003", description = "公告-删除")
	@Transactional
	public Object deleteNotice(ManagerSocketServer socket, JSONObject params) {
		checkNull(params);
		Long id = params.getLong("id");
		if (id == null || id <= 0) {
			throwExp("id不能为空");
		}
		int rows = noticeService.deleteNoticeById(id);
		JSONObject resp = new JSONObject();
		resp.put("rows", rows);
		return resp;
	}

	@ServiceMethod(code = "004", description = "公告-编辑")
	@Transactional
	public Object updateNotice(ManagerSocketServer socket, JSONObject params) {
		checkNull(params);

		Long id = params.getLong("id");
		if (id == null || id <= 0) {
			throwExp("id不能为空");
		}

		String title = params.getString("title");
		String context = params.getString("context");
		Integer type = params.getInteger("type");

		if (title == null || title.trim().isEmpty()) {
			throwExp("title不能为空");
		}
		if (context == null || context.trim().isEmpty()) {
			throwExp("context不能为空");
		}
		if (type == null) {
			type = 1;
		}

		int rows = noticeService.updateNotice(id, title.trim(), context.trim(), type);

		// 编辑后立即推送给在线用户
		Integer push = params.getInteger("push");
		if (push != null && push == 1) {
			pushNoticeText(context.trim());
		}

		JSONObject resp = new JSONObject();
		resp.put("rows", rows);
		return resp;
	}

	@ServiceMethod(code = "005", description = "公告-推送")
	public Object pushNotice(ManagerSocketServer socket, JSONObject params) {
		checkNull(params);

        /**
         两种推送方式:
         1.传Id 就从表里取Notice.context 推送
         2.传notice 就直接推送文本
         */
		Long id = params.getLong("id");
		String noticeText = params.getString("notice");

		if (id != null && id > 0) {
			Notice notice = noticeService.getNoticeById(id);
			if (notice == null) {
				throwExp("公告不存在");
			}
			noticeText = notice.getContext();
		}

		if (noticeText == null || noticeText.trim().isEmpty()) {
			throwExp("notice不能为空");
		}

		pushNoticeText(noticeText.trim());

		JSONObject resp = new JSONObject();
		resp.put("success", true);
		return resp;
	}

	private void pushNoticeText(String noticeText) {
		JSONObject data = new JSONObject();
		data.put("notice", noticeText);
		Push.push(PushCode.sendNotice, null, data);
	}

}
