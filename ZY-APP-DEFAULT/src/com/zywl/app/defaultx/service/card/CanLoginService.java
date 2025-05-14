package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.BuyGiftRecord;
import com.zywl.app.base.bean.card.CanLogin;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CanLoginService extends DaoService{

	public CanLoginService( ) {
		super("CanLoginMapper");
	}


	public List<CanLogin> findAllCanLogin(){
		return findAll();
	}

}
