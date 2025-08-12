package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.MailGoldTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.MailService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserStatisticService;
import com.zywl.app.defaultx.service.XianWanOrderService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
@WebServlet(name = "PlayGamePlatform2Servlet", urlPatterns = "/playGame2Notify")
public class PlayGamePlatform2Servlet extends BaseServlet {


    private static final Log logger = LogFactory.getLog(PlayGamePlatform2Servlet.class);

    private XianWanOrderService xianWanOrderService;

    private UserCapitalService userCapitalService;

    private AppConfigCacheService appConfigCacheService;

    private UserCacheService userCacheService;

    private Map<String, String> keys = new ConcurrentHashMap<>();

    private ManagerUserService managerUserService;

    private UserCapitalCacheService userCapitalCacheService;

    private ManagerSocketService managerSocketService;

    private ManagerConfigService managerConfigService;

    private MailService mailService;

    private UserStatisticService userStatisticService;

    private CardGameCacheService cardGameCacheService;

    private PlayGameService gameService;

    public PlayGamePlatform2Servlet() {
        xianWanOrderService = SpringUtil.getService(XianWanOrderService.class);
        userCapitalService = SpringUtil.getService(UserCapitalService.class);
        appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
        userCacheService = SpringUtil.getService(UserCacheService.class);
        keys.put("11695", "c0qf54l22bi4ojn3");
        keys.put("11694", "1hjlxh9i5eka8v3b");
        managerUserService = SpringUtil.getService(ManagerUserService.class);
        managerConfigService = SpringUtil.getService(ManagerConfigService.class);
        userCapitalCacheService = SpringUtil.getService(UserCapitalCacheService.class);
        managerSocketService = SpringUtil.getService(ManagerSocketService.class);
        mailService = SpringUtil.getService(MailService.class);
        userStatisticService = SpringUtil.getService(UserStatisticService.class);
        cardGameCacheService = SpringUtil.getService(CardGameCacheService.class);
        gameService = SpringUtil.getService(PlayGameService.class);
    }

    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
        XianWanOrder xianWanOrder = new XianWanOrder();
        JSONObject result = new JSONObject();
        xianWanOrder.setAdid(Integer.parseInt(request.getParameter("adid")));
        xianWanOrder.setAdname(request.getParameter("adname"));
        xianWanOrder.setAppid(request.getParameter("appid"));
        xianWanOrder.setOrdernum(request.getParameter("ordernum"));
        xianWanOrder.setDlevel(Integer.parseInt(request.getParameter("dlevel")));
        xianWanOrder.setPagename(request.getParameter("pagename"));
        xianWanOrder.setAtype(Integer.parseInt(request.getParameter("atype")));
        xianWanOrder.setDeviceid(request.getParameter("deviceid"));
        xianWanOrder.setSimid(request.getParameter("simid"));
        xianWanOrder.setAppsign(request.getParameter("appsign"));
        xianWanOrder.setMerid(request.getParameter("merid"));
        xianWanOrder.setEvent(request.getParameter("event"));
        xianWanOrder.setAdicon(request.getParameter("adicon"));
        xianWanOrder.setPrice(request.getParameter("price"));
        xianWanOrder.setMoney(request.getParameter("money"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        xianWanOrder.setItime(format.parse(request.getParameter("itime")));

        XianWanOrder orderByID = xianWanOrderService.getOrderByID(xianWanOrder.getOrdernum());
        if (orderByID != null) {
            result.put("success", 0);
            result.put("message", "订单已存在");
        } else {
            String s = MD5Util.md5(xianWanOrder.getAdid() + xianWanOrder.getAppid() + xianWanOrder.getOrdernum() + xianWanOrder.getDlevel() +
                    xianWanOrder.getDeviceid() + xianWanOrder.getAppsign() + xianWanOrder.getPrice() + xianWanOrder.getMoney() + keys.get(request.getParameter("appid")));
            String upperCaseStr = s.toUpperCase();
            double rate = Double.parseDouble(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_PLAY_TEST_RATE, Config.PLAYTEST_RATE));
            if (upperCaseStr.equals(request.getParameter("keycode"))) {
                Long dataId = xianWanOrderService.addOrder(xianWanOrder);
                String status = managerConfigService.getString(Config.PLAYGAME_2_STATUS);
                if (status!=null && status.equals("0")){
                    result.put("success", 0);
                    result.put("message", "试玩暂时关闭");
                    return result;
                }
                status = status.equals("0")?"1":status;
                BigDecimal addAmount = new BigDecimal(xianWanOrder.getMoney()).multiply(new BigDecimal(String.valueOf(rate))).multiply(new BigDecimal(status));
                userCapitalService.addUserBalanceByPlayTest(addAmount, Long.parseLong(xianWanOrder.getAppsign()), xianWanOrder.getOrdernum(), dataId);
                UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(Long.parseLong(xianWanOrder.getAppsign()), UserCapitalTypeEnum.currency_2.getValue());
                JSONObject pushData = new JSONObject();
                pushData.put("userId", xianWanOrder.getAppsign());
                pushData.put("capitalType", UserCapitalTypeEnum.currency_2.getValue());
                pushData.put("balance", userCapital.getBalance());
                Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(Long.parseLong(xianWanOrder.getAppsign())), pushData);
                User user = userCacheService.getUserInfoById(xianWanOrder.getAppsign());
                if (user.getParentId() != null && user.getIsCash()==1) {
                    Double toParentRate = Double.parseDouble(appConfigCacheService.getConfigByKey(RedisKeyConstant.APP_CONFIG_PLAY_TEST_TO_PARENT_RATE, Config.PLAYTEST_TO_PARENT_RATE));
                    BigDecimal amount = addAmount.multiply(new BigDecimal(toParentRate.toString()));
                    managerUserService.checkPlayTestIncome(user.getId(), amount);
                    //userCapitalService.addUserBalanceByFriendPlayGame(amount,user.getParentId(),xianWanOrder.getOrdernum(),dataId);
                }
                result.put("success", 1);
                result.put("message", "接收成功");
            } else {
                result.put("success", 0);
                result.put("message", "签名失败");
            }
        }
        return result;
    }
    public void sendMailByBuy(Long userId,JSONObject data ) {
        BigDecimal amount = data.getBigDecimal("amount");
        String title = "试玩奖励";
        String context = "完成试玩任务奖励金币"+amount+"个";
        JSONArray jsonArray = new JSONArray();
        JSONObject info = new JSONObject();
        info.put("type",1);
        info.put("id", ItemIdEnum.GOLD.getValue());
        info.put("number",amount);
        info.put("goldType", MailGoldTypeEnum.PLAY_GAME.getValue());
        jsonArray.add(info);
        User user = userCacheService.getUserInfoById(userId);
        mailService.sendMail(null, userId, title, context, null, 1, jsonArray);
        String serverIdByUserId = managerSocketService.getServerIdByUserId(userId);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("event", KafkaEventContext.MAIL);
        JSONArray array = new JSONArray();
        pushDate.put("data", array);
        cardGameCacheService.setUserRedPointInfo(userId,  KafkaEventContext.MAIL, array);
        Push.push(PushCode.redPointShow, serverIdByUserId, pushDate);
        sendMailByBuy2(userId,user.getUserNo(),user.getName(),user.getParentId(),amount);
    }


    public void sendMailByBuy2(Long userId,String userNo,String userName,Long parentId,BigDecimal amount ) {
        if (parentId==null){
            return;
        }
        User parent = userCacheService.getUserInfoById(parentId);
        if (parent==null){
            return;
        }
        if (  parent.getIsChannel()==1){
            amount = amount.multiply(new BigDecimal("0.3")).setScale(2,BigDecimal.ROUND_DOWN);
        }else{
            amount = amount.multiply(new BigDecimal("0.2")).setScale(2,BigDecimal.ROUND_DOWN);
        }
        userCacheService.addUserTodayCreateSw(userId,amount);
        UserStatistic userStatistic = gameService.getUserStatistic(userId.toString());
        userStatistic.setCreateSw(userStatistic.getCreateSw().add(amount));
        userStatisticService.updateUserCreateSw(userId,amount);
        String title = "好友试玩奖励";
        String context = "好友【"+userName+"("+userNo+")】完成试玩任务奖励金币"+amount+"个";
        JSONArray jsonArray = new JSONArray();
        JSONObject info = new JSONObject();
        info.put("type",1);
        info.put("id", ItemIdEnum.GOLD.getValue());
        info.put("number",amount);
        info.put("goldType", MailGoldTypeEnum.PLAY_GAME.getValue());
        jsonArray.add(info);
        mailService.sendMail(null, parentId, title, context, null, 1, jsonArray);
        logger.info("调用上级返利");
        String serverIdByUserId = managerSocketService.getServerIdByUserId(parentId);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", parentId);
        pushDate.put("event", KafkaEventContext.MAIL);
        JSONArray array = new JSONArray();
        pushDate.put("data", array);
        cardGameCacheService.setUserRedPointInfo(parentId,  KafkaEventContext.MAIL, array);
        Push.push(PushCode.redPointShow, serverIdByUserId, pushDate);
        logger.info("调用上级返利");
    }
    @Override
    protected Log logger() {
        return logger;
    }
}
