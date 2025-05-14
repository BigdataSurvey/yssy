package com.zywl.app.socket;
import javax.websocket.ClientEndpoint;

import com.live.app.ws.bean.Command;
import com.live.app.ws.interfacex.PushListener;
import com.zywl.app.base.bean.Config;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.service.GameService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.defaultx.service.IncomeRecordService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.GameFoodService;
import com.zywl.app.service.ServerStateService;

@ClientEndpoint
public class ManagerSocket extends BaseClientSocket {
	private static final Log logger = LogFactory.getLog(ManagerSocket.class);

	
	private VersionService versionService;
	
	private GameFoodService gameFoodService;
	
	private IncomeRecordService incomeRecordService;

	private GameService gameService;
	
	
	
	
	
	public ManagerSocket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
		super(socketType, false, reconnect, server, shakeHandsDatas);
		versionService = SpringUtil.getService(VersionService.class);
		incomeRecordService = SpringUtil.getService(IncomeRecordService.class);
		gameFoodService = SpringUtil.getService(GameFoodService.class);
		gameService = SpringUtil.getService(GameService.class);
		
		Push.addPushSuport(PushCode.syncTaskNum, new DefaultPushHandler() {
			public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
				pushBean.setShakeHands(Executer.size() + "," + Executer.QPS());
			}
		});
		Push.addPushSuport(PushCode.syncIsService, new DefaultPushHandler() {
			public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
				pushBean.setShakeHands(ServerStateService.isService());
			}
		});
	}

	@Override
	public void onConnect(Object data) {
		Push.registPush(new PushBean(PushCode.updateConfig), new PushListener() {
			public void onRegist(BaseSocket baseSocket, Object data) {

			}

			public void onReceive(BaseSocket baseSocket, Object data) {
				logger.info("收到系统参数修改推送：" + data);
				JSONObject object = (JSONObject) data;
				Config config = object.toJavaObject(Config.class);
				if (config.getKey().equals(Config.FOOD_STATUS)){
					int status = Integer.parseInt(config.getValue());
					GameFoodService.STATUS=status;
					gameService.updateGameStatus(GameTypeEnum.food.getValue(),status);
				}

			}
		}, this);
		
		new Thread("同步握手数据监测") {
			public void run() {
				try {
					long t1 = System.currentTimeMillis();
					logger.info("握手数据初始化完毕[" + (System.currentTimeMillis() - t1) + "ms]");
					ServerStateService.startService();
				} catch (Exception e) {
					logger.error("同步握手数据异常：" + e, e);
				}
			};
		}.start();
	}

	@Override
	public void onDisconnect(int surplusReconnectNum) {
		logger.debug("剩余重连次数：" + surplusReconnectNum);
		ServerStateService.stopService();
	//	ServerNoticeService.setOpenNotice(false);
	}
	@Override
	public boolean isEncrypt(Command command) {
		return false;
	}
	@Override
	protected Log logger() {
		return logger;
	}
}
