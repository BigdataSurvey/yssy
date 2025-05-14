package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.RechargeOrder;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.RechargeStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RechargeOrderService extends DaoService {

    public RechargeOrderService() {
        super("RechargeOrderMapper");
    }

    @Transactional
    public RechargeOrder addOrder(Long userId, String orderNo, String prepayId, Long productId, int price, Date expireTime) {
        RechargeOrder order = new RechargeOrder();
        order.setOrderNo(orderNo);
        order.setStatus(0);
        order.setPrepayId(prepayId);
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

    public RechargeOrder findByOrderNo(String orderNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("orderNo", orderNo);
        return (RechargeOrder) findOne("findByOrderNo", params);
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
}
