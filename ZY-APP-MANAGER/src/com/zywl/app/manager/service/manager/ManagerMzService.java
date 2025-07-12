package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.DicMzItem;
import com.zywl.app.base.bean.MzTrad;
import com.zywl.app.base.bean.MzUserItem;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.*;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;


@Service
@ServiceClass(code = MessageCodeContext.MZ)
public class ManagerMzService extends BaseService {

    @Autowired
    private DicMzItemService dicMzItemService;

    @Autowired
    private MzUserItemService mzUserItemService;


    @Autowired
    private ManagerGameBaseService managerGameBaseService;

    @Autowired
    private MzBuyRecordService mzBuyRecordService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private MzTradService mzTradService;


    @Transactional
    @ServiceMethod(code = "001", description = "获取商店信息")
    public Object getShopInfo(ManagerSocketServer adminSocketServer, JSONObject params) {
        List<DicMzItem> canBuy = dicMzItemService.findCanBuy();
        return canBuy;
    }

    @Transactional
    @ServiceMethod(code = "002", description = "商店购买慢涨道具")
    public Object buy(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"), params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)) {
            DicMzItem dicMzItem = dicMzItemService.findById(id);
            if (dicMzItem == null) {
                throwExp("道具不存在");
            }
            if (dicMzItem.getIsShop() == 0) {
                throwExp("该道具不可购买");
            }
            if (dicMzItem.getShopNumber() <= 0) {
                throwExp("道具已售罄");
            }
            //检查资产
            managerGameBaseService.checkBalance(userId, dicMzItem.getPrice(), UserCapitalTypeEnum.currency_2);
            String orderNo = OrderUtil.getOrder5Number();
            //添加购买记录
            Long dataId = mzBuyRecordService.addShopBuyRecord(userId, 1, orderNo, dicMzItem.getPrice());
            //添加玩家的道具
            mzUserItemService.addMzItem(userId, dicMzItem.getId(), null, null);
            //减少资产
            userCapitalService.subUserBalanceByBuyMz(userId, dicMzItem.getPrice(), orderNo, dataId);
            //推送资产变动后
            managerGameBaseService.pushCapitalUpdate(userId, UserCapitalTypeEnum.currency_2.getValue());
            JSONObject result = new JSONObject();
            return result;
        }
    }

    @Transactional
    @ServiceMethod(code = "003", description = "升级慢涨道具")
    public Object up(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"),params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            MzUserItem byId = mzUserItemService.findById(id);
            if (byId==null){
                throwExp("道具不存在");
            }
            if(!Objects.equals(byId.getUserId(), userId) || byId.getStatus()==-1){
                throwExp("非法请求");
            }
            if (byId.getStatus()==1){
                throwExp("该道具正在修复中");
            }
            if (byId.getStatus()==2){
                throwExp("已经修复过了，无法连续修复");
            }
            byId.setStatus(1);
            byId.setUpTime(new Date());
            byId.setUpEndTime(DateUtil.getDateByDay(7));
            mzUserItemService.updateMzUserItem(byId);
        }
        return new JSONObject();
    }


    @Transactional
    @ServiceMethod(code = "004", description = "领取升级完成的道具")
    public Object receive(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"),params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            MzUserItem byId = mzUserItemService.findById(id);
            if (byId==null){
                throwExp("道具不存在");
            }
            if(!Objects.equals(byId.getUserId(), userId) || byId.getStatus()!=1){
                throwExp("非法请求");
            }
            //升级道具ID 变成新道具
            byId.setMzItemId(byId.getMzItemId()+1);
            User user = userCacheService.getUserInfoById(userId);
            byId.setLastUserNo(user.getUserNo());
            byId.setLastUserName(user.getName());
            byId.setStatus(2);
            mzUserItemService.updateMzUserItem(byId);
            return new JSONObject();
        }
    }

    @Transactional
    @ServiceMethod(code = "005", description = "上架慢涨道具")
    public Object sell(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"),params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            MzUserItem byId = mzUserItemService.findById(id);
            if (byId==null){
                throwExp("道具不存在");
            }
            if(!Objects.equals(byId.getUserId(), userId) ){
                throwExp("非法请求");
            }
            if ( byId.getStatus()!=2){
                throwExp("当前不可上架该道具");
            }
            byId.setStatus(3);
            mzUserItemService.updateMzUserItem(byId);
            DicMzItem dicMzItem = dicMzItemService.findById(id);
            BigDecimal sellPrice = dicMzItem.getPrice();
            BigDecimal fee = sellPrice.multiply(new BigDecimal("0.3"));
            mzTradService.addMzTrad(byId.getMzItemId(),byId.getId(),userId,sellPrice,fee,sellPrice.subtract(fee));
        }
        return new JSONObject();
    }

    @Transactional
    @ServiceMethod(code = "007", description = "交易行购买慢涨道具")
    public Object tradBuy(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"),params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(id)){
            MzTrad trad = mzTradService.findById(id);
            if (trad.getStatus()!=1){
                throwExp("已下架或被买走，清刷新后查看");
            }
            if (Objects.equals(trad.getSellUserId(), userId)){
                throwExp("不能购买自己上架的道具");
            }
            //检查货币余额
            managerGameBaseService.checkBalance(userId,trad.getSellPrice(),UserCapitalTypeEnum.currency_2);
            //更改订单状态
            trad.setStatus(0);
            //更改DB
            mzTradService.updateTrad(trad);
            //查询道具 更改道具所属主人 更改道具未可升级可交易状态
            MzUserItem byId = mzUserItemService.findById(trad.getUserItemId());
            if (byId==null){
                throwExp("道具不存在");
            }
            byId.setStatus(0);
            byId.setUserId(userId);
            mzUserItemService.updateMzUserItem(byId);
            //更改余额
            userCapitalService.subUserBalanceByMzTradingBuy(userId,trad.getSellPrice(),trad.getId());
            //推送余额
            managerGameBaseService.pushCapitalUpdate(userId,UserCapitalTypeEnum.currency_2.getValue());
        }
        return new JSONObject();
    }


    @Transactional
    @ServiceMethod(code = "008", description = "交易行下架道具")
    public Object cancelTrad(ManagerSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("id"),params.get("userId"));
        Long id = params.getLongValue("id");
        Long userId = params.getLong("userId");
        synchronized (LockUtil.getlock(userId)){
            MzTrad trad = mzTradService.findById(id);
            if (trad.getStatus()!=1){
                throwExp("已下架或被买走，清刷新后查看");
            }
            if (!Objects.equals(trad.getSellUserId(), userId)){
                throwExp("非法请求");
            }
            trad.setStatus(-1);
            mzTradService.updateTrad(trad);
            MzUserItem byId = mzUserItemService.findById(trad.getUserItemId());
            byId.setStatus(2);
            mzUserItemService.updateMzUserItem(byId);
            return new JSONObject();
        }
    }
}
