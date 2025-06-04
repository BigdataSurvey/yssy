package com.zywl.app.base.bean;

import java.net.URLEncoder;

public class WeChatConfig {
    private String appId;
    private String appSecret;
    private String redirectUri;

    // 授权地址
    public String getAuthorizeUrl() {
        return String.format("https://open.weixin.qq.com/connect/qrconnect?" +
                        "appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=STATE#wechat_redirect",
                appId, URLEncoder.encode(redirectUri));
    }

    // 获取access_token地址
    public String getAccessTokenUrl(String code,String appId,String appSecret) {
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
                appId, refreshToken);
    }
}
