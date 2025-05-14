package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Notice;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticeService extends DaoService {

	private static final Log logger = LogFactory.getLog(NoticeService.class);

	public NoticeService() {
		super("NoticeMapper");
	}


	@Transactional
	public void addNotice(String title ,String context,int type){
		Notice notice = new Notice();
		insert(notice);
	}
	
	@Transactional
	public int deleteNoticeById(String id){
		JSONObject parameters = new JSONObject();
		parameters.put("id", id);
		return execute("deleteNoticeById", parameters);
	}
	

	public List<Notice> findHistoryNotice(){
		return findAll();
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
	
}
