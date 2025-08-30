package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.UserPit;
import com.zywl.app.base.bean.UserProcess;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.DicZytService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserIncomeService;
import com.zywl.app.defaultx.service.UserProcessService;
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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.ZYT)
public class ManagerZytService extends BaseService {

    @Autowired
    private UserProcessService userProcessService;


    @Autowired
    private UserIncomeService userIncomeService;

    @Autowired
    private DicZytService dicZytService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    private static List<Map<String, JSONArray>> ytInfo = new ArrayList<>();


    @PostConstruct
    public void _ServerMineService() {
        init();
    }

    public void init() {
        ytInfo = dicZytService.findDicZytList();
    }

    @Transactional
    @ServiceMethod(code = "001", description = "激活")
    public Object open(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            JSONObject jsonObject = new JSONObject();
            //初始化上次领取时间 上次查看时间 未领取
            jsonObject.put("userId", userId);
            //扣取通宝 开通金矿洞需要“1000通宝”，开通银矿洞需要“500通宝”
            List<UserProcess> userProcesses =  userProcessService.findByUserId(userId);
            if(userProcesses!=null){
                throwExp("已经开通过九层妖塔");
            }
            Integer i = userProcessService.addUserProecss(params);
            managerGameBaseService.checkBalance(userId, BigDecimal.valueOf(500), UserCapitalTypeEnum.yyb);
            userCapitalService.subUserBalanceByOpenPit(userId, BigDecimal.valueOf(500));
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            return i;
        }
    }

    @Transactional
    @ServiceMethod(code = "002", description = "抽奖")
    public Object draw(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        String type  = params.getString("type");
        Random random = new Random();
        int randomNumber = 0;
        synchronized (LockUtil.getlock(userId)) {
            //type1为摇骰子,2为扑克牌
            if("1".equals(type)){
                // 生成1-6的随机数
                randomNumber =  random.nextInt(6) + 1;
            }else{
                randomNumber =  random.nextInt(3) + 1;
            }
        }
        return randomNumber;
    }

    @Transactional
    @ServiceMethod(code = "003", description = "爆炸")
    public Object boom(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        String type  = params.getString("type");
        Random random = new Random();
        int randomNumber = 0;
        synchronized (LockUtil.getlock(userId)) {
            //type1为摇骰子,2为扑克牌
            if("1".equals(type)){
                // 生成1-6的随机数
                randomNumber =  random.nextInt(6) + 1;
            }else{
                randomNumber =  random.nextInt(3) + 1;
            }
        }
        return randomNumber;
    }

    @Transactional
    @ServiceMethod(code = "004", description = "进入页面")
    public Object see(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        JSONObject jsonObject = new JSONObject();
        synchronized (LockUtil.getlock(userId)) {
            //查看当前用户当前的进度条和塔层

        }
        return jsonObject;
    }
















}
