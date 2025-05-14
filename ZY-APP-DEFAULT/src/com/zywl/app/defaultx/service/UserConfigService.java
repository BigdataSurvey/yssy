package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserConfig;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserConfigService extends DaoService{
	
	

	
	private static final Log logger = LogFactory.getLog(UserConfigService.class);
	
	
	public UserConfigService() {
		super("UserConfigMapper");
	}

	
	@Transactional
	public void addUserConfig(UserConfig userConfig){
		userConfig.setCreateTime(new Date());
		userConfig.setUpdateTime(new Date());
		save(userConfig);
	}
	
	public UserConfig findUserConfigByUserId(Long userId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId",userId);
		return findOne( params);
	}
	
	@Transactional
	public int updateUserSoundsSetting(Long userId,int audio,int music) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("userId",userId);
		params.put("audio_setting", audio);
		params.put("music_setting", music);
		return update(params);
	}
	

}
