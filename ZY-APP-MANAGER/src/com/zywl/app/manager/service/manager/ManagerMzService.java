package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;
import com.zywl.app.base.bean.DicMzItem;
import com.zywl.app.base.bean.MzBuyRecord;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.DicMzItemService;
import com.zywl.app.defaultx.service.MzBuyRecordService;
import com.zywl.app.defaultx.service.MzUserItemService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.KafkaEventContext;
import com.zywl.app.manager.context.KafkaTopicContext;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@ServiceClass(code = MessageCodeContext.MINE)
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

        return null;
    }

}
