package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.AsyncServletProcessor;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.Response;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.LoginService;
import com.zywl.app.manager.service.manager.ManagerConfigService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 手机号验证码登录接口
 * **/
@SuppressWarnings("serial")
@WebServlet(name = "TelLoginServlet", urlPatterns = "/telLoginOrRegister", asyncSupported = true)
public class TelLoginServlet extends BaseServlet {


    private LoginService loginService;

    private ManagerConfigService managerConfigService;


    private AppConfigCacheService appConfigCacheService;

    private UserCacheService userCacheService;


    public TelLoginServlet() {
        loginService = SpringUtil.getService(LoginService.class);
        managerConfigService = SpringUtil.getService(ManagerConfigService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
        userCacheService = SpringUtil.getService(UserCacheService.class);
    }


    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp)
            throws AppException, Exception {
        return new AsyncServletProcessor(request) {
            public void run() {
                try {
                    JSONObject result = new JSONObject();
                    request.getSession().invalidate();

                    if (managerConfigService.getInteger(Config.SERVICE_STATUS) == 0) {
                        String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
                        if (!clientIp.equals(baiIp)) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, result, "游戏正在维护").toJSONString());
                            return;
                        }
                    }

                    String versionId = request.getParameter("versionId");
                    String inviteCode = request.getParameter("inviteCode");
                    String os = request.getParameter("os");
                    String gameToken = request.getParameter("gameToken");
                    String tel = request.getParameter("tel");
                    String code = request.getParameter("code");
                    String oldWsid = request.getParameter("oldWsid");

                    logger.info("[TelLoginServlet] tel=" + tel + ", inviteCode=" + inviteCode + ", os=" + os + ", gameToken=" + gameToken + ", versionId=" + versionId);

                    if (gameToken != null) {
                        Response.doResponse(asyncContext, loginService.loginByGameToken(gameToken, oldWsid, versionId, clientIp).toJSONString());
                        return;
                    }

                    if (tel == null || tel.length() != 11) {
                        Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, "手机号码格式有误").toJSONString());
                        return;
                    }

                    if (code == null || "".equals(code.trim())) {
                        Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, "请输入正确的验证码").toJSONString());
                        return;
                    }

                    // 保留测试后门：手机号前三位为101，且验证码等于手机号后6位
                    String telStart = tel.substring(0, 3);
                    String telEnd = tel.substring(5, 11);
                    if (telStart.equals("101") && telEnd.equals(code)) {
                        JSONObject jsonObject = loginService.loginOrRegisterByTel(tel, clientIp, versionId, oldWsid, inviteCode, os);
                        if (jsonObject.containsKey("error") && jsonObject.getInteger("error") == 1) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, jsonObject.getString("msg")).toJSONString());
                        } else {
                            Response.doResponse(asyncContext, jsonObject.toJSONString());
                        }
                        return;
                    }

                    // 万能验证码：开启后，只要输入匹配的万能验证码即可直接跳过短信验证码校验
                    boolean masterCodePassed = false;
                    String masterCodeSwitch = managerConfigService.getString(Config.TEL_LOGIN_MASTER_SWITCH);
                    String masterCode = managerConfigService.getString(Config.TEL_LOGIN_MASTER_CODE);
                    if ("1".equals(masterCodeSwitch)
                            && masterCode != null
                            && !"".equals(masterCode.trim())
                            && masterCode.trim().equals(code)) {
                        masterCodePassed = true;
                    }

                    if (!masterCodePassed) {
                        String telMessageCode = userCacheService.getTelMessageCode(tel);
                        if (telMessageCode != null
                                && telMessageCode.length() >= 2
                                && telMessageCode.startsWith("\"")
                                && telMessageCode.endsWith("\"")) {
                            telMessageCode = telMessageCode.substring(1, telMessageCode.length() - 1);
                        }

                        if (telMessageCode == null || !telMessageCode.equals(code)) {
                            Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, "请输入正确的验证码").toJSONString());
                            return;
                        }
                    }

                    JSONObject jsonObject = loginService.loginOrRegisterByTel(tel, clientIp, versionId, oldWsid, inviteCode, os);
                    if (jsonObject.containsKey("error") && jsonObject.getInteger("error") == 1) {
                        Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, jsonObject.getString("msg")).toJSONString());
                    } else {
                        Response.doResponse(asyncContext, jsonObject.toJSONString());
                    }
                } catch (AppException e) {
                    logger().warn("执行异常：" + e);
                    Response.doResponse(asyncContext, e.getMessage());
                } catch (Exception e) {
                    logger().error("未知异常", e);
                    Response.doResponse(asyncContext, JSONUtil.getReturnDate(0, null, "网络异常，连接服务器失败").toJSONString());
                }
            }
        };
    }


}
