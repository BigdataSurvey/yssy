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


/**
 * 配置服务中心
 * 数据来源于Config表
 * **/
@Service
@ServiceClass(code = MessageCodeContext.CONFIG_SERVER)
public class ManagerConfigService extends BaseService {
	//静态初始化的配置服务Map
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
	private ManagerBuyGiftService managerBuyGiftService;

	@Autowired
	private UserCacheService userCacheService;


	//容器启动后创建该bend 推送配置信息
	@PostConstruct
	public void _construct() {
		//推送配置更新
		Push.addPushSuport(PushCode.updateConfig, new DefaultPushHandler());
		//弹公告/弹窗
		Push.addPushSuport(PushCode.sendNotice, new DefaultPushHandler());
		//聊天消息
		Push.addPushSuport(PushCode.chat, new DefaultPushHandler());
		//静态表版本更新
		Push.addPushSuport(PushCode.updateTableVersion, new DefaultPushHandler());
		//猜灯谜推送
		Push.addPushSuport(PushCode.caidengmi, new DefaultPushHandler());
		//将Config信息赋值到Map
		List<Config> findAll = configService.findAll();
		for (Config config : findAll) {
			setConfigCache(config);
		}
		logger.info("config信息：" + CONFIG);
	}

	public void setConfigCache(Config config) {
		CONFIG.put(config.getKey(), config.getValue());
	}

	public Map<String, String> getConfigData() {
		return CONFIG;
	}

	public Object getConfig(String key) {
		return CONFIG.get(key);
	}

	public String getString(String key) {
		return CONFIG.get(key);
	}

	public BigDecimal getBigDecimal(String key) {
		return new BigDecimal(getString(key));
	}

	public Long getLong(String key) {
		return Long.parseLong(getString(key));
	}

	public Double getDouble(String key) {
		if (StringUtils.isEmpty(getString(key))) {
			return (double) 0;
		}
		return Double.parseDouble(getString(key));
	}

	public Integer getInteger(String key) {
		return Integer.parseInt(getString(key));
	}

	public Boolean getBoolean(String key) {
		return Boolean.parseBoolean(getString(key));
	}

	/**
	 * 获取所有系统配置
	 **/
	@ServiceMethod(code = "001", description = "获取系统配置")
	public Map<String, String> getConfigData(AdminSocketServer adminSocketServer, Command command) {
		Map<String, String> configData = new HashMap<String, String>(getConfigData());
		return configData;
	}

	/**
	 * 修改系统配置
	 * 更新Config表+推送+更新内存Config
	 **/
	@Transactional
	@ServiceMethod(code = "002", description = "修改配置")
	public void updateConfigData(AdminSocketServer adminSocketServer, Command command, JSONObject params) {
		checkNull(params);
		checkNull(params.get("key"), params.get("value"));
		String key = params.getString("key");
		String value = params.getString("value");
		//改 DB + 更新 CONFIG + 推 PushCode.updateConfig
		updateConfigData(key, value);
		//根据 key 做联动 init、清缓存、push 等
		updateGameKey(key, value);

	}

	/**
	 * 获取对应Key的配置信息
	 **/
	@ServiceMethod(code = "003", description = "获取系统配置")
	public Object getConfigByKey(AdminSocketServer adminSocketServer, Command command, JSONObject params) {
		checkNull(params);
		checkNull(params.get("key"));

		String key = params.getString("key");
		return getConfig(key);
	}

	/**
	 * 改 DB + 更新 CONFIG + 推 PushCode.updateConfig
	 * 重载了Config数据库、客户端、内存
	 * **/
	@Transactional
	public void updateConfigData(String key, String value) {

		if (getConfig(key) == null) {
			throwExp("配置不存在");
		}
		Config config = new Config();
		config.setKey(key);
		config.setValue(value);
		//更新数据库
		configService.updateConfig(config);
		//推送Config信息给在线客户端；
		Push.push(PushCode.updateConfig, null, config);
		//更新内存Map
		setConfigCache(config);
		
		/*if(eq(Config.LIVE_SET_LIVE_PASSWORD, key)) {
			aodianyunService.updatePushPassword();
			aodianyunService.updatePlayPassword();
		}
		if(eq(Config.LIVE_VALIDATE_PLAY_IP, key)) {
			aodianyunService.updatePlayPassword();
		}*/
	}

	/**
	 * 修改系统配置时根据不同的Key做差异化的联动操作；
	 * 清缓存、重载表、推送公告等；
	 * 重载表init 是因为本来在gameService中@PostConstruct在服务启动的时候执行了一次，后来如果表改了、版本变了、数据在DB更新了这些那就必须主动调用init才能把最新配置加载进来。
	 * updateGameKey是热更新入口；gameService中@PostConstruct冷启动初始化.
	 **/
	public void updateGameKey(String key, String value) {
		if (key.equals(Config.CASH_FEE)) {
			//清除缓存中的提现手续费
			appConfigCacheService.removeConvertRate();
		} else if (key.equals(Config.CONVERT_RATE)) {
			//清除缓存中的余额兑换比例
			appConfigCacheService.removerConvertRate();
		} else if (key.equals(Config.MAIL_VALIDITY)) {
			//清除缓存中的邮件有效期
			appConfigCacheService.removeKey(RedisKeyConstant.APP_MAIL_VALIDITY);
		} else if (key.equals(Config.TOP_NUMBER)) {
			//清除缓存中的排行榜数量
			appConfigCacheService.removeKey(RedisKeyConstant.APP_TOP_NUMBER);
		} else if (key.equals(Config.TRADING_FEE)) {
			//清除缓存中的交易行手续费
			appConfigCacheService.removeTradingRate();
		} else if (key.equals(Config.REGISTER_NUM)) {
			appConfigCacheService.removeKey(RedisKeyConstant.APP_CONFIG_REGISTER_NUMBER);
		} else if (key.equals(Config.BAI_IP)) {
			appConfigCacheService.removeKey(RedisKeyConstant.APP_CONFIG_BAI_IP);
		} else if (key.equals(Config.CHANNEL_CASH_SILL)) {
			appConfigCacheService.del(RedisKeyConstant.APP_CONFIG_CHANNEL_CASH_SILL);
		} else if (key.equals(Config.REFRESH_USER_ITEM)) {
			//清Map缓存中的的玩家背包缓存
			PlayGameService.playerItems.clear();
		} else if (key.equals(Config.REFRESH_USER_CAPITAL)) {
			//清除Map缓存中的玩家资产缓存
			UserCapitalCacheService.userCapitals.clear();
		} else if (key.equals(Config.REFRESH_USER_COIN)) {
			//清除Map缓存中的货币缓存
			PlayGameService.playercoins.clear();
		} else if (key.equals(Config.SHOP_VERSION)) {
			//商城版本更新
			PlayGameService.DIC_SHOP_LIST.clear();
			PlayGameService.DIC_SHOP_MAP.clear();
			//重载表
			gameService.initShop();
		} else if (key.equals(Config.ITEM_VERSION)) {
			//重载道具表
			gameService.initItem();
			//重载手册相关配置
			gameService.initDicHandBook();
			gameService.initDicHandBookReward();
			//重载抽奖配置
			gameService.initPrize();
			//删除redis中的版本号
			appConfigCacheService.removeKey(RedisKeyConstant.APP_CONFIG_VERSION_ITEM);
			//删除redis中游戏内的物品信息
			itemCacheService.removeAllItemCache();
			/*构造静态表更新推送给客户端*/
			//取itemMap中所有item组成一个 list
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
			tableInfo.put("itemTable", obj);
			//推送给所在在线客户端；
			Push.push(PushCode.updateTableVersion, null, tableInfo);
		} else if (key.equals(Config.MINE_VERSION)) {
			//重新玩家矿场表后推送给在线客户端
			gameService.initMine();
			List<DicMine> mines = new ArrayList<>(PlayGameService.DIC_MINE.values());
			JSONObject obj = new JSONObject();
			obj.put("version", value);
			obj.put("data", mines);
			JSONObject tableInfo = new JSONObject();
			tableInfo.put("mineTable", obj);
			Push.push(PushCode.updateTableVersion, null, tableInfo);
		}else if (key.equals(Config.FARM_TABLE_VERSION)) {
			//重置玩家矿场表后重新推送给在线客户端
			gameService.initFarm();
			List<DicFarm> farms = new ArrayList<>(PlayGameService.DIC_FARM.values());
			JSONObject obj = new JSONObject();
			obj.put("version", value);
			obj.put("data", farms);
			JSONObject tableInfo = new JSONObject();
			tableInfo.put("farmTable", obj);
			//推送给在线客户端
			Push.push(PushCode.updateTableVersion, null, tableInfo);
		} else if (key.equals(Config.ROLE_VERSION)) {
			//重新玩家角色表后推送给在线客户端
			gameService.initRole();
			List<DicRole> roles = new ArrayList<>(PlayGameService.DIC_ROLE.values());
			JSONObject obj = new JSONObject();
			obj.put("version", value);
			obj.put("data", roles);
			JSONObject tableInfo = new JSONObject();
			tableInfo.put("roleTable", obj);
			Push.push(PushCode.updateTableVersion, null, tableInfo);
		} else if (key.equals(Config.SERVICE_STATUS)) {
			JSONObject data = new JSONObject();
			data.put("type", 2);
			//强制下线推送到客户端
			Push.push(PushCode.fcAppLoginOut, null, data);
			//把userStatisticMap里的玩家统计数据批量写会数据库，避免中途丢数据
			gameService.updateStatic();
			//把userAchievementMap里的玩家成就数据批量写会数据库，避免中途丢数据
			gameService.updateUserAchievement();
			//managerGameBaseService.persistenceTaskData();
			//managerGameBaseService.persistencePrizeDrawData();
		} else if (key.equals("CESHITANCHUANG")) {
			//测试弹窗
			JSONObject object = new JSONObject();
			object.put("notice", value);
			Push.push(PushCode.sendNotice, null, object);

		} else if (key.equals(Config.APP_VERSION)) {
			//把版本表缓存更新到内存
			versionService.reloadCache();
			//清每个用户本地的版本缓存
			userCacheService.removeVersionCache();
			AuthService.version = null;
		} else if (key.equals(Config.VV_USER_GIFT)) {
			//玩家购买礼包
			if (value.contains("，")) {
				throwExp("请使用英文逗号");
			}
			String[] info = value.split(",");
			String userNo = info[0];
			int number = Integer.parseInt(info[1]);
			//通过value中解析的版本，用户编号,购买礼包数量。之后给玩家发礼包
			managerBuyGiftService.addGift(userNo, number);
		} else if (key.equals(Config.CASH_LIMIT_DAY)) {
			appConfigCacheService.del(RedisKeyConstant.APP_CONFIG_CASH_LIMIT_DAY);
		} else if (key.equals(Config.CASH_LIMIT_TIPS)) {
			appConfigCacheService.del(RedisKeyConstant.APP_CONFIG_CASH_LIMIT_TIPS);
		} else if (key.equals(Config.VIP_MONTH_PRICE)) {
			appConfigCacheService.del(RedisKeyConstant.VIP_MONTH_PRICE);
		} else if (key.equals(Config.VIP_WEEK_PRICE)) {
			appConfigCacheService.del(RedisKeyConstant.VIP_WEEK_PRICE);

		}
	}
}
