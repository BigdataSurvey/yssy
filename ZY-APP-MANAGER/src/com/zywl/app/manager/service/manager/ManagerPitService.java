package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.bean.Command;
import com.zywl.app.base.bean.PitRecord;
import com.zywl.app.base.bean.PitUserParent;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserPit;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.service.PitRecordService;
import com.zywl.app.defaultx.service.PitService;
import com.zywl.app.defaultx.service.PitUserParentService;
import com.zywl.app.defaultx.service.UserPitService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ServiceClass(code = MessageCodeContext.USER_PIT)
public class ManagerPitService extends BaseService {


    @Autowired
    private PitService pitService;

    @Autowired
    private UserPitService userPitService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private PitUserParentService pitUserParentService;

    @Autowired
    private PlayGameService gameService;
    @Autowired
    private PitRecordService pitRecordService;


    @PostConstruct
    public void _ServerMineService() {
        init();
    }

    public void init() {
    }

    @Transactional
    @ServiceMethod(code = "001", description = "开通")
    public Object open(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        JSONObject jsonObject = new JSONObject();
        //初始化上次领取时间 上次查看时间 未领取
        jsonObject.put("userId", userId);
        jsonObject.put("pitId", params.getLong("pitId"));
        //扣取通宝 开通金矿洞需要“1000通宝”，开通银矿洞需要“500通宝”
        List<UserPit> pitId = userPitService.findInitInfo(jsonObject);
        if(pitId.size()>0 ){
            throwExp("已经开通过此矿洞");
        }
        if ("1".equals(params.getString("pitId"))){
            gameService.checkUserItemNumber(userId, String.valueOf(2),1000);
            gameService.updateUserBackpack(userId, "2", -1000, LogUserBackpackTypeEnum.buy_pit);
        }else{
            gameService.checkUserItemNumber(userId, String.valueOf(2),500);
            gameService.updateUserBackpack(userId, "2", -500, LogUserBackpackTypeEnum.buy_pit);
        }
        int i = userPitService.insertUserPit(jsonObject);
        return i;
    }


    @Transactional
    @ServiceMethod(code = "002", description = "进入矿洞页面")
    public Object bindingParent(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        User user = userCacheService.getUserInfoById(userId);
        PitUserParent parentId = pitUserParentService.findParentByUserId(userId);
        JSONObject jsonObject1 = new JSONObject();
        if (parentId == null) {
            jsonObject1.put("bingingStatus",0);
            return jsonObject1;
        }
        if (user == null) {
            throwExp("用户信息异常");
        }
        //查询当前用户是否有数据，是否已开通，是否已查看
        List<UserPit> userPitList = userPitService.findInitInfo(params);
        JSONObject jsonObject = new JSONObject();
                /*
                  1.是否开通
                  2.收益几天未领取
                  3.上次查看时
                 */
        ArrayList<UserPit> newUserPitList = new ArrayList<>();
        if(userPitList.size()==0){
            throwExp("未开通此矿山，去开通！");
        }
        jsonObject.put("openStatus",1);

        for (UserPit userPit : userPitList) {
            long targetDate =  System.currentTimeMillis();
            if( (System.currentTimeMillis() - userPit.getEndTime().getTime())/1000/84600 >=0){
                 targetDate = userPit.getEndTime().getTime();
            }
            //根据上一次计算时间去计算未领取收益,相差天数
            //获取上次领取时间去计算收益递增数量值，加到多少了
            long diffLastReceiveTime = ( targetDate- userPit.getLastReceiveTime().getTime() )/ 1000 / 86400;
            long diffLastLookTime = (targetDate - userPit.getLastLookTime().getTime()) / 1000 / 86400;
            userPit.setDays((int) diffLastReceiveTime);
            int start = 95;      // 起始值
            int increment = 1;   // 每次递增的值
            int steps = (int) diffLastReceiveTime;// 递增次数
            Map<String, Integer> numberMap = new HashMap<>();
            for (int i = 0; i < steps; i++) {
                int currentValue = start + (i * increment);
                Integer a = i + 1;
                numberMap.put(a+"", currentValue);
            }
            //获取未领取收益
            if (userPit.getUnReceive() != null) {
                for (Object o : userPit.getUnReceive()) {
                    JSONObject raword = (JSONObject) o;
                    Integer number = raword.getIntValue("number");
                    long needCount = diffLastReceiveTime - diffLastLookTime;
                    Integer b = Math.toIntExact(needCount + 1);
                    Integer startNumber = numberMap.get(b+""); //继续新增的起始值
                    Integer sumNumber = 0;    // 累加总和
                    for (int i = 0; i < diffLastLookTime; i++) {
                        sumNumber += startNumber + i; // 依次累加 95, 96, 97, 98, 99
                    }
                    //赋值未领取收益
                    raword.put("number", number + sumNumber);
                }
            }
            userPit.setLastLookTime(new Date());
            newUserPitList.add(userPit);
            if( (System.currentTimeMillis() -userPit.getOpenTime().getTime())/1000/84600 ==7 ){
                userPit.setIstk(1);
            }else {
                userPit.setIstk(0);
            }
        }

        jsonObject.put("userPit",userPitList);
        userPitService.batchUpdateNumber(newUserPitList);
        return jsonObject;
    }

    @Transactional
    @ServiceMethod(code = "003", description = "领取收益")
    public Object receiveNumber(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");

        JSONObject jsonObject = new JSONObject();
        //初始化上次领取时间 上次查看时间 未领取
        jsonObject.put("userId", userId);
        jsonObject.put("pitId", params.getString("pitId"));
        List<UserPit> userPitList = userPitService.findInitInfo(params);
        List<PitRecord> pitRecordList = new ArrayList<>();
        PitRecord pitRecord = null;
        for (UserPit userPit : userPitList) {
            String orderNo = OrderUtil.getOrder5Number();
            pitRecord = new PitRecord();
            pitRecord.setUserId(Math.toIntExact(userId));
            pitRecord.setCrteTime(new Date());
            pitRecord.setOrderNo(orderNo);
            pitRecord.setReceiveTime(new Date());
            pitRecord.setPitId(params.getIntValue("pitId"));
            for (Object o :userPit.getUnReceive()) {
                JSONObject newUserPit = (JSONObject) o;
                Integer number =  newUserPit.getIntValue("number");
                pitRecord.setNumber(number);

                //计算上级以及上上级收益
                BigDecimal createParentAmount = BigDecimal.ZERO;
                BigDecimal createGrandfaAmount = BigDecimal.ZERO;

                    if (1 == userPit.getPitId()) {
                        createParentAmount = createParentAmount.add(BigDecimal.valueOf(number).multiply(BigDecimal.valueOf(200)));
                        createGrandfaAmount = createGrandfaAmount.add(BigDecimal.valueOf(number).multiply(BigDecimal.valueOf(50)));
                        params.put("createParentAmount", createParentAmount);
                        params.put("createGrandfaAmount", createGrandfaAmount);
                    } else {
                        createParentAmount = createParentAmount.add(BigDecimal.valueOf(number).multiply(BigDecimal.valueOf(100)));
                        createGrandfaAmount = createGrandfaAmount.add(BigDecimal.valueOf(number).multiply(BigDecimal.valueOf(25)));
                        params.put("createParentAmount", createParentAmount);
                        params.put("createGrandfaAmount", createGrandfaAmount);
                    }
                    pitUserParentService.updateParent(params);
                gameService.updateUserBackpack(userId, "2", +number, LogUserBackpackTypeEnum.receive_number);
            }
            pitRecordList.add(pitRecord);
        }
        //批量新增領取记录
        pitRecordService.batchAddRecord(pitRecordList);
        int i = userPitService.receiveNumber(jsonObject);
        return i;
    }

    @Transactional
    @ServiceMethod(code = "004", description = "绑定")
    public int binding(ManagerSocketServer adminSocketServer, Command appCommand, JSONObject params) {
        checkNull(params);
        /**
         * 绑定成功之后赠送上级和上上级收益
         * 赋值上级id 查询父级的父级 赋值上上级id
         */
        Long userId = params.getLong("userId");
        User parentUser = userCacheService.getUserInfoByUserNo(params.getString("userNo"));
        if(Objects.equals(parentUser.getId(), userId)){
            throwExp("不能绑定自己的id");
        }
        if (parentUser == null) {
            throwExp("父级id异常");
        }
        long parentId =  parentUser.getId();
        params.put("parentId",parentId);
        JSONObject jsonObject = new JSONObject();
        //初始化上次领取时间 上次查看时间 未领取
        jsonObject.put("userId", userId);
        //查询上上级id
        PitUserParent pitUserParent = pitUserParentService.findParentByUserId(parentId);
        if(null != pitUserParent){
              params.put("pitGrandfaId", pitUserParent.getPitParentId());
        }
        int i = pitUserParentService.insertPitUserParent(params);
        return i ;
    }

    @Transactional
    @ServiceMethod(code = "005", description = "退款")
    public Object refund(ManagerSocketServer adminSocketServer, JSONObject params) {
        JSONObject jsonObject = new JSONObject();
        checkNull(params);
        Long userId = params.getLong("userId");
        Long pitId = params.getLong("pitId");
        //初始化上次领取时间 上次查看时间 未领取
        List<UserPit> userPitList = userPitService.findOpenPitTypeByUserId(params);
        for (UserPit userPit : userPitList) {
            //如果上次领取时间-开通时间 =0 未领取过
            long diffLastReceiveTime = (userPit.getLastReceiveTime().getTime() - userPit.getOpenTime().getTime()) / 1000 / 86400;
            if(diffLastReceiveTime > 0 ){
                throwExp("已领取过奖励，不可退款");
            }
            //退换全部通宝
            if ("1".equals(userPit.getPitId())){
                gameService.updateUserBackpack(userId, "2", +1000, LogUserBackpackTypeEnum.pit_refund);
            }else{
                gameService.updateUserBackpack(userId, "2", +500, LogUserBackpackTypeEnum.pit_refund);
            }
            jsonObject.put("refundStatus",1);
        }
        return  jsonObject;
    }
    @Transactional
    @ServiceMethod(code = "006", description = "领取记录")
    public JSONObject receiveRecord(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        //初始化上次领取时间 上次查看时间 未领取
        List<PitRecord> userPitList = pitRecordService.findPitRecord(params);
        JSONObject result = new JSONObject();
        result.put("myRecord", userPitList);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "007", description = "直接矿工")
    public JSONObject getSuborList(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        JSONObject result = new JSONObject();
        //根据userId查询我的下级矿工
        List<PitUserParent> suborList = pitUserParentService.findSubor(params);
        if(suborList.size()==0){
            return  result;
        }
        JSONArray array = new JSONArray();
        for (PitUserParent pitUserParent : suborList) {
            JSONObject jsonObject = new JSONObject();
            User user = userCacheService.getUserInfoById(pitUserParent.getUserId().longValue());
            jsonObject.put("userId",user.getId());
            jsonObject.put("userNo",user.getUserNo());
            jsonObject.put("name",user.getName());
            jsonObject.put("headImageUrl",user.getHeadImageUrl());
            //根据两个集合查询出我下级员工的所有收益
            jsonObject.put("number",pitUserParent.getCreateParentAmount());
            array.add(jsonObject);
        }
        result.put("suborList", array);
        return result;
    }

    @Transactional
    @ServiceMethod(code = "008", description = "间接矿工")
    public JSONObject getIndirSuborList(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        Long userId = params.getLong("userId");
        //根据userId查询我的下下级矿工
        List<PitUserParent> indirSuborList = pitUserParentService.findIndirSubor(params);
        JSONArray array = new JSONArray();
        for (PitUserParent pitUserParent : indirSuborList) {
            JSONObject jsonObject = new JSONObject();
            User user = userCacheService.getUserInfoById(pitUserParent.getUserId().longValue());
            jsonObject.put("userId",user.getId());
            jsonObject.put("userNo",user.getUserNo());
            jsonObject.put("name",user.getName());
            jsonObject.put("headImageUrl",user.getHeadImageUrl());
            //根据两个集合查询出我下级员工的所有收益
            jsonObject.put("number",pitUserParent.getCreateGrandfaAmount());
            array.add(jsonObject);
        }
        JSONObject result = new JSONObject();
        result.put("indirSuborList", array);
        return result;
    }
}
