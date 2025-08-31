package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DicHandBook;
import com.zywl.app.base.bean.DicPit;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DicHandBookService extends DaoService {

	public DicHandBookService() {
		super("DicHandBookMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(DicHandBookService.class);

	
	public List<DicHandBook> findAllHandBook() {
		return findAll();
	}





	@Override
	protected Log logger() {
		return logger;
	}
	
	
}
