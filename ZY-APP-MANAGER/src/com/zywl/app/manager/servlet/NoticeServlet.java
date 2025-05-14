package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Notice;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.service.NoticeService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.AuthService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@SuppressWarnings("serial")
@WebServlet(name = "NoticeServlet", urlPatterns = "/getNotice")
public class NoticeServlet extends BaseServlet {

	private static final Log logger = LogFactory.getLog(NoticeServlet.class);

	private NoticeService noticeService;


	private AppConfigCacheService appConfigCacheService;
	public NoticeServlet() {
		noticeService = SpringUtil.getService(NoticeService.class);

	}

	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
		request.getSession().invalidate();
		List<Notice> historyNotice = noticeService.findHistoryNotice();
		JSONObject result = JSONUtil.getReturnDate(1, historyNotice, "");
		return result;
	}

	@Override
	protected Log logger() {
		return logger;
	}
}
