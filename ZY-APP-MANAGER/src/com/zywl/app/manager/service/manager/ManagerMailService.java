package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.shoop.ShopManager;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
@ServiceClass(code = MessageCodeContext.MAIL_SERVER)
public class ManagerMailService extends BaseService {

    @Autowired
    private MailService mailService;

    @Autowired
    private UserMailService userMailService;

    @Autowired
    private ManagerConfigService managerConfigService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private PlayGameService gameService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;


    @Autowired
    private UserVipService userVipService;
    @Autowired
    private UserCapitalService userCapitalService;



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
                    gameService.addReward(userId, details, LogCapitalTypeEnum.mail,LogUserBackpackTypeEnum.mail);
                }
            }
            userMail.setReadMailList(userReadMails);
            userMailService.userReadMail(userId, userMail.getReadMailList());
            result.put("mailIds", mailIds);
            result.put("rewards", rewards);
            return result;
        }
    }

    private boolean isManager(String userId){
        return "userId".equals(userId);
    }

    public void  shopManagerSend(Long userId,String userNo){
        //判断发送人是否是店长
        if(isManager(userId.toString())){
            System.out.println("发送人是店长，无需检查接收人");
        }else{
            //如果不是店长，需要判断接收人是不是店长
            System.out.println("发送人不是店长，检查接收人是否为店长");
            boolean recriverUserNo =isManager(userNo);
            if(recriverUserNo){
                System.out.println("接收人是店长，允许发送");
            }else {
                throwExp("只能赠送给店长");
            }
        }
    }


    @Transactional
    @ServiceMethod(code = "200", description = "玩家发送邮件（转赠功能）")
    @KafkaProducer(topic = KafkaTopicContext.RED_POINT, event = KafkaEventContext.SEND_MAIL, sendParams = true)
    public JSONObject sendMail(ManagerSocketServer adminSocketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("toUserId"), data.get("userId"));
        checkNull(data.get("amount"));
        //需要转赠的道具ID
        String itemId   = data.getString("itemId");
        long   userId   = data.getLongValue("userId");
        long   toUserId = data.getLongValue("toUserId");
        String toUserNo = data.getString("toUserNo");
        int    amount   = data.getIntValue("amount");

        if (amount <= 0) {
            throwExp("转赠数量必须大于0");
        }

        boolean isCapitalItem =
                ItemIdEnum.CORE_POINT.getValue().equals(itemId)
                        || ItemIdEnum.GAME_CONSUME_COIN.getValue().equals(itemId);


        User fromUser = userCacheService.getUserInfoById(userId);
        if (fromUser == null) {
            throwExp("发送人信息有误");
        }
        User toUser = userCacheService.getUserInfoById(toUserId);
        if (toUser == null) {
            throwExp("收件人不存在");
        }
        if (fromUser.getUserNo().equals(toUserNo)) {
            throwExp("不能给自己转赠");
        }

        // 转赠门槛
        Integer sill = null;
        try {
            sill = managerConfigService.getInteger(Config.TRANSFER_SILL);
        } catch (Exception ignore) {}
        if (sill != null && sill > 0 && amount < sill) {
            throwExp("转赠数量不能低于" + sill);
        }

        // 手续费配置
        Integer feeNum;
        try {
            feeNum = managerConfigService.getInteger(Config.TRANSFER_COST_ITEM_NUM);
        } catch (Exception e) {
            feeNum = 0;
        }
        if (feeNum == null || feeNum < 0) {
            feeNum = 0;
        }

        synchronized (LockUtil.getlock(userId)) {
            if (isCapitalItem) {
                //如果是资产性校验资产是否充足
                BigDecimal transferAmount = BigDecimal.valueOf(amount);
                int capitalType = Integer.parseInt(itemId);

                UserCapital capital = userCapitalService
                        .findUserCapitalByUserIdAndCapitalType(userId, capitalType);
                if (capital == null || capital.getBalance().compareTo(transferAmount) < 0) {
                    throwExp("资产不足");
                }
            } else {
                //普通道具走背包检查资产是否充足
                gameService.checkUserItemNumber(userId, itemId, amount);
            }


            //手续费统一为核心积分走资产表校验
            BigDecimal feeAmount = BigDecimal.ZERO;
            if (feeNum > 0) {
                feeAmount = BigDecimal.valueOf(feeNum);

                UserCapital feeCapital = userCapitalService
                        .findUserCapitalByUserIdAndCapitalType(userId, UserCapitalTypeEnum.hxjf.getValue());
                if (feeCapital == null || feeCapital.getBalance().compareTo(feeAmount) < 0) {
                    throwExp("手续费核心积分不足");
                }
            }


            String title   = data.getString("title");
            String context = data.getString("context");
            if (title == null || title.trim().isEmpty()) {
                title = "好友转赠";
                if (ItemIdEnum.CORE_POINT.getValue().equals(itemId)) {
                    title = "好友转赠核心积分";
                }
            }
            if (context == null || context.trim().isEmpty()) {
                context = fromUser.getName()
                        + "(" + fromUser.getUserNo() + ") 转赠你 "
                        + amount + " 个 [" + itemId + "]";
            }


            JSONArray attachments = new JSONArray();
            JSONObject detail = new JSONObject();
            detail.put("type", 1);
            detail.put("id", itemId);
            detail.put("number", amount);
            detail.put("channel", MailGoldTypeEnum.FRIEND.getValue());
            detail.put("fromUserId", String.valueOf(userId));
            detail.put("fromUserNo", fromUser.getUserNo());
            attachments.add(detail);

            //发送邮件
            Long mailId = mailService.sendMail( userId,toUserId, title, context, null, 1, attachments );

            //扣费 资产走资产扣减；普通道具走背包扣减
            String orderNo = OrderUtil.getOrder5Number();
            if (isCapitalItem) {
                BigDecimal transferAmount = BigDecimal.valueOf(amount);
                int capitalType = Integer.parseInt(itemId);
                userCapitalService.subUserBalanceBySendMail(
                        userId,
                        transferAmount,
                        capitalType,
                        orderNo,
                        mailId,
                        LogCapitalTypeEnum.friend_transfer
                );
            } else {
                gameService.updateUserBackpack(
                        userId,
                        itemId,
                        -amount,
                        LogUserBackpackTypeEnum.zsg,
                        String.valueOf(toUserNo)
                );
            }

            //扣除手续费
            if (feeAmount.compareTo(BigDecimal.ZERO) > 0) {
                String feeOrderNo = OrderUtil.getOrder5Number();
                userCapitalService.subUserBalanceBySendMail(
                        userId,
                        feeAmount,
                        UserCapitalTypeEnum.hxjf.getValue(),
                        feeOrderNo,
                        mailId,
                        LogCapitalTypeEnum.send_mail
                );
            }

            JSONObject result = new JSONObject();
            result.put("mailId", mailId);
            result.put("amount", amount);
            result.put("itemId", itemId);
            result.put("toUserId", toUserId);
            result.put("toUserNo", toUserNo);
            result.put("isCapitalItem", isCapitalItem);
            result.put("fee", feeNum);
            return result;
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
