package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserStatistic;
import com.zywl.app.base.bean.WoWanOrder;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.MD5Util;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.MailGoldTypeEnum;
import com.zywl.app.defaultx.service.MailService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserStatisticService;
import com.zywl.app.defaultx.service.WoWanOrderService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
@WebServlet(name = "PlayGamePlatform3Servlet", urlPatterns = "/playGame3Notify")
public class PlayGamePlatform3Servlet extends BaseServlet {

    //我玩

    private static final Log logger = LogFactory.getLog(PlayGamePlatform3Servlet.class);

    private WoWanOrderService woWanOrderService;

    private UserCapitalService userCapitalService;

    private AppConfigCacheService appConfigCacheService;

    private UserCacheService userCacheService;

    private Map<String, String> keys = new ConcurrentHashMap<>();

    private ManagerUserService managerUserService;

    private UserCapitalCacheService userCapitalCacheService;

    private ManagerSocketService managerSocketService;

    private ManagerConfigService managerConfigService;

    private MailService mailService;
    private CardGameCacheService cardGameCacheService;

    private UserStatisticService userStatisticService;


    private PlayGameService gameService;

    private static String key = "sPwMIu63Hak7rnUdx3kWRcQRlhBGl0MX";


    public PlayGamePlatform3Servlet() {
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
        cardGameCacheService =SpringUtil.getService(CardGameCacheService.class);
        woWanOrderService = SpringUtil.getService(WoWanOrderService.class);
        userStatisticService = SpringUtil.getService(UserStatisticService.class);
        gameService = SpringUtil.getService(PlayGameService.class);
    }

    @Override
    public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
        JSONObject result = new JSONObject();
        int orderId = Integer.parseInt(request.getParameter("orderid"));
        int cid = Integer.parseInt(request.getParameter("cid"));
        String cuid = request.getParameter("cuid");
        String devid = request.getParameter("devid");
        String adName = URLDecoder.decode(request.getParameter("adname"),"UTF-8");
        String time = request.getParameter("time");
        String point = request.getParameter("points");
        int atype = Integer.parseInt(request.getParameter("atype"));
        String sign = request.getParameter("sign");
        String platPoints = request.getParameter("plat_points");
        String event =URLDecoder.decode(request.getParameter("event"),"UTF-8");
        String dlevel = request.getParameter("dlevel");
        String icon = request.getParameter("icon");
        String adid = request.getParameter("adid");
        String phonetype = request.getParameter("phonetype");
        WoWanOrder woWanOrder = new WoWanOrder(orderId,cid,cuid,devid,adName,time,new BigDecimal(point),atype,sign,platPoints,event,dlevel,icon,adid,phonetype);
        WoWanOrder orderByID = woWanOrderService.getOrderByID(woWanOrder.getOrderId());
        if (orderByID != null) {
            result.put("status", 0);
            result.put("msg", "订单已存在");
        } else {
            String need = String.valueOf(orderId)+String.valueOf(cid)+cuid+devid+adName+time+point+atype+key;
            String s = MD5Util.md5(need).toLowerCase();
            String mySign = s.substring(10,20);
            logger.info("=======:"+mySign);
            logger.info("=======:"+sign);
            if (mySign.equals(sign)){
                //成功
                String status = managerConfigService.getString(Config.PLAYGAME_2_STATUS);
                status = status.equals("0")?"1":status;
                BigDecimal addAmount = new BigDecimal(point).multiply(new BigDecimal(status));
                Long dataId = woWanOrderService.addOrder(woWanOrder);
                if (status!=null && status.equals("0")){
                    result.put("status", 0);
                    result.put("msg", "试玩暂时关闭");
                    return result;
                }
                if (addAmount.compareTo(BigDecimal.ZERO)>0){
                        JSONObject info = new JSONObject();
                        info.put("amount",addAmount);
                        sendMailByBuy(Long.valueOf(cuid),info);
                }
                result.put("status", 1);
                result.put("msg", "成功");
            }else {
                result.put("success", 0);
                result.put("message", "验签失败");
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
        mailService.sendMail(null, userId, title, context, null, 1, jsonArray);
        String serverIdByUserId = managerSocketService.getServerIdByUserId(userId);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("event", KafkaEventContext.MAIL);
        JSONArray array = new JSONArray();
        pushDate.put("data", array);
        cardGameCacheService.setUserRedPointInfo(userId,  KafkaEventContext.MAIL, array);
        Push.push(PushCode.redPointShow, serverIdByUserId, pushDate);
        User user = userCacheService.getUserInfoById(userId);
        if (user.getIsCash()==1){
            sendMailByBuy2(userId,user.getUserNo(),user.getName(),user.getParentId(),amount);
        }

    }
    public void sendMailByBuy2(Long userId,String userNo,String userName,Long parentId,BigDecimal amount ) {
        if (parentId==null){
            return;
        }
        User parent = userCacheService.getUserInfoById(parentId);
        if (parent==null){
            return;
        }
        if ( parent.getIsChannel()==1){
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
        info.put("goldType", MailGoldTypeEnum.FRIEND_PLAY_GAME.getValue());
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

    public static void main(String[] args) throws UnsupportedEncodingException {
        int orderId = 1379782;
        int cid = 13748;
        String cuid = "123";
        String devid = "testdevid";
        String adName = URLDecoder.decode("%e5%89%91%e9%81%93%e4%bb%99%e6%a2%a6%e7%ba%a2%e5%8c%85%e7%89%88","UTF-8");
        String time = "1738135490";
        String point ="0.45";
        int atype = 1;
        String sign = "c00242b294";
        String platPoints = "0.90";
        String event = URLDecoder.decode("%e8%b6%a3%e5%91%b3%e7%b4%af%e8%ae%a1%e4%bc%a4%e5%ae%b316%e4%b8%87%e9%87%91%e5%b8%81","UTF-8");
        String dlevel = "1";
        String icon = "http://ddzgg.dandanz.com/20241126/1452/638682295424238654.png";
        String adid = "1379808";
        String phonetype = "2";
        System.out.println(adName);
        System.out.println(event);
        String need = String.valueOf(orderId)+String.valueOf(cid)+cuid+devid+adName+time+point+atype+key;
        String nn = "137978213748123testdevid剑道仙梦红包版17381354900.451sPwMIu63Hak7rnUdx3kWRcQRlhBGl0MX";
        String s = MD5Util.md5(need).toLowerCase();
        System.out.println(need);
        System.out.println(nn);
        String mySign = s.substring(10,20);
        System.out.println(mySign);

    }
}
