package com.zywl.app.manager.service.pay;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.PayOrder;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.PayOrderService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@ServiceClass(code = MessageCodeContext.PAY_ORDER_SERVER)
public class GetPayOrderService extends BaseService {


    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private PayOrderService payOrderService;

    private static final Map<String, PayOrder> PAY_ORDERS = new ConcurrentHashMap<>();

    //订单超时时间  5分钟
    private static final Long EXPIRE_TIME = 300000L;

    @PostConstruct
    public void _construct() {
        new Timer("定时检查订单是否过期").schedule(new TimerTask() {
            public void run() {
                try {
                    Set<String> orderNos = PAY_ORDERS.keySet();
                    for (String orderNo : orderNos) {
                        PayOrder order = PAY_ORDERS.get(orderNo);
                        if (order.getCreateTime().getTime() + EXPIRE_TIME < System.currentTimeMillis()) {
                            //订单超时
                            if (order.getStatus()!=1){
                                payOrderService.orderExpire(orderNo);
                            }
                            PAY_ORDERS.remove(orderNo);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1);
    }

    public void paySuccess(String orderNo){
        if (PAY_ORDERS.containsKey(orderNo)){
            PAY_ORDERS.get(orderNo).setStatus(1);
        }
    }
    @Transactional
    @ServiceMethod(code = "001", description = "获取支付订单及通道")
    public Object getPayOrder(ManagerSocketServer adminSocketServer, JSONObject data) throws IOException {
        Long productId = data.getLong("productId");
        Long userId = data.getLong("userId");
        JSONObject result = new JSONObject();
        if (!PlayGameService.productMap.containsKey(productId.toString())) {
            throwExp("订购产品不存在");
        }
        int type = managerConfigService.getInteger(Config.PAY_TYPE);
        result.put("payType", type);
        if (type == 0) {
            return result;
        }
        try {
            PayOrder byUserId = payOrderService.findByUserId(userId);
            if (byUserId!=null && PAY_ORDERS.containsKey(byUserId.getOrderNo()) && byUserId.getPrice()==PlayGameService.productMap.get(productId.toString()).getPrice()){
                result.put("orderNo", byUserId.getOrderNo());
                return result;
            }
        }catch (Exception e){
            throwExp("操作频繁，请于5分钟后再试");
        }

        String orderNo = OrderUtil.getOrder5Number();

        if (type == 2) {
            PayOrder order = payOrderService.addOrder(userId, orderNo, productId, type, PlayGameService.productMap.get(productId.toString()).getPrice());
            PAY_ORDERS.put(orderNo, order);
            result.put("orderNo", orderNo);
        }
        String info = HTTPUtil.get("https://ntjapi.myzywlkj.com/easypay/pay/makeorder?userId=" + userId + "&gameOrder=" + orderNo + "&amount=" + PlayGameService.productMap.get(productId.toString()).getPrice());
        if (info == null) {
            throwExp("获取订单信息失败，请稍后再试");
        }
        JSONObject returnInfo = null;
        try {
            returnInfo = JSONObject.parseObject(info);
        } catch (Exception e) {
            logger.error("解析json失败");
        }
        if (returnInfo.containsKey("code") && returnInfo.getInteger("code") == 1) {
            return result;
        } else {
            throwExp("获取订单信息失败，请稍后再试");
        }
        return null;
    }
}
