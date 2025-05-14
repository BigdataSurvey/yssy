package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.Sign;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Service
public class SignService extends DaoService{
	
	@Autowired
	private LogSignService logSignService;

	public SignService() {
		super("SignMapper");
		// TODO Auto-generated constructor stub
	}
	
	@Transactional
	public void addSign(Long userId,int signNums) {
		Sign sign = new Sign();
		sign.setDays(1);
		sign.setLastSignTime(new Date());
		sign.setWeekDays(1);
		sign.setMonthDays(1);
		sign.setYearDays(1);
		sign.setSignNums(signNums);
		sign.setUserId(userId);
		save(sign);
	}
	
	@Transactional
	public int userSign(Long userId,int type,int patchDays) {
		Sign sign = findUserSign(userId);
		if (sign==null) {
			int singNums = 0;
			if (DateUtil.getDay(new Date())!=patchDays) {
				singNums = 1;
			}
			addSign(userId,singNums);
			return 1;
		}
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		if (DateUtil.isIn(sign.getLastSignTime())) {
			//昨天签到了 今天需判断是连续的第几天
			if (sign.getWeekDays()==7) {
				params.put("weekDays", 1);
			}else {
				params.put("weekDays", sign.getWeekDays()+1);
			}
			if (sign.getMonthDays()==30) {
				params.put("monthDays", 1);
			}else {
				params.put("monthDays", sign.getMonthDays()+1);
			}
			
			if (sign.getYearDays()==365) {
				params.put("yearDays", 1);
			}else {
				params.put("yearDays", sign.getYearDays()+1);
			}
		}else {
			params.put("weekDays", 1);
			params.put("monthDays", 1);
			params.put("yearDays", 1);
		}
		params.put("lastSignTime", new Date());
		int a= execute("userSign", params);
		return a;
		
		
		
	}
	
	public Sign findUserSign(Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId", userId);
		return findOne(params);
	}

}
