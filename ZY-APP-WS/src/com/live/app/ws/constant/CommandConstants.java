package com.live.app.ws.constant;

/**
 * 
 * @author DOE
 * 
 */
public class CommandConstants {

	/** 心跳 */
	public static final String CMD_PING = "0";

	/** 连接确认 **/
	public static final String CMD_CONNECTED = "0000";
	
	/** 连接失败 */
	public static final String CMD_CONNECT_FAILS = "9999";

	public static final String PING_PONG = "999999";

	/** 注册推送 */
	public static final String CMD_PUSH_REGIST = "0001";
	
	/** 取消推送 */
	public static final String CMD_PUSH_UNREGIST = "0002";
	
	/** 注册推送结果 */
	public static final String CMD_PUSH_REGIST_RESULT = "0003";
	
	/** 强制退出登录 */
	public static final String CMD_FC_LOGINOUT = "0004";
	
	/** 登录超时 */
	public static final String CMD_LOGIN_TIMEOUT = "0005";
	
	/** 系统参数修改 */
	public static final String CMD_UPDATE_CONFIG = "updateConfig";

	public static final String CDM="caidengmi";

	public static final String PVP_SETTLE = "5001002";

	/** 聊天区礼物广播 **/
	public static final String CMD_CHAT_GIFT = "0008";
	
	/** 横幅区礼物广播 **/
	public static final String CMD_BANNER_GIFT = "0009";
	
	/** 修改礼物信息 **/
	public static final String CMD_GIFT_UPDATE = "0010";
	
	/** 新增礼物 **/
	public static final String CMD_GIFT_ADD = "0011";
	
	/** 级别信息更新 */
	public static final String CMD_LEVEL_UPDATE = "0012";

	/** 新增守护信息 **/
	public static final String CMD_GUARD_ADD = "0013";
	
	/** 修改守护信息 **/
	public static final String CMD_GUARD_UPDATE = "0014";

	/** 直播间列表有更新 **/
	public static final String CMD_LIVE_UPDATE = "0015";
	
}