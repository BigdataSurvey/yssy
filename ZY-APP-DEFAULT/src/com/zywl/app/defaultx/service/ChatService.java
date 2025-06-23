package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.CashTotalInfo;
import com.zywl.app.base.bean.Chat;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.CashStatusTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class ChatService extends DaoService{

	private static final Log logger = LogFactory.getLog(ChatService.class);



	public ChatService() {
		super("ChatMapper");
	}
	
	
	
	
	@Transactional
	public Long addChat(Long userId,String name,String userHeadImg,String text,int type,String orderNo,String userNo,int vipLv) {
		Chat chat = new Chat();
		chat.setType(type);
		chat.setCreateTime(new Date());
		chat.setUserNo(userNo);
		chat.setText(text);
		chat.setUserId(userId);
		chat.setUserName(name);
		chat.setVipLv(vipLv);
		chat.setUserHeadImg(userHeadImg);
		chat.setOrderNo(orderNo);
		save(chat);
		return chat.getId();
	}

	public List<Chat> findLast10(){
		return findList("findLast10",null);
	}

	public Chat findLastType2(){
		Chat chat = (Chat) findOne("findLastType2",null);
		return chat;
	}



}
