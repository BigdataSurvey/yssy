package com.zywl.app.server.huifu.adapay;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.huifu.adapay.model.AdapayTools;
import com.huifu.adapay.model.Bill;
import com.huifu.adapay.model.Wallet;

/**
 * @author yingyong.wang
 */
public class AdapayToolsDemo extends BaseDemo {

    /**
     * 获取银联用户号
     *
     * @throws Exception 异常
     */
    public static void executeToolsTest(String appId, String merchantKey) throws Exception {
        Map<String, Object> unionParam = new HashMap<String, Object>(2);
        unionParam.put("order_no", "20190912");
        unionParam.put("app_id", appId);
        unionParam.put("user_auth_code", "20190912");
        unionParam.put("app_up_identifier", "20190912");
        Map<String, Object> result = AdapayTools.unionUserId(unionParam, merchantKey);

        String errorCode = (String) result.get("error_code");

        if (null != errorCode) {

            System.out.println("对账单下载，请求参数：" + JSON.toJSONString(unionParam));
            System.out.println("对账单下载，返回参数：" + JSON.toJSONString(result));

        } else {
            System.out.println("对账单下载，成功");
        }

    }

    /**
     * 获取银联用户号
     *
     * @throws Exception 异常
     */
    public static void executeToolsTest(String appId) throws Exception {

        Map<String, Object> unionParam = new HashMap<String, Object>(2);
        unionParam.put("order_no", "jsdk_payment_" + System.currentTimeMillis());
        unionParam.put("app_id", appId);
        unionParam.put("user_auth_code", "5yRGbi+IRda5khIQoQf1Hw==");
        unionParam.put("app_up_identifier", "CloudPay");
        Map<String, Object> result = AdapayTools.unionUserId(unionParam);
        System.out.println("获取银联云闪付用户标识：" + JSON.toJSONString(result));
        String errorCode = (String) result.get("error_code");

        if (null != errorCode) {

            System.out.println("获取银联云闪付用户标识，请求参数：" + JSON.toJSONString(unionParam));
            System.out.println("获取银联云闪付用户标识，返回参数：" + JSON.toJSONString(result));

        } else {
            System.out.println("获取银联云闪付用户标识，成功");
        }

    }


    /**
     * 钱包登录申请
     *
     * @throws Exception 异常
     */
    public static void executeLoginTest(String appId, String merchantKey) throws Exception {
        Map<String, Object> queryParams = new HashMap<String, Object>(5);
        queryParams.put("ip", "127.0.0.1");
        queryParams.put("member_id", "0");
        //queryParams.put("member_id", "iris1234_14151");
        queryParams.put("app_id", appId);
        Map<String, Object> login = Wallet.login(queryParams, merchantKey);
        if (login != null && "succeeded".equals(login.get("status"))) {
            String formString = login.get("redirect_url").toString();
            System.out.println("跳转地址:" + formString);
        }

    }

    /**
     * 钱包登录申请
     *
     * @throws Exception 异常
     */
    public static void executeLoginTest(String appId) throws Exception {
        Map<String, Object> queryParams = new HashMap<String, Object>(5);
        queryParams.put("ip", "127.0.0.1");
        queryParams.put("member_id", "0");
        //queryParams.put("member_id", "iris1234_14151");
        queryParams.put("app_id", appId);
        Map<String, Object> login = Wallet.login(queryParams);
        if (login != null && "succeeded".equals(login.get("status"))) {
            String formString = login.get("redirect_url").toString();
            System.out.println("跳转地址:" + formString);
        }

    }

    /**
     * 执行一个下载对账文件操作
     *
     * @return 下载链接
     * @throws Exception 异常
     */
    public Map<String, Object> executeBillDownLoad() throws Exception {


        Map<String, Object> downloadParam = new HashMap<String, Object>(2);
        downloadParam.put("bill_date", "20190912");

        Map<String, Object> download = Bill.download(downloadParam);

        String errorCode = (String) download.get("error_code");

        if (null != errorCode) {

            System.out.println("对账单下载，请求参数：" + JSON.toJSONString(downloadParam));
            System.out.println("对账单下载，返回参数：" + JSON.toJSONString(download));

        } else {
            System.out.println("对账单下载，成功");
        }
        return download;
    }

}
