package com.zywl.app.server.service;

import cn.hutool.core.date.DateTime;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.TsgPayOrderService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@ServiceClass(code = MessageCodeContext.PAY_ROLE)
public class ServerPayRoleService extends BaseService {


    @Autowired
    private ServerConfigService serverConfigService;

    @Autowired
    private TsgPayOrderService tsgPayOrderService;


    @ServiceMethod(code = "001", description = "获取支付地址")
    public Object getAddress(AppSocket appSocket, Command command, JSONObject params) throws Exception {
        checkNull(params);
        checkNull(params.get("productId"));
        Long productId = params.getLong("productId");
        Long userId = appSocket.getWsidBean().getUserId();
        BigDecimal price = BigDecimal.ONE;
        String goodsName = "";
        if (productId==1L){
            price = serverConfigService.getBigDecimal(Config.GIFT_PRICE_1).setScale(2);
            goodsName = "单角小礼包";
        } else if (productId==2L) {
            price = serverConfigService.getBigDecimal(Config.GIFT_PRICE_2).setScale(2);
            goodsName = "角色大礼包";
        }else {
            price = new BigDecimal("1").setScale(2);
            goodsName = "测试礼包";
        }
        String merchantId = serverConfigService.getString(Config.PAY_MERCHANT_ID);
        String merReqNo = OrderUtil.getOrder5Number();
        String ip = appSocket.getIp();
        String notifyUrl = serverConfigService.getString(Config.PAY_NOTIFY_URL);
        String returnUrl = "";
        String tranDateTime = DateUtil.getCurrent3();
        String sign = "";
        String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
        DateTime dateTime = cn.hutool.core.date.DateUtil.parse(timeExpire);
        Date expireDate = new Date(dateTime.getTime());
        tsgPayOrderService.addOrder(userId,merReqNo,productId,price,expireDate);
        Map<String,String> data = new HashMap<>();
        data.put("merchantId",merchantId);
        data.put("merReqNo",merReqNo);
        data.put("amt", String.valueOf(price));
        data.put("goodsName",goodsName);
        data.put("creatip",ip);
        data.put("notifyUrl",notifyUrl);
        data.put("returnUrl",returnUrl);
        data.put("tranDateTime",tranDateTime);
        TreeMap<String,String> treeMap = new TreeMap<>(data);
        StringBuffer stringBuffer = new StringBuffer();
        treeMap.forEach((key, value) -> stringBuffer.append(key).append("=").append(value).append("&"));
        stringBuffer.deleteCharAt(stringBuffer.length()-1);
        stringBuffer.append("=================");
        String signMd5 = MD5Util.md5(stringBuffer.toString());
        data.put("sign",signMd5);
        String result = HTTPUtil.postJSON("http://121.199.171.139/webapis/tran/addTrans.php","", data);
        if (result==null){
            throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        }
        JSONObject jsonResult = JSONObject.parseObject(result);
        if (jsonResult.containsKey("success") && jsonResult.getBoolean("success")){
            return jsonResult.getJSONObject("data");
        }
        throwExp("当前没有可用的支付地址，请联系客服或稍后再试");
        return null;
    }



}
