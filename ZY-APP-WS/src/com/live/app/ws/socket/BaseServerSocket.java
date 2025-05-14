package com.live.app.ws.socket;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.bean.ConnectedData;
import com.live.app.ws.bean.PushBean;
import com.live.app.ws.constant.CommandConstants;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.socket.manager.SocketManager;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.live.app.ws.util.KeyUtil;
import com.live.app.ws.util.Push;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.util.DesUtil;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

public abstract class BaseServerSocket extends BaseSocket {

    private String privateKey;

    private boolean useDefaultPrivateKey = true;

    protected String ip;

    protected String id;

    private long lastActiveTime;

    public BaseServerSocket(TargetSocketType socketType, boolean gzip, boolean useDefaultPrivateKey) {
        super(socketType, gzip);
        this.useDefaultPrivateKey = useDefaultPrivateKey;
    }

    private JSONObject getShakeHandsData(Map<String, String> shakeHandsMap) {


        String pk = shakeHandsMap.get("pk");
        String shakeHandsStr = shakeHandsMap.get("data");
        if (useDefaultPrivateKey) {
            privateKey = KeyUtil.privateKey(pk, "get");
        } else {
            privateKey = getPrivateKey(pk);
        }
        if (privateKey == null || "".equals(privateKey)) {
            sendCommand(CommandBuilder.builder().request(CommandConstants.CMD_CONNECT_FAILS, "未获取到私钥").build());
        }
        try {
            shakeHandsStr = URLDecoder.decode(shakeHandsStr, "UTF-8");
            if (shakeHandsStr.contains("%")) {
                shakeHandsStr = URLDecoder.decode(shakeHandsStr, "UTF-8");
            }
//			shakeHandsStr = URLDecoder.decode(URLDecoder.decode(shakeHandsStr, "UTF-8"), "UTF-8");
            if (shakeHandsStr != null) {
                shakeHandsStr = DesUtil.decrypt(shakeHandsStr, privateKey);
                return JSON.parseObject(shakeHandsStr);
            }
        } catch (Exception e) {
            logger().error(shakeHandsStr + "|" + privateKey + "|" + e, e);
            throwExp("解密握手数据失败");
        }
        return null;
    }

    public static void main(String[] args) throws IOException, Exception {
        String a = "YpPk14q4zLW83jQGXsQJmHPFBPD/5Lm9SaLCb+FNjRXtGghmla2DN4DNpg+0/8yocdb1E7krSl0BQbbIe9QbQz+cCxNQHwFy+peosOztFMAXBIdJ2PztQ+Sg62ooGgq+I8cWeHCP4NYpkjupY8QSFPyTDX5kYYeh62dsvSrrg43cP9Tpfpjp83lf12OuqtMP9O3MtQeXHi4=";
        String key = "BClZUZcv1SQ83YOdCKHqadNAvv8PE6zZ";
        System.out.println(DesUtil.decrypt(a, key));
    }

    public void onOpen(HttpSession httpSession, Map<String, String> shakeHandsMap) {
        ip = httpSession.getAttribute("ip").toString();
        lastActiveTime = System.currentTimeMillis();
        logger().info("新的连接 -> " + ip);
        logger().info("连接session：" + getSessionId());
        JSONObject shakeHandsData = null;
        try {
            shakeHandsData = getShakeHandsData(shakeHandsMap);
        } catch (Exception e) {
            sendCommand(CommandBuilder.builder().request(CommandConstants.CMD_CONNECT_FAILS, e.getMessage()).build());
            throw e;
        }

        logger().info("握手数据 -> " + ip + " -> " + shakeHandsData.toJSONString());

        ConnectedData connectedData = onConnect(shakeHandsData);
        id = connectedData.getId();
        if (id == null) {
            throwExp("未分配连接id");
        } else {
            //检查连接id唯一性
            Map<String, BaseServerSocket> servers = SocketManager.getClients(getSocketType());
            if (servers != null && servers.containsKey(id)) {
                logger().error("发现重复连接：" + id + "|" + ip);
                servers.get(id).close();
            }
            SocketManager.addClient(getSocketType(), id, this);
            if (!getSocketType().equals(TargetSocketType.app)) {
                sendCommand(CommandBuilder.builder().request(CommandConstants.CMD_CONNECTED, connectedData).build());
            }
            logger().info("客户端连接成功 -> " + ip);
        }
    }

    public void onMessage(String message) {
        try {
            if (!message.startsWith("{")) {
                message = DesUtil.decrypt(message, privateKey);
            }
        } catch (Exception e) {
            throwExp("不能解密的消息");
        }
        Command command = JSON.parseObject(message, Command.class);
        if (command.getCode().equals("syncTaskNum") || command.getCode().equals("700200") || command.getCode().equals("007002") || command.getCode().equals("999999")) {

        } else {
            logger().info("收到客户端指令：" + message);
        }
        if (eq(CommandConstants.PING_PONG, command.getCode())) {
            sendCommand(CommandBuilder.builder().request(CommandConstants.PING_PONG, null).build());
        } else if (eq(CommandConstants.CMD_PUSH_REGIST, command.getCode())) {
            logger().info("注册推送：" + JSON.toJSONString(command.getData()));
            Push.doAddPush(this, JSON.parseObject(JSON.toJSONString(command.getData()), PushBean.class));
        } else if (eq(CommandConstants.CMD_PUSH_UNREGIST, command.getCode())) {
            logger().info("取消推送：" + JSON.toJSONString(command.getData()));
            Push.doRemovePush(this, JSON.parseObject(JSON.toJSONString(command.getData()), PushBean.class));

        } else if (eq(CommandConstants.CMD_PUSH_REGIST_RESULT, command.getCode())) {
            logger().info("注册推送成功：" + JSON.toJSONString(command.getData()));
            Push.onRegistSuccess(this, JSON.parseObject(JSON.toJSONString(command.getData()), PushBean.class));
        } else if (command.isPush()) {
            Push.onPush(this, PushCode.valueOf(command.getCode()), command.getData());
        } else {
            if (isNull(command.getLocale())) {
                command.setLocale(getLocale());
            }
            Set<String> whiteList = getWhiteList();
            if (whiteList != null && whiteList.contains(command.getCode())) {
                sendLoginTimeout(command);
            } else {
                disposeRequest(command);
            }
        }
        lastActiveTime = System.currentTimeMillis();
    }

    public void disposeRequest(Command command) {
        Executer.disposeRequest(this, command);
    }

    public void sendLoginTimeout(Command command) {
        JSONObject object = new JSONObject();
        object.put("login", false);
        Command loginTimeoutCommand = CommandBuilder.builder(command).success(object, "登录失效请重新登录").build();
        loginTimeoutCommand.setId(command.getId());
        loginTimeoutCommand.setCode(CommandConstants.CMD_LOGIN_TIMEOUT);
        sendCommand(loginTimeoutCommand);
    }

    public void sendLoginTimeout() {
        sendLoginTimeout(new Command());
    }

    public void disconnect() {
        if (id != null) {
            Push.removeAll(this);
            Map<String, BaseServerSocket> servers = SocketManager.getClients(getSocketType());
            if (servers != null && servers.get(id) == this) {
                SocketManager.removeClient(getSocketType(), id);
            }
            onDisconnect();
        }
    }


    public void sendCommand(Command command) {
        filterCommand(command);
        String data = JSON.toJSONString(command);
        try {
            if (!command.isPush()) {
                if (!command.getCode().equals("700200") && !command.getCode().equals("007002") && !command.getCode().equals("999999")) {
                    logger().debug("发送到" + getSocketType() + "：" + data);
                }
            }
            if (isEncrypt(command)) {
                data = DesUtil.encrypt(data, privateKey);
            }
            send(data);
        } catch (Exception e) {
            logger().error("发送失败: " + JSON.toJSONString(command) + " | " + e, e);
        }
    }


    public String getIp() {
        return ip;
    }

    public String getId() {
        return id;
    }

    public long getLastActiveTime() {
        return lastActiveTime;
    }

    public boolean isEncrypt(Command command) {
        return true;
    }


    /**
     * 连接成功触发，如果握手数据不合法可以抛出异常中断客户端连接
     *
     * @param shakeHandsData
     * @return
     * @author DOE
     */
    public abstract ConnectedData onConnect(JSONObject shakeHandsData) throws AppException;

    protected abstract void filterCommand(Command command);

    protected abstract void onDisconnect();

    protected abstract String getPrivateKey(String pk);

    protected abstract Set<String> getWhiteList();

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
