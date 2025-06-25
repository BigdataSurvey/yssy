package com.zywl.app.server.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.live.app.ws.enums.TargetSocketType;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Executer;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.card.DicMine;
import com.zywl.app.base.bean.card.DicMineHoe;
import com.zywl.app.base.bean.card.UserMine;
import com.zywl.app.base.bean.vo.card.UserMineVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.service.card.DicMineHoeService;
import com.zywl.app.defaultx.service.card.DicMineService;
import com.zywl.app.defaultx.service.card.UserMineService;
import com.zywl.app.server.context.MessageCodeContext;
import com.zywl.app.server.socket.AppSocket;
import com.zywl.app.server.util.RequestManagerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = MessageCodeContext.ROLE)
public class ServerRoleService extends BaseService {


    public static final Map<String, DicMine> DIC_MINE = new ConcurrentHashMap<>();

    public static Map<String, DicMineHoe> DIC_MINE_HOE = new ConcurrentHashMap<>();


    @Autowired
    private DicMineHoeService dicMineHoeService;
    @Autowired
    private DicMineService dicMineService;

    @Autowired
    private UserCacheService userCacheService;

    @PostConstruct
    public void _ServerMineService() {
        initMine();
        initMineHoe();
    }

    public void initMine() {
        List<DicMine> list = dicMineService.findAllMine();
        list.forEach(e -> DIC_MINE.put(String.valueOf(e.getId()), e));
    }

    public void initMineHoe() {
        logger.info("初始化矿场锄头相关信息");
        List<DicMineHoe> list = dicMineHoeService.findAllMineHoe();
        list.forEach(e -> DIC_MINE_HOE.put(String.valueOf(e.getItemId()), e));
        logger.info("初始化矿场锄头信息完成,加载数据数量：" + DIC_MINE_HOE.size());
    }

    @Autowired
    private UserMineService userMineService;

    @Transactional
    @ServiceMethod(code = "001", description = "进入角色工作界面信息")
    public Object getInfo(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        User user = userCacheService.getUserInfoById(userId);
        if (user == null) {
            throwExp("用户信息异常");
        }
        List<UserMine> userMineByUserId = userMineService.findUserMineByUserId(userId);
        List<UserMineVo> userMineVos = new ArrayList<>();
        //TODO 判断该矿产是否要补充内容
        /*
          是否在探索中
          探索结束时间
          当前收益
          剩余次数
          单次收益
          当前书境总收益
          探索单次花费
          探索使用的道具
         */
        for (UserMine userMine : userMineByUserId) {
            checkMine(userMine);
            UserMineVo vo = new UserMineVo();
            BeanUtils.copy(userMine, vo);
            vo.setMinEndTime(userMine.getMinEndTime() == null ? 0L : userMine.getMinEndTime().getTime());
            vo.setLastMineTime(userMine.getLastMineTime() == null ? 0l : userMine.getLastMineTime().getTime());
            int hour = 24;
            vo.setHour(hour);
            vo.setUseItem(Long.parseLong(DIC_MINE.get(userMine.getMineId().toString()).getMiningItem()));
            int useNumber = 10;
            vo.setUseNumber(useNumber);
            userMineVos.add(vo);
        }
        return userMineVos;
    }


    public void checkMine(UserMine userMine) {
            /*
                先判断是否在产出
                如果产出 判断此刻是否已经到了结束产出的时间 如果是的话 更改是否产出的字段 更新产出内容 更新产出时间
                如果没到结束产出的时间 不更改是否产出 但更新产出内容 更新产出时间
             */
        if (userMine.getIsMining() == 1) {
            Date endDate;
            if (userMine.getMinEndTime().getTime() > System.currentTimeMillis()) {
                //到现在也没产完
                endDate = new Date();
            } else {
                //到现在产完了
                endDate = userMine.getMinEndTime();
                userMine.setIsMining(0);
            }
            long subHour = DateUtil.getSubHour(userMine.getLastOutputTime(), endDate);
            if (subHour >= 1) {
                //产出
                JSONArray reward = DIC_MINE.get(userMine.getMineId().toString()).getReward();
                JSONArray addReward = new JSONArray();
                int all = 0;
                for (Object o : reward) {
                    JSONObject item = (JSONObject) o;
                    String id = item.getString("id");
                    int number = item.getIntValue("number");
                    JSONObject add = new JSONObject();
                    add.put("type", item.getIntValue("type"));
                    add.put("id", id);
                    add.put("number", number * subHour);
                    all = (int) (number*subHour);
                    addReward.add(add);
                }
                userMine.setOutput(userMine.getOutput()+all);
                //更新产出完成，更新产出时间
                userMine.setLastOutputTime(DateUtil.getDateByHour(userMine.getLastOutputTime(), (int) subHour));
                //更新完成 更新数据库
                userMineService.updateUserMine(userMine);
            }
        }
    }

    @ServiceMethod(code = "002", description = "开通书境")
    public Object openMine(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"));
        int index = params.getIntValue("index");
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        if (index < 0 || index > 7) {
            throwExp("非法请求");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("7001001", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "003", description = "探索书境")
    public Object lvUpMine(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"));
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId",userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("7001002", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "004", description = "领取书境产出")
    public Object gather(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        checkNull(params.get("index"));
        int index = params.getIntValue("index");
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        if (index < 0 || index > 7) {
            throwExp("非法请求");
        }
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("7001003", params).build(), new RequestManagerListener(appCommand));
        return async();
    }

    @ServiceMethod(code = "005", description = "一键领取收益")
    public Object gatherAll(final AppSocket appSocket, Command appCommand, JSONObject params) {
        checkNull(params);
        Long userId = appSocket.getWsidBean().getUserId();
        params.put("userId", userId);
        Executer.request(TargetSocketType.manager, CommandBuilder.builder().request("7001004", params).build(), new RequestManagerListener(appCommand));
        return async();
    }



}
