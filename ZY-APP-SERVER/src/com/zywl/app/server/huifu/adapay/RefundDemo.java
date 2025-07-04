package com.zywl.app.server.huifu.adapay;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.huifu.adapay.model.Refund;

/**
 * @author jane.zhao
 */
public class RefundDemo extends BaseDemo {

    /**
     * 运行退款类接口
     *
     * @throws Exception 异常
     */
    public static void executeRefundTest(String merchantKey, String paymentId) throws Exception {
        RefundDemo demo = new RefundDemo();
        //退款接口
        Map<String, Object> refund = demo.executeRefund(merchantKey, paymentId);
        //退款查询接口（通过pamentId查询）
        demo.queryByPaymentId(merchantKey, paymentId);
        //退款查询接口（通过refundId查询）
        demo.queryByRefundId(merchantKey, (String) refund.get("id"));
    }

    /**
     * 执行一个退款交易
     *
     * @param paymentId 要退款的原支付paymentId
     * @return 创建的退款对象
     * @throws Exception 异常
     */
    public Map<String, Object> executeRefund(String merchantKey, String paymentId) throws Exception {
        System.out.println("=======execute refund begin=======");
        Map<String, Object> refundParams = new HashMap<String, Object>(2);
        refundParams.put("refund_amt", "0.01");
        refundParams.put("app_id", "your appid");
        refundParams.put("refund_order_no", "jsdk_refund_"+System.currentTimeMillis());
        System.out.println("退款请求参数：" + JSON.toJSONString(refundParams));
        Map<String, Object> refund = Refund.create(paymentId, refundParams, merchantKey);
        System.out.println("退款返回参数：" + JSON.toJSONString(refund));
        System.out.println("=======execute refund end=======");
        
        return refund;
    }

    /**
     * 根据原支付id查询一个退款交易
     *
     * @param paymentId 要查询退款的原支付paymentId
     * @return 查询的退款对象，可能含多个退款明细RefundDetail
     * @throws Exception 异常
     */
    public Map<String, Object> queryByPaymentId(String merchantKey, String paymentId) throws Exception {
        System.out.println("=======query refund by paymentId begin=======");
        Map<String, Object> refundParams = new  HashMap<String, Object>(1);
        refundParams.put("payment_id", paymentId);
        System.out.println("通过原支付ID查询退款交易，请求参数：" + JSON.toJSONString(refundParams));
        Map<String, Object> refund = Refund.query(refundParams, merchantKey);
        System.out.println("通过原支付ID查询退款交易，返回参数：" + JSON.toJSONString(refund));
        System.out.println("=======query refund by paymentId end=======");
        return refund;
    }

    /**
     * 根据退款refundId查询一个退款交易
     *
     * @param refundId 要查询的退款refundId
     * @return 查询的退款对象
     * @throws Exception 异常
     */
    public Map<String, Object> queryByRefundId(String merchantKey, String refundId) throws Exception {
        System.out.println("=======query refund by refundid begin=======");
        Map<String, Object> refundParams = new  HashMap<String, Object>(1);
        refundParams.put("refund_id", refundId);
        System.out.println("通过refundId查询退款交易，请求参数：" + JSON.toJSONString(refundParams));
        Map<String, Object> refund = Refund.query(refundParams, merchantKey);
        System.out.println("通过refundId查询退款交易，返回参数：" + JSON.toJSONString(refund));
        System.out.println("=======query refund by refundid end=======");
        return refund;
    }

    /**
     * 运行退款类接口
     *
     * @throws Exception 异常
     */
    public static void executeRefundTest( String paymentId) throws Exception {
        RefundDemo demo = new RefundDemo();
        //退款接口
        Map<String, Object> refund = demo.executeRefund( paymentId);
        //退款查询接口（通过pamentId查询）
        demo.queryByPaymentId( paymentId);
        //退款查询接口（通过refundId查询）
        demo.queryByRefundId( (String) refund.get("id"));
    }

    /**
     * 执行一个退款交易
     *
     * @param paymentId 要退款的原支付paymentId
     * @return 创建的退款对象
     * @throws Exception 异常
     */
    public Map<String, Object> executeRefund( String paymentId) throws Exception {
        System.out.println("=======execute refund begin=======");
        Map<String, Object> refundParams = new HashMap<String, Object>(2);
        refundParams.put("refund_amt", "0.01");
        refundParams.put("app_id", "your appid");
        refundParams.put("refund_order_no", "jsdk_refund_"+System.currentTimeMillis());
        System.out.println("退款请求参数：" + JSON.toJSONString(refundParams));
        Map<String, Object> refund = Refund.create(paymentId, refundParams);
        System.out.println("退款返回参数：" + JSON.toJSONString(refund));
        System.out.println("=======execute refund end=======");
        
        return refund;
    }

    /**
     * 根据原支付id查询一个退款交易
     *
     * @param paymentId 要查询退款的原支付paymentId
     * @return 查询的退款对象，可能含多个退款明细RefundDetail
     * @throws Exception 异常
     */
    public Map<String, Object> queryByPaymentId( String paymentId) throws Exception {
        System.out.println("=======query refund by paymentId begin=======");
        Map<String, Object> refundParams = new  HashMap<String, Object>(1);
        refundParams.put("payment_id", paymentId);
        System.out.println("通过原支付ID查询退款交易，请求参数：" + JSON.toJSONString(refundParams));
        Map<String, Object> refund = Refund.query(refundParams);
        System.out.println("通过原支付ID查询退款交易，返回参数：" + JSON.toJSONString(refund));
        System.out.println("=======query refund by paymentId end=======");
        return refund;
    }

    /**
     * 根据退款refundId查询一个退款交易
     *
     * @param refundId 要查询的退款refundId
     * @return 查询的退款对象
     * @throws Exception 异常
     */
    public Map<String, Object> queryByRefundId( String refundId) throws Exception {
        System.out.println("=======query refund by refundid begin=======");
        Map<String, Object> refundParams = new  HashMap<String, Object>(1);
        refundParams.put("refund_id", refundId);
        System.out.println("通过refundId查询退款交易，请求参数：" + JSON.toJSONString(refundParams));
        Map<String, Object> refund = Refund.query(refundParams);
        System.out.println("通过refundId查询退款交易，返回参数：" + JSON.toJSONString(refund));
        System.out.println("=======query refund by refundid end=======");
        return refund;
    }
}
