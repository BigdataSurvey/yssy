package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.DicDrawProbability;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DicDrawProbabilityService extends DaoService {

	public DicDrawProbabilityService() {
		super("DicDrawProbabilityMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(DicDrawProbabilityService.class);

	
	public List<DicDrawProbability> findAllDrawProbability() {
		return findAll();
	}
	



	@Override
	protected Log logger() {
		return logger;
	}
	
	
}
