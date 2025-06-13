package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.RechargeOrder;
import com.zywl.app.base.bean.TsgPayOrder;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.RechargeStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TsgPayOrderService extends DaoService {

    public TsgPayOrderService() {
        super("TsgPayOrderMapper");
    }

    @Transactional
    public TsgPayOrder addOrder(Long userId, String orderNo, Long productId, BigDecimal price, Date expireTime,int channel) {
        TsgPayOrder order = new TsgPayOrder();
        order.setOrderNo(orderNo);
        order.setStatus(0);
        order.setChannel(channel);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setPrice(price);
        order.setCreateTime(new Date());
        order.setExpireTime(expireTime);
        save(order);
        return order;
    }


    public List<RechargeOrder> findAllRechargeOrder() {
        return findAll();
    }

    @Transactional
    public int rechargeSuccess(String orderNo, String remark, String payer) {
        return updateStatus(orderNo, RechargeStatusEnum.SUCCESS.getValue(), remark, payer);
    }

    @Transactional
    public int rechargeFail(String orderNo, String remark, String payer) {
        return updateStatus(orderNo, RechargeStatusEnum.FAIL.getValue(), remark, payer);
    }

    @Transactional
    public int rechargeExpire(String orderNo, String remark, String payer) {
        return updateStatus(orderNo, RechargeStatusEnum.EXPIRE.getValue(), remark, payer);
    }

    public List<TsgPayOrder> findNoPayOrder(){
        Map<String,Object> params = new HashMap<>();
        params.put("time", DateUtil.getDateByM(-3));
        return findList("findNoPayOrder",params);
    }

    public TsgPayOrder findByOrderNo(String orderNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        return (TsgPayOrder) findOne("findByOrderNo", params);
    }

    @Transactional
    private int updateStatus(String orderNo, int status, String remark, String payer) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        params.put("status", status);
        params.put("remark",remark);
        params.put("payer",payer);
        return execute("updateStatus", params);
    }

    @Transactional
    public int updateOrderExpire(){
        Map<String, Object> params = new HashMap<>();
        params.put("time", new Date());
        return execute("updateOrderExpire",params);
    }

    @Transactional
    public int updateOrder(TsgPayOrder order){

        return execute("updateOrder",order);
    }
}
