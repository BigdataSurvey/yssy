package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.ArrayList;
import java.util.List;

public class Config extends BaseBean {

	public static final String ACTIVE1 = "ACTIVE1";
	public static final String ACTIVE2 = "ACTIVE2";
	public static final String ACTIVE3 = "ACTIVE3";
	public static final String SHOW_TOP_LIST = "SHOW_TOP_LIST";
	public static final String RED_SEND_COUNT = "RED_SEND_COUNT";
	public static final String RED_RATE = "RED_RATE";

	public static final String CHANNEL_RATE = "CHANNEL_RATE";

	public static final String RED_NUMBER = "RED_NUMBER";
	public static final String LHD_KILL_RATE = "GAME_LHD_KILL_RATE";
	public static final String DTS_KILL_RATE = "GAME_DTS_KILL_RATE";
	public static final String DTS_BOT_MONEY = "GAME_DTS_BOT_MONEY";
	public static final String NEW_USER_TIME = "NEW_USER_TIME";
	public static final String MZ_NEED_WHITE = "MZ_NEED_WHITE";
	public static final String ALIPAY_ONE_MONEY = "ALIPAY_ONE_MONEY";
	public static final String FREE_ROLE_NUM = "FREE_ROLE_NUM";
	public static final String UPDATE_ACH = "UPDATE_ACH";
	public static final String IS_AUTO_PAY = "IS_AUTO_PAY";
	public static final String MAX_BUY_GIFT_NUMBER = "MAX_BUY_GIFT_NUMBER";
	public static final String VV_USER_GIFT = "VV_USER_GIFT";
	public static final String TEST_USER_NO = "TEST_USER_NO";
	public static final String PAY_CHANNEL = "PAY_CHANNEL";

	public static final String PAY_NOTIFY_HF_URL = "PAY_NOTIFY_HF_URL";

	public static final String GIFT_PRICE_2_GAME = "GIFT_PRICE_2_GAME";
	public static final String GIFT_PRICE_1_GAME = "GIFT_PRICE_1_GAME";

	public static final String HF_SYS_ID = "HF_SYS_ID";
	public static final String HF_RSA_PRIVATE_KEY = "HF_RSA_PRIVATE_KEY";
	public static final String HF_RSA_PUBLIC_KEY = "HF_RSA_PUBLIC_KEY";
	public static final String GIFT_RMB_STATUS = "GIFT_RMB_STATUS";

	public static final String GIFT_GAME_STATUS = "GIFT_GAME_STATUS";

	public static final String JZ_ITEM = "JZ_ITEM";
	public static final String ADD_HP_WFSB = "ADD_HP_WFSB";
	public static final String PAY_NOTIFY_URL = "PAY_NOTIFY_URL";

	public static final String PAY_REDIRECT_URL = "PAY_REDIRECT_URL";
	public static final String PAY_MERCHANT_ID = "PAY_MERCHANT_ID";
	public static final String GIFT_PRICE_1 = "GIFT_PRICE_1";

	public static final String GIFT_PRICE_2 = "GIFT_PRICE_2";

	public static final String QQ = "QQ";


	public static final String LHD_STATUS="LHD_STATUS";
	public static final String QNYH_RATE="QNYH_RATE";

	public static final String DTS2_STATUS="GAME_DTS2_STATUS";

	public static final String GAME_DTS2_NEED_BOT="GAME_DTS2_NEED_BOT";

	public static final String GAME_LHD_NEED_BOT="GAME_LHD_NEED_BOT";

	public static final String GAME_LHD_KKK="GAME_LHD_KKK";

	public static final String DAILY_STOLEN_COUNT = "DAILY_STOLEN_COUNT";
	public static final String DTS_STATUS="GAME_DTS_STATUS";
	public static final String DZ_GAME_ON = "GAME_DZ_GAME_ON";
	public static final String SG_RATE = "SG_RATE";
	public static final String SG_STATUS="GAME_SG_STATUS";
	public static final String DZ_LUCK_USER = "DZ_LUCK_USER";

	public static final String SG_ISK="SG_ISK";
	public static final String IP_USER_NUMBER = "IP_USER_NUMBER";

	public static final String IP_LOGIN_RISK = "IP_LOGIN_RISK";

	public static final String GOOD_AD = "GOOD_AD";
	public static final String IS_REGISTER = "IS_REGISTER";
	public static final String VIP_MONTH_PRICE = "VIP_MONTH_PRICE";
	public static final String VIP_WEEK_PRICE = "VIP_WEEK_PRICE";
	public static final String REAL_NAME_REWARD = "REAL_NAME_REWARD";

	public static final String REGISTER_NUM = "REGISTER_NUM";


	/** 服务状态 **/
	public static final String SERVICE_STATUS = "SERVICE_STATUS";



	/** APP资源文件在线地址 */
	public static final String APP_RESOURCE_ONLINE_URL = "APP_RESOURCE_ONLINE_URL";



	/** 用户ID长度 */
	public static final String USER_NO_LENGTH = "USER_NO_LENGTH";


	public static final String REFRESH_USER_CAPITAL="REFRESH_USER_CAPITAL";
	public static final String REFRESH_USER_COIN="REFRESH_USER_COIN";
	public static final String REFRESH_USER_ITEM="REFRESH_USER_ITEM";
	//苹果版本
	public static final String IPHONE_V="IPHONE_V";
	// 交易行手续费
	public static final String TRADING_FEE = "TRADING_FEE";


	// 提现手续费
	public static final String CASH_FEE = "CASH_FEE";

	// 余额兑换比例
	public static final String CONVERT_RATE = "CONVERT_RATE";


	// 邮件有效期
	public static final String MAIL_VALIDITY = "MAIL_VALIDITY";

	// 排行榜显示数量
	public static final String TOP_NUMBER = "TOP_NUMBER";

	// 物品表版本
	public static final String ITEM_VERSION = "VERSION_ITEM";

	public static final String SHOP_VERSION = "VERSION_SHOP";


	public static final String MINE_VERSION = "VERSION_MINE";

	public static final String ROLE_VERSION = "VERSION_ROLE";



	//开通公会质押价格
	public static final String GUILD_FEE = "GUILD_FEE";

	//店长申请通宝余额
	public static final String SHOP_MANAGER = "SHOP_MANAGER";

	//添加公会成员质押价格
	public static final String GUILD_MEMBER_FEE = "GUILD_MEMBER_FEE";

	//当前开通渠道需要的收益
	public static final String CHANNEL_FEE="CHANNEL_FEE";

	public static final String PLAYGAME_1_STATUS = "PLAYGAME_1_STATUS";

	public static final String PLAYGAME_2_STATUS = "PLAYGAME_2_STATUS";

	public static final String PLAYTEST_RATE= "PLAYTEST_RATE";

	public static final String PLAYTEST_TO_PARENT_RATE= "PLAYTEST_TO_PARENT_RATE";


	public static final String BAI_IP= "BAI_IP";

	public static final String SIGN_REWARD="SIGN_REWARD";

	public static final String CHANNEL_MAX_NUM= "CHANNEL_MAX_NUM";

	public static final String CHANNEL_CASH_SILL= "CHANNEL_CASH_SILL";

	public static final String SYS_TRADING_USER_ID= "SYS_TRADING_USER_ID";

	public static final String HOME_POPUP= "HOME_POPUP";


	public static final String APP_VERSION= "APP_VERSION";


	public static final String SERVER_MAX_CONNECT="SERVER_MAX_CONNECT";



	public static final String RANK_IS_OPEN="RANK_IS_OPEN";


	public static final String PAY_TYPE = "PAY_TYPE";

	public static final String TRAD_MIN = "TRAD_MIN";
	public static final String TRAD_MAX = "TRAD_MAX";

	public static final String GZS_FK="GZS_FK";


	public static final String ALIPAY_CASH_TYPE = "ALIPAY_CASH_TYPE";

	public static final String ALIPAY_MAX_NUMBER = "ALIPAY_MAX_NUMBER";


	public static final String CASH_LIMIT_DAY = "CASH_LIMIT_DAY";

	public static final String CASH_LIMIT_TIPS = "CASH_LIMIT_TIPS";


	private String key;

	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
