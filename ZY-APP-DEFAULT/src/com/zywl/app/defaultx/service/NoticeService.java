package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Notice;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @Author: lzx
 * @Create: 2026/1/5
 * @Version: V2.0
 * @Description: 公告 Service 优化
 */

@Service
public class NoticeService extends DaoService {

	private static final Log logger = LogFactory.getLog(NoticeService.class);

	public NoticeService() {
		super("NoticeMapper");
	}

	/**
	 * 历史公告列表
	 * **/
	public List<Notice> findHistoryNotice(){
		return findAll();
	}

	/**
	 * 新增公告
	 * **/
	@Transactional
	public Notice addNotice(String title, String context, int type) {
		Notice notice = new Notice();
		notice.setTitle(title);
		notice.setContext(context);
		notice.setType(type);
		notice.setCreateTime(new Date());
		insert(notice);
		return notice;
	}

	/**
	 * 公告删除
	 * **/
	@Transactional
	public int deleteNoticeById(Long id) {
		JSONObject parameters = new JSONObject();
		parameters.put("id", id);
		return execute("deleteNoticeById", parameters);
	}

	/**
	 * 公告更新
	 * **/
	@Transactional
	public int updateNotice(Long id, String title, String context, Integer type) {
		Notice notice = new Notice();
		notice.setId(id);
		notice.setTitle(title);
		notice.setContext(context);
		if (type != null) {
			notice.setType(type);
		}
		return update(notice);
	}

	/**
	 * 获取单条公告
	 * **/
	public Notice getNoticeById(Long id) {
		JSONObject p = new JSONObject();
		p.put("id", id);
		return (Notice) findOne("findOne", p);
	}


	
	@Override
	protected Log logger() {
		return logger;
	}
	
}
