package com.zywl.app.server.socket;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.defaultx.service.IncomeRecordService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.ServerNoticeService;
import com.zywl.app.server.service.ServerStateService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.ClientEndpoint;

@ClientEndpoint
public class LogSocket extends BaseClientSocket {
    private static final Log logger = LogFactory.getLog(LogSocket.class);

    private VersionService versionService;


    private IncomeRecordService incomeRecordService;


    public LogSocket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
        super(socketType, false, reconnect, server, shakeHandsDatas);
        versionService = SpringUtil.getService(VersionService.class);
        incomeRecordService = SpringUtil.getService(IncomeRecordService.class);


    }

    @Override
    public void onConnect(Object data) {

        JSONObject connectedData = ((JSONObject) data).getJSONObject("responseShakeHandsData");
        if (connectedData != null) {
        }
        new Thread("同步握手数据监测") {
            public void run() {
                try {
                    long t1 = System.currentTimeMillis();
                    logger.info("握手数据初始化完毕[" + (System.currentTimeMillis() - t1) + "ms]");
                    ServerStateService.startService();
                } catch (Exception e) {
                    logger.error("同步握手数据异常：" + e, e);
                }
            }

            ;
        }.start();
    }
    @Override
    public boolean isEncrypt(Command command) {
        return false;
    }
    @Override
    public void onDisconnect(int surplusReconnectNum) {
        logger.debug("剩余重连次数：" + surplusReconnectNum);
        ServerNoticeService.setOpenNotice(false);
    }

    @Override
    protected Log logger() {
        return logger;
    }
}
