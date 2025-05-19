package com.zywl.app.socket;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Config;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.service.GameService;
import com.zywl.app.defaultx.service.IncomeRecordService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.BattleRoyaleService2;
import com.zywl.app.service.ServerStateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.ClientEndpoint;
import java.math.BigDecimal;

@ClientEndpoint
public class ManagerSocket2 extends BaseClientSocket {
	private static final Log logger = LogFactory.getLog(ManagerSocket2.class);

	
	private VersionService versionService;
	
	private BattleRoyaleService2 battleRoyaleService2;

	private GameService gameService;
	
	private IncomeRecordService incomeRecordService;
	
	
	
	
	
	public ManagerSocket2(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
		super(socketType, false, reconnect, server, shakeHandsDatas);
		versionService = SpringUtil.getService(VersionService.class);
		incomeRecordService = SpringUtil.getService(IncomeRecordService.class);
		battleRoyaleService2 = SpringUtil.getService(BattleRoyaleService2.class);
		gameService = SpringUtil.getService(GameService.class);
		Push.addPushSuport(PushCode.cancelBet, new DefaultPushHandler() {
			public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
			}
		});
		
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
				if (config.getKey().equals(Config.DTS2_STATUS)){
					int status = Integer.parseInt(config.getValue());
					BattleRoyaleService2.STATUS=status;
					gameService.updateGameStatus(GameTypeEnum.battleRoyale.getValue(),status);
				}
				if (config.getKey().equals(Config.DAILY_STOLEN_COUNT)){
					BigDecimal status = new BigDecimal(config.getValue());
					logger.info("调整飞仙手续费："+status);
					battleRoyaleService2.updateRate(status);
					//gameService.updateGameStatus(GameTypeEnum.battleRoyale.getValue(),status);
				}
				if (config.getKey().equals(Config.GAME_DTS2_NEED_BOT)){
					BattleRoyaleService2.NEED_BOT=Integer.parseInt(config.getValue());
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
