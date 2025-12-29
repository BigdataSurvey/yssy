package com.zywl.app.server.servlet;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@WebServlet(name = "PbxDebugServlet", urlPatterns = "/pbxDebug")
public class PbxDebugServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String remote = req.getRemoteAddr();
        if (!(remote.equals("127.0.0.1") || remote.equals("0:0:0:0:0:0:0:1"))) {
            resp.setStatus(403);
            resp.getWriter().write("{\"success\":false,\"message\":\"forbidden\"}");
            return;
        }

        // join | op | leave | query | settle
        String action = req.getParameter("action");
        String userId = req.getParameter("userId");
        String gameId = req.getParameter("gameId");
        if (gameId == null || gameId.isEmpty()) gameId = "12";
        if (userId == null || userId.isEmpty()) userId = "1";

        String code;
        // 加入房间 -> DTS2 102101 (joinRoom) -> Manager 200722(pbxQuery) 取 poolBalance/serverTime
        if ("join".equalsIgnoreCase(action)) {
            code = "102101";
        }
        // 下注 -> DTS2 102103 (operate) -> Manager 200720(pbxBet) 扣款入全服奖池
        else if ("op".equalsIgnoreCase(action)) {
            code = "102103";
        }
        // 离开房间 -> DTS2 102104 (leaveRoom)
        else if ("leave".equalsIgnoreCase(action)) {
            code = "102104";
        }
        // 查询奖池 -> DTS2 102105 (processQuery) -> Manager 200722(pbxQuery)
        else if ("query".equalsIgnoreCase(action)) {
            code = "102105";
        }
        // 结算派奖 -> DTS2 102106 (processSettle) -> Manager 200721(pbxSettle)
        else if ("settle".equalsIgnoreCase(action)) {
            code = "102106";
        }
        // 周榜结算 -> DTS2 102107 (processWeekSettle) -> Manager 200723(pbxWeekSettle)
        else if ("weekSettle".equalsIgnoreCase(action)) {
            code = "102107";
        } else {
            resp.getWriter().write("{\"success\":false,\"message\":\"action must be join|op|leave|query|settle|weekSettle\"}");
            return;
        }
        JSONObject data = new JSONObject();
        data.put("userId", userId);
        data.put("gameId", gameId);
        // 兼容旧参数：op 时若只传 betAmount，则补齐 chip（Step C-2.1 以 chip+elementId 为准）
        if ("op".equalsIgnoreCase(action)) {
            String chip = req.getParameter("chip");
            if (chip == null || chip.trim().length() == 0) {
                String betAmount = req.getParameter("betAmount");
                if (betAmount != null && betAmount.trim().length() > 0) {
                    data.put("chip", betAmount);
                }
            }
        }

        // 将所有 querystring 参数灌进 data
        req.getParameterMap().forEach((k, v) -> {
            if (!data.containsKey(k) && v != null && v.length > 0) data.put(k, v[0]);
        });

        // settle 支持 payouts 或 gross 快捷参数
        if ("settle".equalsIgnoreCase(action)) {
            String winListStr = req.getParameter("winList");
            if (winListStr != null && winListStr.trim().length() > 0) {
                JSONArray winList = JSONArray.parse(winListStr);
                data.put("winList", winList);
            } else {
                // 兼容快捷参数：直接传 returnAmount
                String returnAmount = req.getParameter("returnAmount");
                if (returnAmount != null && returnAmount.trim().length() > 0) {
                    JSONArray winList = new JSONArray();
                    JSONObject one = new JSONObject();
                    one.put("userId", userId);
                    one.put("returnAmount", returnAmount);
                    winList.add(one);
                    data.put("winList", winList);
                }
            }
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Command> back = new AtomicReference<>();

        TargetSocketType socketType = TargetSocketType.getServerEnum((int) Long.parseLong(gameId));
        Command cmd = CommandBuilder.builder().request(code, data).build();

        Executer.request(socketType, cmd, new Listener() {
            @Override
            public void handle(BaseClientSocket clientSocket, Command command) {
                back.set(command);
                latch.countDown();
            }
        });

        boolean ok;
        try {
            ok = latch.await(6, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            ok = false;
        }

        resp.setContentType("application/json;charset=utf-8");
        if (!ok) {
            resp.getWriter().write("{\"success\":false,\"message\":\"timeout/no response, check DTS2 connection\"}");
            return;
        }

        Command r = back.get();
        JSONObject out = new JSONObject();
        out.put("success", r.isSuccess());
        out.put("message", r.getMessage());
        out.put("data", r.getData());
        resp.getWriter().write(out.toJSONString());
    }
}
