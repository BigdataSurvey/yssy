package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.LoginService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "LoginOauthServlet", urlPatterns = "/wxLoginOauth", asyncSupported = true)
public class LoginOauthServlet extends BaseServlet {

    private final static String CHECK_ACCESS_TOKEN = "https://api.weixin.qq.com/sns/auth";

    private final static String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/userinfo";

    private LoginService loginService;

    private ManagerConfigService managerConfigService;


    private AppConfigCacheService appConfigCacheService;


    public LoginOauthServlet() {
        loginService = SpringUtil.getService(LoginService.class);
        managerConfigService = SpringUtil.getService(ManagerConfigService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
    }


    /**
     * 处理旧版微信登录 / Taptap登录 / gameToken登录
     *
     * 支持三种登录路径：
     * 1. gameToken 登录
     * 2. Taptap 登录（tabtabId）
     * 3. 微信登录（accessToken + openId）
     *
     */
    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp)
            throws AppException, Exception {

        return new AsyncServletProcessor(request) {
            @Override
            public void run() {
                try {
                    JSONObject result = new JSONObject();

                    // 读取请求参数
                    String accessToken = trimToNull(request.getParameter("accessToken"));
                    String openId = trimToNull(request.getParameter("openId"));
                    String oldWsid = trimToNull(request.getParameter("oldWsid"));
                    String versionId = trimToNull(request.getParameter("versionId"));
                    String inviteCode = trimToNull(request.getParameter("inviteCode"));
                    String tabtabId = trimToNull(request.getParameter("tabtabId"));
                    String authCode = trimToNull(request.getParameter("auth_code"));
                    String deviceId = trimToNull(request.getParameter("deviceId"));
                    String os = trimToNull(request.getParameter("os"));
                    String gameToken = trimToNull(request.getParameter("gameToken"));
                    String userName = trimToNull(request.getParameter("userName"));
                    String userHead = trimToNull(request.getParameter("userHead"));

                    // 入口日志
                    logger.info(String.format("wxLoginOauth请求开始，uri=%s, method=%s, queryString=%s, contentType=%s, clientIp=%s",
                            request.getRequestURI(),
                            request.getMethod(),
                            request.getQueryString(),
                            request.getContentType(),
                            clientIp));

                    logger.info(String.format("wxLoginOauth参数：oldWsid=%s, versionId=%s, inviteCode=%s, tabtabId=%s, authCode=%s, deviceId=%s, os=%s, openId=%s, accessToken=%s, gameToken=%s, userName=%s, userHead=%s",
                            oldWsid,
                            versionId,
                            inviteCode,
                            tabtabId,
                            authCode,
                            deviceId,
                            os,
                            openId,
                            accessToken,
                            gameToken,
                            userName,
                            userHead));

                    // session处理
                    if (request.getSession(false) != null) {
                        request.getSession(false).invalidate();
                    }

                    // 服务状态校验
                    if (managerConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
                        String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
                        if (!clientIp.equals(baiIp)) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, result, "系统维护中").toJSONString());
                            return;
                        }
                    }

                    String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);

                    // gameToken登录
                    if (gameToken != null) {
                        logger.info("wxLoginOauth走gameToken登录");
                        Response.doResponse(
                                asyncContext,
                                loginService.loginByGameToken(gameToken, oldWsid, versionId, clientIp).toJSONString()
                        );
                        return;
                    }

                    // Taptap登录
                    if (StringUtils.isNotEmpty(tabtabId)) {
                        logger.info(String.format("wxLoginOauth走Taptap登录，tabtabId=%s", tabtabId));
                        Response.doResponse(
                                asyncContext,
                                loginService.loginOrRegisterTabtab(
                                        tabtabId,
                                        clientIp,
                                        versionId,
                                        oldWsid,
                                        inviteCode,
                                        userName,
                                        userHead
                                ).toJSONString()
                        );
                        return;
                    }

                    // 微信 accessToken + openId 登录
                    if (accessToken == null || openId == null) {
                        logger.warn(String.format("wxLoginOauth缺少accessToken或openId，accessToken=%s, openId=%s",
                                accessToken,
                                openId));
                        throwExp("accessToken或openId异常");
                    }

                    String urlParameters = "?access_token=" + accessToken + "&openid=" + openId;
                    String wxLoginURL = WX_LOGIN_URL + urlParameters;
                    String getJSON;
                    JSONObject wxInfo = new JSONObject();
                    int accessTokenVail = 1;

                    // 测试号逻辑：只对白名单IP生效，且openId长度很短
                    if (openId != null && openId.length() < 5 && clientIp.equals(baiIp)) {
                        logger.info(String.format("wxLoginOauth命中测试号逻辑，openId=%s", openId));
                        wxInfo.put("nickname", "测试号-" + openId);
                    } else {
                        try {
                            checkAccessToken(urlParameters);
                            logger.info(String.format("请求微信登录接口：%s", wxLoginURL));

                            getJSON = HTTPUtil.get(wxLoginURL);
                            logger.info(String.format("微信登录接口请求结果：%s", getJSON));

                            wxInfo = JSON.parseObject(getJSON);
                            openId = wxInfo.getString("openid");
                        } catch (Exception e) {
                            // 将格式化后的字符串作为 message，e 作为 Throwable 传入，完美兼容 Commons Logging
                            logger.warn(String.format("wxLoginOauth accessToken校验失败或已过期：%s", e.getMessage()), e);
                            accessTokenVail = 0;
                        }
                    }

                    if (wxInfo.containsKey("errcode") && "40001".equals(wxInfo.getString("errcode"))) {
                        logger.warn(String.format("微信登录接口返回40001，wxInfo=%s", wxInfo.toJSONString()));
                        Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                        return;
                    }

                    wxInfo.put("password", accessToken);

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
                    logger().warn(String.format("wxLoginOauth执行异常：%s", e.getMessage()), e);
                    Response.doResponse(asyncContext, e.getMessage());
                } catch (Exception e) {
                    logger().error("wxLoginOauth未知异常", e);
                    Response.doResponse(asyncContext, "网络异常，连接服务器失败");
                }
            }
        };
    }

    /**
     * 去掉前后空格；空串转为 null
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
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
