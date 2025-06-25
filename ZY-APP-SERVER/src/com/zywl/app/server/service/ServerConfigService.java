package com.zywl.app.server.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.ConcurrentHashSet;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.ConfigService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;

import javax.annotation.PostConstruct;

@Service
@ServiceClass(code = MessageCodeContext.CONFIG_SETVER)
public class ServerConfigService extends BaseService {

	private final static Map<String, String> CONFIG = new ConcurrentHashMap<String, String>();

	private final static Set<String> WHITE_LIST = new ConcurrentHashSet<String>();
	
	
	@Autowired
	private ConfigService configService;

	@PostConstruct
	public void _ServerConfigService(){
		initCache();
	}

	public void initCache(){
		CONFIG.clear();
		List<Config> findAll = configService.findAll();
		for (Config config : findAll) {
			setConfigCache(config);
		}
	}

	static {
		WHITE_LIST.add(Config.TRAD_MIN);
		WHITE_LIST.add(Config.TRAD_MAX);
	}

	public void setConfigCache(Config config){
		CONFIG.put(config.getKey(), config.getValue());
	}

	public Map<String, String> getConfigData(){
		return CONFIG;
	}
	
	public Object getConfig(String key){
		return CONFIG.get(key);
	}

	public String getString(String key){
		return CONFIG.get(key);
	}
	
	public BigDecimal getBigDecimal(String key){
		return new BigDecimal(getString(key));
	}
	
	public Long getLong(String key){
		return Long.parseLong(getString(key));
	}
	
	public Double getDouble(String key){
		return Double.parseDouble(getString(key));
	}
	
	public Integer getInteger(String key){
		return Integer.parseInt(getString(key));
	}
	
	public BigDecimal getBoolean(String key){
		return new BigDecimal(getString(key));
	}
	
	public JSONObject getAllWhiteConfig(){
		JSONObject result = new JSONObject();
		for (String key : WHITE_LIST) {
			result.put(key, getConfig(key));
		}
		return result;
	}
	
	@ServiceMethod(code="001", description="获取白名单配置")
	public Object getWhiteListConfig(AppSocket appSocket, Command command, JSONObject params){
		checkNull(params);
		String keyStr = params.getString("key");
		checkNull(keyStr);
		String keyItems[] = keyStr.replaceAll(" ", "").split(",");
		JSONObject result = new JSONObject();
		for (String key : keyItems) {
			if(WHITE_LIST.contains(key)){
				result.put(key, getConfig(key));
			}
		}
		return result;
	}
}
