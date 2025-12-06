package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.interfacex.Listener;
import com.live.app.ws.socket.BaseClientSocket;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.ActiveGiftRecord;
import com.zywl.app.base.bean.Mail;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserMail;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.Async;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserGroupEnum;
import com.zywl.app.defaultx.service.ActiveGiftRecordService;
import com.zywl.app.defaultx.service.MailService;
import com.zywl.app.defaultx.service.UserMailService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ServiceClass(code = MessageCodeContext.MAIL_SERVER)
public class ServerMailService extends BaseService{
	
	@Autowired
	private UserCacheService userCacheService;

	@Autowired
	private RequestManagerService requestManagerService;

	@Autowired
	private UserMailService userMailService;
	
	@Autowired
	private MailService mailService;

	@Autowired
	private ActiveGiftRecordService activeGiftRecordService;




	
	
	
	@ServiceMethod(code = "001", description = "查看邮件列表")
	public JSONObject getMailInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		long userId = appSocket.getWsidBean().getUserId();
		int page = params.getInteger("page");
		int num = params.getIntValue("num");
		return getMailInfo(userId,page,num);
	}

	@ServiceMethod(code = "002", description = "发送邮件 (转增)")
	public Async sendMail(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		checkNull(params.get("toUserNo"));
		checkNull(params.get("amount"));
		checkNull(params.get("itemId"));
		long userId = appSocket.getWsidBean().getUserId();
		params.put("userId", userId);

		//收件人账号校验
		String toUserNo = params.getString("toUserNo");
		User toUser = userCacheService.getUserInfoByUserNo(toUserNo);
		if (toUser == null) {
			throwExp("玩家不存在");
		}
		params.put("toUserId", toUser.getId());

		User user = userCacheService.getUserInfoById(userId);
		if (user == null) {
			throwExp("玩家信息有误");
		}

		if (user.getUserNo().equals(toUserNo)) {
			throwExp("不能给自己转赠");
		}

		// 注册24小时内且未达成指定条件，禁止转赠
		if ((System.currentTimeMillis() - user.getRegistTime().getTime()) / 1000 < 86400) {
			List<ActiveGiftRecord> records = activeGiftRecordService.findByUserId(userId, 2);
			if (records == null || records.isEmpty()) {
				throwExp("注册24小时后解锁转赠功能");
			}
		}

		if (user.getRisk() == 1) {
			throwExp("账号存在风险，请联系客服进行核实");
		}

		Executer.request(
				TargetSocketType.manager,
				CommandBuilder.builder().request("800200", params).build(),
				new RequestManagerListener(appCommand)
		);
		return async();
	}


	public JSONObject getMailInfo(Long userId,int page,int num){
		User user = userCacheService.getUserInfoById(userId);
		UserMail userMail = userMailService.findUserReadMailInfo(userId);
		JSONArray userReadMails = userMail.getReadMailList();
		int type = 1;
		if (user == null) {
			throwExp("未查询到邮件信息！");
		}
		List<Mail> myMail = mailService.findMyEmail(userId,type,page,num);
		JSONObject result = new JSONObject();
		List<Mail> newList = new ArrayList<>();
		for (Mail mail : myMail) {
			if (mail.getType()==2 && userReadMails.toList(Long.class).contains(mail.getId())){
				if (!userMail.getDeleteMailList().toList(Long.class).contains(mail.getId())){
					mail.setIsRead(1);
					newList.add(mail);
				}
			}else{
				newList.add(mail);
			}
		}
		result.put("mailList", newList);
		return result;
	}

	

	
	@ServiceMethod(code = "003", description = "领取邮件")
	public Async readMail(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		long userId = appSocket.getWsidBean().getUserId();
		params.put("userId", userId);
		Long mailId = params.getLongValue("mailId");
		User user = userCacheService.getUserInfoById(userId);
		if ( mailId!=0L) {
			Mail mail = mailService.getMailByMailId(mailId);
			if (mail.getType()!=2 && mail.getToUserId()!=userId) {
				throwExp("禁止领取");
			}
		}
		requestManagerService.requestManagerReadMail(params, new Listener() {
			public void handle(BaseClientSocket clientSocket, Command command) {
				if (command.isSuccess()) {
					JSONObject result = JSONObject.from(command.getData());
					Executer.response(CommandBuilder.builder(appCommand).success(result).build());
				} else {
					Executer.response(
							CommandBuilder.builder(appCommand).error(command.getMessage(), command.getData()).build());
				}
            }
		});
		return async();
	}
	
	@ServiceMethod(code = "004", description = "查询收件人信息")
	public Object findUserInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		checkNull(params.get("userNo"));
		long userId = appSocket.getWsidBean().getUserId();
		params.put("userId",userId);
		Executer.request(TargetSocketType.manager,CommandBuilder.builder().request("800300", params).build(), new RequestManagerListener(appCommand));
		return async();

	}
	@ServiceMethod(code = "005", description = "查询玩家邮件往来")
	public Object findByUser(final AppSocket appSocket, Command appCommand, JSONObject params) {
		checkNull(params);
		checkNull(params.get("userNo"),params.get("page"),params.get("num"),params.getIntValue("type"));
		long userId = appSocket.getWsidBean().getUserId();
		String findUserNo = params.getString("userNo");
		int type = params.getIntValue("type");
		User user = userCacheService.getUserInfoById(userId);
		if (user==null) {
			throwExp("查询异常！");
		}
		User findUser =  userCacheService.getUserInfoByUserNo(findUserNo);
		if (findUser==null) {
			throwExp("玩家不存在");
		}
		if (String.valueOf(userId).equals(findUser.getId().toString())){
			throwExp("不可以搜索自己");
		}
		List<Mail> mails = mailService.findByUserId(findUser.getId(),userId,params.getIntValue("page"),params.getIntValue("num"),type);
		return mails;

	}

	@ServiceMethod(code = "006", description = "删除已读邮件")
	public Object deleteReadMail(final AppSocket appSocket, Command appCommand, JSONObject params) {
		Long userId = appSocket.getWsidBean().getUserId();
		userMailService.updateUserDeleteMailList(userId);
		mailService.deleteReadMail(userId);
		return null;
	}

}
