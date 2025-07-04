package com.zywl.app.server.huifu.adapay;

import com.alibaba.fastjson.JSON;
import com.huifu.adapay.model.FastPay;

import java.util.HashMap;
import java.util.Map;


/**
 * @author gxw
 * @date   2020-12-04
 */
public class FastPayDemo extends BaseDemo{

    /**
     * 运行 member 类接口
     * @throws Exception 异常
     */
    public static Map<String, Object> executeCardTest(String app_id) throws Exception{
        FastPayDemo fastPayDemo = new FastPayDemo();
        Map<String, Object> result = new HashMap<>();
        result = fastPayDemo.executeCardApply(app_id);
        result = fastPayDemo.executeCardConfirm(app_id);
        result = fastPayDemo.executeCardList(app_id);
        result = fastPayDemo.executeConfirm(app_id);
        result = fastPayDemo.executeSendSms(app_id);
        return result;
    }


    /**
     * 创建快捷绑卡
     * @return 创建快捷绑卡 对象
     * @throws Exception 异常
     */
    public Map<String, Object> executeCardApply(String app_id) throws Exception {
        System.out.println("=======execute executeCardApply begin=======");
        Map<String, Object> applyParam = new  HashMap<String, Object>();
        applyParam.put("app_id", app_id);
        applyParam.put("member_id", "member_id_test");
        applyParam.put("card_id", "666666666666666666666666");
        applyParam.put("tel_no", "13888888888");
        applyParam.put("vip_code", "321");
        applyParam.put("expiration", "0225");
        Map<String, Object> result = FastPay.cardApply(applyParam);
        System.out.println("=======execute executeCardApply end=======");
        return result;
    
    }


    /**
     * 创建快捷绑卡确认
     * @return 创建快捷绑卡确认 对象
     * @throws Exception 异常
     */
    public Map<String, Object> executeCardConfirm(String app_id) throws Exception {
        System.out.println("=======execute executeCardConfirm begin=======");
        Map<String, Object> confirmParam = new  HashMap<String, Object>();
        confirmParam.put("apply_id", app_id);
        confirmParam.put("sms_code", "123456");
        confirmParam.put("notify_url", "https://xxxx.com/xxxx");
        Map<String, Object> result = FastPay.cardConfirm(confirmParam);
        System.out.println("创建用户，返回参数：" + JSON.toJSONString(result));
        System.out.println("=======execute executeCardConfirm end=======");

        return result;

    }
    /**
     * 查询快捷卡对象列表
     * @return 查询快捷卡对象列表
     * @throws Exception 异常
     */
    public Map<String, Object> executeCardList( String app_id) throws Exception {
        System.out.println("=======execute executeCardList begin=======");
        Map<String, Object> listParam = new  HashMap<String, Object>();
        listParam.put("app_id", app_id);
        listParam.put("member_id", "member_id_test");
        listParam.put("token_no", "10000067502");
        Map<String, Object> result = FastPay.cardList(listParam);
        System.out.println("查询用户，返回参数：" + JSON.toJSONString(result));

        System.out.println("=======execute executeCardList end=======");
        
        return result;
    
    }
    /**
     * 创建支付对象
     * @return 创建支付对象
     * @throws Exception 异常
     */
    public Map<String, Object> executeConfirm( String app_id) throws Exception {
        System.out.println("=======execute executeConfirm begin=======");
        Map<String, Object> confirmParams = new HashMap<String, Object>();
        confirmParams.put("payment_id",  "002112020012010545810065165317376983040");
        confirmParams.put("sms_code", "123456");
        confirmParams.put("app_id", app_id);
        Map<String, Object> result = FastPay.confirm(confirmParams);
        System.out.println("=======execute executeConfirm end=======");
        return result;

    }

    /**
     * 创建快捷支付短信重发
     * @return 创建快捷支付短信重发
     * @throws Exception 异常
     */
    public Map<String, Object> executeSendSms( String app_id) throws Exception {
        System.out.println("=======execute executeSendSms begin=======");
        Map<String, Object> smsCodeParam = new  HashMap<String, Object>();
        smsCodeParam.put("payment_id", "20190912");
        Map<String, Object> result = FastPay.smsCode(smsCodeParam);
        System.out.println("=======execute executeSendSms end=======");
        return result;

    }

}
