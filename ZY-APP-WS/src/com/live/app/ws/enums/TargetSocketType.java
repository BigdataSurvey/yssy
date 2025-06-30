package com.live.app.ws.enums;

/**
 * 服务类型
 * @author DOE
 *
 */
public enum TargetSocketType {
	manager,
	server,
	app,
	adminWeb,
	agentWeb,
	notice,
	//静态文件服务器
	staticFile,
	//大逃杀服务器
	battleRoyale,
	//大逃杀多杀服务器
	battleRoyale2,
	//斗转星移服务器
	starChange,
	//接口服务器
	interfaceServer,

	foodServer,
	logServer,


	//登录服务器 防止注册请求过多
	loginServer,





	//打年兽
	dns,

	nh,
	dgs,
	dts2,
	//算卦
	sg,
	bt;


	
	
	public static TargetSocketType getServerEnum(int gameId) {
		if (gameId==1) {
			return dts2;
		}else if(gameId==2) {
			return starChange;
		}else if(gameId==3) {
			return foodServer;
		} else if (gameId==4) {
			return dns;
		}else if(gameId==5){
			return nh;
		} else if (gameId==7) {
			return battleRoyale;
		} else if (gameId==8) {
			return sg;
		}else if (gameId==9) {
			return loginServer;
		}else if (gameId==10) {
			return dgs;
		}
		return null;
	}
	
	
	
}
