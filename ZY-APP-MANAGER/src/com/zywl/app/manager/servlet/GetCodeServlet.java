package com.zywl.app.manager.servlet;

import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.AsyncServletProcessor;
import com.zywl.app.base.util.JSONUtil;
import com.zywl.app.base.util.Response;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.util.SpringUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 验证码接口
 */
@SuppressWarnings("serial")
@WebServlet(name = "GetCodeServlet", urlPatterns = "/getMessageCode", asyncSupported = true)
public class GetCodeServlet extends BaseServlet {

    private UserCacheService userCacheService;

    @Override
    public void init() throws ServletException {
        super.init();
        userCacheService = SpringUtil.getService(UserCacheService.class);
    }

    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String clientIp)
            throws AppException, Exception {
        return new AsyncServletProcessor(request) {
            @Override
            public void run() {
                String tel = request.getParameter("tel");

                String ipCache = userCacheService.getIpRequestCache(clientIp);
                if (ipCache != null && ipCache.equals("1")) {
                    Response.doResponse(asyncContext,
                            JSONUtil.getReturnDate(0, null, "请求频繁,请稍后再试").toJSONString());
                    return;
                }

                userCacheService.setIpRequestCache(clientIp);

                if (!isChinaPhoto(tel)) {
                    Response.doResponse(asyncContext,
                            JSONUtil.getReturnDate(0, null, "手机号码格式有误").toJSONString());
                    return;
                }

                String code = getCode2(tel);
                userCacheService.setTelMessageCode(tel, code);

                Response.doResponse(asyncContext,
                        JSONUtil.getReturnDate(1, null, "验证码发送成功。三分钟内有效").toJSONString());
            }
        };
    }

    /*
     * 手机号码11位数，匹配格式：
     * 13x、14[5/7/9]、15[0-3/5-9]、16[2/5/6/7]、17x、18x、19x
     */
    private static boolean isChinaPhoto(String str) {
        if (str == null) {
            return false;
        }
        String regExp = "1(3\\d|4[579]|5[0-35-9]|6[2567]|7\\d|8\\d|9\\d)\\d{8}$";
        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public static String getCode2(String tel) {
//        String testUsername = "aa15650986"; // 在短信宝注册的用户名
//        String testPassword = "aa1234";     // 在短信宝注册的密码
        String testUsername = "niutong"; // 在短信宝注册的用户名
        String testPassword = "Niutong123";     // 在短信宝注册的密码
        int mobileCode = (int) ((Math.random() * 9 + 1) * 100000);

        String content = "【马戏团】您的验证码是：" + mobileCode + "。请不要把验证码泄露给其他人。";
        String httpUrl = "http://api.smsbao.com/sms";

        StringBuffer httpArg = new StringBuffer();
        httpArg.append("u=").append(testUsername).append("&");
        httpArg.append("p=").append(md5(testPassword)).append("&");
        httpArg.append("m=").append(tel).append("&");
        httpArg.append("c=").append(encodeUrlString(content, "UTF-8"));

        String result = request(httpUrl, httpArg.toString());
        System.out.println(result);

        return String.valueOf(mobileCode);
    }

    public static String request(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String strRead = reader.readLine();
            if (strRead != null) {
                sbf.append(strRead);
                while ((strRead = reader.readLine()) != null) {
                    sbf.append("\n");
                    sbf.append(strRead);
                }
            }

            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String md5(String plainText) {
        StringBuffer buf = new StringBuffer();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte[] b = md.digest();

            for (byte value : b) {
                int i = value;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return buf.toString();
    }

    public static String encodeUrlString(String str, String charset) {
        if (str == null) {
            return null;
        }
        try {
            return java.net.URLEncoder.encode(str, charset);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}