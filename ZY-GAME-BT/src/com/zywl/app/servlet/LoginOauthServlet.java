package com.zywl.app.servlet;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.AsyncServletProcessor;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.Response;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.service.AliPayCashService;
import com.zywl.app.service.LoginConfigService;
import com.zywl.app.service.LoginService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@SuppressWarnings("serial")
@WebServlet(name = "LoginOauthServlet", urlPatterns = "/wxLoginOauth", asyncSupported = true)
public class LoginOauthServlet extends BaseServlet {

    private final static String CHECK_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/auth";

    private final static String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/userinfo";

    private LoginService loginService;

    private LoginConfigService loginConfigService;


    private AppConfigCacheService appConfigCacheService;

    private UserService userService;


    public LoginOauthServlet() {
        loginService = SpringUtil.getService(LoginService.class);
        loginConfigService = SpringUtil.getService(LoginConfigService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
        userService = SpringUtil.getService(UserService.class);
    }


    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp)
            throws AppException, Exception {
        return new AsyncServletProcessor(request) {
            public void run() {
                try {
                    JSONObject result = new JSONObject();
                    request.getSession().invalidate();
                    if (loginConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
                        String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
                        if (!clientIp.equals(baiIp)) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, result, "系统维护中").toJSONString());
                            return;
                        }
                    }
                    String accessToken = request.getParameter("accessToken");
                    String openId = request.getParameter("openId");
                    String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
                   /* if (!clientIp.equals(baiIp) && !openId.equals("zongyitest")) {
                        String startStr = openId.substring(0, 6);
                        if (!startStr.equals("orp1j6")) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, result, "errorCode:A02080101").toJSONString());
                            return;
                        }
                    }*/
                    String oldWsid = request.getParameter("oldWsid");
                    String versionId = request.getParameter("versionId");
                    String inviteCode = request.getParameter("inviteCode");
                    String tabtabId = request.getParameter("tabtabId");
                    String authCode = request.getParameter("auth_code");
                    String deviceId = request.getParameter("deviceId");
                    String os = request.getParameter("os");
                    String gameToken = request.getParameter("gameToken");
                    if (gameToken != null) {
                        Response.doResponse(asyncContext, loginService.loginByGameToken(gameToken, oldWsid, versionId, clientIp).toJSONString());
                        return;
                    }
                    if (StringUtils.isNotEmpty(authCode)) {
                        AlipayClient alipayClient = AliPayCashService.alipayClient;
                        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
                        request.setGrantType("authorization_code");
                        request.setCode(authCode);
                        AlipaySystemOauthTokenResponse returnInfo = alipayClient.certificateExecute(request);
                        logger.info(returnInfo);

                        if (!returnInfo.isSuccess()) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, "获取支付宝用户信息失败，请稍后重试！").toJSONString());
                        } else {
                            String aliPayUserId = returnInfo.getOpenId();
                            List<User> byAliUserId = userService.findByAliUserId(aliPayUserId);
                            if (byAliUserId.size() >= 3) {
                                Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, "该支付宝已绑定其他账号！").toJSONString());
                            }else{
                                AlipayUserInfoShareRequest userInfoRequest = new AlipayUserInfoShareRequest();
                                AlipayUserInfoShareResponse response = alipayClient.certificateExecute(userInfoRequest, returnInfo.getAccessToken());
                                if (!response.isSuccess()) {
                                    Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, "获取支付宝用户信息失败，请稍后重试！").toJSONString());
                                }else {
                                    //支付宝登录
                                    Response.doResponse(asyncContext, loginService.loginOrRegisterAlipay(byAliUserId,aliPayUserId,response,authCode, clientIp, versionId, oldWsid, inviteCode).toJSONString());
                                }
                            }
                        }
                        return;
                    }
                    //throwExp("异常操作");
                    if (isNull(accessToken) || isNull(openId)) {
                        throwExp("accessToken或openId异常");
                    }
                    String urlParameters = "?access_token=" + accessToken + "&openid=" + openId;
                    String wxLoginURL = WX_LOGIN_URL + urlParameters;
                    String getJSON;
                    JSONObject wxInfo = new JSONObject();
                    int accessTokenVail = 1;
                    if (openId.length() < 5 && clientIp.equals(baiIp)) {
                        wxInfo.put("nickname", "测试号-" + openId);
                    } else {
                        try {
                            checkAccessToken(urlParameters);
                            logger.info("请求微信登录接口[" + wxLoginURL + "]");
                            getJSON = HTTPUtil.get(wxLoginURL);
                            logger.info("微信登录接口请求结果[" + wxLoginURL + "]：" + getJSON);
                            wxInfo = JSON.parseObject(getJSON);
                            openId = wxInfo.getString("openid");
                        } catch (Exception e) {
                            logger.info("accessToken过期，跳过验证");
                            accessTokenVail = 0;
                        }

                    }
                    if (wxInfo.containsKey("errcode") && wxInfo.getString("errcode").equals("40001")) {
                        Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                        return;
                    }
                    Response.doResponse(asyncContext, loginService.loginOrRegister(openId, clientIp, versionId, oldWsid, inviteCode, wxInfo, accessTokenVail, deviceId, os).toJSONString());
                    return;
                } catch (AppException e) {
                    logger().warn("执行异常：" + e);
                    Response.doResponse(asyncContext, e.getMessage());
                } catch (Exception e) {
                    logger().error("未知异常", e);
                    Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                }


            }
        };

    }


    private void checkAccessToken(String urlParameters) {
        String checkURL = CHECK_ACCESS_TOKEN + urlParameters;
        String getCheckResultJSON = HTTPUtil.get(checkURL);
        if (isNull(getCheckResultJSON)) {
            throwExp("获取微信信息失败，请稍后再试");
        }
        JSONObject checkResult = JSON.parseObject(getCheckResultJSON);
        if (checkResult.getInteger("errcode") != 0) {
            throwExp(checkResult.getString("errmsg"));
        }
    }


}
