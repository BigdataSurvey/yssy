package com.zywl.app.manager.servlet;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.WeChatAccessToken;
import com.zywl.app.base.bean.WeChatUserInfo;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.AsyncServletProcessor;
import com.zywl.app.base.util.HTTPUtil;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.Response;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.LoginService;
import com.zywl.app.manager.service.manager.ManagerConfigService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "WXLoginOauthServlet", urlPatterns = "/wechatLoginOauth", asyncSupported = true)
public class WXLoginOauthServlet extends BaseServlet {

    private final static String CHECK_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/auth";

    private final static String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/userinfo";

    private final static String APPID = "wx2c284333481b27d2";

    private final static String AppSecret = "f323a1711b5791050e7382972e440999";

    private LoginService loginService;

    private ManagerConfigService managerConfigService;


    private AppConfigCacheService appConfigCacheService;


    public WXLoginOauthServlet() {
        loginService = SpringUtil.getService(LoginService.class);
        managerConfigService = SpringUtil.getService(ManagerConfigService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
    }


    /**
     * 处理微信回调
     */
    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp) throws AppException, Exception {

        return new AsyncServletProcessor(request) {
            public void run() {
                try {
                    // 1. 获取access_token
                    String oldWsid = request.getParameter("oldWsid");
                    String versionId = request.getParameter("versionId");
                    String inviteCode = request.getParameter("inviteCode");
                    String deviceId = request.getParameter("deviceId");
                    String os = request.getParameter("os");
                    String code = request.getParameter("code");
                    logger.info("获取code:" + code);
                    if (code==null){
                        String gameToken = request.getParameter("gameToken");
                        if (gameToken != null) {
                            Response.doResponse(asyncContext, loginService.loginByGameToken(gameToken, oldWsid, versionId, clientIp).toJSONString());
                            return;
                        }
                    }
                    WeChatAccessToken accessToken = getAccessToken(code);
                    logger.info("accessToken:" + accessToken);
                    if (accessToken.getErrcode() != null) {
                        logger.info(accessToken.getErrmsg());
                        Response.doResponse(asyncContext, "网络异常，连接服务器失败,错误码：" + accessToken.getErrcode());
                        return;
                    }
                    JSONObject result = new JSONObject();
                    request.getSession().invalidate();
                    if (managerConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
                        String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
                        if (!clientIp.equals(baiIp)) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, result, "系统维护中").toJSONString());
                            return;
                        }
                    }


                    String openId = accessToken.getOpenid();
                    String wxLoginURL = WX_LOGIN_URL + "?access_token=" + accessToken.getAccess_token() + "&openid=" + openId;;
                    String getJSON;
                    JSONObject wxInfo = new JSONObject();
                    int accessTokenVail = 1;
                    try {
                        logger.info("请求微信登录接口[" + wxLoginURL + "]");
                        getJSON = HTTPUtil.get(wxLoginURL);
                        logger.info("微信登录接口请求结果[" + wxLoginURL + "]：" + getJSON);
                        wxInfo = JSON.parseObject(getJSON);
                        openId = wxInfo.getString("openid");
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.info(e.getMessage());
                        accessTokenVail = 0;
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

    /**
     * 使用code获取access_token
     */
    public WeChatAccessToken getAccessToken(String code) throws Exception {
        String url = getAccessTokenUrl(code, APPID, AppSecret);
        String response = HttpUtil.get(url);
        if (response == null) {
            throwExp("请求微信失败");
        }
        WeChatAccessToken accessToken = JSON.parseObject(response, WeChatAccessToken.class);
        return accessToken;
    }

    /**
     * 获取用户信息
     */
    private WeChatUserInfo getUserInfo(String accessToken, String openId) throws Exception {
        String url = getUserInfoUrl(accessToken, openId);
        String response = HttpUtil.get(url);
        WeChatUserInfo userInfo = JSON.parseObject(response, WeChatUserInfo.class);
        return userInfo;
    }

    /**
     * 刷新access_token
     */
    public WeChatAccessToken refreshToken(String refreshToken) throws Exception {
        String url = getRefreshTokenUrl(refreshToken);
        String response = HttpUtil.get(url);
        WeChatAccessToken accessToken = JSON.parseObject(response, WeChatAccessToken.class);
        return accessToken;
    }


    //
//    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp,String code)
//            throws AppException, Exception {
//        return null;
//
//    }
    private void checkAccessToken(String accessToken,String openId) {
        String checkURL = CHECK_ACCESS_TOKEN + "?access_token=" + accessToken + "&openid=" + openId;
        String getCheckResultJSON = HTTPUtil.get(checkURL);
        logger.info("getCheckResultJSON" + getCheckResultJSON);
        if (isNull(getCheckResultJSON)) {
            throwExp("获取微信信息失败，请稍后再试");
        }
        JSONObject checkResult = JSON.parseObject(getCheckResultJSON);
        if (checkResult.getInteger("errcode") != 0) {
            throwExp(checkResult.getString("errmsg"));
        }
    }

    // 获取access_token地址
    public String getAccessTokenUrl(String code, String appId, String appSecret) {
        return String.format("https://api.weixin.qq.com/sns/oauth2/access_token?" +
                        "appid=%s&secret=%s&code=%s&grant_type=authorization_code",
                appId, appSecret, code);
    }

    // 获取用户信息地址
    public String getUserInfoUrl(String accessToken, String openId) {
        return String.format("https://api.weixin.qq.com/sns/userinfo?" +
                "access_token=%s&openid=%s", accessToken, openId);
    }

    // 刷新token地址
    public String getRefreshTokenUrl(String refreshToken) {
        return String.format("https://api.weixin.qq.com/sns/oauth2/refresh_token?" +
                        "appid=%s&grant_type=refresh_token&refresh_token=%s",
                APPID, refreshToken);
    }


}
