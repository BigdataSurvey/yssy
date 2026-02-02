package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DicVip;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DicVipService extends DaoService {
	private static final Log logger = LogFactory.getLog(DicVipService.class);

	public DicVipService() {
		super("DicVipMapper");
	}

	/**
	 * 查询VIP配置信息
	 */
	public List<DicVip> findAllVip() {
		return findAll();
	}

	/**
	 * 根据主键查询
	 */
	@Transactional(readOnly = true)
	public DicVip findOneById(Long id) {
		return (DicVip) findOne("selectByPrimaryKey", id);
	}

	/**
	 * 条件查询
	 * **/
	@Transactional(readOnly = true)
	public int countByConditions(Map<String,Object> cond) {
		return (Integer) findOne("countByConditions", cond);
	}


	
}
