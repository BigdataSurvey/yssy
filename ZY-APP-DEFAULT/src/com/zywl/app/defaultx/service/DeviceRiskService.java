package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.DeviceRisk;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DeviceRiskService extends DaoService{

	public DeviceRiskService( ) {
		super("DeviceRiskMapper");
	}


	
	public List<DeviceRisk> findAllDeviceRisk(){
		return findAll();
	}

	@Transactional
	public int updateStatus(int status,long id){
		Map<String,Object> params = new HashMap<>();
		params.put("status",status);
		params.put("id",id);
		return execute("updateDeviceRiskStatus",params);
	}

	public List<DeviceRisk> findByStatus(int status){
		Map<String,Object> params = new HashMap<>();
		params.put("status",status);
		return findByConditions(params);
	}

	public DeviceRisk findById(long id){
		Map<String,Object> params = new HashMap<>();
		params.put("id",id);
		return (DeviceRisk) findOne("findById",params);
	}
}
