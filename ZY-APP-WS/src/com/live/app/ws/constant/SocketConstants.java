package com.live.app.ws.constant;

/**
 * Socket通信常量
 * @author Aaron
 * @date 2017-4-27
 */
public class SocketConstants {

	/** 二进制缓存池最大长度 */
	public static final int SOCKET_SIZE_BINARY = 630 * 1024;
	
	public static final int SOCKET_SIZE_BINARY_MINI = 80 * 1024;
	
	/** 文本缓存池最大长度 */
	public static final int SOCKET_SIZE_TEXT = 8 * 1024;
	
	/** 连接超时 */
	public static final int SOCKET_CONNECT_TIMEOUT = 15 * 1000;
	
	/** 心跳间隔 */
	public static final int SOCKET_CONNECT_HEARTINTERVAL = 5 * 1000;
	
	/** 默认连接握手数据 */
	public static final String SOCKET_CONNECT_SHAKE_HANDS = "/{pk}/{data}";
	
}
