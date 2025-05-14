package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.LogLogin;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class LogLoginService extends DaoService{

	public LogLoginService( ) {
		super("LogLoginMapper");
	}


	@Transactional
	public void addLog(Long userId,String type,String requestId,String deviceId,String code,String riskLevel,String detail,String model,
					   String ip,String ipCountry,String ipProvince,String ipCity,String message){

		LogLogin logLogin = new LogLogin();
		logLogin.setCode(code);
		logLogin.setCreateTime(new Date());
		logLogin.setDetail(detail);
		logLogin.setDeviceId(deviceId);
		logLogin.setIp(ip);
		logLogin.setIpCity(ipCity);
		logLogin.setIpCountry(ipCountry);
		logLogin.setMessage(message);
		logLogin.setModel(model);
		logLogin.setUserId(userId);
		logLogin.setType(type);
		logLogin.setIpProvince(ipProvince);
		logLogin.setRequestId(requestId);
		logLogin.setRiskLevel(riskLevel);
		Map map = objectToMap(logLogin);
		map.put("tableName", LogLogin.tablePrefix+userId.toString().charAt(userId.toString().length()-1));
		insert(map);

	}
	
	public List<LogLogin> findAllLv(){
		return findAll();
	}
}
