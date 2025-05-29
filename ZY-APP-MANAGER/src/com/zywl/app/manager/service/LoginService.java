package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.UserConfigContext;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import com.zywl.app.manager.service.manager.ManagerPromoteService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginService extends BaseService {

    public static final String secret = "315077466fa7acb5794c02a9dc662700";


    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;


    @Autowired
    private UserConfigService userConfigService;

    @Autowired
    private ManagerUserService managerUserService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private AppConfigCacheService appConfigCacheService;

    @Autowired
    private UserMailService userMailService;



    @Autowired
    private IPService ipService;

    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private ManagerPromoteService managerPromoteService;


    @Autowired
    private UserInviteInfoService userInviteInfoService;

    @Autowired
    private DeviceRiskService deviceRiskService;

    @Autowired
    private PlayGameService gameService;
    public static Set<String> userNos = new ConcurrentHashSet<>();

    public static Map<String, User> onlineUsers = new ConcurrentHashMap<>();


    private static final List<String> MODELS = new ArrayList<>();

    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCPlaG50AVwC/yXOHK7ssP2KMV3TEoVFiH7xkKRyKUkPBzcL7LfbnrospRaigAVUOIoBqhC0uMKzS60Ji3EYxtsVu8Z9Qakslr7E6TqVQ9EOdPfSJCxokpASrdM/1yl9SqECkP+I9GvuVD5mU3cjdVMwUMvomHoHL3DgByHieWEqibtNijSXbeO6XEJzXd7k67y5ecjqXzgpYWXXsZOOjsRih2eEyytWSyqlbi4/f250PckMaHtvIjW4ASebGKncipnk3hN3u/R6eWq0x4CgvjsMMWbjLMw3av9aLV2Bt7Xkpd5n1xi4yEwcwN9T7ihI3Y58lsMwkW/E+dCoQWYv8oLAgMBAAECggEAKvRiu4nl9o0/daXnfQuP4FZ2LKhgCUrjw8SeKarS7LInGCAU7Q7KKk8yXpumRro5ziufrs4UKikT7cT2MChODe07/pH0+NR6r15DGe90b761CblVwC6C9BTmHVzPxL5Bh9riWGcy1dUkymb4iiDMTPgMN3XmwF/IzXHIFyxDw5oFe4ototWRdJq2PtDMiRBo6lyr/4PwBVTV+KTTAVejw6Ft02zYr9/t9be+d78Xh3LawI1SFE4w9U9uBdSVQv7dmF3tIiCGw0U7Cnu6fNVM1HhoIDDixWpFjmPXQuzCMO0q50lvis9DuyEYLMojPI0i0n5ACDTBAEoeGkK4qU994QKBgQD07VpPZoq/D46rpIxNzH2JwqJmy5DOEke8lUksSVa0KKm8ybnb8H1DCRo9klTHtKWZWX4cIX8MoLz+tMS1hO554G23uImcCSmwFgmsXlzSj0/jHZFVyf5NOML7l8eIYi/7iO8njXL2UupBTPHLluo/a4UVAU5PsyhYLAc17h4+KQKBgQCWE2WWc1RP0mwm9x3Clfppd76MRqcd/CJA2qrS/6hPoojgNGOaXv7hzRI8xS502DH/L9dyqSo468itAK4WEXXDScI144ZBEBHJazqPHfnJKc79EZ6sDf3OlORTssruWCU3XhPLzFb8bcDp6RiyOJ+/Y8mbguqVVMPOwBlXl4NlEwKBgQC4+H/ZsyFZhZBDxHNJVgQBBALOCzKCzn9qxnuKfKCEUqlNsDMzDP4soDU3BsoMQDtIArQg3pMqoEHbQf3E8G2BkaKKu00BkFHxb9NCX8lOI3k7llrqJTBudU2b4FaKg0yldBbZEhQePyQ2yLta+9BQsQzCfkf8HNt9K1MOwZQJcQKBgBWHGMZ5Krn8jEkWn60/CFnCtJG4vNY/ScaV13VG+STbQtkuiq8lO1i2qwwOmPhn3twlR7mJ7KWXpQS0GUTPIl5uIS7LwYFpxbNn71GCUkd5+Ngyg9lYdHUCxLIA7r075bLIivxsBnpVYBvttP4zwy6YKN5m7DGZpDDvO3NmJ5IDAoGAGzRupqjYUvo25seXwl2Fy9B3mV38pccSwr6VJPjNNEGLxqnFJIx7j4KVyvOCh2915elI/T4QsqplaXhmek7Y75HxPz2qvTVPIfizvUsu2RmL5kbjlUU6WPHy7KPsSZNIzpzagjMxxBFfydIuQMNS/UaqJlF3tNclA3RQofomrjU=";

    @PostConstruct
    public void INIT_MP() {
        logger.info("开始加载数美防控策略");
        List<DeviceRisk> byStatus = deviceRiskService.findByStatus(1);
        byStatus.forEach(e -> MODELS.add(e.getModels()));
        logger.info("加载数美防控策略完成，共加载:" + MODELS.size() + "条");
        logger.info("加载的防控策略：" + MODELS);
        Set<String> allUserNo = userService.findAllUserNo();
        userNos.addAll(allUserNo);
    }

    public List<String> getShuMeiModels() {
        return MODELS;
    }

    public void addShuMeiModel(String model) {
        if (MODELS.contains(model)) {
            return;
        }
        MODELS.add(model);
    }

    public void removeShuMeiModel(String model) {
        if (MODELS.contains(model)) {
            MODELS.remove(model);
        }
    }


    public void checkParent(String inviteCode, User user, Long uid) {
        if (StringUtils.isNotEmpty(inviteCode) && user.getParentId() == null) {
            //邀请码不为空并且上级ID为null 绑定上级
            User parent = userService.findUserByInviteCode(inviteCode);
            if (parent == null && user.getParentId().toString().equals(parent.getId().toString())) {
                return;
            }
            if (user.getAuthentication() == 1) {
                //用户实名了 绑上级 增加有效好友数
            }
            userService.addParentIdAndGrandfaId(uid, parent.getId(), parent.getParentId(), parent.getChannelNo());
        }
    }

    public void checkUserCno(User user) {
        if (user.getParentId() == null) {
            return;
        }
        if (user.getCno() != null) {
            return;
        }
        User parent = userCacheService.getUserInfoById(user.getParentId());
        if (parent == null) {
            return;
        }
        if (parent.getCno() != null) {
            userService.updateUserCNo(parent.getCno(), user.getId());
        }
    }

    public void checkChannelNo(User user) {
        if (user.getIsChannel() == 1 && user.getUserNo().equals(user.getChannelNo())) {
            return;
        }
        if (user.getIsChannel() == 1 && !user.getUserNo().equals(user.getChannelNo()) && user.getUserNo().length() == 8) {
            //更改渠道号为自己的userNo
            userService.updateUserChannelNo(user.getUserNo(), user.getId());
        }
        if (user.getParentId() == null) {
            return;
        }
        User parent = userCacheService.getUserInfoById(user.getParentId());
        if (parent == null) {
            return;
        }
        if (user.getChannelNo() == null && parent.getChannelNo() != null) {
            //更改渠道号为上级的渠道号
            userService.updateUserChannelNo(parent.getChannelNo(), user.getId());
            User channelUser = gameService.getUserByChannelNo(parent.getChannelNo());
            if (channelUser != null) {
                userCacheService.addTodayChannelAddUserNum(channelUser.getId());
            }
        }
        if (parent.getChannelNo() != null && parent.getIsChannel() == 1 && user.getChannelNo() != null && !user.getChannelNo().equals(parent.getChannelNo())) {
            userService.updateUserChannelNo(parent.getChannelNo(), user.getId());
        }
    }

    @Transactional
    public JSONObject loginOrRegister(String wxOpenId, String clientIp, String versionId, String oldWsid, String inviteCode, JSONObject wxInfo, int accessTokenVail, String deviceId, String os) {
        String baiIp = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_BAI_IP, Config.BAI_IP);
        /*if (!clientIp.equals(baiIp)) {
            if (wxInfo == null || !wxInfo.containsKey("unionid") || wxInfo.getString("unionid") == null) {
                return JSONUtil.getReturnDate(0, null, "登录失败!");
            }
        }*/
        Map<String, String> map = new HashedMap<>();
        map.put("openId", wxOpenId);
        User user = userService.findByOpenId(wxOpenId);
        Long uid;

        if (user != null) {
            if (user.getStatus() == 0) {
                return JSONUtil.getReturnDate(0, null, "账号已注销！");
            }
            if (user.getStatus() == 2) {
                return JSONUtil.getReturnDate(0, null, "账号被封禁！");
            }
            String password = wxInfo.getString("password");
            if (!user.getUnionId().equals(password)){
                return JSONUtil.getReturnDate(0, null, "密码错误！");
            }
            //包含用户  直接登录  更新用户信息
            uid = user.getId();
           /* if (ipService.isInternetAddress(clientIp)) {
                String s = checkLogin(uid.toString(), "login", clientIp, deviceId, "signupPlatform");
                switch (s) {
                    case "0":
                        return JSONUtil.getReturnDate(0, null, "errorcode：002。");
                    case "-1":
                        return JSONUtil.getReturnDate(0, null, "当前设备存在安全风险，请更换设备后重试。");
                    case "2":
                        if (user.getRisk() != 1) {
                            user.setRisk(1);
                            userService.updateUserRisk(1, user.getId());
                        }
                        break;
                }
            }*/
            if (!onlineUsers.containsKey(user.getId().toString())) {
                onlineUsers.put(user.getId().toString(), user);
            }
            checkUserCno(user);
            checkChannelNo(user);
            WsidBean wsid;
            wsid = authService.createWsid(user.getId(), oldWsid, versionId);
            // 已有用户，登录
            JSONObject result = new JSONObject();
            user.setGameToken(generateToken());
            result.put("userInfo", user);
            result.put("wsInfo", wsid);
            JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
            //修改用户登录时间登录ip登录次数
            if (user.getLastLoginTime().getTime() < DateUtil.getToDayBegin()) {
                userCacheService.addTodayLogin();
                managerUserService.pushAddUser();
            }
            userService.loginSuccess(uid, clientIp, wxInfo == null ? null : (String) wxInfo.getOrDefault("nickname", null), wxInfo == null ? null : (String) wxInfo.getOrDefault("headimgurl", null), user.getGameToken(), DateUtil.getDateByDay(7));
            if (user.getVipExpireTime() != null && System.currentTimeMillis() > user.getVipExpireTime().getTime()) {
                userService.removeUserWeek(user.getId());
            }
            if (user.getVip2ExpireTime() != null && System.currentTimeMillis() > user.getVip2ExpireTime().getTime()) {
                userService.removeUserMonth(user.getId());
            }
            return result2;
        } else {
            //注册用户
            return register(clientIp, oldWsid, versionId, wxOpenId, inviteCode, wxInfo, accessTokenVail, deviceId, os);
        }

    }


    @Transactional
    public JSONObject loginByGameToken(String gameToken, String oldWsid, String versionId, String clientIp) {
        User user = userService.findByUserGameToken(gameToken);
        if (user == null || user.getTokenTime().getTime() < System.currentTimeMillis()) {
            throwExp("支付宝未授权或授权过期，请重新登录");
        }
        if (user.getStatus() == 0) {
            return JSONUtil.getReturnDate(0, null, "账号已注销！");
        }
        if (user.getStatus() == 2) {
            return JSONUtil.getReturnDate(0, null, "账号被封禁！");
        }
        if (!onlineUsers.containsKey(user.getId().toString())) {
            onlineUsers.put(user.getId().toString(), user);
        }
        checkUserCno(user);
        checkChannelNo(user);
        WsidBean wsid;
        wsid = authService.createWsid(user.getId(), oldWsid, versionId);
        // 已有用户，登录
        JSONObject result = new JSONObject();
        result.put("userInfo", user);
        result.put("wsInfo", wsid);
        JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
        //修改用户登录时间登录ip登录次数
        if (user.getLastLoginTime().getTime() < DateUtil.getToDayBegin()) {
            userCacheService.addTodayLogin();
            managerUserService.pushAddUser();
        }
        userService.loginSuccess(user.getId(), clientIp, null, null, user.getGameToken(), user.getTokenTime());
        return result2;
    }


    @Transactional
    public JSONObject loginOrRegisterTabtab(String tabtabId, String clientIp, String versionId, String oldWsid, String inviteCode, String userName, String userHead) {
        User user = userService.findUserByTabtabId(tabtabId);
        Long uid;

        if (user != null) {
            if (user.getStatus() == 0) {
                return JSONUtil.getReturnDate(0, null, "账号已注销！");
            }
            if (user.getStatus() == 2) {
                return JSONUtil.getReturnDate(0, null, "账号被封禁！");
            }
            if (!onlineUsers.containsKey(user.getId().toString())) {
                onlineUsers.put(user.getId().toString(), user);
            }
            //包含用户  直接登录  更新用户信息
            uid = user.getId();
            //checkParent(inviteCode, user, uid);
            checkChannelNo(user);
            WsidBean wsid;
            wsid = authService.createWsid(user.getId(), oldWsid, versionId);
            // 已有用户，登录
            JSONObject result = new JSONObject();
            result.put("userInfo", user);
            result.put("wsInfo", wsid);
            JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
            //修改用户登录时间登录ip登录次数
            if (user.getLastLoginTime().getTime() < DateUtil.getToDayBegin()) {
                userCacheService.addTodayLogin();
                managerUserService.pushAddUser();
            }
            userService.loginSuccess(uid, clientIp, user.getNameStatus() == 1 ? userName : null, userHead, user.getGameToken(), user.getTokenTime());

            return result2;
        } else {
            //注册用户
            return registerByTabtabId(clientIp, oldWsid, versionId, inviteCode, tabtabId, userName, userHead);

        }

    }

    @Transactional
    public JSONObject loginOrRegisterAlipay(String authCode, String clientIp, String versionId, String oldWsid, String inviteCode) throws AlipayApiException {
        AlipayClient alipayClient = AliPayCashService.alipayClient;
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(authCode);
        AlipaySystemOauthTokenResponse returnInfo = alipayClient.certificateExecute(request);
        logger.info(returnInfo);
        if (!returnInfo.isSuccess()) {
            return JSONUtil.getReturnDate(0, null, "获取支付宝用户信息失败，请稍后重试！");
        }
        String aliPayUserId = returnInfo.getOpenId();
        ;
        List<User> byAliUserId = userService.findByAliUserId(aliPayUserId);
        if (byAliUserId.size() >= managerConfigService.getInteger(Config.ALIPAY_MAX_NUMBER)) {
            return JSONUtil.getReturnDate(0, null, "该支付宝已绑定其他账号！");
        }
        String accessToken = returnInfo.getAccessToken();
        AlipayUserInfoShareRequest userInfoRequest = new AlipayUserInfoShareRequest();
        AlipayUserInfoShareResponse response = alipayClient.certificateExecute(userInfoRequest, accessToken);
        if (!response.isSuccess()) {
            return JSONUtil.getReturnDate(0, null, "获取支付宝用户信息失败，请稍后重试！");
        }
        String headImgUrl = response.getAvatar();
        String name = response.getNickName();
        if (name == null) {
            name = "alipayUser" + authCode.substring(0, 6);
        }
        if (byAliUserId.size() == 0) {
            return registerByAlipay(clientIp, oldWsid, versionId, inviteCode, aliPayUserId, name, headImgUrl);
        } else {
            User user = byAliUserId.get(0);
            Long uid;
            if (user.getStatus() == 0) {
                return JSONUtil.getReturnDate(0, null, "账号已注销！");
            }
            if (user.getStatus() == 2) {
                return JSONUtil.getReturnDate(0, null, "账号被封禁！");
            }
            if (!onlineUsers.containsKey(user.getId().toString())) {
                onlineUsers.put(user.getId().toString(), user);
            }
            //包含用户  直接登录  更新用户信息
            uid = user.getId();
            String gameToken = generateToken();
            user.setGameToken(gameToken);
            //checkParent(inviteCode, user, uid);
            checkUserCno(user);
            checkChannelNo(user);
            WsidBean wsid;
            wsid = authService.createWsid(user.getId(), oldWsid, versionId);
            // 已有用户，登录
            JSONObject result = new JSONObject();
            result.put("userInfo", user);
            result.put("wsInfo", wsid);
            JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
            //修改用户登录时间登录ip登录次数
            if (user.getLastLoginTime().getTime() < DateUtil.getToDayBegin()) {
                userCacheService.addTodayLogin();
                managerUserService.pushAddUser();
            }
            userService.loginSuccess(uid, clientIp, user.getNameStatus() == 1 ? name : null, headImgUrl, user.getGameToken(), DateUtil.getDateByDay(7));
            return result2;
        }
    }

    @Transactional
    public JSONObject registerByAlipay(String clientIp, String oldWisid, String versionId, String inviteCode, String alipayUserId, String userName, String userHead) {
        logger.info("新用户注册，注册使用邀请码：" + inviteCode);
        User parentUser = userService.findUserByInviteCode(inviteCode);
        String cno = null;
        if (parentUser == null) {
            inviteCode = null;
        } else {
            cno = parentUser.getCno();
        }
        String gameToken = generateToken();
        User newPlayer = userService.insertUserInfoByAlipay(clientIp, alipayUserId, inviteCode, getNo(), userName, userHead, gameToken, cno,ipService.getCityName(clientIp),ipService.getRegionName(clientIp));
        initUserInfo(newPlayer.getId());
        // 创建ws 返回用户用以创建握手连接

        WsidBean wsid = authService.createWsid(newPlayer.getId(), oldWisid, versionId);
        JSONObject result = new JSONObject();
        result.put("userInfo", newPlayer);
        result.put("wsInfo", wsid);
        JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
        userCacheService.addTodayRegister();
        managerUserService.pushAddUser();
        //上级增加下级人数
        if (parentUser == null) {
            managerPromoteService.parentAddFriendNumber(newPlayer.getId());
        }
        logger.info("支付宝注册返回：" + result2);
        return result2;
    }

    @Transactional
    public JSONObject registerByTabtabId(String clientIp, String oldWisid, String versionId, String inviteCode, String tabtabId, String userName, String userHead) {
        logger.info("新用户注册，注册使用邀请码：" + inviteCode);
        User parentUser = userService.findUserByInviteCode(inviteCode);
        if (parentUser == null) {
            inviteCode = null;
        }
        String gameToken = generateToken();
        User newPlayer = userService.insertUserInfoByTabtabId(clientIp, tabtabId, inviteCode, getNo(), userName, userHead, gameToken);
        initUserInfo(newPlayer.getId());
        // 创建ws 返回用户用以创建握手连接
        WsidBean wsid = authService.createWsid(newPlayer.getId(), oldWisid, versionId);
        JSONObject result = new JSONObject();
        result.put("userInfo", newPlayer);
        result.put("wsInfo", wsid);
        JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
        userCacheService.addTodayRegister();
        managerUserService.pushAddUser();
        //上级增加下级人数
        if (parentUser == null) {
        }

        return result2;
    }

    public JSONObject emailLogin(String mail, String password, String versionId, String ip) {
        User user = userService.findUserByMail(mail);
        JSONObject result2;
        if (user != null && user.getPassword().equals(MD5Util.md5(password))) {
            //包含用户  直接登录
            WsidBean wsid;
            wsid = authService.createWsid(user.getId(), null, versionId);
            // 已有用户，登录
            JSONObject result = new JSONObject();
            result.put("userInfo", user);
            result.put("wsInfo", wsid == null ? new JSONObject() : wsid);
            result2 = JSONUtil.getReturnDate(1, result, "success!");
            //修改用户登录时间登录ip登录次数
            userService.loginSuccess(user.getId(), ip, null, null, user.getGameToken(), DateUtil.getDateByDay(7));

        } else {
            result2 = JSONUtil.getReturnDate(0, null, "success!");
        }
        return result2;
    }

    @Transactional
    public void emailUpdatePassword(String email, String newPassword) {
        userService.updatePasswordByEmail(email, newPassword);
    }

    @Transactional
    public JSONObject emailRegister(String email, String password, String ip, String inviteCode) {
        User parentUser = userService.findUserByInviteCode(inviteCode);
        if (parentUser == null) {
            return JSONUtil.getReturnDate(0, null, "Invalid invitation code");
        }
        User newPlayer = userService.insertUserInfoByEmail(ip, email, password, inviteCode);
        initUserInfo(newPlayer.getId());
        // 创建ws 返回用户用以创建握手连接
        //WsidBean wsid = authService.createWsid(newPlayer.getId(), null, versionId);
        JSONObject result = new JSONObject();
        result.put("userInfo", newPlayer);
        //result.put("wsInfo", wsid);
        return JSONUtil.getReturnDate(1, result, "");

    }


    public String getNo() {
        //TODO  递归优化
        String userNo = RandomStringUtils.randomNumeric(8);
        User byUserNo = userService.findByUserNo(userNo);
        if (byUserNo != null) {
            return getNo();
        }
        userNos.add(userNo);
        return userNo;
    }

    public void pushLoginLog(JSONObject result, String userId, String eventId, String ip, String deviceId, String type) {
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("type", eventId);
        pushDate.put("requestId", result.getString("requestId"));
        pushDate.put("deviceId", deviceId);
        pushDate.put("code", result.getString("code"));
        pushDate.put("riskLevel", result.getString("riskLevel"));
        pushDate.put("detail", result.getString("detail"));
        JSONObject detail = JSONObject.parseObject(result.getString("detail"));
        pushDate.put("model", detail.getString("model"));
        pushDate.put("ip", ip);
        pushDate.put("ipCountry", detail.getString("ip_country"));
        pushDate.put("ipProvince", detail.getString("ip_province"));
        pushDate.put("ipCity", detail.getString("ip_city"));
        pushDate.put("message", result.getString("message"));
        pushDate.put("logType", 3);
        Push.push(PushCode.insertLog, null, pushDate);
    }

    public String checkLogin(String userId, String eventId, String ip, String deviceId, String type) {
        String str = ShuMeiUtil.requestShuMei(userId, eventId, ip, deviceId, type);
        if (str == null) {
            return "0";
        }
        JSONObject result = JSONObject.parseObject(str);
        if (!result.getString("code").equals("1100")) {
            return "0";
        }
        pushLoginLog(result, userId, eventId, ip, deviceId, type);
        if ("PASS".equals(result.getString("riskLevel"))) {
            return "1";
        } else {
            //拒绝  判断风控策略
            JSONObject detail = JSONObject.parseObject(result.getString("detail"));
            /*if (!MODELS.contains(detail.getString("model"))) {
                //不包含 可以放 但是不让看广告
                return "2";
            } else {
                //包含  拒绝登录
                return "-1";
            }*/
            try {
                if (detail.containsKey("hits")) {
                    JSONArray hits = detail.getJSONArray("hits");
                    List<String> models = new ArrayList<>();
                    for (Object hit : hits) {
                        JSONObject hitObj = (JSONObject) hit;
                        if (models.contains("model")) {
                            models.add(hitObj.getString("model"));
                        }
                    }
                    boolean b = true;
                    for (String model : models) {
                        if (MODELS.contains(detail.getString("model"))) {
                            b = false;
                        }
                    }
                    if (b) {
                        //不包含风控策略  放过
                        return "2";
                    } else {
                        //包含  拒绝登录
                        return "-1";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.info("数美验证异常：" + e);
                return "2";
            }
        }
        return "2";
    }


    @Transactional
    public JSONObject register(String clientIp, String oldWisid, String versionId, String wxOpenId, String inviteCode, JSONObject wxInfo, int accessTokenVail, String deviceId, String os) {
        logger.info("新用户注册，注册使用邀请码：" + inviteCode);
        /*if (wxInfo==null || !wxInfo.containsKey("unionid") || wxInfo.getString("unionid")==null || !wxInfo.containsKey("nickname") || wxInfo.get("nickname")==null){
            return JSONUtil.getReturnDate(0, null, "errorCode:A02080102");
        }*/
        String configByKey = appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_REGISTER_NUMBER, Config.REGISTER_NUM);
       /* if (accessTokenVail==0){
            return JSONUtil.getReturnDate(0,null,"微信验证失败");
        }*/
        long number = Long.parseLong(configByKey);
        if (number != -1) {
            long todayRegister = userCacheService.getTodayRegister();
            if (todayRegister > number) {
                return JSONUtil.getReturnDate(0, null, "内测名额已发放完毕，后续更多名额请留意官方公告。");
            }
        }
        User parentUser = userService.findUserByInviteCode(inviteCode);
        String cno = null;
        if (parentUser == null) {
            inviteCode = null;
        } else {
            cno = parentUser.getCno();
        }
        String gameToken = generateToken();
        User newPlayer = userService.insertUserInfo(clientIp, wxOpenId, inviteCode, getNo(), wxInfo, ipService.getCityName(clientIp), ipService.getRegionName(clientIp), gameToken, cno);
        /*if (ipService.isInternetAddress(clientIp)) {
            String s = checkLogin(newPlayer.getId().toString(), "register", clientIp, deviceId, "signupPlatform");
            switch (s) {
                case "0":
                    return JSONUtil.getReturnDate(0, null, "errorcode：002");
                case "-1":
                    return JSONUtil.getReturnDate(0, null, "当前设备存在安全风险，请更换设备后重试。");
                case "2":
                    newPlayer.setRisk(1);
                    userService.updateUserRisk(1, newPlayer.getId());
                    break;
            }
        }*/
        if ("android".equals(os)) {
            userService.addDeviceCount(1);
        } else if ("ios".equals(os)) {
            userService.addDeviceCount(2);
        }
        initUserInfo(newPlayer.getId());
        // 创建ws 返回用户用以创建握手连接
        WsidBean wsid = authService.createWsid(newPlayer.getId(), oldWisid, versionId);
        JSONObject result = new JSONObject();
        result.put("userInfo", newPlayer);
        result.put("wsInfo", wsid);
        JSONObject result2 = JSONUtil.getReturnDate(1, result, "");
        userCacheService.addTodayRegister();
        managerUserService.pushAddUser();
        //上级增加下级人数
        if (parentUser == null) {
            managerPromoteService.parentAddFriendNumber(newPlayer.getId());
        }
        logger.info("账号注册返回：" + result2);
        return result2;
    }

    // 初始化用户信息
    @Transactional
    public void initUserInfo(Long userId) {
        // 初始化用户配置信息 资产信息 角色信息
        UserConfig userConfig = new UserConfig();
        userConfig.setUserId(userId);
        userConfig.setAudioSetting(UserConfigContext.USER_CONFIG_ADDIO_SETTING);
        userConfig.setMusicSetting(UserConfigContext.USER_CONFIG_MUSIC_SETTING);
        userConfig.setBackpackVol(UserConfigContext.USER_CONFIG_BACKPACK_VOL);
        userConfigService.addUserConfig(userConfig);
        for (UserCapitalTypeEnum e : UserCapitalTypeEnum.values()) {
            UserCapital userCapital = new UserCapital();
            userCapital.setUserId(userId);
            userCapital.setCapitalType(e.getValue());
            if (e.getValue() == UserCapitalTypeEnum.rmb.getValue()) {
                userCapital.setBalance(new BigDecimal("0.3"));
            } else {
                userCapital.setBalance(BigDecimal.ZERO);
            }

            userCapital.setOccupyBalance(BigDecimal.ZERO);
            userCapitalService.insertUserCapital(userCapital);
        }
        userStatisticService.addUserStatistic(userId);

        userMailService.addUserMail(userId);
        userInviteInfoService.addUserInviteInfo(userId);
    }

    public static String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        // 将字节转换为无符号整数，然后转换为十六进制字符串
        StringBuilder hexBuilder = new StringBuilder();
        for (byte b : bytes) {
            hexBuilder.append(String.format("%02X", b));
        }
        return hexBuilder.toString();
    }


    public boolean isHaveEmail(String mail) {
        User user = userService.findUserByMail(mail);
        return user == null;
    }


}
