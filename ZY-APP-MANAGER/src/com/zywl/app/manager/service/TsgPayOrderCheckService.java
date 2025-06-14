package com.zywl.app.manager.service;


import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.TsgPayOrder;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.defaultx.service.TsgPayOrderService;
import com.zywl.app.manager.service.pay.HfCheckOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class TsgPayOrderCheckService extends BaseService {

    public static final String USER_ID = "88162050";

    public static final String SECRET = "e7a15a9d4e6946bb97edf329035297d1";

    @Autowired
    private HfCheckOrderService hfCheckOrderService;


    @Autowired
    private TsgPayOrderService tsgPayOrderService;


    public void checkOrder() throws Exception {
        List<TsgPayOrder> noPayOrder = tsgPayOrderService.findNoPayOrder();
        for (TsgPayOrder order : noPayOrder) {
            checkOrder(order);
        }
    }

    public void checkOrder(TsgPayOrder order) throws Exception {
        if (order.getStatus()==1 && order.getExpireTime().getTime()<System.currentTimeMillis()){
            order.setStatus(4);
            order.setRemark("支付超时");
            tsgPayOrderService.updateOrder(order);
            return;
        }
        if (order.getChannel() == 1) {
            String requestNo = order.getOrderNo();
            Map<String, Object> data = new HashMap<>();
            data.put("userId", USER_ID);
            data.put("requestNo", requestNo);
            TreeMap<String, Object> treeMap = new TreeMap<>(data);
            StringBuffer stringBuffer = new StringBuffer();
            treeMap.forEach((key, value) -> stringBuffer.append(key).append("=").append(value).append("&"));
            String s = stringBuffer + "key=" + SECRET;
            String signMd5 = MD5Util.md5(s).toLowerCase();
            data.put("sign", signMd5);
            long time = System.currentTimeMillis();
            JSONObject from = JSONObject.from(data);
            String s1 = from.toJSONString();
            String result = HTTPUtil.postJSON("https://api-kaite.jjoms.com/query/pay", s1);
            if (result!=null){
                JSONObject jsonObject = JSONObject.parseObject(result);
                //1:预下单成功,2:预下单失败3:交易成功,4:交易超时,5:交易失败,6:处理中
                Integer status = jsonObject.getInteger("status");
                if (status==3 || status==1){
                    return;
                }
                String message = jsonObject.getString("message");
                order.setRemark(message);
                order.setStatus(2);
                tsgPayOrderService.updateOrder(order);
            }
        } else if (order.getChannel() == 2) {
            Map<String, Object> map = HfCheckOrderService.checkOrder(order.getOrderNo());
            if (map.containsKey("acct_stat")){
                String stat =String.valueOf( map.get("acct_stat") );
                if (stat.equals("F")){
                    order.setStatus(2);
                    tsgPayOrderService.updateOrder(order);
                }
            }
        }


    }
}
