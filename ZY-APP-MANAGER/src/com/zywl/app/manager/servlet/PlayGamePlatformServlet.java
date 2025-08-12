package com.zywl.app.manager.servlet;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.servlet.BaseServlet;
import com.zywl.app.base.util.DuoYouUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.enmus.MailGoldTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.DuoYouOrderService;
import com.zywl.app.defaultx.service.MailService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserStatisticService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
@WebServlet(name = "PlayGamePlatformServlet", urlPatterns = "/playGameNotify")
public class PlayGamePlatformServlet extends BaseServlet {

	private static final Log logger = LogFactory.getLog(PlayGamePlatformServlet.class);

	private DuoYouOrderService duoYouOrderService;

	private UserCapitalService userCapitalService;

	private UserCacheService userCacheService;

	private AppConfigCacheService appConfigCacheService;

	private PlayGameService gameService;
	private ManagerUserService managerUserService;

	private ManagerConfigService managerConfigService;

	private UserCapitalCacheService userCapitalCacheService;

	private ManagerSocketService managerSocketService;

	private CardGameCacheService cardGameCacheService;

	private UserStatisticService userStatisticService;

	private MailService mailService;

	private  Map<String, String> keys = new ConcurrentHashMap<>();

	public PlayGamePlatformServlet() {
		duoYouOrderService = SpringUtil.getService(DuoYouOrderService.class);
		userCapitalService = SpringUtil.getService(UserCapitalService.class);
		userCacheService = SpringUtil.getService(UserCacheService.class);
		appConfigCacheService = SpringUtil.getService(AppConfigCacheService.class);
		keys.put("dy_59645202","b9644b6f8b38f4e3b588675053787a93");
		keys.put("dy_59640747","5a70ee65e42c31d4eb535d3b5cfd2f1e");
		managerUserService = SpringUtil.getService(ManagerUserService.class);
		managerConfigService = SpringUtil.getService(ManagerConfigService.class);
		userCapitalCacheService = SpringUtil.getService(UserCapitalCacheService.class);
		managerSocketService = SpringUtil.getService(ManagerSocketService.class);
		mailService = SpringUtil.getService(MailService.class);
		cardGameCacheService = SpringUtil.getService(CardGameCacheService.class);
		userStatisticService = SpringUtil.getService(UserStatisticService.class);
		gameService = SpringUtil.getService(PlayGameService.class);
	}

	@Override
	public Object doProcess(HttpServletRequest request, HttpServletResponse response, String ip) throws Exception {
		String status = managerConfigService.getString(Config.PLAYGAME_1_STATUS);

		JSONObject result = new JSONObject();
		Map<String,String> map = new HashMap<>();
		String order_id = request.getParameter("order_id");
		String advert_id = request.getParameter("advert_id");
		String advert_name = request.getParameter("advert_name");
		Long created = Long.parseLong(request.getParameter("created"));
		String media_income = request.getParameter("media_income");
		String member_income = request.getParameter("member_income");
		String media_id = request.getParameter("media_id");
		String user_id = request.getParameter("user_id");
		String device_id = request.getParameter("device_id");
		String content = request.getParameter("content");
		String sign = request.getParameter("sign");
		map.put("order_id",order_id);
		map.put("advert_name",advert_name);
		map.put("advert_id",String.valueOf(advert_id));
		map.put("created",String.valueOf(created));
		map.put("media_income",String.valueOf(media_income));
		map.put("member_income",String.valueOf(member_income));
		map.put("media_id",String.valueOf(media_id));
		map.put("user_id",user_id);
		map.put("device_id",device_id);
		map.put("content",content);
		DuoYouOrder orderByID = duoYouOrderService.getOrderByID(order_id);
		if (orderByID!=null){
			result.put("status_code",200);
			return result;
		}
		logger.info("试玩map："+map);
		logger.info("试玩key："+keys.get(String.valueOf(media_id)));
		String s = DuoYouUtil.generateSignature(map, keys.get(String.valueOf(media_id)));
		String s2 = DuoYouUtil.generateSignature2(map, keys.get(String.valueOf(media_id)));
		logger.info(s);
		logger.info(s2);
		try {
			if (s.equals(sign) || s2.equals(sign)){
				//验签通过
				Long dataId = duoYouOrderService.addOrder(Long.parseLong(user_id), order_id, advert_name, String.valueOf(advert_id),
						String.valueOf(created), String.valueOf(media_income), String.valueOf(member_income), media_id, device_id, content);
				if (status!=null && status.equals("0")){
					result.put("status_code",404);
					return result;
				}
				status = status.equals("0")?"1":status;
				BigDecimal addAmount = new BigDecimal(String.valueOf(member_income)).multiply(new BigDecimal(status));
				if (addAmount.compareTo(BigDecimal.ZERO)>0){
					JSONObject info = new JSONObject();
					info.put("amount",addAmount);
					sendMailByBuy(Long.valueOf(user_id),info);
				}
				result.put("status_code",200);
			}else{
				result.put("status_code",403);
			}
			return result;
		}catch (Exception e){
			e.printStackTrace();
			result.put("status_code",400);
			return result;

		}
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
		info.put("channel", MailGoldTypeEnum.PLAY_GAME.getValue());
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
		info.put("channel", MailGoldTypeEnum.FRIEND_PLAY_GAME.getValue());
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

	public static void main(String[] args) throws Exception {
		Map<String,String> k = new HashMap<>();
		k.put("dy_59640876","3a8e38ab4d0dd7925ee939402c63d85a");
		k.put("dy_59640747","5a70ee65e42c31d4eb535d3b5cfd2f1e");
		Map<String,String> map = new HashMap<>();
		map.put("order_id","1480694960");
		map.put("advert_name","若水保卫熊猫(原萌宠点点消)");
		map.put("advert_id","1500023334");
		map.put("created","1704077744");
		map.put("media_income","27");
		map.put("member_income","27");
		map.put("media_id","dy_59640747");
		map.put("user_id","9456");
		map.put("device_id","F6E6B56677BF46789557B463D38DB31099825bf1a4436922e50b3948f0543eb5");
		map.put("content","[充值]若水保卫熊猫(原萌宠点点消)-第2期累计充值1999元获得27灵石");
		String s = DuoYouUtil.generateSignature(map, "5a70ee65e42c31d4eb535d3b5cfd2f1e");
		System.out.println(s);
	}
}
