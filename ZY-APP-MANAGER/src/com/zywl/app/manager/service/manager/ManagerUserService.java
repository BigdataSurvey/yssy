package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.sun.org.apache.bcel.internal.generic.IF_ACMPEQ;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.card.CanLogin;
import com.zywl.app.base.bean.vo.UserSonVo;
import com.zywl.app.base.bean.vo.UserVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.*;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.AnimaTreeFromEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.defaultx.service.card.BuyVipRecordService;
import com.zywl.app.defaultx.service.card.CanLoginService;
import com.zywl.app.manager.bean.IDCardReportDataBean;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.*;
import com.zywl.app.manager.servlet.AppUploadFileServlet;
import com.zywl.app.manager.socket.AdminSocketServer;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@ServiceClass(code = MessageCodeContext.USER_SERVER)
public class ManagerUserService extends BaseService {

    private static final Log logger = LogFactory.getLog(ManagerUserService.class);


    //<userId, User>
    private static final Map<Long, User> USER_ID_CACHE = new ConcurrentHashMap<>();

    //<userShortId, UserId>
    private static final Map<String, String> USER_SHORT_ID_CACHE = new ConcurrentHashMap<>();

    //<userNo, userId>
    private static final Map<Long, Long> USER_NO_CACHE = new ConcurrentHashMap<>();

    //<phone, userId>
    private static final Map<String, String> USER_PHONE_CACHE = new ConcurrentHashMap<>();


    private static final Set<String> DEVICE_KEY_CACHE = new ConcurrentHashSet<>();

    private static final Map<String, Map<String, Long>> WARN_USER_CACHE = new ConcurrentHashMap<>();


    @Autowired
    private UserService userService;

    @Autowired
    private ManagerPromoteService managerPromoteService;

    @Autowired
    private UserCacheService userCacheService;

    private PropertiesUtil newsProperties;


    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private AdminSocketService adminSocketService;

    @Autowired
    private ManagerSocketService managerSocketService;

    @Autowired
    private ApplyForService applyForService;

    @Autowired
    private ManagerGameBaseService managerGameBaseService;


    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private BuyVipRecordService buyVipRecordService;


    @Autowired
    private ManagerCapitalService managerCapitalService;


    @Autowired
    private UserStatisticService userStatisticService;


    @Autowired
    private PlayGameService gameService;

    @Autowired
    private CheckAchievementService checkAchievementService;

    @Autowired
    private CanLoginService canLoginService;

    @Autowired
    private IDCardService idCardService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private UserPowerService userPowerService;

    public static List<User> users = new CopyOnWriteArrayList<>();


    @PostConstruct
    public void _construct() {
        Config config = configService.getConfigByKey(Config.CHANNEL_MAX_NUM);
    }

    public Map<Long, User> getAllUser() {
        return USER_ID_CACHE;
    }

    public void setUserCache(User user) {
    }

    public void removeUserCache(Long userId) {
        User user = getUserCacheById(userId);
        if (user != null) {
            USER_ID_CACHE.remove(userId);
            USER_NO_CACHE.remove(user.getUserNo());
            USER_SHORT_ID_CACHE.remove(null);
            if (user.getPhone() != null) {
                USER_PHONE_CACHE.remove(user.getPhone());
            }
        }
    }

    public void clearUserCache() {
        USER_ID_CACHE.clear();
        USER_PHONE_CACHE.clear();
        USER_NO_CACHE.clear();
        USER_SHORT_ID_CACHE.clear();
    }

    public User getUserCacheById(Long userId) {
        return USER_ID_CACHE.get(userId);
    }

    public User getUserCacheByPhone(String phone) {
        return null;
    }

    public User getUserCacheByShortId(String shortId) {
        return null;
    }

    public boolean containsDeviceKey(String deviceKey) {
        return DEVICE_KEY_CACHE.contains(deviceKey);
    }

    @ServiceMethod(code = "001", description = "更新用户头像")
    public String uploadUserPhoto(ManagerSocketServer managerSocketServer, Command command, JSONObject params) throws IOException {
        checkNull(params);
        checkNull(params.get("userId"), params.get("photo"));
        String userId = params.getString("userId");
        String photo = params.getString("photo");
        if (photo.length() == UID.DEFAULT_LENGTH) {
            String base64 = AppUploadFileServlet.getAndRemoveBase64(photo);
            if (isNull(base64)) {
                throwExp("图片已过期，请重新提交");
            }
            photo = base64;
        }

        String imagePath = newsProperties.get("user.photo.path"); //图片文件夹路径
        String imageWebPath = newsProperties.get("user.photo.webPath"); //图片web访问路径
        byte[] photoByte = Base64Util.base64Str2ByteArray(photo);
        String fileName = userId + ".jpg";
        File photoFile = new File(imagePath + File.separator + fileName);
        FileUtils.writeByteArrayToFile(photoFile, photoByte);

        User user = new User();
        updateUser(user);

        return user.getHeadImageUrl();
    }

    @Transactional
    @ServiceMethod(code = "002", description = "同步用户信息(新增用户)")
    public synchronized User addUser(ManagerSocketServer managerSocketServer, User user) {
        User userCache = getUserCacheByPhone(user.getPhone());
        long userNo = 0;
        int userNoLength = managerConfigService.getInteger(Config.USER_NO_LENGTH);
        long time = System.currentTimeMillis();
        do {
            if (System.currentTimeMillis() - time > 8000) {
                throwExp("用户ID长度不足");
            }
            userNo = RandomNumberUtils.create(userNoLength);
        } while (USER_NO_CACHE.containsKey(userNo) || RegexUtil.isLiangHao(userNo + ""));

        return user;
    }

    @Transactional
    @ServiceMethod(code = "003", description = "同步用户信息(修改用户)")
    public User updateUser(ManagerSocketServer managerSocketServer, User user) {
        return updateUser(user);
    }

    @Transactional
    public User updateUser(User user) {
        pushUpdateUser(user);
        return user;
    }

    private void requestUserLocationInfo(User user) {
    }

    @ServiceMethod(code = "004", description = "获取玩家列表")
    public JSONObject adminGetUserList(AdminSocketServer adminSocketServer, JSONObject params) {
        int start = params.getIntValue("page");
        int limit = params.getIntValue("limit");
        Long userId = params.getLong("userId");
        String userNo = params.getString("userNo");
        String startDate = params.getString("startDate");
        String endDate = params.getString("endDate");
        Integer onLine = params.getInteger("online");
        List<User> list = null;
        JSONObject result = new JSONObject();
        JSONArray array = new JSONArray();
        long count = 0L;
        if (onLine != null) {
            if (onLine == 1) {
                list.addAll(LoginService.onlineUsers.values());
            } else {
                list = userService.adminFindUser(start, limit, userId, userNo, startDate, endDate);
                count = userService.adminFindCount(userId, userNo, startDate, endDate);
                for (User user : list) {
                    JSONObject obj = (JSONObject) JSON.toJSON(user);
                    if (managerSocketService.getUserOnlineInfo(user.getId().toString()) != null) {
                        continue;
                    }
                    obj.put("online", managerSocketService.getUserOnlineInfo(user.getId().toString()) != null);
                    obj.remove("password");
                    array.add(obj);
                }
            }
        } else {
            list = userService.adminFindUser(start, limit, userId, userNo, startDate, endDate);
            count = userService.adminFindCount(userId, userNo, startDate, endDate);
            for (User user : list) {
                JSONObject obj = (JSONObject) JSON.toJSON(user);
                obj.put("online", managerSocketService.getUserOnlineInfo(user.getId().toString()) != null);
                obj.remove("password");
                array.add(obj);
            }
        }


        result.put("list", array);
        result.put("count", count);
        JSONObject defaultFee = new JSONObject();
        result.put("defaultFee", defaultFee);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "005", description = "管理员修改用户信息")
    public void adminUpdateUser(AdminSocketServer adminSocketServer, User user) {
    }

    @ServiceMethod(code = "006", description = "管理员获取用户预警信息")
    public Map<String, Map<String, Long>> adminGetUserWarn(AdminSocketServer adminSocketServer) {
        Map<String, Map<String, Long>> data = new HashMap<String, Map<String, Long>>();
        WARN_USER_CACHE.forEach((name, userData) -> {
            if (userData.size() > 1) {
                data.put(name, userData);
            }
        });
        return data;
    }


    @ServiceMethod(code = "007", description = "修改收获地址信息")
    public JSONObject updateUserCourier(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("courierName"), data.get("courierPhone"), data.get("courierAddress"));
        long userId = data.getLongValue("userId");
        String name = data.getString("courierName");
        String phone = data.getString("courierPhone");
        String address = data.getString("courierAddress");
        userService.updateUserCourierInfo(userId, name, phone, address);
        return data;
    }


    @Transactional
    @ServiceMethod(code = "008", description = "实名认证")
    public JSONObject authentication(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("realName"));
        Long userId = data.getLong("userId");
        String realName = data.getString("realName");
        User userInfo = userCacheService.getUserInfoById(userId);
        String idCard = data.getString("idCard");
        JSONObject result = new JSONObject();
        final Integer authentication = userInfo.getAuthentication();
        int isRegister = managerConfigService.getInteger(Config.IS_REGISTER);
        if (isRegister == 0) {
            List<CanLogin> allCanLogin = canLoginService.findAllCanLogin();
            List<String> idCards = new ArrayList<>();
            allCanLogin.forEach(e -> idCards.add(e.getIdCard()));
            boolean contain = false;
            for (String card : idCards) {
                String conStr = idCard.substring(Math.max(0, idCard.length() - 6));
                if (card.contains(conStr)) {
                    contain=true;
                }
            }
            if (!contain){
                throwExp("未开放注册权限，请联系官方客服获取测试账号");
            }
        }
        if (!IDCardUtil.check(idCard)) {
            throwExp("身份证格式错误！");
        }
        Map<String,Object> obj = new HashMap<>();
        obj.put("idCard", idCard);
        long count = userService.count("countByConditions", obj);
        if (count >= 3) {
            throwExp("认证数量已达上限！");
        }
        if (authentication == 0) {//未认证
            int status = idCardService.checkIDCard(userInfo.getId().toString(), realName, idCard);
//            int status = 0;
            if (status == 0) {
                JSONArray reward = JSONArray.parseArray(managerConfigService.getString(Config.REAL_NAME_REWARD));
                gameService.addReward(userId, reward, null);
                managerPromoteService.parentAddEffectiveFriendNumber(userId);
                data.put("reward", reward);

                userService.authentication(userId, realName, idCard);
                userService.updateAuthentication(userId, 1);
                if (userInfo.getParentId() != null) {
                    userCacheService.subSonCount(userInfo.getParentId(), 3);
                    userCacheService.addSonCount(userInfo.getParentId(), 1);
                }
                if (userInfo.getGrandfaId() != null) {
                    userCacheService.subSonCount(userInfo.getGrandfaId(), 3);
                    userCacheService.addSonCount(userInfo.getGrandfaId(), 2);
                }
                if (userInfo.getAlipayId() != null) {
                    managerCapitalService.cash(userId, 2, new BigDecimal("0.3"), userInfo.getUserNo(), userInfo.getName(), realName, userInfo.getOpenId());
                }
                return data;
            } else if (status == -1) {
                throwExp("网络拥挤，请稍后重试！");
            } else if (status == -2) {//待查询
                userService.updateAuthentication(userId, 2);
                throwExp("网络拥挤，请稍后重试！");
            } else if (status == -3) {
                throwExp("验证失败！");
            } else if (status == -101) {
                throwExp("身份证格式错误！");
            } else if (status == -102) {
                throwExp("未满18岁，禁止登录！");
            } else {
                throwExp("系统错误！");
            }
        } else if (authentication == 1) {//已认证
            if(userInfo.getIsUpdateIdCard()==1){
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
                if (userCapital.getBalance().compareTo(new BigDecimal("100")) < 0) {
                    throwExp("金币不足");
                }
                userCapitalService.subUserBalanceByUpdateIdCard(userId, new BigDecimal("100"), UserCapitalTypeEnum.currency_2.getValue());
                managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
            }
            int status = idCardService.checkIDCard(userInfo.getId().toString(), realName, idCard);
//             int status=0;
            if (status == 0) {
                userService.authentication2(userId, realName, idCard);
                userService.updateAuthentication(userId, 1);
                return data;
            } else if (status == -1) {
                throwExp("网络拥挤，请稍后重试！");
            } else if (status == -2) {//待查询
                userService.updateAuthentication(userId, 2);
                throwExp("网络拥挤，请稍后重试！");
            } else if (status == -3) {
                throwExp("验证失败！");
            } else if (status == -101) {
                throwExp("身份证格式错误！");
            } else if (status == -102) {
                throwExp("未满18岁，禁止登录！");
            } else {
                throwExp("系统错误！");
            }
        } else if (authentication == 2) {//待确认
            int status = idCardService.queryIDCard(userInfo.getId().toString());
            if (status == 0) {
                userService.authentication(userId, realName, idCard);
                userService.updateAuthentication(userId, 1);
                managerPromoteService.parentAddEffectiveFriendNumber(userId);
                if (userInfo.getParentId() != null) {
                    userCacheService.subSonCount(userInfo.getParentId(), 3);
                    userCacheService.addSonCount(userInfo.getParentId(), 1);
                }
                if (userInfo.getAlipayId() != null) {
                    managerCapitalService.cash(userId, 2, new BigDecimal("0.3"), userInfo.getUserNo(), userInfo.getName(), userInfo.getRealName(), userInfo.getOpenId());
                }
                return data;
            } else if (status != -2) {
                userService.updateAuthentication(userId, 0);
                throwExp("验证失败！");
            }
        } else {
            throwExp("非法参数！");
        }

        return result;
    }


    @ServiceMethod(code = "009", description = "设置社交信息")
    public JSONObject qqwx(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        Long userId = data.getLong("userId");
        String wx = data.getString("wx");
        String qq = data.getString("qq");
        userService.setQQWX(userId, wx, qq);
        return data;
    }

    @ServiceMethod(code = "010", description = "注销")
    public JSONObject delete(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        User user = userCacheService.getUserInfoById(data.getLong("userId"));
        userCacheService.deleteUser(data.getLong("userId"), data.getString("wsId"), user.getUserNo());
        userService.userDeleteAccount(data.getLong("userId"));
        return data;
    }


    @Transactional
    @ServiceMethod(code = "012", description = "绑定邀请码")
    public JSONObject addInviteCode(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        String channelNo = data.containsKey("channelNo") ? data.getString("channelNo") : null;
        Long parentId = data.getLong("parentId");
        Long grandfaId = data.containsKey("grandfaId") ? data.getLong("grandfaId") : null;
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            userService.addParentIdAndGrandfaId(userId, parentId, grandfaId, channelNo);
            if (channelNo != null) {
                User channelUser = gameService.getUserByChannelNo(channelNo);
                if (channelUser != null) {
                    userCacheService.addTodayChannelAddUserNum(channelUser.getId());
                }
            }
            if (managerConfigService.getInteger(Config.GZS_FK) == 1) {
                if (userCacheService.isRisk(parentId)) {
                    //工作室
                    User my = userCacheService.getUserInfoById(userId);
                    userService.updateUserRisk(1, userId);
                    userCacheService.addRiskTime(parentId);
                } else {
                    userCacheService.setUserInviteRisk(parentId);
                }
            }
            User parentUser = userCacheService.getUserInfoById(parentId);
            JSONObject result = new JSONObject();
            User user = userCacheService.getUserInfoById(userId);
            if (user.getAuthentication() == 1) {
                if (parentId != null) {
                    userCacheService.addSonCount(parentId, 1);
                }
                if (grandfaId != null) {
                    userCacheService.addSonCount(grandfaId, 2);
                }

            }
            if (parentUser.getCno() != null) {
                user.setCno(parentUser.getCno());
            }
            userService.updateUserCNo(user.getCno(), userId);
            managerPromoteService.parentAddEffectiveFriendNumber(userId);
            userStatisticService.addOneCount(parentId);
            if (parentUser.getGrandfaId()!=null){
                userStatisticService.addTwoCount(parentUser.getParentId());
            }
            result.put("name", parentUser.getName());
            result.put("headImgUrl", parentUser.getHeadImageUrl());
            result.put("userNo", parentUser.getUserNo());
            result.put("wx", parentUser.getWechatId());
            result.put("qq", parentUser.getQq());
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "013", description = "申请开通渠道")
    public JSONObject bet(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        String userId = data.getString("userId");
        UserStatistic userStatistic = gameService.getUserStatistic(userId);

        List<ApplyFor> allApplyFor = applyForService.findAllApplyFor();
        if (allApplyFor.size() >= managerConfigService.getInteger(Config.CHANNEL_MAX_NUM)) {
            if (userStatistic.getGetAllIncome().compareTo(new BigDecimal("10000")) < 0
                    && userStatistic.getGetAnima2().compareTo(new BigDecimal("15000"))<0) {
                throwExp("广告收益累计10000或友情值累计15000可申请开通");
            }
        }else{
            if (userStatistic.getGetAllIncome().compareTo(new BigDecimal("5000")) < 0
                    && userStatistic.getGetAnima2().compareTo(new BigDecimal("7500"))<0) {
                throwExp("广告收益累计5000或友情值累计7500可申请开通");
            }
        }
        ApplyFor byUserId = applyForService.findByUserId(Long.parseLong(userId));
        if (byUserId != null) {
            if (byUserId.getStatus() == 1) {
                throwExp("您已开通，重新登录查看");
            } else if (byUserId.getStatus() == 2) {
                throwExp("账号存在风险，申请已拒绝，请联系客服");
            } else {
                throwExp("您已提交申请，无需重复提交");
            }
        }
        applyForService.addApplyFor(Long.parseLong(userId), userStatistic.getGetAllIncome(), managerConfigService.getBigDecimal(Config.CHANNEL_FEE));
        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "014", description = "添加手机号")
    public JSONObject addTel(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"), data.get("phone"));
        Long userId = data.getLong("userId");
        String phone = data.getString("phone");
        userService.addTel(userId, phone);
        JSONObject result = new JSONObject();
        result.put("phone", phone);
        return result;
    }

    /**
     * 增加用户在线时长
     *
     * @param userId
     * @param onlineTime 单位秒
     */
    @Transactional
    public void addUserOnlineTime(String userId, long onlineTime) {

    }

    public void pushAddUser() {
        adminSocketService.syncMonitor();
    }


    public void pushUpdateUser(User user) {
        Push.push(PushCode.userUpdate, null, user);
    }


    public void addUserLookAdNumber(Long userId, int adIndex) {
        try {
            User user = userCacheService.getUserInfoById(userId);
            userCacheService.addUserAdvertLookNum(userId, adIndex, user.getParentId(), user.getVip2() > 0);
            if ((userCacheService.isLookAd(userId, adIndex) && user.getVip2() <= 1)) {
                throwExp("未看完广告不能领取奖励~");
            }

            if (user.getAuthentication() == 0) {
                throwExp("未看完广告不能领取奖励!");
            }
            if (user.getVip2() < 1 && user.getAuthentication() == 1 && user.getIsCash() == 1) {
                if ((managerConfigService.getInteger(Config.GZS_FK) == 1 && user.getRisk() == 0) || managerConfigService.getInteger(Config.GZS_FK) == 0) {
                    //非免广告用户计算收益部分
                    checkIncomeByUserId(userId);
                }
            }
            if (user.getParentId() != null && user.getAuthentication() == 1 && user.getIsCash() == 1 && user.getVip2() < 1) {
                managerPromoteService.parentAddAdNumber(userId, user.getParentId());
            }

            JSONObject pushData = new JSONObject();
            pushData.put("adInfo", gameService.getUserAdCountInfo(userId));
            pushData.put("userId", userId);
            String serverIdByUserId = managerSocketService.getServerIdByUserId(userId);
            Push.push(PushCode.updateAdCount, serverIdByUserId, pushData);
            pushAddUser();//同步首页广告观看次数
        }catch (Exception e){
            logger.error("广告异常"+e.getMessage());
            e.printStackTrace();
        }

    }

    public void checkPlayTestIncome(Long userId, BigDecimal income) {
        User user = userCacheService.getUserInfoById(userId);
        JSONObject result = new JSONObject();
        result.put("parentId", user.getParentId());
        result.put("parentIncome", income);
        gameService.addCreateParentIncome(userId.toString(), income, user.getParentId().toString());
        userStatisticService.updateUserCreateIncome(userId, income, BigDecimal.ZERO);
        Push.push(PushCode.addStatement, null, result);
    }

    public void checkIncomeByUserId(Long userId) {
        Long userAdAllLookNum = userCacheService.getUserAdAllLookNum(userId);
        User user = userCacheService.getUserInfoById(userId);
        JSONObject result = new JSONObject();
        result.put("parentId", "0");
        result.put("parentIncome", BigDecimal.ZERO);
        result.put("grandfaId", "0");
        result.put("grandfaIncome", BigDecimal.ZERO);
        long registerDays = (System.currentTimeMillis() - user.getRegistTime().getTime()) / 1000 / 60 / 60 / 24 + 1;
        if (registerDays > 30 || userAdAllLookNum > 30) {
            return;
        }
        BigDecimal income = null;
        BigDecimal grandfaIncome = null;
        if (user.getParentId() != null) {
            income = PlayGameService.parentIncomeMap.get(String.valueOf(registerDays)).getIncomeByNum((Integer.parseInt(userAdAllLookNum.toString())));
            result.put("parentId", user.getParentId());
            User parent = userCacheService.getUserInfoById(user.getParentId());
            if (parent.getIsChannel()==1){
                income = income.multiply(new BigDecimal("1.5"));
            }
            result.put("parentIncome", income);
            gameService.addCreateParentIncome(userId.toString(), income, user.getParentId().toString());
        }
        if (user.getGrandfaId() != null) {
            grandfaIncome = PlayGameService.grandfaIncomeMap.get(String.valueOf(registerDays)).getIncomeByNum((Integer.parseInt(userAdAllLookNum.toString())));
            User grandfa = userCacheService.getUserInfoById(user.getGrandfaId());
            if (grandfa.getIsChannel()==1){
                income = income.multiply(new BigDecimal( "1.5"));
            }
            result.put("grandfaId", user.getGrandfaId());
            result.put("grandfaIncome", grandfaIncome);
            gameService.addCreateGrandfaIncome(userId.toString(), grandfaIncome, user.getGrandfaId().toString());
        }
        if (user.getChannelNo() != null) {
            //gameService.addChannelNoIncome(userId, user.getChannelNo());
        }

        userStatisticService.updateUserCreateIncome(userId, income, grandfaIncome);
        Push.push(PushCode.addStatement, null, result);
    }

    public void updateUserVipLv(Long userId, Long productId) {
        logger.info("更新玩家VIP等级");
        User user = userCacheService.getUserInfoById(userId);
        if (productId == 1L) {
            //周卡
            if (user.getVip1() == 0) {
                userService.openWeek(userId, DateUtil.getDateByDay(PlayGameService.productMap.get(productId.toString()).getDays()));
            }
            if (user.getVip1() == 1) {
                userService.openWeek(userId, DateUtil.getDateByDay(user.getVipExpireTime(), PlayGameService.productMap.get(productId.toString()).getDays()));
            }
        } else if (productId == 2L) {
            //月卡
            if (user.getVip2() == 0) {
                userService.openMonth(userId, DateUtil.getDateByDay(PlayGameService.productMap.get(productId.toString()).getDays()));
            }
            if (user.getVip2() == 1) {
                userService.openMonth(userId, DateUtil.getDateByDay(user.getVip2ExpireTime(), PlayGameService.productMap.get(productId.toString()).getDays()));
            }
        }
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        user = userCacheService.getUserInfoById(userId);
        UserVo vo = new UserVo();
        BeanUtils.copy(user, vo);
        pushDate.put("userInfo", vo);
        logger.info("推送玩家会员数据");
        Push.push(PushCode.updateUserInfo, managerSocketService.getServerIdByUserId(userId), pushDate);
    }

    public void updateUserWeekVipLv(Long userId) {
        logger.info("更新玩家周卡VIP等级");
        User user = userCacheService.getUserInfoById(userId);
        //周卡
        if (user.getVip1() == 0) {
            userService.openWeek(userId, DateUtil.getDateByDay(1));
        }
        if (user.getVip1() == 1) {
            userService.openWeek(userId, DateUtil.getDateByDay(user.getVipExpireTime(), 1));
        }
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        user = userCacheService.getUserInfoById(userId);
        UserVo vo = new UserVo();
        BeanUtils.copy(user, vo);
        pushDate.put("userInfo", vo);
        logger.info("推送玩家会员数据");
        Push.push(PushCode.updateUserInfo, managerSocketService.getServerIdByUserId(userId), pushDate);
    }

    public void online(Long userId) {

    }

    public void offline(User user) {
        users.add(user);
    }

    public void updateUserOfflineTime() {
        userService.batchUpdateUserOfflineTime(users);
        users.clear();
    }


    @Transactional
    @ServiceMethod(code = "015", description = "周卡")
    public JSONObject week(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        checkVipPrice(userId, 1);
        updateUserVipLv(userId, 1L);
        return new JSONObject();
    }

    public void checkVipPrice(Long userId, int vipType) {
        String key;
        if (vipType == 1) {
            key = Config.VIP_WEEK_PRICE;
        } else {
            key = Config.VIP_MONTH_PRICE;
        }
        BigDecimal amount = managerConfigService.getBigDecimal(key);
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, UserCapitalTypeEnum.currency_2.getValue());
        if (userCapital.getBalance().compareTo(amount) < 0) {
            throwExp(UserCapitalTypeEnum.currency_2.getName() + "不足");
        }
        String orderNo = OrderUtil.getOrder5Number();
        Long dataId = buyVipRecordService.addRecord(userId, orderNo, vipType, amount);
        addAnimaToInviter(userId, amount, new BigDecimal("0.1"));
        userCapitalService.subUserBalanceByVip(userId, amount, dataId);
        managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
    }

    public void addAnimaToInviter(Long userId, BigDecimal amount, BigDecimal rate) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        User user = userCacheService.getUserInfoById(userId);
        if (user.getIsCash()==0){
            return;
        }
        if (user.getParentId() != null) {
            BigDecimal parentAnima = amount.multiply(rate);
            User parent = userCacheService.getUserInfoById(user.getParentId());
            if (parent != null) {
                if (parent.getIsChannel() == 1) {
                    parentAnima = parentAnima.multiply(new BigDecimal("1.5"));
                }
                gameService.addParentGetAnima(user.getId(), user.getParentId().toString(), parentAnima);
            }
            if (user.getGrandfaId() != null) {
                User grandfa = userCacheService.getUserInfoById(user.getGrandfaId());
                if (grandfa != null) {
                    BigDecimal grandfaAmount = amount.multiply(new BigDecimal("0.05"));
                    if (grandfa.getIsChannel() == 1) {
                        grandfaAmount = grandfaAmount.multiply(new BigDecimal("1.5"));
                    }
                    gameService.addGrandfaGetAnima(user.getId(), user.getGrandfaId().toString(), grandfaAmount);
                }
            }
        }
    }

    @Transactional
    @ServiceMethod(code = "016", description = "月卡")
    public JSONObject month(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        checkVipPrice(userId, 2);
        updateUserVipLv(userId, 2L);
        JSONObject result = new JSONObject();
        return result;
    }


    @ServiceMethod(code = "020", description = "获取好友列表")
    public Object getSon(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        int vip = params.getIntValue("vip");
        Long userId = params.getLong("userId");
        int type = 1;
        if (params.containsKey("type")) {
            type = params.getIntValue("type");
        }
        List<User> users = null;
        if (type == 1) {
            users = userService.findUsersByParentId(userId, params.getInteger("page"), params.getInteger("num"), vip);
        } else if (type == 2) {
            users = userService.findUsersByGrandfaId(userId, params.getInteger("page"), params.getInteger("num"), vip);
        } else if (type == 3) {
            users = userService.findMySonNoAuthicatino(userId, params.getInteger("page"), params.getInteger("num"), vip);
        }
        long count = userCacheService.getMySonCount(userId, type);
        List<UserSonVo> list = new ArrayList<>();
        for (User user1 : users) {
            UserSonVo userSonVo = new UserSonVo();
            BeanUtils.copy(user1, userSonVo);
            double todayCreateParentIncome = 0.0;
            double todayMyCreateAnima = 0.0;
            if (type==1){
                todayCreateParentIncome= userCacheService.getTodayCreateParentIncome(user1.getId());
                todayMyCreateAnima = userCacheService.getTodayMyCreateAnima(user1.getId());
            }else if(type==2){
                todayCreateParentIncome= userCacheService.getGrandfaTodayIncome(user1.getId());
                todayMyCreateAnima = userCacheService.getTodayMyCreateGrandfaAnima(user1.getId());
            }
            userSonVo.setTodayCreateIncome(Math.round(todayCreateParentIncome * 100.0) / 100.0);
            userSonVo.setTodayCreatAnima(Math.round(todayMyCreateAnima * 100.0) / 100.0);
            userSonVo.setLastLoginTime(user1.getLastLoginTime().getTime());
            list.add(userSonVo);
        }
        long oneJuniorCount = userService.getOneJuniorCount(userId);
        long twoJuniorCount = userService.getTwoJuniorCount(userId);
        long noAuthCount = userService.countNoAuthenticationJunior(userId);
        JSONObject result = new JSONObject();
        result.put("userList", list);
        result.put("count", count < 0 ? 0 : count);
        result.put("oneFriend", oneJuniorCount);
        result.put("twoFriend", twoJuniorCount);
        result.put("threeFriend",noAuthCount);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "021", description = "获取好友信息")
    public JSONObject getFriendInfo(ManagerSocketService managerSocketService, JSONObject params) {
        Long myId = params.getLong("userId");
        User my = userCacheService.getUserInfoById(myId);
        JSONObject result = new JSONObject();
        String userNo = params.getString("userNo");
        if (userNo.equals(my.getUserNo())) {
            throwExp("不能搜索自己");
        }
        User sonUser = userCacheService.getUserInfoByUserNo(userNo);
        if (sonUser == null) {
            throwExp("不存在该好友");
        }
        if (sonUser.getParentId() == null && sonUser.getGrandfaId() == null) {
            throwExp("该玩家不是您的好友");
        }
        if (sonUser.getParentId() != null && !sonUser.getParentId().toString().equals(String.valueOf(myId))) {
            if (sonUser.getGrandfaId() != null && !sonUser.getGrandfaId().toString().equals(String.valueOf(myId))) {
                throwExp("该玩家不是您的好友");
            }
        }
        if (sonUser.getParentId()!=null){
            User parent = userCacheService.getUserInfoById(sonUser.getParentId());
            if (parent!=null){
                result.put("parentUserNo",parent.getUserNo());
            }else{
                result.put("parentUserNo","暂无");
            }

        }
        long userId = sonUser.getId();
        UserStatistic userStatistic = gameService.getUserStatistic(String.valueOf(userId));
        BigDecimal all;
        double today;
        if (sonUser.getParentId().toString().equals(String.valueOf(myId))) {
            all = userStatistic.getCreateIncome();
            today = userCacheService.getTodayCreateParentIncome(userId);
            result.put("todaySw",userCacheService.getUserTodayCreateSw(userId));
            result.put("allSw",userStatistic.getCreateSw());
        } else {
            today = userCacheService.getGrandfaTodayIncome(userId);
            all = userStatistic.getCreateGrandfaIncome();
            result.put("todaySw",BigDecimal.ZERO);
            result.put("allSw",BigDecimal.ZERO);
        }
        result.put("isCash",sonUser.getIsCash());
        result.put("name", sonUser.getName());
        result.put("userNo", sonUser.getUserNo());
        result.put("headImageUrl", sonUser.getHeadImageUrl());
        result.put("wx", sonUser.getWechatId() == null ? "暂未填写" : sonUser.getWechatId());
        result.put("qq", sonUser.getQq() == null ? "暂未填写" : sonUser.getQq());
        result.put("todayIncome", Math.round(today * 100.0) / 100.0);
        result.put("isVip1", sonUser.getVip1());
        result.put("isVip2", sonUser.getVip2());
        result.put("allIncome", all);
        result.put("registTime", sonUser.getRegistTime());
        result.put("loginTime", sonUser.getLastLoginTime());
        return result;
    }


}
