package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.DeviceCount;
import com.zywl.app.base.bean.RechargeOrder;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.vo.DSTopVo;
import com.zywl.app.base.bean.vo.TempVo;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.EnNameUtil;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class UserService extends DaoService {


    public UserService() {
        super("UserMapper");
    }

    @Autowired
    private UserCacheService userCacheService;

    @Transactional
    public User insertUserInfo(String clientIp, String wxOpenId, String inviteCode, String userNo, JSONObject wxInfo, String city, String province,String gameToken,String cno) {
        User newPlayer = createUser(clientIp, inviteCode, userNo,gameToken, cno);
        newPlayer.setName(wxInfo.getString("nickname"));
        if (newPlayer.getName()==null){
            newPlayer.setName("账号"+wxOpenId);
        }
        newPlayer.setNameStatus(0);
        newPlayer.setMail(null);
        newPlayer.setOpenId(wxOpenId);
        newPlayer.setRoleId(1);
        newPlayer.setCity(city);
        newPlayer.setRiskPlus(0);
        newPlayer.setProvince(province);
        newPlayer.setIsCash(0);
        newPlayer.setUnionId(wxInfo.getString("unionid"));
        newPlayer.setHeadImageUrl(wxInfo.getString("headimgurl") == null ? "" : wxInfo.getString("headimgurl"));
        newPlayer.setStatus(1);
        newPlayer.setAuthentication(0);
        save(newPlayer);
        return newPlayer;
    }

    @Transactional
    public User insertUserInfoText(Long id,String clientIp, String wxOpenId, String inviteCode, String userNo, JSONObject wxInfo, String city, String province,String gameToken,String cno) {
        User newPlayer = createUser(clientIp, inviteCode, userNo,gameToken, cno);
        newPlayer.setName(wxInfo.getString("nickname"));
        if (newPlayer.getName()==null){
            newPlayer.setName("账号"+wxOpenId);
        }
        newPlayer.setId(id);
        newPlayer.setNameStatus(0);
        newPlayer.setMail(null);
        newPlayer.setOpenId(wxOpenId);
        newPlayer.setRoleId(1);
        newPlayer.setCity(city);
        newPlayer.setRiskPlus(0);
        newPlayer.setProvince(province);
        newPlayer.setIsCash(0);
        newPlayer.setUnionId(wxInfo.getString("unionid"));
        newPlayer.setHeadImageUrl(wxInfo.getString("headimgurl") == null ? "" : wxInfo.getString("headimgurl"));
        newPlayer.setStatus(1);
        newPlayer.setAuthentication(0);
        save(newPlayer);
        return newPlayer;
    }

    private User createUser(String clientIp, String inviteCode, String userNo,String gameToken,String cno) {
        User newPlayer = new User();
        if (inviteCode != null && !inviteCode.equals("")) {
            User parentUser = findUserByInviteCode(inviteCode);
            if (parentUser != null) {
                Long parentId = parentUser.getId();
                newPlayer.setParentId(parentId);
                userCacheService.addSonCount(parentId, 3);
                if (parentUser.getParentId() != null) {
                    newPlayer.setGrandfaId(parentUser.getParentId());
                    userCacheService.addSonCount(parentUser.getParentId(), 3);
                } else {
                    newPlayer.setGrandfaId(null);
                }
            }
        }
        newPlayer.setUserNo(userNo);
        newPlayer.setGroup(1);
        newPlayer.setTokenTime(DateUtil.getDateByDay(7));
        newPlayer.setIsChannel(0);
        newPlayer.setIsCash(0);
        newPlayer.setVip1(1);
        newPlayer.setVip2(0);
        newPlayer.setVipExpireTime(DateUtil.getDateByDay(7));
        newPlayer.setVip2ExpireTime(new Date());
        newPlayer.setFirstCharge(0);
        newPlayer.setInviteCode(newPlayer.getUserNo());
        newPlayer.setLastLoginTime(new Date());
        newPlayer.setRegistTime(new Date());
        newPlayer.setRegistIp(clientIp);
        newPlayer.setLastLoginIp(clientIp);
        newPlayer.setGameToken(gameToken);
        newPlayer.setRisk(0);
        newPlayer.setIsUpdateIdCard(0);
        newPlayer.setCno(cno);
        return newPlayer;
    }



    @Transactional
    public User insertUserInfoByTabtabId(String clientIp, String tabtabId, String inviteCode, String userNo, String userName, String userHead,String gameToken) {
        User newPlayer = createUser(clientIp, inviteCode, userNo,gameToken,"");
        newPlayer.setName(userName);
        newPlayer.setNameStatus(0);
        newPlayer.setMail(null);
        newPlayer.setTabtabId(tabtabId);
        newPlayer.setOpenId(null);
        newPlayer.setRoleId(1);
        newPlayer.setUnionId(null);
        newPlayer.setHeadImageUrl(userHead == null ? "" : userHead);
        newPlayer.setStatus(1);
        newPlayer.setAuthentication(0);
        newPlayer.setVipExpireTime(new Date());
        newPlayer.setVip2ExpireTime(new Date());
        newPlayer.setVip1(0);
        newPlayer.setVip2(0);
        save(newPlayer);
        return newPlayer;
    }
    @Transactional
    public User insertUserInfoByAlipay(String clientIp, String alipayUserId, String inviteCode, String userNo, String userName, String userHead,String gameToken,String cno, String city, String province) {
        User newPlayer = createUser(clientIp, inviteCode, userNo,gameToken, cno);
        newPlayer.setName(userName);
        newPlayer.setNameStatus(0);
        newPlayer.setMail(null);
        newPlayer.setAlipayId(alipayUserId);
        newPlayer.setOpenId(null);
        newPlayer.setRoleId(1);
        newPlayer.setCity(city);
        newPlayer.setProvince(province);
        newPlayer.setUnionId(null);
        newPlayer.setHeadImageUrl(userHead == null ? "" : userHead);
        newPlayer.setStatus(1);
        newPlayer.setAuthentication(0);
        save(newPlayer);
        return newPlayer;
    }

    @Transactional
    public int updateUserRisk(int risk, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("risk", risk);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateUserRisk", params);
    }

    @Transactional
    public int removeParent(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return execute("removeParent", params);
    }

    @Transactional
    public User insertUserInfoByEmail(String clientIp, String email, String password, String inviteCode) {
        User newPlayer = new User();
        Random random = new Random();
        if (inviteCode != null && !inviteCode.equals("")) {
            User parentUser = findUserByInviteCode(inviteCode);
            Long parentId = parentUser == null ? null : parentUser.getId();
            newPlayer.setParentId(parentId);
            newPlayer.setGrandfaId(parentUser == null ? null : parentUser.getParentId());
        }
        newPlayer.setMail(email);
        newPlayer.setPassword(MD5Util.md5(password));
        newPlayer.setUserNo(random.nextInt(10000) + 1 + "");
        newPlayer.setGroup(1);
        newPlayer.setInviteCode(newPlayer.getUserNo());
        newPlayer.setLastLoginTime(new Date());
        newPlayer.setRegistTime(new Date());
        newPlayer.setRegistIp(clientIp);
        newPlayer.setLastLoginIp(clientIp);
        newPlayer.setName(EnNameUtil.namesEn[new Random().nextInt(EnNameUtil.namesEn.length)]);
        newPlayer.setHeadImageUrl(String.valueOf(new Random().nextInt(10) + 1));
        newPlayer.setNameStatus(0);
        newPlayer.setOpenId("");
        newPlayer.setRoleId(1);
        newPlayer.setUnionId("");
        newPlayer.setStatus(1);
        save(newPlayer);
        return newPlayer;
    }

    public List<User> findPlayer(Map<String, String> map) {
        return findByConditions(map);
    }

    public User findByUserNo(String userNo) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userNo", userNo);
        return (User) findOne("findOneByUserNo", params);
    }

    public User findByCno(String cno) {
        Map<String, Object> params = new HashedMap<>();
        params.put("cno", cno);
        return (User) findOne("findOneByCno", params);
    }

    public User findByUserGameToken(String gameToken) {
        Map<String, Object> params = new HashedMap<>();
        params.put("gameToken", gameToken);
        return (User) findOne("findOneByGameToken", params);
    }

    public List<User> findByGZSFK1(){
        return findList("findByGzsfk1",null);
    }

    public List<User> findByGZSFK2(){
        return findList("findByGzsfk2",null);
    }

    public User findById(Long userId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("id", userId);
        return findOne(params);
    }

    public List<Long> findIdByParentId(List<Long> ids){
        Map<String, Object> params = new HashedMap<>();
        params.put("ids",ids);
        return findList("findIdByParentId",params);
    }

    public User findByIdAllStatus(Long userId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("id", userId);
        return (User) findOne("findByIdAllStatus", params);
    }

    public User findByOpenId(String openId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("openId", openId);
        return (User) findOne("findOneByOpenId", params);
    }

    public List<User> findByCacheInfo() {
        return findList("findCacheInfo", null);
    }

    public Long getOneJuniorCount(Long parentId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("parentId", parentId);
        return (Long) findOne("countOneJunior", params);
    }

    public Long getTwoJuniorCount(Long grandfaId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("grandfaId", grandfaId);
        return (Long) findOne("countTwoJunior", params);
    }

    public Long countNoAuthenticationJunior(Long parentId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("parentId", parentId);
        return (Long) findOne("countNoAuthenticationJunior", params);
    }

    public List<User> findUsersByParentId(Long parentId, Integer start, Integer limit, int vip) {
        Map<String, Object> params = new HashedMap<>();
        params.put("parentId", parentId);
        params.put("vip", vip);
        if (start != null && limit != null) {
            params.put("start", start * limit);
            params.put("limit", limit);
        }
        return findList("findByParentId", params);
    }

    public List<User> findUsersByGrandfaId(Long grandfaId, Integer start, Integer limit, int vip) {
        Map<String, Object> params = new HashedMap<>();
        params.put("grandfaId", grandfaId);
        params.put("vip", vip);
        if (start != null && limit != null) {
            params.put("start", start * limit);
            params.put("limit", limit);
        }
        return findList("findByGrandfaId", params);
    }

    public List<User> findMySonNoAuthicatino(Long parentId, Integer start, Integer limit, int vip) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("parentId", parentId);
        if (start != null && limit != null) {
            params.put("start", start * limit);
            params.put("limit", limit);
        }
        return findList("findMySonNoAuthicatino", params);
    }

    @Transactional
    public int subUserPoints(Long userId, BigDecimal value, BigDecimal points) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("points", value);
        params.put("old", points);
        int a = execute("subUserPoints", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    @Transactional
    public int updateUserCourierInfo(Long userId, String courierName, String courierPhone, String courierAddress) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("courierName", courierName);
        params.put("courierPhone", courierPhone);
        params.put("courierAddress", courierAddress);
        int a = execute("updateCourierInfo", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    public User findUserByMail(String mail) {
        Map<String, Object> params = new HashedMap<>();
        params.put("mail", mail);
        return (User) findOne("findUserMail", params);
    }

    @Transactional
    public void addTel(Long userId, String tel) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("phone", tel);
        execute("addTel", params);
        userCacheService.removeUserInfoCache(userId);
    }

    @Transactional
    public int loginSuccess(Long userId, String ip, String name, String headImageUrl,String gameToken,Date tokenTime) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("lastLoginTime", new Date());
        params.put("ip", ip);
        params.put("gameToken",gameToken);
        params.put("tokenTime",tokenTime);
        if (name != null) {
            params.put("name", name);
        }
        if (headImageUrl != null) {
            params.put("headImageUrl", headImageUrl);
        }
        userCacheService.removeUserInfoCache(userId);
        return execute("loginSuccess", params);
    }

    @Transactional
    public int userOffline(Long userId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("lastLeaveTime", new Date());
        userCacheService.removeUserInfoCache(userId);
        return execute("userOffline", params);
    }

    @Transactional
    public int updatePasswordByEmail(String email, String newPassword) {
        Map<String, Object> params = new HashedMap<>();
        params.put("email", email);
        params.put("newPassword", newPassword);
        return execute("updatePasswordByEmail", params);
    }

    public User findUserByInviteCode(String inviteCode) {
        Map<String, Object> params = new HashedMap<>();
        params.put("inviteCode", inviteCode);
        return (User) findOne("findUserByInviteCode", params);
    }

    public User findUserByTabtabId(String tabtabId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("tabtabId", tabtabId);
        return (User) findOne("findUserByTabtabId", params);
    }

    public User findUserByGameToken(String gameToken) {
        Map<String, Object> params = new HashedMap<>();
        params.put("gameToken", gameToken);
        return (User) findOne("findUserByGameToken", params);
    }

    @Transactional
    public int updateOfflineTime(Long userId) {
        Map<String, Object> params = new HashedMap<>();
        params.put("lastLeaveTime", new Date());
        params.put("userId", userId);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateOffLineTime", params);
    }


    public List<User> adminFindUser(int start, int limit, Long userId, String userNo, String begin, String end) {
        Map<String, Object> params = new HashedMap<>();
        params.put("start", (start - 1) * limit);
        params.put("limit", limit);
        params.put("userId", userId);
        params.put("userNo", userNo);
        params.put("registStartDate", begin);
        params.put("registEndDate", end);
        return findByConditions(params);
    }

    public long adminFindCount(Long userId, String userNo, String begin, String end) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("userNo", userNo);
        params.put("registStartDate", begin);
        params.put("registEndDate", end);
        return count("countByConditions", params);
    }

    public long getChannelNum(Long userId, String channelNo) {
        Map<String, Object> params = new HashedMap<>();
        params.put("channelNo", channelNo);
        return count("countByConditions", params);
    }


    @Transactional
    public int authentication(Long userId, String realName, String idCard) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("realName", realName);
        params.put("idCard", idCard);
        userCacheService.removeUserInfoCache(userId);
        return execute("authentication", params);
    }

    @Transactional
    public int authentication2(Long userId, String realName, String idCard) {
        Map<String, Object> params = new HashedMap<>();
        params.put("userId", userId);
        params.put("realName", realName);
        params.put("idCard", idCard);
        userCacheService.removeUserInfoCache(userId);
        return execute("authentication2", params);
    }

    @Transactional
    public int updateAuthentication(Long userId, Integer auth) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        params.put("authentication", auth);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateAuthentication", params);
    }


    @Transactional
    public int userBeChannel(Long userId) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        userCacheService.removeUserInfoCache(userId);
        return execute("userBeChannel", params);
    }

    @Transactional
    public int userDeleteAccount(Long userId) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        userCacheService.removeUserInfoCache(userId);
        return execute("userDeleteAccount", params);
    }


    @Transactional
    public int setQQWX(Long userId, String wechatId, String qq) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        params.put("wechatId", wechatId);
        params.put("qq", qq);
        userCacheService.removeUserInfoCache(userId);
        return execute("setQQWX", params);
    }


    public Set<String> findAllUserNo() {
        List<User> users = findList("findAllUserNo", null);
        Set<String> nos = new HashSet<String>();
        for (User user : users) {
            nos.add(user.getUserNo());
        }
        return nos;
    }

    public List<User> findBot(){
        List<User> users = findList("findAllBotUser", null);
        return users;
    }

    @Transactional
    public int addParentIdAndGrandfaId(Long userId, Long parentId, Long grandfaId, String channelNo) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("parentId", parentId);
        params.put("grandfaId", grandfaId);
        params.put("channelNo", channelNo);
        userCacheService.removeUserInfoCache(userId);
        userCacheService.removeUserInfoCache(parentId);
        userCacheService.removeUserInfoCache(grandfaId);
        return execute("addParentIdAndGrandfaId", params);
    }

    public long findMySonCount(Long userId, int type) {
        Map<String, Object> params = new HashedMap<String, Object>();
        if (type == 1) {
            params.put("parentId", userId);
            return count("countMySonAuthen", params);
        } else if (type == 2) {
            params.put("grandfaId", userId);
            return count("countMySonSonAuthen", params);
        } else {
            params.put("parentId", userId);
            params.put("grandfaId", userId);
            return count("countMySonNoAuthen", params);
        }
    }

    @Transactional
    public int updateUserRoleId(Long userId, int roleId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("roleId", roleId);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateUserRoleId", params);
    }

    public List<User> findTodayRegister() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("time", DateUtil.getToDayDateBegin());
        return findList("findTodayRegister", params);
    }

    public List<User> findTodayLogin() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("time", DateUtil.getToDayDateBegin());
        return findList("findTodayLogin", params);
    }

    public String getKeepAlive(int begin, int end) {
        Map<String, Object> params = new HashMap<>();
        params.put("registStartDate", DateUtil.getDaysAgoBegin(begin));
        params.put("registEndDate", DateUtil.getDaysAgoBegin(end));
        List<User> users = findByConditions(params);
        int count = 0;
        for (User user : users) {
            if (user.getLastLoginTime().getTime() > DateUtil.getOneDaysAgoBegin().getTime()) {
                count++;
            }
        }
        if (users.size() == 0) {
            return "0%";
        }
        return (float) count / users.size() * 100 + "%";
    }

    public List<User> findYesterdayRegUser(){
        Map<String, Object> params = new HashMap<>();
        params.put("registStartDate", DateUtil.getDaysAgoBegin(1));
        params.put("registEndDate", DateUtil.getDaysAgoBegin(0));
        return findByConditions(params);
    }

    @Transactional
    public void openWeek(Long userId,Date expireTime){
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("expireTime", expireTime);
        execute("openWeek",params);
        userCacheService.removeUserInfoCache(userId);

    }

    @Transactional
    public void openMonth(Long userId,Date expireTime){
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("expireTime", expireTime);
        execute("openMonth",params);
        userCacheService.removeUserInfoCache(userId);
    }


    public User findUserByChannelNo(String channelNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("channelNo", channelNo);
        List<User> users = findList("findUserByChannelNo", params);
        if (users.size() > 0) {
            return users.get(0);
        } else {
            return null;
        }

    }


    @Transactional
    public int updateUserChannelNo(String channelNo, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("channelNo", channelNo);
        params.put("userId", userId);
        int a = execute("updateUserChannelNo", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    @Transactional
    public int updateUserCNo(String cno, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cno", cno);
        params.put("userId", userId);
        int a = execute("updateUserCNo", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    @Transactional
    public int updateUserVip2( Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        int a = execute("updateUserVip2", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    @Transactional
    public int updateUserName(String name, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("userId", userId);
        int a = execute("updateUserName", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    @Transactional
    public int updateUserNo(String userNo, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userNo", userNo);
        params.put("userId", userId);
        int a = execute("updateUserNo", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    @Transactional
    public void updateUserToVip(Long userId, int vipLv, Date expireTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("vipLv", vipLv);
        params.put("expireTime", expireTime);
        execute("updateUserToVip", params);
        userCacheService.removeUserInfoCache(userId);
    }

    @Transactional
    public void updateIsCash(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        execute("updateIsCash", params);
        userCacheService.removeUserInfoCache(userId);
    }

    @Transactional
    public int updateUserAddVip(Long userId, Integer vipLv, Date expireTime) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("vipLv", vipLv);
        params.put("expireTime", expireTime);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateUserAddVip", params);
    }



    @Transactional
    public int removeUserWeek(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        int a = execute("removeUserWeek", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }

    @Transactional
    public int removeUserMonth(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        int a = execute("removeUserMonth", params);
        userCacheService.removeUserInfoCache(userId);
        return a;
    }


    public List<User> findOneMonthNoLogin() {
        Map<String, Object> params = new HashMap<>();
        params.put("time", DateUtil.getDateByDay(-30));
        return findList("findOneMonthNoLogin", params);
    }

    @Transactional
    public int updateOneMonthNoLoginInfo() {
        Map<String, Object> params = new HashMap<>();
        params.put("time", DateUtil.getDateByDay(-30));
        return execute("updateOneMonthNoLoginInfo", params);
    }

    @Transactional
    public int updateUserIpCityInfo(Long userId, String country, String province, String city) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("country", country);
        params.put("province", province);
        params.put("city", city);
        return execute("updateUserIpCityInfo", params);

    }

    @Transactional
    public int addDeviceCount(int type) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        return execute("addDeviceCount", params);
    }

    public DeviceCount getDeviceCount() {
        return (DeviceCount) findOne("getDeviceCount", null);
    }

    public int updateStatus(long userId, int status) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("status", status);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateStatus", params);
    }

    public int updateRiskPlus(long userId, int status) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("riskPlus", status);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateUseRiskPlus", params);
    }

    public void batchUpdateUserOfflineTime(List<User> users) {
        if (users != null) {
            List<User> newList = new ArrayList<>();
            for (int i = 0; i < users.size(); i++) {
                newList.add(users.get(i));
                if (i % 5000 == 0) {
                    execute("batchUpdateUser", newList);
                    newList.clear();
                }
            }
            if (!newList.isEmpty()) {
                execute("batchUpdateUser", newList);
            }
        }
    }

    public List<User> findAllUserId() {
        return findList("findAllUserId", null);
    }

    public List<User> findssss() {
        return findList("findxxx", null);
    }

    public List<User> findaa(String userNo) {
        Map<String, Object> map = new HashMap<>();
        map.put("userNo", userNo);
        return findList("findaa", null);
    }

    @Transactional
    public int uuu(String userNo, Long id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("userNo", userNo);
        return execute("uuu", map);
    }

    public List<User> findErrorV1() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", "2024-02-01 02:15:00");
        map.put("b", "2024-02-01 09:30:00");
        return findList("findErrorV1", map);
    }

    public RechargeOrder findErrorOrder(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        return (RechargeOrder) findOne("findRec", map);
    }

    @Transactional
    public int cancelVip(Long id) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        return execute("cancelVip", map);
    }

    @Transactional
    public int updateUserNoPassIdCard(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("risk", 1);
        map.put("riskPlus", 1);
        map.put("userId", userId);
        userCacheService.removeUserInfoCache(userId);
        return execute("updateUserNoPassIdCard", map);
    }

    @Transactional
    public int addAliPayUserId(Long userId,String aliPayUserId){
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("alipayId", aliPayUserId);
        return execute("addAliPayUserId",map);
    }

    public List<User> findByAliUserId(String aliPayUserId){
        Map<String, Object> map = new HashMap<>();
        map.put("alipayId", aliPayUserId);
        return findList("findByAliUserId",map);
    }


    public Long findCountByIp(String ip){
        Map<String, Object> map = new HashMap<>();
        map.put("ip", ip);
        return (Long) findOne("findCountByIp",map);
    }


    public List<TempVo> temp() {
        return findList("temp", null);
    }




    public List<DSTopVo> findTopByDl(){
        return findList("findTopByDl",null);
    }

}
