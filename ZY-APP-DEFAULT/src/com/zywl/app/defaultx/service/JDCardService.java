package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.Game;
import com.zywl.app.base.bean.card.JDCard;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class JDCardService extends DaoService {

	public JDCardService() {
		super("JDCardMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(JDCardService.class);
	
	
	
	
	
	
	

	
	
	public JDCard findNoExchange() {
		return (JDCard) findOne("findNoExchange",null);
	}
	@Transactional
	public void userExchange(Long userId,Long id){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		params.put("id",id);
		execute("exchange",params);
	}

	public List<JDCard> findByUserId(Long userId){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("userId", userId);
		List<JDCard> findByUserId = findList("findByUserId", params);
		return findByUserId;
	}

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}
