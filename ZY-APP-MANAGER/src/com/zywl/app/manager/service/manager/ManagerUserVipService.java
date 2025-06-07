package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.*;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.constant.KafkaEventContext;
import com.zywl.app.base.constant.KafkaTopicContext;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.DicVipService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserVipService;
import com.zywl.app.defaultx.service.VipReceiveRecordService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.USER_VIP)
public class ManagerUserVipService extends BaseService {

    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private UserCapitalCacheService userCapitalCacheService;
    @Autowired
    private UserVipService userVipService;

    @Autowired
    private VipReceiveRecordService vipReceiveRecordService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private ManagerSocketService managerSocketService;
    @Autowired
    private DicVipService dicVipService;
    @Autowired
    private PlayGameService playGameService;
    private final static Map<String, DicVip> DIC_VIP_MAP = new ConcurrentHashMap<>();
    private final static String RECEIVED = "1";
    private final static String UNRECEIVE = "0";

    @PostConstruct
    public void _ServerUserVipService() {
        initDicVip();
    }

    public void initDicVip(){
        List<DicVip> allVip = dicVipService.findAllVip();
        allVip.forEach(e->DIC_VIP_MAP.put(String.valueOf(e.getLv()),e));
    }
    /**
     * 新增经验
     */
    public void addExper(long userId, BigDecimal rechargeAmount) {
        //vip等级
        UserVip uservip = userVipService.findRechargeAmountByUserId(userId);
        //充值金额（经验）
        uservip.setRechargeAmount(uservip.getRechargeAmount().add(rechargeAmount));
        //获取当前等级vip信息
        DicVip dicVip = PlayGameService.DIC_VIP_MAP.get(String.valueOf(uservip.getVipLevel()));
        //如果当前的经验大于了当前等级的最大经验  就是升级了  遍历map判断升到几级了
        if (Integer.parseInt(uservip.getRechargeAmount().toString()) > dicVip.getEndExp()) {
            Collection<DicVip> values = PlayGameService.DIC_VIP_MAP.values();
            for (DicVip value : values) {
                if (uservip.getRechargeAmount().compareTo(new BigDecimal(value.getBeginExp())) > 0
                        && uservip.getRechargeAmount().compareTo(new BigDecimal(value.getEndExp())) < 0) {
                    uservip.setVipLevel(value.getLv());
                    break;
                }
            }
        }
        userVipService.updateUserVipInfo(uservip);
    }

    @Transactional
    @ServiceMethod(code = "011", description = "领取vip礼包")
    public  JSONObject receiveUserVipGift(JSONObject data) throws Exception {
        checkNull(data);
        long userId = data.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {UserVip userVip = userVipService.findRechargeAmountByUserId(userId);
        long vipLevel = userVip.getVipLevel();
        JSONArray reward = DIC_VIP_MAP.get(String.valueOf(vipLevel)).getReward();
        String orderNo = OrderUtil.getOrder5Number();
        synchronized (LockUtil.getlock(userId.toString())) {
            List<VipReceiveRecord> vipReceiveRecord = vipReceiveRecordService.findVipReceiveRecordByLevel(userId, vipLevel);
            if (vipReceiveRecord.size() > 0) {
                throwExp("已领取过该奖励");
            }

            //获取当前用户已经当前用户等级，新增一条记录到记录表
            long id = vipReceiveRecordService.addVipReceiveRecord(Long.valueOf(userId), orderNo, vipLevel, reward.toString(), new Date(), new Date());


            long id = vipReceiveRecordService.addVipReceiveRecord(userId, orderNo, vipLevel, reward.toString(), new Date(), new Date());
            playGameService.addReward(userId,reward,LogCapitalTypeEnum.VIP_RECEIVE);
            JSONObject result = new JSONObject();
            result.put("id", id);
            return result;
        }


    }
    public void pushCapitalUpdate(Long userId, int capitalType) {
        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(userId, capitalType);
        JSONObject pushData = new JSONObject();
        pushData.put("userId", userId);
        pushData.put("capitalType", capitalType);
        pushData.put("balance", userCapital.getBalance());
        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(userId), pushData);
    }
}