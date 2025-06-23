package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.AlipayConfig;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.domain.AlipayFundTransUniTransferModel;
import com.alipay.api.domain.Participant;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.CashOrderErrorMessageEnum;
import com.zywl.app.defaultx.enmus.CashStatusTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class AliPayCashService extends BaseService {
    //this.getClass().getClassLoader().getResource("appCertPublicKey.crt").getPath()

    public static List<String> cashOrderNos = new ArrayList<String>();

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private ManagerSocketService managerSocketService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private UserCapitalService userCapitalService;

    @PostConstruct
    public void _AliPayCashService() {

    }

    public static final String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCH64aTMxSKThUasoPQXqh16LIyZgr0hTppYcZjHWTkrwYiLvqXAvo+7puSz6DqJ4T8gTAq/HpG+UE1uhE8o7FW4bNFN2HlGBJoIxGdfSQru9HpBd5FXPxjJdgvecCtWFWd7X27UEtd8/WRGckS7QE58fglLHH7UG26Ew444FTYqJwt4A3Dwx0+ejXYM7h/tp/Y52G7qhmO7EfCjbAyq/b3H9r3myssBtcf9q31ZTAJLzNsBshgfvPuusLoLV35d0PDaEYySOAfSd3IW06JuStlkjEiFQtn2B78Ziq55CIyuHIkjovO1ZFuBJYdTGwinqrQ8yEFkNkMdP81Tmr01MQ1AgMBAAECggEACRJJGw3vswNfGTcM4R1QNP5g1khFNuila18hNIBDuZeM2z8zbly+gvC95WCCJt6YW5nrsxLCvnXoSkOu3tM2BMxvgJEfsYCXyed9R3uQrHKOofTITuzT03I1KYYAvBEHNdEh8Rx8yzp7C0IjT2YODOX/X8vFl7ZYfHDFJDqkrwkIqQrbaZhWnpLhUVsaa4uaF29pY6P930g5NoraWjTmsPcH63S9VZOM+XYEGHV0syy6SLxE3S1mFIaqKcj2xLCo1jAyTjKWAlRlilfgMn4dEiiN/l9yzfv9lTPs9t2It84XWq1X5pnTekFE8ALeVkqqzpH0OJeSFyRk1Z3xZRQ4gQKBgQDX33W+SxICh1nGESXQR7bjkij7BdtLE/tQ4T1J4/xqT7ke6C2RPyO+GqDd+Q8k797T+wHJ9Kxmwoo9vhn3BdfV+bvlwVEzAuK7ovBc8m7hnAIJIyJwyNYMMQt/2A+dGtoeJBuwd0rznSHhroV/y2I8ngd73xDQhe02BcjCpOMwMQKBgQChL2yCA0zGf8nwaFT5CWjfVQ23arHOK+xex8rJgh3hSvGgpAzAER9ZV5EveZyPVaGGGeXhbIJpeaL1vsmCAv2RrJVJtaB1ONLU9qE5yuS1skp/2aPBwDSMsjdOmKQzT2whn3xr6JbJHybg1rvIVj2nXdXTluKrxqX8znAPYNJ3RQKBgQCizgKsu00f3xhTiocsJ4nE42xItMgIPU+iVdy1J14sh2ej6ZLEIgxAyTBdeAJ15vn0gS9+Mir7bOh5XC+U3zFCTQ/qXPtyL1D0FhSU5Lm/KLtYTMkiqjTUCQEVL8vGvoOVi8HeOnmqdO0imU5RbP/vm3clcrvpp7eYsJASS+yTYQKBgCVrCveLqJEULV7/+Wnuw0fFSO/hdvFzxOWj3/GzoRgh+8HdE5Cq6Oomp++rfarQqDSnYnRYalXuBOSjq8fgxdjBhc6cuWk9DcSelIMEFOARSbYwYhGiexCGdsxqJwQ6VdHgYlPypL5/2tirQOCbFKj74Z3DE0/pR7NOgTkwsus5AoGBAJ0FppZgs5EbIi8CmjZNs/eyAW3fPcVKPZtE0nWHOaO9Pb3cJ3EbL4MNjy570i5mokj8lH52pyU96fkFsRwppce8Bd7fm9fHXNDiHGMfsiex3jw6Yw2lPCEJT9DDzNsXIrZBO3LTo3zdWaeba/FqGv4qUJC2fAzV61MffqvN8BEg";
    public static AlipayClient alipayClient;

    public void cash(Long dataId, Long userId, BigDecimal amount, String tel, String name, String outBizNo, String originalOrderId, String alipayUserId) throws AlipayApiException {
        try {
            // 初始化SDK
            //AlipayClient alipayClient = new DefaultAlipayClient(getAlipayConfig());
            amount.setScale(2, BigDecimal.ROUND_DOWN);
            // 构造请求参数以调用接口
            AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
            AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
            // 设置转账业务的标题
            model.setOrderTitle("游戏奖励");
            // 设置描述特定的业务场景
            model.setBizScene("DIRECT_TRANSFER");
        /*// 设置签名信息
        SignData signData = new SignData();
        signData.setOriSign("EqHFP0z4a9iaQ1ep==");
        signData.setPartnerId("签名被授权方支付宝账号ID");
        signData.setOriAppId("2021004135615110");
        signData.setOriOutBizNo("商户订单号");
        signData.setOriSignType("RSA2");
        signData.setOriCharSet("UTF-8");
        model.setSignData(signData);*/

            // 设置转账业务请求的扩展参数
            model.setBusinessParams("{\"payer_show_name_use_alias\":\"true\"}");
            // 设置业务备注
            model.setRemark("游戏奖励");
            // 设置商家侧唯一订单号
            model.setOutBizNo(outBizNo);
            // 设置订单总金额
            model.setTransAmount(amount.toString());
            // 设置业务产品码
            model.setProductCode("TRANS_ACCOUNT_NO_PWD");
            // 设置收款方信息
            Participant payeeInfo = new Participant();
            //因为让玩家填写了手机号所以读这个字段
            payeeInfo.setIdentity(alipayUserId);
            payeeInfo.setName(name);
            payeeInfo.setIdentityType("ALIPAY_LOGON_ID");
            model.setPayeeInfo(payeeInfo);
            // 设置原支付宝业务单号
            model.setOriginalOrderId(originalOrderId);
            request.setBizModel(model);
            AlipayFundTransUniTransferResponse response = alipayClient.certificateExecute(request);
            System.out.println(response.getBody());
            if (response.isSuccess()) {
                System.out.println("调用成功");
                User user = userCacheService.getUserInfoById(userId);
                if (user.getIsCash() == 0) {
                    userService.updateIsCash(user.getId());
                }
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
                cashRecordService.cashRecordSuccess(outBizNo, CashStatusTypeEnum.SUCCESS.getName());
                //userCapitalService.subUserOccupyBalanceByCashSuccess(userId, userCapital.getBalance(),
                  //      userCapital.getOccupyBalance(), outBizNo, amount, dataId);
            } else {
                System.out.println("调用失败");
                JSONObject body = JSONObject.parseObject(response.getBody());
                String errorCode = body.getJSONObject("alipay_fund_trans_uni_transfer_response").getString("code");
                String errorMessage = body.getJSONObject("alipay_fund_trans_uni_transfer_response").getString("sub_msg");
                cashRecordService.cashRecordFail(outBizNo, errorMessage);
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
                //userCapitalService.subUserOccupyBalanceByCashFail(amount, userId,
                  //      userCapital.getBalance(), userCapital.getOccupyBalance(), outBizNo, dataId);
                if (errorMessage.equals("收款账号不存在或姓名有误，建议核实账号和姓名是否准确")) {
                    //实名不一致 直接修改为未实名认证状态 并且没有收益状态
                    userService.updateUserNoPassIdCard(userId);
                    managerSocketService.noPassAuthUser(userId);
                }
                userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.rmb.getValue());
                JSONObject pushData = new JSONObject();
                pushData.put("userId", userId);
                pushData.put("capitalType", UserCapitalTypeEnum.rmb.getValue());
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
            }
        } catch (Exception e) {
            logger.info("提现失败," + e.getMessage());
        }
    }

    @PostConstruct
    private void getAlipayConfig() throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setPrivateKey(privateKey);
        alipayConfig.setServerUrl("https://openapi.alipay.com/gateway.do");
        alipayConfig.setAppId("2021005164630138");
        ClassLoader classLoader = this.getClass().getClassLoader();

        URL resource = classLoader.getResource("appCertPublicKey_2021005164630138.crt");
        String path = resource.getPath();
        alipayConfig.setAppCertPath(path);
        alipayConfig.setAlipayPublicCertPath(this.getClass().getClassLoader().getResource("alipayCertPublicKey_RSA2.crt").getPath());
        alipayConfig.setRootCertPath(this.getClass().getClassLoader().getResource("alipayRootCert.crt").getPath());
        alipayClient = new DefaultAlipayClient(alipayConfig);


        new Timer("监控未提交至支付宝的提现订单").schedule(new TimerTask() {
            public void run() {
                try {
                    // 获取20条数据
                    /*if (cashOrderNos.size() <= 0) {
                        return;
                    }*/
                    List<CashRecord> cashRecords = cashRecordService.findSingleOrderByBatchOrderNo();
                    if (cashRecords.size() <= 0) {
                        return;
                    }
                    logger.info("检测到未推送的提现订单数量：" + cashRecords.size());
                    // 生成批量订单 插入数据库 同时推送至微信或支付宝
                    String orderNo = OrderUtil.getBatchOrder32Number();
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    for (CashRecord cashRecord : cashRecords) {
                        BigDecimal realAmount = cashRecord.getAmount().subtract(cashRecord.getFee());
                        totalAmount = totalAmount.add(realAmount);
                        User user = userCacheService.getUserInfoById(cashRecord.getUserId());
                        cash(cashRecord.getId(), cashRecord.getUserId(), realAmount, user.getPhone(), user.getRealName(), cashRecord.getOrderNo(), cashRecord.getOrderNo(), user.getAlipayId());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        }, 1000L, 10000L);
    }

    public static void main(String[] args) throws AlipayApiException {
        String batchOrder32Number = OrderUtil.getBatchOrder32Number();
        System.out.println(batchOrder32Number);
    }

}
