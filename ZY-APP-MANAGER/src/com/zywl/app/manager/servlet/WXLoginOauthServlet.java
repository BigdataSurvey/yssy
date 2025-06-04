package com.zywl.app.manager.servlet;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.WeChatAccessToken;
import com.zywl.app.base.bean.WeChatConfig;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URL;

@SuppressWarnings("serial")
@WebServlet(name = "LoginOauthServlet", urlPatterns = "/wechatLoginOauth", asyncSupported = true)
public class WXLoginOauthServlet extends BaseServlet {

    private final static String CHECK_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/auth";

    private final static String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/userinfo";

    private final static String APPID = "wx2c284333481b27d2";

    private final static String AppSecret = "f323a1711b5791050e7382972e440999";

    private LoginService loginService;

    private ManagerConfigService managerConfigService;


    private AppConfigCacheService appConfigCacheService;

    @Autowired
    private WeChatConfig weChatConfig;


    /**
     * 处理微信回调
     */
    public Object handleCallback(HttpServletRequest request, HttpServletResponse response, String clientIp,String code) throws Exception {
        // 1. 获取access_token
        WeChatAccessToken accessToken = getAccessToken(code);

        // 2. 获取用户信息
        WeChatUserInfo userInfo = getUserInfo(accessToken.getAccessToken(), accessToken.getOpenid());

        return new AsyncServletProcessor(request) {
            public void run() {
                try {
                    JSONObject result = new JSONObject();
                    request.getSession().invalidate();
                    if (managerConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
                        String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
                        if (!clientIp.equals(baiIp)) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, result, "系统维护中").toJSONString());
                            return;
                        }
                    }
                    String oldWsid = request.getParameter("oldWsid");
                    String versionId = request.getParameter("versionId");
                    String gameToken = request.getParameter("gameToken");
                    if (gameToken != null) {
                        Response.doResponse(asyncContext, loginService.loginByGameToken(gameToken, oldWsid, versionId, clientIp).toJSONString());
                        return;
                    }
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
    private WeChatAccessToken getAccessToken(String code) throws Exception {
        String url = weChatConfig.getAccessTokenUrl(code);
        String response = HttpUtil.get(url);

        WeChatAccessToken accessToken = JSON.parseObject(response, WeChatAccessToken.class);
        if (accessToken.getErrcode() != null) {

        }

        return accessToken;
    }

    /**
     * 获取用户信息
     */
    private WeChatUserInfo getUserInfo(String accessToken, String openId) throws Exception {
        String url = weChatConfig.getUserInfoUrl(accessToken, openId);
        String response = HttpUtil.get(url);

        WeChatUserInfo userInfo = JSON.parseObject(response, WeChatUserInfo.class);
        if (userInfo.getErrcode() != null) {
        }

        return userInfo;
    }

    /**
     * 刷新access_token
     */
    public WeChatAccessToken refreshToken(String refreshToken) throws Exception {
        String url = weChatConfig.getRefreshTokenUrl(refreshToken);
        String response = HttpUtil.get(url);

        WeChatAccessToken accessToken = JSON.parseObject(response, WeChatAccessToken.class);
        if (accessToken.getErrcode() != null) {
        }

        return accessToken;
    }


    public WXLoginOauthServlet() {
        loginService = SpringUtil.getService(LoginService.class);
        managerConfigService = SpringUtil.getService(ManagerConfigService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
    }

    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp) throws AppException, Exception {
        return null;
    }
//
//    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp,String code)
//            throws AppException, Exception {
//        return null;
//
//    }
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
