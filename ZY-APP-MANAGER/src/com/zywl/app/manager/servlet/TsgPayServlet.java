package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.TsgPayOrder;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.defaultx.service.TsgPayOrderService;
import com.zywl.app.defaultx.service.UserGiftService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.manager.ManagerUserVipService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.TreeMap;

@SuppressWarnings("serial")
@WebServlet(name = "TsgPayServlet", urlPatterns = "/tsgPayNotify")
public class TsgPayServlet extends BaseServlet {

    private static final Log logger = LogFactory.getLog(TsgPayServlet.class);

    private TsgPayOrderService tsgPayOrderService;

    private UserGiftService userGiftService;

    private ManagerUserVipService managerUserVipService;

    public TsgPayServlet() {
        tsgPayOrderService = SpringUtil.getService(TsgPayOrderService.class);
        userGiftService = SpringUtil.getService(UserGiftService.class);
        managerUserVipService = SpringUtil.getService(ManagerUserVipService.class);
    }

    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws IOException {
        System.out.println("========================================");
        String morderid = request.getHeader("userId");
        //1:预下单成功,2:预下单失败3:交易成功,4:交易超时,5:交易失败,6:处理中
        int status = Integer.parseInt(request.getHeader("status"));
        //成功则返回”success”,失败返回原因
        String message = request.getHeader("message");
        //以分为单位
        String amount = request.getHeader("amount");
        //实际支付金额
        String payAmount = request.getHeader("payAmount");
        //订单号
        String requestNo = request.getHeader("requestNo");
        String orderNo = request.getHeader("orderNo");
        String payTime = request.getHeader("payTime");
        String sign = request.getHeader("sign");
        TreeMap<String, String> treeMap = new TreeMap<>();
        treeMap.put("userId", morderid);
        treeMap.put("status", String.valueOf(status));
        treeMap.put("message", message);
        treeMap.put("amount", amount);
        treeMap.put("payAmount", payAmount);
        treeMap.put("requestNo", requestNo);
        treeMap.put("orderNo", orderNo);
        treeMap.put("payTime", payTime);
        StringBuffer stringBuffer = new StringBuffer();
        treeMap.forEach((key, value) -> stringBuffer.append(key).append("=").append(value).append("&"));
        String signMd5 = MD5Util.md5(stringBuffer + "key=e7a15a9d4e6946bb97edf329035297d1").toLowerCase();
        if (signMd5.equals(sign)) {
             updatePayOrder(status,requestNo);
        }
        return null;
    }


    public void updatePayOrder(int status, String requestNo) {
        TsgPayOrder tsgPayOrder = tsgPayOrderService.findByOrderNo(requestNo);
        if (tsgPayOrder==null){
            return ;
        }
        tsgPayOrder.setStatus(status);
		if (status==3){
            Long userId = tsgPayOrder.getUserId();
            int productId = Math.toIntExact(tsgPayOrder.getProductId());
            userGiftService.addUserGiftNumber(userId, productId);
            managerUserVipService.addExper(userId, tsgPayOrder.getPrice());
        }
        tsgPayOrderService.updateOrder(tsgPayOrder);
    }

}
