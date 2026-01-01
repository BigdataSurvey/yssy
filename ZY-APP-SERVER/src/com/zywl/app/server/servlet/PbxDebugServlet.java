package com.zywl.app.server.servlet;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: lzx
 * @Create: 2025/12/20
 * @Version: V1.0
 * @Description: PBX 推箱子 玩法调试入口 仅限本地回路访问-HTTP转WebSocket指令，模拟客户端请求，用于后端接口联调与状态查询
 */
@WebServlet(name = "PbxDebugServlet", urlPatterns = "/pbxDebug")
public class PbxDebugServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(PbxDebugServlet.class);

    private static final String DEFAULT_GAME_ID = "12";
    private static final String DEFAULT_USER_ID = "1";
    private static final int TIMEOUT_SECONDS = 6;

    /**
     * 指令映射枚举
     * 维护 Action -> CmdCode 的映射关系，对应 DTS2 -> Manager 的调用链路
     */
    private enum ActionEnum {
        JOIN("join", "102101", "加入房间: DTS2->Manager(200722)"),
        OP("op", "102103", "下注操作: DTS2->Manager(200720)"),
        LEAVE("leave", "102104", "离开房间: DTS2本地逻辑"),
        QUERY("query", "102105", "查询奖池: DTS2->Manager(200722)"),
        SETTLE("settle", "102106", "结算派奖: DTS2->Manager(200721)"),
        WEEK_SETTLE("weekSettle", "102107", "周榜结算: DTS2->Manager(200723)");

        private final String action;
        private final String code;
        private final String desc;

        ActionEnum(String action, String code, String desc) {
            this.action = action;
            this.code = code;
            this.desc = desc;
        }

        public static ActionEnum getByAction(String action) {
            return Arrays.stream(values())
                    .filter(e -> e.action.equalsIgnoreCase(action))
                    .findFirst()
                    .orElse(null);
        }

        public String getCode() {
            return code;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=utf-8");

        if (!isLocalRequest(req)) {
            sendResponse(resp, 403, false, "Forbidden: Local access only", null);
            return;
        }

        String actionStr = req.getParameter("action");
        ActionEnum actionEnum = ActionEnum.getByAction(actionStr);
        if (actionEnum == null) {
            sendResponse(resp, 400, false, "Invalid action. Support: join|op|leave|query|settle|weekSettle", null);
            return;
        }

        String gameId = StringUtils.defaultIfBlank(req.getParameter("gameId"), DEFAULT_GAME_ID);
        String userId = StringUtils.defaultIfBlank(req.getParameter("userId"), DEFAULT_USER_ID);

        JSONObject bizData = buildBusinessData(req, actionEnum, userId, gameId);

        // 路由转发
        TargetSocketType socketType;
        try {
            socketType = TargetSocketType.getServerEnum(Integer.parseInt(gameId));
        } catch (NumberFormatException e) {
            sendResponse(resp, 400, false, "Invalid gameId format", null);
            return;
        }

        // 响应
        Command resultCmd = executeRpc(socketType, actionEnum.getCode(), bizData);
        if (resultCmd == null) {
            sendResponse(resp, 504, false, "RPC Timeout or DTS2 Connection Failed", null);
        } else {
            sendResponse(resp, 200, resultCmd.isSuccess(), resultCmd.getMessage(), resultCmd.getData());
        }
    }

    /**
     * 构建透传给 DTS2 的业务参数
     */
    private JSONObject buildBusinessData(HttpServletRequest req, ActionEnum action, String userId, String gameId) {
        JSONObject data = new JSONObject();

        data.put("userId", userId);
        data.put("gameId", gameId);

        Map<String, String[]> paramMap = req.getParameterMap();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            if (!data.containsKey(entry.getKey()) && entry.getValue() != null && entry.getValue().length > 0) {
                data.put(entry.getKey(), entry.getValue()[0]);
            }
        }

        switch (action) {
            case OP:
                if (StringUtils.isBlank(data.getString("chip"))) {
                    String betAmount = data.getString("betAmount");
                    if (StringUtils.isNotBlank(betAmount)) {
                        data.put("chip", betAmount);
                    }
                }
                break;
            case SETTLE:
                String winListStr = req.getParameter("winList");
                if (StringUtils.isNotBlank(winListStr)) {
                    try {
                        data.put("winList", JSONArray.parse(winListStr));
                    } catch (Exception e) {
                        logger.warn("Parse winList json failed", e);
                    }
                } else {
                    String returnAmount = req.getParameter("returnAmount");
                    if (StringUtils.isNotBlank(returnAmount)) {
                        JSONArray simpleWinList = new JSONArray();
                        JSONObject item = new JSONObject();
                        item.put("userId", userId);
                        item.put("returnAmount", returnAmount);
                        simpleWinList.add(item);
                        data.put("winList", simpleWinList);
                    }
                }
                break;
            default:
                break;
        }
        return data;
    }

    /**
     * 执行 WebSocket 同步调用
     */
    private Command executeRpc(TargetSocketType target, String code, JSONObject data) {
        Command requestCmd = CommandBuilder.builder().request(code, data).build();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Command> responseRef = new AtomicReference<>();

        Executer.request(target, requestCmd, new Listener() {
            @Override
            public void handle(BaseClientSocket clientSocket, Command command) {
                responseRef.set(command);
                latch.countDown();
            }
        });

        try {
            boolean completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                logger.error("[PbxDebug] RPC timeout, code={}, data={}", code, data);
                return null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        return responseRef.get();
    }

    /**
     * 本地回路检测
     */
    private boolean isLocalRequest(HttpServletRequest req) {
        String ip = req.getRemoteAddr();
        return "127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "localhost".equals(ip);
    }

    /**
     * 统一响应输出
     */
    private void sendResponse(HttpServletResponse resp, int status, boolean success, String msg, Object data) throws IOException {
        resp.setStatus(status);
        JSONObject output = new JSONObject();
        output.put("success", success);
        output.put("message", msg);
        if (data != null) {
            output.put("data", data);
        }
        resp.getOutputStream().write(output.toJSONString().getBytes(StandardCharsets.UTF_8));
    }
}