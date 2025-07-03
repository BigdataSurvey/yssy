package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.*;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;

@Service
@ServiceClass(code = MessageCodeContext.MAIL_SERVER)
public class ManagerMailService extends BaseService {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMailService userMailService;



    @Autowired
    private UserCacheService userCacheService;


    @Autowired
    private AppConfigCacheService appConfigCacheService;

    @Autowired
    private PlayGameService gameService;


    @Autowired
    private UserVipService userVipService;


    @PostConstruct
    public void _ManagerMailService() {
        new Timer("监控邮件是否过期").schedule(new TimerTask() {
            public void run() {
                int result = mailService.mailExpired();
                if (result != 0) {
                    logger.info(result + "条邮件已过期。");
                }
            }
        }, 1000L, 60000L);
    }


    @Transactional
    @ServiceMethod(code = "100", description = "玩家读取邮件")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.READ_MAIL, sendParams = true)
    public JSONObject userReadMail(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        JSONObject result = new JSONObject();

        Long userId = data.getLongValue("userId");
        synchronized (LockUtil.getlock(userId.toString())) {
            if (userCacheService.canReceiveMail(userId.toString())) {
                throwExp("点击过快");
            }
            userCacheService.userReceiveMailTime(userId.toString());
            Long mailId = data.getLongValue("mailId");
            UserMail userMail = userMailService.findUserReadMailInfo(userId);
            if (userMail == null) {
                throwExp("领取邮件失败！");
            }
            List<Mail> mails = new ArrayList<>();
            JSONArray userReadMails = userMail.getReadMailList();
            List<Mail> myMail = mailService.findToMyEmail(userId);
            List<Mail> allNoRead = new ArrayList<>();
            //全部未读
            for (Mail m : myMail) {
                if ((!userReadMails.toList(Long.class).contains(m.getId()) && m.getType() == 2) || (m.getType() == 1 && m.getIsRead() == 0)) {
                    allNoRead.add(m);
                }
            }
            if (mailId == 0L) {
                //全部领取
                mails.addAll(allNoRead);
            } else {
                Mail mailById = mailService.findMailById(mailId);
                if (mailById != null) {
                    mails.add(mailById);
                }
            }
            JSONArray rewards = new JSONArray();
            List<Integer> capitalTypes = new ArrayList<>();
            if (mails.size() == 0) {
                throwExp("没有可领取的邮件");
            }

            // 读取邮件完成 更新邮件为已读状态
            List<Long> mailIds = new ArrayList<>();
            for (Mail mail : mails) {
                if (mail.getIsRead() == 1) {
                    throwExp("您已领取过该邮件，不能重复领取");
                }
                if (mail.getType() == 2 && userReadMails.toList(Long.class).contains(mail.getId())) {
                    throwExp("您已领取过该邮件，不能重复领取");
                }
                mailIds.add(mail.getId());
            }
            mailService.batchUpdateIsRead(userId, mails);
            for (Mail m : mails) {
                Long mId = m.getId();
                if (m.getType() == 2) {
                    //系统邮件
                    userReadMails.add(mId);
                }
                Mail mail = mailService.getMailByMailId(mId);
                if (mail.getIsAttachments() != 0) {
                    // 需要领取附件
                    JSONArray details = mail.getAttachmentsDetails();
                    rewards.addAll(details);
                    gameService.addReward(userId, details, LogCapitalTypeEnum.mail);
                }
            }
            userMail.setReadMailList(userReadMails);
            userMailService.userReadMail(userId, userMail.getReadMailList());
            result.put("mailIds", mailIds);
            result.put("rewards", rewards);
            return result;
        }
    }


    @Transactional
    @ServiceMethod(code = "200", description = "玩家发送邮件（转赠功能）")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT,event = KafkaEventContext.SEND_MAIL,sendParams = true)
    public JSONObject sendMail(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("toUserId"), data.get("userId"));
        JSONArray array = new JSONArray();
        //第一个是赠送的文房四宝
        JSONObject detail = new JSONObject();
        //第二个是根据等级额外获得的信封道具
        long userId = data.getLongValue("userId");
        synchronized (LockUtil.getlock(userId)){
            long toUserId = data.getLongValue("toUserId");
            String toUserNo  = data.getString("toUserNo");
            String context = data.getString("context");
            int number = data.getIntValue("amount");
            if (number < 0) {
                throwExp("数值异常");
            }
            String itemId = ItemIdEnum.WFSB.getValue();
            String useItemId = ItemIdEnum.XG.getValue();
            String smallItemId = ItemIdEnum.XXF.getValue();
            String bigItemId = ItemIdEnum.DXF.getValue();
            String title = data.getString("title");
            User user = userCacheService.getUserInfoById(userId);
            gameService.checkUserItemNumber(userId, itemId, number);
            //根据userId查询出当前用户的vip等级
            UserVip uservip = userVipService.findRechargeAmountByUserId(userId);
            UserVip toUservip = userVipService.findRechargeAmountByUserId(toUserId);
            if(uservip.getVipLevel()<4 ){
                //需要消耗一个信鸽
                gameService.checkUserItemNumber(userId, useItemId, number);
                //修改该用户的道具
                gameService.updateUserBackpack(userId, useItemId,-number, LogUserBackpackTypeEnum.use);
            }
            if(uservip.getVipLevel()<4 && toUservip.getVipLevel() == 4){
                //收件人将获得一个小信封
                //修改该用户的道具
                //gameService.updateUserBackpack(toUserId, smallItemId,number, LogUserBackpackTypeEnum.use);
                JSONObject detail1 = new JSONObject();
                detail1.put("type", 1);
                detail1.put("id", smallItemId);
                detail1.put("number", number);
                detail1.put("channel", MailGoldTypeEnum.FRIEND.getValue());
                detail1.put("fromUserId",user.getUserNo());
                array.add(detail1);
            }else if(uservip.getVipLevel()<4 && toUservip.getVipLevel() > 4){
                //gameService.updateUserBackpack(toUserId, bigItemId,number, LogUserBackpackTypeEnum.use);
                JSONObject detail1 = new JSONObject();
                detail1.put("type", 1);
                detail1.put("id", bigItemId);
                detail1.put("number", number);
                detail1.put("channel", MailGoldTypeEnum.FRIEND.getValue());
                detail1.put("fromUserId",user.getUserNo());
                array.add(detail1);
            }
            if (title == null) {
                title = "好友赠送";
            }
            if (context == null) {
                context = user.getName() + "(" + user.getUserNo() + ")赠送" + PlayGameService.itemMap.get(itemId).getName()
                        + ":" + number;
            }

            detail.put("type", 1);
            detail.put("id", itemId);
            detail.put("number", number);
            detail.put("channel", MailGoldTypeEnum.FRIEND.getValue());
            detail.put("fromUserId",user.getUserNo());
            //添加邮件记录
            int isAttachments = 1;
            array.add(detail);
            Long mailId = mailService.sendMail(userId, toUserId, title, context, null, isAttachments, array);
            String orderNo = OrderUtil.getOrder5Number();
            //如果是赠送   则再扣除赠送的金额  增加流水  赠送记录
            gameService.updateUserBackpack(userId,itemId,-number, LogUserBackpackTypeEnum.zsg, String.valueOf(toUserNo));
            return null;
        }

    }

    @Transactional
    @ServiceMethod(code = "300", description = "查询玩家信息")
    public JSONObject findUserInfo(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        String findUserNo = data.getString("userNo");
        Long userId = data.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("查询异常！");
        }
        User findUser = userCacheService.getUserInfoByUserNo(findUserNo);
        if (findUser == null) {
            throwExp("玩家不存在");
        }
        JSONObject userInfo = new JSONObject();
        userInfo.put("userNo", findUser.getUserNo());
        userInfo.put("headImgUrl", findUser.getHeadImageUrl());
        userInfo.put("name", findUser.getName());
        userInfo.put("isPop", findUser.getRoleId() == 1 ? 0 : findUser.getRoleId());
        userInfo.put("roleId", findUser.getRoleId());
        userInfo.put("name", findUser.getName());
        userInfo.put("wechatId", findUser.getWechatId());
        userInfo.put("qq", findUser.getQq());
        JSONObject result = new JSONObject();
        result.put("userInfo", userInfo);
        return result;

    }



}
