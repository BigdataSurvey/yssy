package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.ApplyFor;
import com.zywl.app.base.bean.UserPickGoods;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserPickGoodsService extends DaoService {

    public UserPickGoodsService() {
        super("UserPickGoodsMapper");
    }

    @Transactional
    public void addPickGoods(Long userId, String name, int number) {
        UserPickGoods userPickGoods = new UserPickGoods();
        userPickGoods.setUserId(userId);
        userPickGoods.setName(name);
        userPickGoods.setNumber(number);
        userPickGoods.setStatus(0);
        userPickGoods.setOrderNo(null);
        save(userPickGoods);
    }


    public List<UserPickGoods> findByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return findList("findByUserId", params);
    }

    @Transactional
    public int pickGoods(Long id, String name, String phone, String address) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("name", name);
        params.put("phone", phone);
        params.put("address", address);
        return execute("pickGood", params);
    }

    public UserPickGoods findById(Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return (UserPickGoods) findOne("findById", params);
    }

}
