package com.zywl.app.server.socket;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.constant.CommandConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.PushListener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.socket.BaseSocket;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.*;
import com.zywl.app.base.bean.*;
import com.zywl.app.defaultx.service.IncomeRecordService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.server.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.ClientEndpoint;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ClientEndpoint
public class ManagerSocket extends BaseClientSocket {
    private static final Log logger = LogFactory.getLog(ManagerSocket.class);

    private VersionService versionService;

    private UpdateAppService updateAppService;

    private IncomeRecordService incomeRecordService;


    private ServerConfigService serverConfigService;


    private ServerMineService serverMineService;

    private ServerUserRoleService serverUserRoleService;


    private GameBaseService gameBaseService;

    public ManagerSocket(TargetSocketType socketType, int reconnect, String server, JSONObject shakeHandsDatas) {
        super(socketType, false, reconnect, server, shakeHandsDatas);
        versionService = SpringUtil.getService(VersionService.class);
        updateAppService = SpringUtil.getService(UpdateAppService.class);
        serverConfigService = SpringUtil.getService(ServerConfigService.class);
        incomeRecordService = SpringUtil.getService(IncomeRecordService.class);
        serverMineService = SpringUtil.getService(ServerMineService.class);
        gameBaseService =SpringUtil.getService(GameBaseService.class);
        serverUserRoleService = SpringUtil.getService(ServerUserRoleService.class);
        Push.addPushSuport(PushCode.syncAppOnline, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
                Map<String, AppSocket> servers = SocketManager.getClients(TargetSocketType.app);
                JSONArray shakeHands = new JSONArray();
                for (AppSocket appSocket : servers.values()) {
                    shakeHands.add(appSocket.getAppData());
                }
                pushBean.setShakeHands(shakeHands);
            }
        });

        Push.addPushSuport(PushCode.syncAppOffline, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {
            }
        });

        Push.addPushSuport(PushCode.syncAppChange, new DefaultPushHandler() {
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
        Push.addPushSuport(PushCode.syncTsgOrder, new DefaultPushHandler() {
            public void onRegist(BaseSocket baseSocket, PushBean pushBean) {

            }
        });
    }

    @Override
    public void onConnect(Object data) {

        Push.registPush(new PushBean(PushCode.updateUserPower), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                String userId = obj.getString("userId");
                Push.push(PushCode.updateUserPower, userId, data);
            }
        }, this);

        Push.registPush(new PushBean(PushCode.redReminder), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到用户展示红点提醒推送：" + data);
                JSONObject object = (JSONObject) data;
                String userId = object.getString("userId");
                Push.push(PushCode.redReminder, userId, data);
            }
        }, this);




        Push.registPush(new PushBean(PushCode.updateUserCapital), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("用户资产变动推送" + data);
                JSONObject obj = (JSONObject) data;
                String userId = obj.getString("userId");
                if (!obj.containsKey("isDts")) {
                    Push.push(PushCode.updateUserCapital, userId, data);
                }
                if (obj.containsKey("isDts")) {
                    //大逃杀结算 判断用户是否在房间 不在的话 推送
                    if (!ServerLotteryGameService.userLotteryPush.containsKey(userId)) {
                        Push.push(PushCode.updateUserCapital, userId, data);
                    }
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updateUserBackpack), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }
            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("用户背包变动推送" + data);
                JSONObject obj = (JSONObject) data;
                String userId = obj.getString("userId");
                Push.push(PushCode.updateUserBackpack, userId, data);
            }
        }, this);




        Push.registPush(new PushBean(PushCode.updateAdCount), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                if (obj.containsKey("userId") && obj.containsKey("adInfo")) {
                    Push.push(PushCode.updateAdCount, obj.getString("userId"), obj.getJSONObject("adInfo"));
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updateUserInfo), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                if (obj.containsKey("userInfo")) {
                    Push.push(PushCode.updateUserInfo, obj.getString("userId"), obj);
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updatePlayerMp), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                Set<String> ids = obj.keySet();
                for (String id : ids) {
                    Push.push(PushCode.updatePlayerMp, id, obj.get(id));
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updatePlayer), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject player = (JSONObject) data;
            }
        }, this);

        Push.registPush(new PushBean(PushCode.fcAppLoginOut), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到Manager推送强制登出指令：" + data);
                if (data != null) {
                    JSONObject fcLoginOutData = (JSONObject) data;
                    int cmdType = fcLoginOutData.getIntValue("type");
                    if (cmdType == 1) {//顶号
                        String sessionId = fcLoginOutData.getString("sessionId");
                        Map<String, AppSocket> servers = SocketManager.getClients(TargetSocketType.app);
                        AppSocket appSocket = servers.get(sessionId);
                        if (appSocket != null) {
                            String newDeviceId = fcLoginOutData.getString("newDeviceId");
                            // 有可能是socket未释放干净导致当前设备强制顶掉了当前设备的登录信息
                            boolean changeDevice = appSocket.getDevice() == null
                                    || !eq(newDeviceId, appSocket.getDevice().getId());
                            Long id = appSocket.getUser().getId();
                            appSocket.updateSocketInfo(null, true, false, !changeDevice);

                            if (changeDevice) {
                                appSocket.sendCommand(CommandBuilder.builder()
                                        .request(CommandConstants.CMD_LOGIN_TIMEOUT, fcLoginOutData,
                                                "您的账号在别处登录，若非本人登录请检查是否泄漏个人数据。")
                                        .build());
                            } else {
                                logger.info("相同设备ID，不推送登出指令");
                            }
                        }
                    } else if (cmdType == 2) {
                        Map<String, AppSocket> clients = SocketManager.getClients(TargetSocketType.app);
                        for (AppSocket appSocket : clients.values()) {
                            appSocket.sendCommand(CommandBuilder.builder()
                                    .request(CommandConstants.CMD_LOGIN_TIMEOUT, fcLoginOutData,
                                            "游戏维护中")
                                    .build());
                        }
                    } else if (cmdType == 3) {
                        String sessionId = fcLoginOutData.getString("sessionId");
                        String reason = fcLoginOutData.getString("reason");

                        Map<String, AppSocket> servers = SocketManager.getClients(TargetSocketType.app);
                        AppSocket appSocket = servers.get(sessionId);
                        if (appSocket != null) {
                            appSocket.updateSocketInfo(null, true, false, true);
                            appSocket.sendCommand(CommandBuilder.builder()
                                    .request(CommandConstants.CMD_LOGIN_TIMEOUT, null, reason)
                                    .build());
                        }

                    }
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updateTableVersion), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                // 推送至客户端，让客户端更新
                if (obj.containsKey("mineTable")){
                    serverMineService.initMine();
                }
                if (obj.containsKey("roleTable")){
                    serverUserRoleService.initRole();
                }
                updateAppService.pushTableVersionUpdate(obj);
            }
        }, this);

        Push.registPush(new PushBean(PushCode.chat), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                Map<String, AppSocket> clients = SocketManager.getClients(TargetSocketType.app);
                for (AppSocket appSocket : clients.values()) {
                    WsidBean wsidBean = appSocket.getWsidBean();
                    if (wsidBean != null && wsidBean.getUserId() != null) {
                        Push.push(appSocket, CommandBuilder.builder().push(PushCode.chat, obj).build());
                    }
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.sendNotice), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                // 推送至客户端，让客户端更新

                Map<String, AppSocket> clients = SocketManager.getClients(TargetSocketType.app);
                for (AppSocket appSocket : clients.values()) {
                    WsidBean wsidBean = appSocket.getWsidBean();
                    if (wsidBean != null && wsidBean.getUserId() != null) {
                        Push.push(appSocket, CommandBuilder.builder().push(PushCode.sendNotice, obj).build());
                    }
                }

            }
        }, this);


        Push.registPush(new PushBean(PushCode.redPackageInfo), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                JSONObject obj = (JSONObject) data;
                // 推送至客户端，让客户端知道有红包发出
                updateAppService.pushRedPackageInfo(obj);
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updateVersion), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                versionService.reloadCache();

                String versionId = data.toString();
                Version version = versionService.findVersionById(versionId);
                if (version != null) {
                    if (version.getRelease() == Version.RELEASE_ENABLE) {
                        // 如果收到的版本已经发布，则推送至客户端，让客户端更新
                        updateAppService.pushVersionUpdate(version);
                    }
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updateConfig), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
                serverConfigService.initCache();
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到系统参数修改推送：" + data);
                JSONObject object = (JSONObject) data;
                Config config = object.toJavaObject(Config.class);
                serverConfigService.setConfigCache(config);
                if (serverConfigService.getAllWhiteConfig().containsKey(config.getKey())) {
                    JSONObject massData = new JSONObject();
                    massData.put(config.getKey(), config.getValue());
                    Map<String, String> map = new ConcurrentHashMap<>();
                    map.put(config.getKey(), config.getValue());
                    Mass.mass(TargetSocketType.app,
                            CommandBuilder.builder().mass(CommandConstants.CMD_UPDATE_CONFIG, map).build());
                }
            }
        }, this);

        Push.registPush(new PushBean(PushCode.pushRed), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
                serverConfigService.initCache();
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到红包雨推送：" + data);
                JSONObject info = (JSONObject) data;
                //处理推送数据  推送给玩家
                Push.push(PushCode.pushRed, "hongbaoyu", info);
            }
        }, this);



        //抽卡游戏 ===============================================



        Push.registPush(new PushBean(PushCode.redPointShow), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到用户展示红点提醒推送：" + data);
                JSONObject object = (JSONObject) data;
                String userId = object.getString("userId");
                Push.push(PushCode.redPointShow, userId, data);
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updateRoleCard), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到用户获得新武将推送：" + data);
                JSONObject object = (JSONObject) data;
                String userId = object.getString("userId");
                Push.push(PushCode.updateRoleCard, userId, data);
            }
        }, this);
        Push.registPush(new PushBean(PushCode.caidengmi), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到灯谜推送：" + data);
                JSONObject object = (JSONObject) data;
                Mass.mass(TargetSocketType.app,
                        CommandBuilder.builder().mass(CommandConstants.CDM, object).build());
            }
        }, this);

        Push.registPush(new PushBean(PushCode.updateRoleCardAll), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到用户获得更新武将推送：" + data);
                JSONObject object = (JSONObject) data;
                String userId = object.getString("userId");
                Push.push(PushCode.updateRoleCardAll, userId, data);
            }
        }, this);
        Push.registPush(new PushBean(PushCode.redPointHide), new PushListener() {
            public void onRegist(BaseSocket baseSocket, Object data) {
            }

            public void onReceive(BaseSocket baseSocket, Object data) {
                logger.info("收到用户隐藏红点提醒推送：" + data);
                JSONObject object = (JSONObject) data;
                String userId = object.getString("userId");
                Push.push(PushCode.redPointHide, userId, data);
            }
        }, this);


        JSONObject connectedData = ((JSONObject) data).getJSONObject("responseShakeHandsData");
        if (connectedData != null) {
            TemplateLoadService.staticWebUrl = connectedData.getString("staticWebUrl");
            TemplateLoadService.managerWebUrl = connectedData.getString("managerWebUrl");
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
    public void onDisconnect(int surplusReconnectNum) {
        logger.debug("剩余重连次数：" + surplusReconnectNum);
        ServerStateService.stopService();
        ServerNoticeService.setOpenNotice(false);
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
