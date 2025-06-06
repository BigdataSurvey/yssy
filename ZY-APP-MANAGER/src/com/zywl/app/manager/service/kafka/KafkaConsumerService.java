package com.zywl.app.manager.service.kafka;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Mail;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserAchievement;
import com.zywl.app.base.bean.card.PlayerCard;
import com.zywl.app.base.bean.card.UserLineup;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import com.zywl.app.defaultx.enmus.*;
import com.zywl.app.defaultx.service.MailService;
import com.zywl.app.defaultx.service.UserAchievementService;
import com.zywl.app.defaultx.service.UserDailyTaskService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import com.zywl.app.manager.service.manager.ManagerUserService;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaConsumerService extends BaseService {

    private ManagerSocketService managerSocketService;
    private static final String TOPIC = KafkaTopicContext.RED_POINT;
    private static final int THREAD_POOL_SIZE = 10;

    private final KafkaConsumer<String, String> consumer;
    private final ExecutorService executorService;


    private ManagerUserService managerUserService;


    private CardGameCacheService cardGameCacheService;

    private UserCacheService userCacheService;

    private UserService userService;

    private UserDailyTaskService userDailyTaskService;

    private MailService mailService;


    private UserAchievementService userAchievementService;

    private PlayGameService gameService;


    private ManagerGameBaseService managerGameBaseService;

    private UserCapitalCacheService userCapitalCacheService;

    private String CLIENT_ID;

    public KafkaConsumerService(String bootstrapServers, String groupId, String clientId) {
        CLIENT_ID = clientId;
        this.managerSocketService = new ManagerSocketService();
        this.consumer = new KafkaConsumer<>(KafkaConsumerConfig.getConsumerProperties(bootstrapServers, groupId));
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.consumer.subscribe(Collections.singletonList(TOPIC)); // 订阅主题
        this.cardGameCacheService = SpringUtil.getService(CardGameCacheService.class);
        this.userCacheService = SpringUtil.getService(UserCacheService.class);
        this.userDailyTaskService = SpringUtil.getService(UserDailyTaskService.class);
        this.mailService = SpringUtil.getService(MailService.class);
        this.userAchievementService = SpringUtil.getService(UserAchievementService.class);
        this.gameService = SpringUtil.getService(PlayGameService.class);
        this.managerGameBaseService = SpringUtil.getService(ManagerGameBaseService.class);
        this.userCapitalCacheService = SpringUtil.getService(UserCapitalCacheService.class);
        this.managerUserService = SpringUtil.getService(ManagerUserService.class);
    }

    public void start() {
        logger.info("================Kafka消费者配置完成================");
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            records.forEach(record -> {
                JSONObject msg = JSONObject.parseObject(record.value());
                logger.info(CLIENT_ID + ", 消费event:" + msg.getString("eventType") + " ,pt：" + record.partition());

                executorService.execute(() -> processMessage(msg));
            });
            // 提交偏移量（手动）
            consumer.commitAsync();
        }
    }

    private void processMessage(JSONObject msg) {
        try {
            // 在这里处理你的消息
            if (msg.containsKey("eventType") && msg.containsKey("data")) {
                String eventType = msg.getString("eventType");
                JSONObject data = msg.getJSONObject("data");
                update(eventType, data);
            }
        } catch (Exception e) {
            logger.error("kafkaConsumerError:" + e);
            // 异常处理逻辑
            e.printStackTrace();
        }
    }

    @Transactional
    public void update(String eventType, JSONObject data) {
        if (KafkaEventContext.MAIL.equals(eventType)) {
            //游戏内容发放奖励邮件
            gameSendMail(data);
        } else if (KafkaEventContext.SYS_MAIL.equals(eventType)) {
            //系统邮件
            sysSendMail(data);
        } else if (KafkaEventContext.READ_MAIL.equals(eventType)) {
            //读取邮件
            readMail(data);
        } else if (KafkaEventContext.ADD_REWARD.equals(eventType)) {
            Long time = System.currentTimeMillis();
            //获取奖励时的红点判断
            // addRewardRedPoint(data);
            JSONArray reward = data.getJSONArray("reward");
            int type = 0;
            for (Object o : reward) {
                JSONObject info = (JSONObject) o;
                int rewardInfo = info.getIntValue("type");
                if (rewardInfo == 2) {
                    type = rewardInfo;
                    break;
                }
            }
            if (type == 2) {
            }
        } else if (KafkaEventContext.LOGIN.equals(eventType)) {
            loginCheck(data);
            checkAchievement(data, AchievementGroupEnum.LOGIN.getValue());
        } else if (KafkaEventContext.DO_DAILY_TASK.equals(eventType)) {
            userReceiveDailyTask(data);
        } else if (KafkaEventContext.PVE_WIN.equals(eventType)) {
            checkAchievement(data, AchievementGroupEnum.CHECKPOINT_NUMBER.getValue());
        } else if (KafkaEventContext.SHOP_BUY.equals(eventType)) {
            checkAchievement(data, AchievementGroupEnum.SHOP_BUY.getValue());
            userBuyShop(data);
        } else if (KafkaEventContext.SYN.equals(eventType)) {
            userSyn(data);
        }else if (KafkaEventContext.DTS.equals(eventType)) {
            userDts(data);
        }else if (KafkaEventContext.LHD.equals(eventType)) {
            userLHD(data);
        } else if (KafkaEventContext.RECEIVE_ACHIEVEMENT.equals(eventType)) {
            checkRemoveRedAchievement(data, KafkaEventContext.ACHIEVEMENT);
        } else if (KafkaEventContext.USE_ITEM.equals(eventType)) {
            //使用道具 判断是否是战马
            checkUserPetAchievement(data);
        } else if (KafkaEventContext.OPEN_MINE.equals(eventType)) {
            checkOpenMineAchievement(data);
        }
    }

    public void checkTopLikeHideRed(JSONObject data) {
        String eventType = null;
        int type = data.getIntValue("type");
        if (type == 5) {
            eventType = KafkaEventContext.TOP_1;
        }
        if (type == 6) {
            eventType = KafkaEventContext.TOP_2;
        }
        if (type == 7) {
            eventType = KafkaEventContext.TOP_3;
        }
        if (type == 8) {
            eventType = KafkaEventContext.TOP_4;
        }
        if (type == 9) {
            eventType = KafkaEventContext.TOP_5;
        }
        pushHideRedPoint(data.getLong("userId"), eventType);

    }





    public void userBuyShop(JSONObject data) {
        Long userId = data.getLong("userId");
        checkDailyTaskIsOk(userId, TaskIdEnum.SHOP_BUY_NUMBER.getValue());
    }
    public void userSyn(JSONObject data) {
        Long userId = data.getLong("userId");
        checkDailyTaskIsOk(userId, TaskIdEnum.SYN.getValue());
    }

    public void userDts(JSONObject data) {
        Long userId = data.getLong("userId");
        checkDailyTaskIsOk(userId, TaskIdEnum.DTS.getValue());
    }

    public void userLHD(JSONObject data) {
        Long userId = data.getLong("userId");
        checkDailyTaskIsOk(userId, TaskIdEnum.LHD.getValue());
    }

    public void checkRemoveRedAchievement(JSONObject data, String event) {
        Long userId = data.getLong("userId");
        UserAchievement userAchievement = gameService.getUserAchievement(userId.toString());
        JSONArray achievementList = userAchievement.getAchievementList();
        boolean isPush = true;
        for (Object o : achievementList) {
            JSONObject achievement = (JSONObject) o;
            if (achievement.getInteger("status") == 2) {
                isPush = false;
            }
        }
        pushHideRedPoint(userId, event);
    }

    public void checkPowerAchievement(JSONObject data) {
        Long userId = data.getLong("userId");
        Long power = userCacheService.getUserPower(userId);
        data.put("power", power);
        checkAchievement(data, AchievementGroupEnum.CHECK_POWER.getValue());
    }


    public void checkOpenMineAchievement(JSONObject data) {
        checkAchievement(data, AchievementGroupEnum.OPEN_MINE.getValue());
    }

    public void checkUserPetAchievement(JSONObject data) {
        Long userId = data.getLong("userId");
        String itemId = data.getString("itemId");
        if (ItemIdEnum.PET_JUEYING.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_1.getValue());
        }
        if (ItemIdEnum.PET_CHITU.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_2.getValue());
        }
        if (ItemIdEnum.PET_DILU.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_3.getValue());
        }
        if (ItemIdEnum.PET_DAWAN.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_4.getValue());
        }
        if (ItemIdEnum.PET_ZHFD.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_5.getValue());
        }
        if (ItemIdEnum.PET_JINGFAN.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_6.getValue());
        }
        if (ItemIdEnum.PET_ZIXIN.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_7.getValue());
        }
        if (ItemIdEnum.PET_BAIHAO.getValue().equals(itemId)) {
            checkAchievement(data, AchievementGroupEnum.GET_PET_8.getValue());
        }

    }


    //检查主线胜利成就
    public void checkAchievement(JSONObject data, int group) {
        Long userId = data.getLong("userId");
        UserAchievement userAchievement = gameService.getUserAchievement(userId.toString());
        JSONArray achievementList = userAchievement.getAchievementList();
        for (Object o : achievementList) {
            JSONObject achievement = (JSONObject) o;
            if (achievement.getInteger("group") == group) {
                if (group == AchievementGroupEnum.CHECK_POWER.getValue()) {
                    achievement.put("schedule", data.getIntValue("power"));
                } else if (group == AchievementGroupEnum.GET_CARD.getValue()) {
                    int type = data.getIntValue("type");
                    int number = 0;
                    if (type == 1) {
                        number = 1;
                    } else if (type == 2) {
                        number = 2;
                    }
                    achievement.put("schedule", number);
                } else if (group == AchievementGroupEnum.WEAR_SET.getValue()) {
                    int lv = data.getIntValue("setLv");
                    if (achievement.getInteger("schedule") < lv) {
                        achievement.put("schedule", lv);
                    }

                } else if (group == AchievementGroupEnum.LOGIN.getValue()) {
                    if (!userCacheService.userTodayIsLogin(userId)) {
                        achievement.put("schedule", achievement.getIntValue("schedule") + 1);
                    }
                } else if (group == AchievementGroupEnum.LV_UP.getValue()) {
                    int lv = data.getIntValue("lv");
                    if (achievement.getInteger("schedule") < lv) {
                        achievement.put("schedule", lv);
                    }
                } else if (group == AchievementGroupEnum.HAS_QUALITY_CARD_NUMBER2.getValue()) {
                    int i = data.getIntValue("i");
                    if (achievement.getInteger("schedule") < i) {
                        achievement.put("schedule", i);
                    }
                } else if (group == AchievementGroupEnum.DISPATCH_NUMBER.getValue()) {
                    Long number = cardGameCacheService.getUserTodayDispatchNumber(userId);
                    achievement.put("schedule", number);
                } else {
                    achievement.put("schedule", achievement.getIntValue("schedule") + 1);
                }
                if (achievement.getInteger("schedule") >= achievement.getInteger("condition")) {
                    //达到条件
                    if (achievement.getInteger("status") == 0) {
                        achievement.put("status", 2);
                    }
                    if (achievement.getInteger("status") == 2) {
                        pushRedPoint(userId, KafkaEventContext.ACHIEVEMENT);
                    }
                }
                if (group == AchievementGroupEnum.PVP_RANK.getValue()) {
                    if (achievement.getInteger("schedule") <= achievement.getInteger("condition")) {
                        //达到条件
                        if (achievement.getInteger("status") == 0) {
                            achievement.put("status", 2);
                        }
                        if (achievement.getInteger("status") == 2) {
                            pushRedPoint(userId, KafkaEventContext.ACHIEVEMENT);
                        }
                    }
                }
            }
        }
    }


    public void checkDailyTaskIsOk(Long userId, String taskId) {
        synchronized (LockUtil.getlock(userId)) {
            Map userTask = cardGameCacheService.getUserTask(userId);
            boolean push = false;
            UserDailyTaskVo task = (UserDailyTaskVo) userTask.get(taskId);
            if (task.getStatus() == 0) {
                task.setSchedule(task.getSchedule() + 1);
                if (task.getSchedule() == task.getCondition()) {
                    task.setStatus(1);
                    push = true;
                }
                userDailyTaskService.updateUserTask(userId, JSONArray.copyOf(userTask.values()));
                cardGameCacheService.updateUserDailyTaskStatus(userId, taskId, task);
                if (push) {
                    pushRedPoint(userId, KafkaEventContext.DAILY_TASK);
                }
            }
        }
    }

    public void checkDailyTaskIsOk(Long userId, String taskId, int number) {
        synchronized (LockUtil.getlock(userId)) {
            Map userTask = cardGameCacheService.getUserTask(userId);
            boolean push = false;
            UserDailyTaskVo task = (UserDailyTaskVo) userTask.get(taskId);
            if (task != null && task.getStatus() == 0) {
                task.setSchedule(task.getSchedule() + number);
                if (task.getSchedule() >= task.getCondition()) {
                    task.setStatus(1);
                    task.setSchedule(task.getCondition());
                    push = true;
                }
                userDailyTaskService.updateUserTask(userId, JSONArray.copyOf(userTask.values()));
                cardGameCacheService.updateUserDailyTaskStatus(userId, taskId, task);
                if (push) {
                    pushRedPoint(userId, KafkaEventContext.DAILY_TASK);
                }
            }
        }
    }

    public void loginCheck(JSONObject data) {
        //登录检查
        //登录时的每日任务判断和红点推送
        userLoginCheckRedPoint(data);
        Long userId = data.getLong("userId");
        checkDailyTaskIsOk(userId, TaskIdEnum.LOGIN.getValue());

    }


    public void userReceiveDailyTask(JSONObject data) {
        //领取每日任务奖励 移除红点 遍历玩家的每日任务 和AP 判断是是否有未领取的
        Long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            Map userTask = cardGameCacheService.getUserTask(userId);
            Collection values = userTask.values();
            boolean push = true;
            for (Object value : values) {
                UserDailyTaskVo vo = (UserDailyTaskVo) value;
                if (vo.getStatus() == 1) {
                    push = false;
                }
            }
            if (push) {
                pushHideRedPoint(userId, KafkaEventContext.DAILY_TASK);
            }
        }
    }

    public void checkAchievementAllTask(JSONObject data) {
        checkAchievement(data, AchievementGroupEnum.DAILY_TASK_AP_100.getValue());
    }

    public void gameSendMail(JSONObject data) {
        JSONArray mailList = data.getJSONArray("array");
        for (Object o : mailList) {
            JSONObject mail = (JSONObject) o;
            Long userId = mail.getLong("userId");
            pushRedPoint(userId, KafkaEventContext.MAIL);
        }
    }

    public void sysSendMail(JSONObject data) {
        JSONArray userArr = data.getJSONArray("userArr");
        for (Object o : userArr) {
            String toId = o.toString();
            User toUser = userCacheService.getUserInfoByUserNo(toId);
            if (toUser == null) {
                continue;
            }
            Long userId = toUser.getId();
            pushRedPoint(userId, KafkaEventContext.MAIL);
        }
    }


    public void readMail(JSONObject data) {
        Long userId = data.getLong("userId");
        List<Mail> toMyEmail = mailService.findToMyEmailNoRead(userId);
        if (toMyEmail.size() == 0) {
            pushHideRedPoint(userId, KafkaEventContext.MAIL);
        }
    }


    public void userLoginCheckRedPoint(JSONObject data) {
        Long userId = data.getLong("userId");
        Map userRedPointInfo = cardGameCacheService.getUserRedPointInfo(userId);
        if (userRedPointInfo != null) {
            if (userRedPointInfo.containsKey(KafkaEventContext.MAIL)) {
                pushRedPoint(userId, KafkaEventContext.MAIL, userRedPointInfo.get(KafkaEventContext.MAIL));
            }

            if (userRedPointInfo.containsKey(KafkaEventContext.DAILY_TASK)) {
                pushRedPoint(userId, KafkaEventContext.DAILY_TASK, userRedPointInfo.get(KafkaEventContext.DAILY_TASK));
            }
            if (userRedPointInfo.containsKey(KafkaEventContext.ACHIEVEMENT)) {
                pushRedPoint(userId, KafkaEventContext.ACHIEVEMENT, userRedPointInfo.get(KafkaEventContext.ACHIEVEMENT));
            }

        }
    }

    private void pushRedPoint(Long userId, String event) {
        String serverIdByUserId = managerSocketService.getServerIdByUserId(userId);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("event", event);
        JSONArray array = new JSONArray();
        pushDate.put("data", array);
        cardGameCacheService.setUserRedPointInfo(userId, event, array);
        Push.push(PushCode.redPointShow, serverIdByUserId, pushDate);
    }

    private void pushHideRedPoint(Long userId, String event) {
        String serverIdByUserId = managerSocketService.getServerIdByUserId(userId);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("event", event);
        JSONArray array = new JSONArray();
        pushDate.put("data", array);
        cardGameCacheService.removeUserRedPointInfo(userId, event);
        Push.push(PushCode.redPointHide, serverIdByUserId, pushDate);
    }

    private void pushHideRedPoint(Long userId, String event, Object object) {
        String serverIdByUserId = managerSocketService.getServerIdByUserId(userId);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("event", event);
        pushDate.put("data", object);
        cardGameCacheService.removeUserRedPointInfo(userId, event);
        Push.push(PushCode.redPointHide, serverIdByUserId, pushDate);
    }

    private void pushRedPoint(Long userId, String event, Object object) {
        String serverIdByUserId = managerSocketService.getServerIdByUserId(userId);
        JSONObject pushDate = new JSONObject();
        pushDate.put("userId", userId);
        pushDate.put("event", event);
        pushDate.put("data", object);
        cardGameCacheService.setUserRedPointInfo(userId, event, object);
        Push.push(PushCode.redPointShow, serverIdByUserId, pushDate);
    }


    public String getEquIdByPosition(int position, PlayerCard card) {
        if (card == null) {
            return null;
        }
        if (position == 1) {
            return String.valueOf(card.getEquA());
        } else if (position == 2) {
            return String.valueOf(card.getEquB());
        } else if (position == 3) {
            return String.valueOf(card.getEquC());
        } else if (position == 4) {
            return String.valueOf(card.getEquD());
        } else if (position == 5) {
            return String.valueOf(card.getPetId());
        } else if (position == 0) {
            return String.valueOf(card.getArtifactId());
        }
        return null;
    }


    public void shutdown() {
        consumer.close();
        executorService.shutdown();
    }
}