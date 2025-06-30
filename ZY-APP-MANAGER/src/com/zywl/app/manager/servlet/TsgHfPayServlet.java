package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSON;
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
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.TreeMap;

@SuppressWarnings("serial")
@WebServlet(name = "TsgHfPayServlet", urlPatterns = "/tsgHfPayNotify")
public class TsgHfPayServlet extends BaseServlet {

    private static final Log logger = LogFactory.getLog(TsgHfPayServlet.class);

    private TsgPayOrderService tsgPayOrderService;

    private UserGiftService userGiftService;

    private ManagerUserVipService managerUserVipService;

    public TsgHfPayServlet() {
        tsgPayOrderService = SpringUtil.getService(TsgPayOrderService.class);
        userGiftService = SpringUtil.getService(UserGiftService.class);
        managerUserVipService = SpringUtil.getService(ManagerUserVipService.class);
    }

    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws IOException {
        System.out.println("========================================");
        String respCode = request.getParameter("resp_code");
        if("00000000".equals(respCode)){
            logger.info("订单支付成功");
            String respData =request.getParameter("resp_data");
            JSONObject data = JSON.parseObject(respData);
            String orderNo = data.getString("req_seq_id");
            String desc = data.getString("resp_desc");
            String code = data.getString("resp_code");
            if (code.equals("00000000") && desc.equals("交易成功")){
                updatePayOrder(3, orderNo);
            }
        }
        return "ok";
    }


    @Transactional
    public void updatePayOrder(int status, String requestNo) {
        TsgPayOrder tsgPayOrder = tsgPayOrderService.findByOrderNo(requestNo);
        if (tsgPayOrder == null) {
            logger.info("未查询到订单+"+requestNo);
            return;
        }
        if (tsgPayOrder.getStatus()==3){
            logger.info("订单已完成。");
            return;
        }
        tsgPayOrder.setStatus(status);
        if (status == 3) {
            Long userId = tsgPayOrder.getUserId();
            int productId = Math.toIntExact(tsgPayOrder.getProductId());
            managerUserVipService.addExper(userId, tsgPayOrder.getAllPrice().setScale(0, RoundingMode.HALF_UP));
            userGiftService.addUserGiftNumber(userId, productId, tsgPayOrder.getNumber());
        }
        tsgPayOrderService.updateOrder(tsgPayOrder);
    }

}
