package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.shoop.ShopManager;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShopManagerService extends DaoService {


    public ShopManagerService() {
        super("ShopManagerMapper");
    }



    public List<ShopManager> queryShopList() {
        return findList("queryShopList", null);
    }

    public ShopManager addShopManager(ShopManager shopManager) {
        save(shopManager);
        return shopManager;
    }

    public ShopManager findByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return (ShopManager) findOne("findByUserId", params);
    }

    public List<ShopManager> queryShopManager() {

        return findList("queryShopManager",null);
    }
}
