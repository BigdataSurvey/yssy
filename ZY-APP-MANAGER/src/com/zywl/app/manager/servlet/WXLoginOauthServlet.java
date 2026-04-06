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

    private final static String APPID = "wxc6c0915b2cc3404f";

    private final static String AppSecret = "08aea79a1302729d6f21c5e17ca80de7";

    private LoginService loginService;

    private ManagerConfigService managerConfigService;


    private AppConfigCacheService appConfigCacheService;


    public WXLoginOauthServlet() {
        loginService = SpringUtil.getService(LoginService.class);
        managerConfigService = SpringUtil.getService(ManagerConfigService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
    }

    /**
     * 处理微信登录回调
     *
     * 支持三种登录路径：
     * 1. accessToken + openId 直传登录（兼容旧 wxLoginOauth，也兼容 iOS/浏览器测试）
     * 2. 微信网页授权回调传 code
     * 3. gameToken 登录
     */
    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp) throws AppException, Exception {

        return new AsyncServletProcessor(request) {
            @Override
            public void run() {
                try {
                    // 读取请求参数
                    String oldWsid = trimToNull(request.getParameter("oldWsid"));
                    String versionId = trimToNull(request.getParameter("versionId"));
                    String inviteCode = trimToNull(request.getParameter("inviteCode"));
                    String deviceId = trimToNull(request.getParameter("deviceId"));
                    String os = trimToNull(request.getParameter("os"));
                    String code = trimToNull(request.getParameter("code"));
                    String iosOpenId = trimToNull(request.getParameter("openId"));
                    String iosAccessToken = trimToNull(request.getParameter("accessToken"));
                    String gameToken = trimToNull(request.getParameter("gameToken"));

                    logger.info(String.format(
                            "wechatLoginOauth 请求开始： uri=%s, method=%s, queryString=%s, contentType=%s, code=%s, openId=%s, accessToken=%s, gameToken=%s",
                            request.getRequestURI(),
                            request.getMethod(),
                            request.getQueryString(),
                            request.getContentType(),
                            code,
                            iosOpenId,
                            iosAccessToken,
                            gameToken
                    ));

                    logger.info(String.format(
                            "wechatLoginOauth 参数：oldWsid=%s, versionId=%s, inviteCode=%s, deviceId=%s, os=%s, code=%s, openId=%s, accessToken=%s, gameToken=%s",
                            oldWsid,
                            versionId,
                            inviteCode,
                            deviceId,
                            os,
                            code,
                            iosOpenId,
                            iosAccessToken,
                            gameToken
                    ));

                    /**
                     * 第一优先级：兼容旧 wxLoginOauth 的 accessToken + openId 登录
                     *
                     * 这里不再限制 os=1
                     * 只要前端传了 accessToken + openId，就走这个分支
                     *
                     * 这样：
                     * - iOS 以前怎么传，还是怎么传
                     * - 浏览器测试时，也可以继续用 accessToken + openId 模拟“账号密码登录”
                     */
                    if (iosAccessToken != null && iosOpenId != null) {
                        String baiIp = appConfigCacheService.getConfigByKey(
                                RedisKeyConstant.APP_CONFIG_BAI_IP,
                                Config.BAI_IP
                        );

                        String urlParameters = "?access_token=" + iosAccessToken + "&openid=" + iosOpenId;
                        String wxLoginURL = WX_LOGIN_URL + urlParameters;
                        String getJSON;
                        JSONObject wxInfo = new JSONObject();
                        int accessTokenVail = 1;

                        try {
                            /**
                             * 兼容旧 wxLoginOauth 的测试号逻辑：
                             * openId 长度小于 5 且当前 IP 为白名单 IP 时，不走微信接口校验
                             */
                            if (iosOpenId.length() < 5 && clientIp.equals(baiIp)) {
                                logger.info(String.format(
                                        "wechatLoginOauth 命中测试号逻辑：clientIp=%s, openId=%s",
                                        clientIp,
                                        iosOpenId
                                ));
                                wxInfo.put("nickname", "测试号-" + iosOpenId);
                            } else {
                                checkAccessToken(urlParameters);
                                logger.info(String.format(
                                        "wechatLoginOauth accessToken登录，请求微信用户信息接口：%s",
                                        wxLoginURL
                                ));

                                getJSON = HTTPUtil.get(wxLoginURL);
                                logger.info(String.format(
                                        "wechatLoginOauth accessToken登录，微信接口返回：%s",
                                        getJSON
                                ));

                                wxInfo = JSON.parseObject(getJSON);
                                iosOpenId = wxInfo.getString("openid");
                            }
                        } catch (Exception e) {
                            logger.warn(String.format(
                                    "wechatLoginOauth accessToken登录，accessToken校验失败或已过期：%s",
                                    e.getMessage()
                            ), e);
                            accessTokenVail = 0;
                        }

                        if (wxInfo.containsKey("errcode") && "40001".equals(wxInfo.getString("errcode"))) {
                            Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                            return;
                        }

                        // 保持和旧 wxLoginOauth 一致：把 accessToken 放到 password
                        wxInfo.put("password", iosAccessToken);

                        Response.doResponse(
                                asyncContext,
                                loginService.loginOrRegister(
                                        iosOpenId,
                                        clientIp,
                                        versionId,
                                        oldWsid,
                                        inviteCode,
                                        wxInfo,
                                        accessTokenVail,
                                        deviceId,
                                        os
                                ).toJSONString()
                        );
                        return;
                    }

                    // 第二优先级：gameToken 登录（保持原逻辑不变）
                    if (gameToken != null) {
                        logger.info("wechatLoginOauth 走gameToken登录");
                        Response.doResponse(
                                asyncContext,
                                loginService.loginByGameToken(gameToken, oldWsid, versionId, clientIp).toJSONString()
                        );
                        return;
                    }

                    // 第三优先级：微信网页授权 code 登录（保持原逻辑不变）
                    if (code == null) {
                        logger.warn("wechatLoginOauth 缺少code参数，无法继续换取accessToken");
                        Response.doResponse(asyncContext, "缺少code参数");
                        return;
                    }

                    logger.info(String.format("wechatLoginOauth 开始使用 code 换取 accessToken，code=%s", code));
                    WeChatAccessToken accessToken = getAccessToken(code);
                    logger.info(String.format("wechatLoginOauth 获取 accessToken 结果：%s", accessToken));

                    if (accessToken == null) {
                        logger.warn("wechatLoginOauth 获取accessToken失败：返回对象为空");
                        Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                        return;
                    }

                    if (accessToken.getErrcode() != null) {
                        logger.warn(String.format(
                                "wechatLoginOauth 获取accessToken失败，errcode=%s, errmsg=%s",
                                accessToken.getErrcode(),
                                accessToken.getErrmsg()
                        ));
                        Response.doResponse(asyncContext, "网络异常，连接服务器失败,错误码：" + accessToken.getErrcode());
                        return;
                    }

                    // session处理（保持原逻辑不变）
                    if (request.getSession(false) != null) {
                        request.getSession(false).invalidate();
                    }

                    // 服务状态校验（保持原逻辑不变）
                    if (managerConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
                        String baiIp = appConfigCacheService.getConfigByKey(
                                RedisKeyConstant.APP_CONFIG_BAI_IP,
                                Config.BAI_IP
                        );
                        if (!clientIp.equals(baiIp)) {
                            JSONObject result = new JSONObject();
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, result, "系统维护中").toJSONString());
                            return;
                        }
                    }

                    // 用 accessToken + openId 请求微信用户信息
                    String openId = accessToken.getOpenid();
                    String wxLoginURL = WX_LOGIN_URL + "?access_token=" + accessToken.getAccess_token() + "&openid=" + openId;
                    String getJSON;
                    JSONObject wxInfo = new JSONObject();
                    int accessTokenVail = 1;

                    try {
                        logger.info(String.format("code登录，请求微信用户信息接口：%s", wxLoginURL));

                        getJSON = HTTPUtil.get(wxLoginURL);
                        logger.info(String.format("code登录，微信用户信息接口返回：%s", getJSON));

                        wxInfo = JSON.parseObject(getJSON);
                        openId = wxInfo.getString("openid");
                    } catch (Exception e) {
                        logger.warn(String.format("code登录，请求微信用户信息接口异常：%s", e.getMessage()), e);
                        accessTokenVail = 0;
                    }

                    if (wxInfo.containsKey("errcode") && "40001".equals(wxInfo.getString("errcode"))) {
                        Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                        return;
                    }

                    // 登录或注册
                    Response.doResponse(
                            asyncContext,
                            loginService.loginOrRegister(
                                    openId,
                                    clientIp,
                                    versionId,
                                    oldWsid,
                                    inviteCode,
                                    wxInfo,
                                    accessTokenVail,
                                    deviceId,
                                    os
                            ).toJSONString()
                    );
                    return;

                } catch (AppException e) {
                    logger().warn(String.format("wechatLoginOauth执行异常：%s", e.getMessage()), e);
                    Response.doResponse(asyncContext, e.getMessage());
                } catch (Exception e) {
                    logger().error(String.format("wechatLoginOauth未知异常：%s", e.getMessage()), e);
                    Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                }
            }
        };
    }

    /**
     * 将字符串去掉前后空格；空串返回 null
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
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
    private void checkAccessToken(String accessToken, String openId) {
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
