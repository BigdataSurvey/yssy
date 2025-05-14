package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DicMineService extends DaoService{

	public DicMineService( ) {
		super("DicMineMapper");
	}

	public List<DicMine> findAllMine(){
		return findAll();
	}


}
