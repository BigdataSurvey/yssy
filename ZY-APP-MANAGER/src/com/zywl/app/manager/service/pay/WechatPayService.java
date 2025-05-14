package com.zywl.app.manager.service.pay;


import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ijpay.core.IJPayHttpResponse;
import com.ijpay.core.enums.AuthTypeEnum;
import com.ijpay.core.enums.RequestMethodEnum;
import com.ijpay.core.kit.AesUtil;
import com.ijpay.core.kit.PayKit;
import com.ijpay.core.kit.WxPayKit;
import com.ijpay.core.utils.DateTimeZoneUtil;
import com.ijpay.wxpay.WxPayApi;
import com.ijpay.wxpay.enums.WxDomainEnum;
import com.ijpay.wxpay.enums.v3.BasePayApiEnum;
import com.ijpay.wxpay.enums.v3.CertAlgorithmTypeEnum;
import com.ijpay.wxpay.model.v3.Amount;
import com.ijpay.wxpay.model.v3.UnifiedOrderModel;
import com.zywl.app.base.bean.Product;
import com.zywl.app.base.bean.RechargeOrder;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.ProductService;
import com.zywl.app.defaultx.service.RechargeOrderService;
import com.zywl.app.manager.bean.WxPayV3Bean;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.apache.poi.hwpf.model.ListTables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@ServiceClass(code = MessageCodeContext.WX_PAY_SERVER)
public class WechatPayService extends BaseService {

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private RechargeOrderService rechargeOrderService;
    private String privateKey="315077466fa7acb5794c02a9dc662700";


    //易云仙舍
    public static String appid = "wx938564a42cf4f4cb";

    public static String merchantId = "1665263042";
    /** 商户API私钥路径 */
    /**
     * 商户证书序列号
     */
    public static String merchantSerialNumber = "698EBED900EF454A6A4D76A52BC6AB98928E4D67";
    /**
     * 商户APIV3密钥
     */
    public static String apiV3Key = "HENANzongyiwangluokejigs20220314";

    public WxPayV3Bean wxPayV3Bean;

    @Autowired
    private PlayGameService gameService;

    private final static int OK = 200;

    private static final String URL="http://123.56.119.157:8001/";



    @Autowired
    private ManagerUserService managerUserService;

    @Autowired
    private ProductService productService;

    String serialNo="698EBED900EF454A6A4D76A52BC6AB98928E4D67";
    String platSerialNo;

    private static List<String> PRODUCT_ID = new ArrayList<>();

    public WxPayV3Bean getBean(){
        return wxPayV3Bean;
    }

    @PostConstruct
    public void _construct(){
        wxPayV3Bean = new WxPayV3Bean();
        wxPayV3Bean.setAppId(appid);
        wxPayV3Bean.setMchId(merchantId);
        wxPayV3Bean.setApiKey(privateKey);
        wxPayV3Bean.setApiKey3(apiV3Key);
        String p12Path = this.getClass().getClassLoader().getResource("apiclient_cert2.p12").getPath();
        wxPayV3Bean.setCertP12Path(p12Path);
        String keyPath = this.getClass().getClassLoader().getResource("apiclient_key2.pem").getPath();
        wxPayV3Bean.setKeyPath(keyPath);
        String certPath = this.getClass().getClassLoader().getResource("apiclient_cert2.pem").getPath();
        wxPayV3Bean.setCertP12Path(certPath);
        wxPayV3Bean.setPlatformCertPath(this.getClass().getClassLoader().getResource("platformCert.pem").getPath());
        wxPayV3Bean.setDomain(URL);

        List<Product> allProduct = productService.findAllProduct();
        allProduct.forEach(e->PRODUCT_ID.add(e.getId().toString()));

    }
   /* @ServiceMethod(code = "001", description = "发起微信支付")
    public Object appPay(ManagerSocketServer adminSocketServer, JSONObject data) {
        User user = userCacheService.getUserInfoById(data.getLong("userId"));
        Long productId = data.getLong("productId");
        int price = 1000000;
        if (PlayGameService.productMap.containsKey(productId.toString())){
            price = PlayGameService.productMap.get(productId.toString()).getPrice();
        }
        String orderNo = OrderUtil.getOrder5Number();
        RechargeOrder order = rechargeOrderService.addOrder(user.getId(),orderNo,"123123",productId,price, com.zywl.app.base.util.DateUtil.getDateByM(10));
        RechargeOrder rechargeOrder = rechargeOrderService.findByOrderNo(orderNo);
        rechargeOrderService.rechargeSuccess(orderNo  ,"模拟充值","111");
        //更改技能释放次数
        if (rechargeOrder.getProductId()>1){
            Player activePlayer = gameService.getActivePlayer(rechargeOrder.getUserId().toString());
            JSONArray skills = JSONArray.parseArray(activePlayer.getSkill());
            JSONArray newSkill = new JSONArray();
            for (Object o : skills) {
                JSONObject skill = (JSONObject) o;
                skill.put("cd",PlayGameService.skillInfo.get(skill.getString("skillId")).getCd()+10);
                newSkill.add(skill);
            }
            PlayGameService.activePlayer.get(rechargeOrder.getUserId().toString()).setSkill(newSkill.toJSONString());
        }
        //更改会员等级
        managerUserService.updateUserVipLv(rechargeOrder.getUserId(),rechargeOrder.getProductId());
        //给上级加灵气
        if (user.getParentId()!=null){
            BigDecimal anima = new BigDecimal(String.valueOf( rechargeOrder.getPrice()/100/10));
            userCacheService.addParentAnima(user.getId(),user.getParentId().toString(),anima);
        }
        return new JSONObject();
    }
*/
    @ServiceMethod(code = "001", description = "发起微信支付")
    public Object appPay(ManagerSocketServer adminSocketServer, JSONObject data) throws IOException {
        checkNull(data);
        checkNull(data.get("productId"));
        User user = userCacheService.getUserInfoById(data.getLong("userId"));
        Long productId = data.getLong("productId");
        if (PRODUCT_ID.indexOf(productId.toString())<0){
            throwExp("增值产品不存在");
        }
        if (user.getFirstCharge()==1 && productId.toString().equals("1")){
            throwExp("您已购买过此产品，不能再次购买");
        }
        int price = 1000000;
        if (PlayGameService.productMap.containsKey(productId.toString())){
            price = PlayGameService.productMap.get(productId.toString()).getPrice();
        }

        try(InputStream  certFileInputStream = PayKit.getCertFileInputStream(wxPayV3Bean.getPlatformCertPath())) {
            String timeExpire = DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3);
            DateTime dateTime = DateUtil.parse(timeExpire);
            Date expireDate = new Date(dateTime.getTime());
            UnifiedOrderModel unifiedOrderModel = new UnifiedOrderModel()
                    .setAppid(wxPayV3Bean.getAppId())
                    .setMchid(wxPayV3Bean.getMchId())
                    .setDescription("逆天纪充值")
                    .setOut_trade_no(PayKit.generateStr())
                    .setTime_expire(timeExpire)
                    .setAttach("逆天纪充值")
                    .setNotify_url(wxPayV3Bean.getDomain().concat("/ZY-APP-MANAGER/payNotify"))
                    .setAmount(new Amount().setTotal(price));
            logger.info("统一下单参数 "+ unifiedOrderModel);
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.POST,
                    WxDomainEnum.CHINA.toString(),
                    BasePayApiEnum.APP_PAY.toString(),
                    wxPayV3Bean.getMchId(),
                    getSerialNumber(),
                    null,
                    wxPayV3Bean.getKeyPath(),
                   JSONObject.toJSONString(unifiedOrderModel),
                    AuthTypeEnum.RSA.getCode()
            );
            logger.info("统一下单响应 "+ response);
            // 根据证书序列号查询对应的证书来验证签名结果

            boolean verifySignature = WxPayKit.verifySignature(response, certFileInputStream);
            logger.info("verifySignature: {}"+ verifySignature);
            if (response.getStatus() == OK && verifySignature) {
                String body = response.getBody();
                JSONObject jsonObject = JSONObject.parseObject(body);
                String prepayId = jsonObject.getString("prepay_id");
                Map<String, String> map = WxPayKit.appCreateSign(wxPayV3Bean.getAppId(), wxPayV3Bean.getMchId(), prepayId, wxPayV3Bean.getKeyPath());
                logger.info("唤起支付参数:"+ map);
                logger.info(JSONObject.toJSONString(response));
                RechargeOrder order = rechargeOrderService.addOrder(user.getId(),unifiedOrderModel.getOut_trade_no(),prepayId,productId,price,expireDate);
                return   map;
            }
            logger.info(JSONObject.toJSONString(response));
            return   response;
        } catch (Exception e) {
            logger.error("系统异常", e);
            return e.getMessage();
        }
    }


    private String getSerialNumber() {
        if (StrUtil.isEmpty(serialNo)) {
            // 获取证书序列号
            X509Certificate certificate = PayKit.getCertificate(wxPayV3Bean.getCertPath());
            if (null != certificate) {
                serialNo = certificate.getSerialNumber().toString(16).toUpperCase();
                // 提前两天检查证书是否有效
                boolean isValid = PayKit.checkCertificateIsValid(certificate, wxPayV3Bean.getMchId(), -2);
            }
//            System.out.println("输出证书信息:\n" + certificate.toString());
//            // 输出关键信息，截取部分并进行标记
//            System.out.println("证书序列号:" + certificate.getSerialNumber().toString(16));
//            System.out.println("版本号:" + certificate.getVersion());
//            System.out.println("签发者：" + certificate.getIssuerDN());
//            System.out.println("有效起始日期：" + certificate.getNotBefore());
//            System.out.println("有效终止日期：" + certificate.getNotAfter());
//            System.out.println("主体名：" + certificate.getSubjectDN());
//            System.out.println("签名算法：" + certificate.getSigAlgName());
//            System.out.println("签名：" + certificate.getSignature().toString());
        }
        System.out.println("serialNo:" + serialNo);
        return serialNo;
    }







    public String v3Get() throws IOException {
        // 获取平台证书列表
        InputStream certFileInputStream = null;
        try {
            IJPayHttpResponse response = WxPayApi.v3(
                    RequestMethodEnum.GET,
                    WxDomainEnum.CHINA.toString(),
                    CertAlgorithmTypeEnum.getCertSuffixUrl(CertAlgorithmTypeEnum.NONE.getCode()),
                    wxPayV3Bean.getMchId(),
                    getSerialNumber(),
                    null,
                    wxPayV3Bean.getKeyPath(),
                    "",
                    AuthTypeEnum.RSA.getCode()
            );
            Map<String, List<String>> headers = response.getHeaders();
            logger.info("请求头: "+ headers);
            String timestamp = response.getHeader("Wechatpay-Timestamp");
            String nonceStr = response.getHeader("Wechatpay-Nonce");
            String serialNumber = response.getHeader("Wechatpay-Serial");
            String signature = response.getHeader("Wechatpay-Signature");

            String body = response.getBody();
            int status = response.getStatus();

            logger.info("serialNumber: "+ serialNumber);
            logger.info("status: "+status);
            logger.info("body: "+ body);
            int isOk = 200;

            if (status == isOk) {
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONArray dataArray = jsonObject.getJSONArray("data");
                // 默认认为只有一个平台证书
                JSONObject encryptObject = dataArray.getJSONObject(0);
                JSONObject encryptCertificate = encryptObject.getJSONObject("encrypt_certificate");
                String associatedData = encryptCertificate.getString("associated_data");
                String cipherText = encryptCertificate.getString("ciphertext");
                String nonce = encryptCertificate.getString("nonce");
                String algorithm = encryptCertificate.getString("algorithm");
                String serialNo = encryptObject.getString("serial_no");
                final String platSerialNo = savePlatformCert(associatedData, nonce, cipherText, algorithm, wxPayV3Bean.getPlatformCertPath());
                logger.info("平台证书序列号: "+platSerialNo+" serialNo: "+ serialNo);
                // 根据证书序列号查询对应的证书来验证签名结果
                certFileInputStream = PayKit.getCertFileInputStream(wxPayV3Bean.getPlatformCertPath());
                boolean verifySignature = WxPayKit.verifySignature(response, certFileInputStream);
                logger.info("verifySignature:"+ verifySignature);
            }
            return body;
        } catch (Exception e) {
            logger.error("获取平台证书列表异常", e);
            return null;
        }finally {
            certFileInputStream.close();
        }
    }



    private String savePlatformCert(String associatedData, String nonce, String cipherText, String algorithm, String certPath) {
        try {
            String key3 = wxPayV3Bean.getApiKey3();
            String publicKey;
            if (StrUtil.equals(algorithm, AuthTypeEnum.SM2.getPlatformCertAlgorithm())) {
                publicKey = PayKit.sm4DecryptToString(key3, cipherText, nonce, associatedData);
            } else {
                AesUtil aesUtil = new AesUtil(wxPayV3Bean.getApiKey3().getBytes(StandardCharsets.UTF_8));
                // 平台证书密文解密
                // encrypt_certificate 中的  associated_data nonce  ciphertext
                publicKey = aesUtil.decryptToString(
                        associatedData.getBytes(StandardCharsets.UTF_8),
                        nonce.getBytes(StandardCharsets.UTF_8),
                        cipherText
                );
            }
            if (StrUtil.isNotEmpty(publicKey)) {
                // 保存证书
                FileWriter writer = new FileWriter(certPath);
                writer.write(publicKey);
                // 获取平台证书序列号
                X509Certificate certificate = PayKit.getCertificate(new ByteArrayInputStream(publicKey.getBytes()));
                return certificate.getSerialNumber().toString(16).toUpperCase();
            }
            return "";
        } catch (Exception e) {
            logger.error("保存平台证书异常", e);
            return e.getMessage();
        }
    }


    public static void main(String[] args) throws Exception {
        System.out.println(  DateTimeZoneUtil.dateToTimeZone(System.currentTimeMillis() + 1000 * 60 * 3));
    }

}
