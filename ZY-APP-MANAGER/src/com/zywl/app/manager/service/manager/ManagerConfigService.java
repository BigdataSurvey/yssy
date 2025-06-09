package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.DicRole;
import com.zywl.app.base.bean.Item;
import com.zywl.app.base.bean.card.*;
import com.zywl.app.base.bean.vo.ItemVo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.ItemCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.service.ConfigService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.AuthService;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.ReleaseAppService;
import com.zywl.app.manager.socket.AdminSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.CONFIG_SERVER)
public class ManagerConfigService extends BaseService {

	private static final Map<String, String> CONFIG = new ConcurrentHashMap<String, String>();


	@Autowired
	private CardGameCacheService cardGameCacheService;

	@Autowired
	private ConfigService configService;

	@Autowired
	private AppConfigCacheService appConfigCacheService;



	@Autowired
	private VersionService versionService;

	@Autowired
	private ManagerGameBaseService managerGameBaseService;

	@Autowired
	private PlayGameService gameService;

	@Autowired
	private ItemCacheService itemCacheService;

	@Autowired
	private ReleaseAppService releaseAppService;
	@Autowired
	ManagerTradingService managerTradingService;


	@Autowired
	private UserCacheService userCacheService;

	@PostConstruct
	public void _construct(){
		Push.addPushSuport(PushCode.updateConfig, new DefaultPushHandler());
		Push.addPushSuport(PushCode.sendNotice, new DefaultPushHandler());
		Push.addPushSuport(PushCode.updateTableVersion, new DefaultPushHandler());
		Push.addPushSuport(PushCode.caidengmi, new DefaultPushHandler());
		List<Config> findAll = configService.findAll();
		for (Config config : findAll) {
			setConfigCache(config);
		}
		logger.info("config信息："+CONFIG);
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
		if (StringUtils.isEmpty(getString(key))) {
			return (double) 0;
		}
		return Double.parseDouble(getString(key));
	}
	
	public Integer getInteger(String key){
		return Integer.parseInt(getString(key));
	}
	
	public Boolean getBoolean(String key){
		return Boolean.parseBoolean(getString(key));
	}
	
	@ServiceMethod(code="001", description="获取系统配置")
	public Map<String, String> getConfigData(AdminSocketServer adminSocketServer, Command command){
		Map<String, String> configData = new HashMap<String, String>(getConfigData());
		return configData;
	}
	
	@Transactional
	@ServiceMethod(code="002", description="修改配置")
	public void updateConfigData(AdminSocketServer adminSocketServer, Command command, JSONObject params){
		checkNull(params);
		checkNull(params.get("key"), params.get("value"));
		String key = params.getString("key");
		String value = params.getString("value");
		updateConfigData(key, value);
		updateGameKey(key,value);

	}

	@ServiceMethod(code="003", description="获取系统配置")
	public Object getConfigByKey(AdminSocketServer adminSocketServer, Command command, JSONObject params){
		checkNull(params);
		checkNull(params.get("key"));
		
		String key = params.getString("key");
		return getConfig(key);
	}
	
	
	@Transactional
	public void updateConfigData(String key, String value){


		if(getConfig(key) == null){
			throwExp("配置不存在");
		}
		Config config = new Config();
		config.setKey(key);
		config.setValue(value);
		configService.updateConfig(config);
		
		Push.push(PushCode.updateConfig, null, config);
		setConfigCache(config);
		
		/*if(eq(Config.LIVE_SET_LIVE_PASSWORD, key)) {
			aodianyunService.updatePushPassword();
			aodianyunService.updatePlayPassword();
		}
		if(eq(Config.LIVE_VALIDATE_PLAY_IP, key)) {
			aodianyunService.updatePlayPassword();
		}*/
	}

	public void updateGameKey(String key,String value){
		if(key.equals(Config.CASH_FEE)){
			appConfigCacheService.removeConvertRate();
		}else if(key.equals(Config.CONVERT_RATE)){
			appConfigCacheService.removerConvertRate();
		}  else if (key.equals(Config.MAIL_VALIDITY)) {
			appConfigCacheService.removeKey(RedisKeyConstant.APP_MAIL_VALIDITY);
		} else if (key.equals(Config.TOP_NUMBER)) {
			appConfigCacheService.removeKey(RedisKeyConstant.APP_TOP_NUMBER);
		}else if (key.equals(Config.TRADING_FEE)) {
			appConfigCacheService.removeTradingRate();
		}else if (key.equals(Config.TRANSFER_FEE)) {
			appConfigCacheService.removeTransferRate();
		}else if (key.equals(Config.TRANSFER_SILL)) {
			appConfigCacheService.removeTransferSill();
		}else if (key.equals(Config.REFRESH_USER_ITEM)) {
			PlayGameService.playerItems.clear();
		}else if (key.equals(Config.REFRESH_USER_CAPITAL)) {
			UserCapitalCacheService.userCapitals.clear();
		}else if (key.equals(Config.REFRESH_USER_COIN)) {
			PlayGameService.playercoins.clear();
		}else if (key.equals(Config.ITEM_VERSION)) {
			gameService.initItem();
			appConfigCacheService.removeKey(RedisKeyConstant.APP_CONFIG_VERSION_ITEM);
			itemCacheService.removeAllItemCache();
			List<Item> items = new ArrayList<>(PlayGameService.itemMap.values());
			List<ItemVo> vos = new ArrayList<>();
			for (Item item : items) {
				ItemVo vo = new ItemVo();
				BeanUtils.copy(item, vo);
				vos.add(vo);
			}
			JSONObject obj = new JSONObject();
			obj.put("version", value);
			obj.put("data", items);
			JSONObject tableInfo = new JSONObject();
			tableInfo.put("itemTable",obj);
			Push.push(PushCode.updateTableVersion,null,tableInfo);
		}else if(key.equals(Config.MINE_VERSION)){
			gameService.initMine();
			List<DicMine> mines = new ArrayList<>(PlayGameService.DIC_MINE.values());
			JSONObject obj = new JSONObject();
			obj.put("version", value);
			obj.put("data", mines);
			JSONObject tableInfo = new JSONObject();
			tableInfo.put("mineTable",obj);
			Push.push(PushCode.updateTableVersion,null,tableInfo);
		}else if(key.equals(Config.ROLE_VERSION)){
			gameService.initRole();
			List<DicRole> roles = new ArrayList<>(PlayGameService.DIC_ROLE.values());
			JSONObject obj = new JSONObject();
			obj.put("version", value);
			obj.put("data", roles);
			JSONObject tableInfo = new JSONObject();
			tableInfo.put("roleTable",obj);
			Push.push(PushCode.updateTableVersion,null,tableInfo);
		}
		else if (key.equals(Config.SERVICE_STATUS)) {
			JSONObject data = new JSONObject();
			data.put("type",2);
			Push.push(PushCode.fcAppLoginOut,null,data);
			//保存服务器数据
			gameService.updateStatic();
			gameService.updateUserAchievement();
			//managerGameBaseService.persistenceTaskData();
			//managerGameBaseService.persistencePrizeDrawData();
		}else if(key.equals("CESHITANCHUANG")){
			JSONObject object = new JSONObject();
			object.put("notice",value);
			Push.push(PushCode.sendNotice,null,object);
		}else if(key.equals(Config.REGISTER_NUM)){
			appConfigCacheService.removeKey(RedisKeyConstant.APP_CONFIG_REGISTER_NUMBER);
		}else if(key.equals(Config.BAI_IP)){
			appConfigCacheService.removeKey(RedisKeyConstant.APP_CONFIG_BAI_IP);
		}else if(key.equals(Config.CHANNEL_CASH_SILL)){
			appConfigCacheService.del(RedisKeyConstant.APP_CONFIG_CHANNEL_CASH_SILL);
		}else if(key.equals(Config.APP_VERSION)){
			versionService.reloadCache();
			userCacheService.removeVersionCache();
			AuthService.version=  null;
		}else if(key.equals(Config.CASH_LIMIT_DAY)){
			appConfigCacheService.del(RedisKeyConstant.APP_CONFIG_CASH_LIMIT_DAY);
		}else if(key.equals(Config.CASH_LIMIT_TIPS)){
			appConfigCacheService.del(RedisKeyConstant.APP_CONFIG_CASH_LIMIT_TIPS);
		}else if(key.equals(Config.VIP_MONTH_PRICE)){
			appConfigCacheService.del(RedisKeyConstant.VIP_MONTH_PRICE);
		}else if(key.equals(Config.VIP_WEEK_PRICE)){
			appConfigCacheService.del(RedisKeyConstant.VIP_WEEK_PRICE);
		}

	}
}
