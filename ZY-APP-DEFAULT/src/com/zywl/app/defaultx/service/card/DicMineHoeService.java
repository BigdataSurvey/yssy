package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.DicMineHoe;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DicMineHoeService extends DaoService{

	public DicMineHoeService( ) {
		super("DicMineHoeMapper");
	}

	public List<DicMineHoe> findAllMineHoe(){
		return findAll();
	}


}
