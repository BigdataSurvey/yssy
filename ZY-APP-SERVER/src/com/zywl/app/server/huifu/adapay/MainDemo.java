package com.zywl.app.server.huifu.adapay;

import com.huifu.adapay.Adapay;
import com.huifu.adapay.model.MerConfig;

import java.util.HashMap;
import java.util.Map;


/**
 * @author jane.zhao
 */
public class MainDemo {
    public static void main(String[] args) throws Exception {
        //若是商户只是自己对接adapay，请使用如下demo
        testMerchant();

        //若是技术性渠道商为其它商户提供服务功能，请使用多商户模式
//        testMultiMerchant();
    }

    public static void testMerchant() throws Exception {
        /**
         * debug 模式，开启后有详细的日志
         */
        Adapay.debug = true;

        /**
         * prodMode 模式，默认为生产模式，false可以使用mock模式
         */
        Adapay.prodMode = true;

        /**
         * 初始化商户配置，服务器启动前，必须通过该方式初始化商户配置完成
         * apiKey为prod模式的API KEY
         * mockApiKey为mock模式的API KEY
         * rsaPrivateKey为商户发起请求时，用于请求参数加签所需要的RSA私钥
         */
        String apiKey = "api_live_9c14f264-e390-41df-984d-df15a6952031";
        String mockApiKey = "api_test_e640fa26-bbe6-458f-ac44-a71723ee2176";
        String rsaPrivateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMQhsygJ2pp4nCiDAXiqnZm6AzKSVAh+C0BgGR6QaeXzt0TdSi9VR0OQ7Qqgm92NREB3ofobXvxxT+wImrDNk6R6lnHPMTuJ/bYpm+sx397rPboRAXpV3kalQmbZ3P7oxtEWOQch0zV5B1bgQnTvxcG3REAsdaUjGs9Xvg0iDS2tAgMBAAECgYAqGFmNdF/4234Yq9V7ApOE1Qmupv1mPTdI/9ckWjaAZkilfSFY+2KqO8bEiygo6xMFCyg2t/0xDVjr/gTFgbn4KRPmYucGG+FzTRLH0nVIqnliG5Ekla6a4gwh9syHfstbOpIvJR4DfldicZ5n7MmcrdEwSmMwXrdinFbIS/P1+QJBAOr6NpFtlxVSGzr6haH5FvBWkAsF7BM0CTAUx6UNHb+RCYYQJbk8g3DLp7/vyio5uiusgCc04gehNHX4laqIdl8CQQDVrckvnYy+NLz+K/RfXEJlqayb0WblrZ1upOdoFyUhu4xqK0BswOh61xjZeS+38R8bOpnYRbLf7eoqb7vGpZ9zAkEAobhdsA99yRW+WgQrzsNxry3Ua1HDHaBVpnrWwNjbHYpDxLn+TJPCXvI7XNU7DX63i/FoLhOucNPZGExjLYBH/wJATHNZQAgGiycjV20yicvgla8XasiJIDP119h4Uu21A1Su8G15J2/9vbWn1mddg1pp3rwgvxhw312oInbHoFMxsQJBAJlyDDu6x05MeZ2nMor8gIokxq2c3+cnm4GYWZgboNgq/BknbIbOMBMoe8dJFj+ji3YNTvi1MSTDdSDqJuN/qS0=";
        MerConfig merConfig = new MerConfig();
        merConfig.setApiKey(apiKey);
        merConfig.setApiMockKey(mockApiKey);
        merConfig.setRSAPrivateKey(rsaPrivateKey);
        Adapay.initWithMerConfig(merConfig);


        /**
         * 初始化完成，可以开始交易
         */
        String appId = "app_7d87c043-aae3-4357-9b2c-269349a980d6";


        // 运行支付类接口
//         String pamentId = PaymentDemo.executePaymentTest(appId);
         // 带区域和超时参数的支付请求Demo
          String pamentId = PaymentRegionAndRequestTimeOutDemo.executePaymentTest(appId);

        // //运行退款类接口
        // RefundDemo.executeRefundTest("002112020010618571810060213271697731584");

        // /**用户类接口*/
        Map<String, Object> member = MemberDemo.executeMemberTest(appId);
        String memberId = (String) member.get("member_id");
        // /** 结算户绑定*/
        SettleAccountDemo.executeSettleAccountTest("app_7d87c043-aae3-4357-9b2c-269349a980d6", memberId);
        /**企业开户*/
        CorpMemberDemo.executeCorpMemberTest("app_7d87c043-aae3-4357-9b2c-269349a980d6");

        /**
         * 对账单下载
         */
        // BillDemo.executeBillTest();

        // /**
        //  * 获取云闪付用户号
        //  */
        // AdapayToolsDemo.executeToolsTest(appId);
        // /**
        //  * 余额查询
        //  */
        // SettleAccountDemo.executeQueryBalance(appId, "member_id", "settleCount_id");
        // /**
        //  *用户取现
        //  */
        // SettleAccountDemo.executeDrawCash(appId, "member_id");
        // /**
        //  * 钱包登录申请
        //  */
        // AdapayToolsDemo.executeLoginTest(appId,"member_id");
        //银行卡管理
        //FastPayDemo.executeCardTest(appId);
    }

    public static void testMultiMerchant() throws Exception {
        /**
         * debug 模式，开启后与详细的日志
         */
        Adapay.debug = true;

        /**
         * prodMode 模式，默认为生产模式，false可以使用mock模式
         */
        Adapay.prodMode = true;

        /**
         * 初始化每个商户配置，服务器启动前，必须通过该方式初始化商户配置完成
         * apiKey为prod模式的API KEY
         * mockApiKey为mock模式的API KEY
         * rsaPrivateKey为商户发起请求时，用于请求参数加签所需要的RSA私钥
         */
        // 创建商户A的商户配置对象
        Map<String, MerConfig> configPathMap = new HashMap<>();
        String apiKeyA = "api_live_9c14f264-e390-41df-984d-df15a6952031";
        String mockApiKeyA = "api_test_e640fa26-bbe6-458f-ac44-a71723ee2176";
        String rsaPrivateKeyA = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMQhsygJ2pp4nCiDAXiqnZm6AzKSVAh+C0BgGR6QaeXzt0TdSi9VR0OQ7Qqgm92NREB3ofobXvxxT+wImrDNk6R6lnHPMTuJ/bYpm+sx397rPboRAXpV3kalQmbZ3P7oxtEWOQch0zV5B1bgQnTvxcG3REAsdaUjGs9Xvg0iDS2tAgMBAAECgYAqGFmNdF/4234Yq9V7ApOE1Qmupv1mPTdI/9ckWjaAZkilfSFY+2KqO8bEiygo6xMFCyg2t/0xDVjr/gTFgbn4KRPmYucGG+FzTRLH0nVIqnliG5Ekla6a4gwh9syHfstbOpIvJR4DfldicZ5n7MmcrdEwSmMwXrdinFbIS/P1+QJBAOr6NpFtlxVSGzr6haH5FvBWkAsF7BM0CTAUx6UNHb+RCYYQJbk8g3DLp7/vyio5uiusgCc04gehNHX4laqIdl8CQQDVrckvnYy+NLz+K/RfXEJlqayb0WblrZ1upOdoFyUhu4xqK0BswOh61xjZeS+38R8bOpnYRbLf7eoqb7vGpZ9zAkEAobhdsA99yRW+WgQrzsNxry3Ua1HDHaBVpnrWwNjbHYpDxLn+TJPCXvI7XNU7DX63i/FoLhOucNPZGExjLYBH/wJATHNZQAgGiycjV20yicvgla8XasiJIDP119h4Uu21A1Su8G15J2/9vbWn1mddg1pp3rwgvxhw312oInbHoFMxsQJBAJlyDDu6x05MeZ2nMor8gIokxq2c3+cnm4GYWZgboNgq/BknbIbOMBMoe8dJFj+ji3YNTvi1MSTDdSDqJuN/qS0=";
        MerConfig merConfigA = new MerConfig();
        merConfigA.setApiKey(apiKeyA);
        merConfigA.setApiMockKey(mockApiKeyA);
        merConfigA.setRSAPrivateKey(rsaPrivateKeyA);
        // 定义一个商户的A的唯一标识，交易发起时，用于定位是哪个商户发起交易
        configPathMap.put("商户A的唯一标识", merConfigA);

        // 创建商户B的商户配置对象
        String apiKeyB = "api_live_9c14f264-e390-41df-984d-df15a6952031";
        String mockApiKeyB = "api_test_e640fa26-bbe6-458f-ac44-a71723ee2176";
        String rsaPrivateKeyB = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAMQhsygJ2pp4nCiDAXiqnZm6AzKSVAh+C0BgGR6QaeXzt0TdSi9VR0OQ7Qqgm92NREB3ofobXvxxT+wImrDNk6R6lnHPMTuJ/bYpm+sx397rPboRAXpV3kalQmbZ3P7oxtEWOQch0zV5B1bgQnTvxcG3REAsdaUjGs9Xvg0iDS2tAgMBAAECgYAqGFmNdF/4234Yq9V7ApOE1Qmupv1mPTdI/9ckWjaAZkilfSFY+2KqO8bEiygo6xMFCyg2t/0xDVjr/gTFgbn4KRPmYucGG+FzTRLH0nVIqnliG5Ekla6a4gwh9syHfstbOpIvJR4DfldicZ5n7MmcrdEwSmMwXrdinFbIS/P1+QJBAOr6NpFtlxVSGzr6haH5FvBWkAsF7BM0CTAUx6UNHb+RCYYQJbk8g3DLp7/vyio5uiusgCc04gehNHX4laqIdl8CQQDVrckvnYy+NLz+K/RfXEJlqayb0WblrZ1upOdoFyUhu4xqK0BswOh61xjZeS+38R8bOpnYRbLf7eoqb7vGpZ9zAkEAobhdsA99yRW+WgQrzsNxry3Ua1HDHaBVpnrWwNjbHYpDxLn+TJPCXvI7XNU7DX63i/FoLhOucNPZGExjLYBH/wJATHNZQAgGiycjV20yicvgla8XasiJIDP119h4Uu21A1Su8G15J2/9vbWn1mddg1pp3rwgvxhw312oInbHoFMxsQJBAJlyDDu6x05MeZ2nMor8gIokxq2c3+cnm4GYWZgboNgq/BknbIbOMBMoe8dJFj+ji3YNTvi1MSTDdSDqJuN/qS0=";
        MerConfig merConfigB = new MerConfig();
        merConfigB.setApiKey(apiKeyB);
        merConfigB.setApiMockKey(mockApiKeyB);
        merConfigB.setRSAPrivateKey(rsaPrivateKeyB);
        // 定义一个商户的B的唯一标识，交易发起时，用于定位是哪个商户发起交易
        configPathMap.put("商户B的唯一标识", merConfigB);

        // 将商户A和B的商户配置放入本地缓存
        Adapay.initWithMerConfigs(configPathMap);

        /**
         * 商户配置初始化完成后，可以发起交易
         */

        // 运行支付类接口，其中app_id必须是商户A下的app_id
        String pamentId = PaymentDemo.executePaymentTest("商户A的唯一标识", "app_7d87c043-aae3-4357-9b2c-269349a980d6");
        // String pamentI2 = PaymentDemo.executePaymentTest("yidian", "app_67ba475b-26e0-4cfa-847c-0f115cae5029");

        // //运行退款类接口
        // RefundDemo.executeRefundTest("yifuyun", "002112019101420422510029799145265012736");
        // RefundDemo.executeRefundTest("yifuyun", "002112019101420422610029799148904755200");
        // /**
        //  * 分账使用配套接口 begin
        //  */
        // /**用户类接口*/
        // Map<String, Object> member = MemberDemo.executeMemberTest("yifuyun", "app_67ba475b-26e0-4cfa-847c-0f115cae5029");
        // String memberId = (String) member.get("member_id");
        // /** 结算户绑定*/
        // SettleAccountDemo.executeSettleAccountTest("yidian", "app_67ba475b-26e0-4cfa-847c-0f115cae5029", memberId);
        // /**企业开户*/
        // CorpMemberDemo.executeCorpMemberTest("yidian", "app_67ba475b-26e0-4cfa-847c-0f115cae5029");
        // /**
        //  * 分账使用配套接口 end
        //  */
        // //对账单下载
        // BillDemo.executeBillTest("yifuyun");
        // String appId = "app_67ba475b-26e0-4cfa-847c-0f115cae5029";

        // /**
        //  * 获取云闪付用户号
        //  */
        // AdapayToolsDemo.executeToolsTest(appId, "yifuyun");
        // /**
        //  * 余额查询
        //  */
        // SettleAccountDemo.executeQueryBalance("yifuyun", appId, "member_id", "settleCount_id");
        // /**
        //  *用户取现
        //  */
        // SettleAccountDemo.executeDrawCash("yifuyun", appId, "member_id");
        // /**
        //  * 钱包登录申请
        //  */
        // AdapayToolsDemo.executeLoginTest(appId,"yifuyun");

    }


}