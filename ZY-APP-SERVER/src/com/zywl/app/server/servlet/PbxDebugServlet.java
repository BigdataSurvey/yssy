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
        if ("join".equalsIgnoreCase(action)) {
            code = "102101";
        } else if ("op".equalsIgnoreCase(action)) {
            code = "102103";
        } else if ("leave".equalsIgnoreCase(action)) {
            code = "102104";
        } else if ("query".equalsIgnoreCase(action)) {
            // 触发 DTS2 -> Manager 200721 查询
            code = "102106";
        } else if ("settle".equalsIgnoreCase(action)) {
            // 触发 DTS2 -> Manager 200722 结算派奖
            code = "102105";
        } else {
            resp.getWriter().write("{\"success\":false,\"message\":\"action must be join|op|leave|query|settle\"}");
            return;
        }

        JSONObject data = new JSONObject();
        data.put("userId", userId);
        data.put("gameId", gameId);

        // 将所有 querystring 参数灌进 data
        req.getParameterMap().forEach((k, v) -> {
            if (!data.containsKey(k) && v != null && v.length > 0) data.put(k, v[0]);
        });

        // settle 支持 payouts 或 gross 快捷参数
        if ("settle".equalsIgnoreCase(action)) {
            String payoutsStr = req.getParameter("payouts");
            if (payoutsStr != null && payoutsStr.trim().length() > 0) {
                JSONArray payouts = JSONArray.parse(payoutsStr);
                data.put("payouts", payouts);
            } else {
                String gross = req.getParameter("gross");
                if (gross != null && gross.trim().length() > 0) {
                    JSONArray payouts = new JSONArray();
                    JSONObject one = new JSONObject();
                    one.put("userId", userId);
                    one.put("gross", gross);
                    payouts.add(one);
                    data.put("payouts", payouts);
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
