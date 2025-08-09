package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Backpack;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.vo.BackpackVo;
import com.zywl.app.defaultx.cache.UserBackpackCacheService;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class BackpackService extends DaoService {

    @Autowired
    private UserBackpackCacheService userBackpackCacheService;

    @Autowired
    private LogUserBackpackService logUserBackpackService;

    @Autowired
    private UserCacheService userCacheService;


    public BackpackService() {
        super("BackpackMapper");
    }

    @Transactional
    public int addBackpackInfo(Long itemId, int itemNumber, Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("itemId", itemId);
        params.put("itemNumber", itemNumber);
        params.put("createTime", new Date());
        params.put("updateTime", new Date());
        params.put("tableName", Backpack.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        return save(params);

    }

    public List<Backpack> getBackpackByUserId(Long userId) {
        Map<String, Object> map = new HashedMap<String, Object>();
        map.put("userId", userId);
        String i = Backpack.tablePrefix + userId.toString().charAt(userId.toString().length() - 1);
        map.put("tableName", i);
        return findByConditions(map);
    }

    public List<BackpackVo> getBackpackTopList(String itemId){
        List<BackpackVo> backpackVos = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            Map<String, Object> map = new HashedMap<String, Object>();
            map.put("tableName",  Backpack.tablePrefix + i);
            map.put("itemId",itemId);
            List<BackpackVo> findTopList = findList("findTopList", map);
            backpackVos.addAll(findTopList);
        }
        return backpackVos;
    }

    //玩家减少道具数量
    @Transactional
    public int subItemNumber(Long userId, Long itemId, int number, LogUserBackpackTypeEnum em, int beforeNumber, Long id) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        params.put("itemId", itemId);
        params.put("number", number);
        params.put("id", id);
        params.put("beforNumber", beforeNumber);
        params.put("tableName", Backpack.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        int a = execute("subItemNumber", params);
        pushLog(userId, itemId, beforeNumber, -number, em);
        return a;
    }

    @Transactional
    public int subItemNumber(Long userId, Long itemId, int number, LogUserBackpackTypeEnum em, int beforeNumber, Long id,String otherUserId) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        params.put("itemId", itemId);
        params.put("number", number);
        params.put("id", id);
        params.put("beforNumber", beforeNumber);
        params.put("tableName", Backpack.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        int a = execute("subItemNumber", params);
        pushLog(userId, itemId, beforeNumber, -number, em,otherUserId);
        return a;
    }

    @Transactional
    public int subItemNumberByDts(Long userId, Long itemId, int number) {
        Map<String, Object> params = new HashedMap<String, Object>();
        params.put("userId", userId);
        params.put("itemId", itemId);
        params.put("number", number);
        params.put("tableName", Backpack.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        int a = execute("subItemNumber", params);
        if (a < 1) {
            throwExp("道具数量不足");
        }
        pushLog(userId, itemId, 0, -number, LogUserBackpackTypeEnum.yy);
        return a;
    }

    public void pushLog(Long userId, Long itemId, int numberBefore, int number, LogUserBackpackTypeEnum em) {
        JSONObject a = new JSONObject();
        a.put("logType", 2);
        a.put("userId", userId);
        a.put("numberBefore", numberBefore);
        a.put("number", number);
        a.put("itemId", itemId);
        a.put("em", em);
        Push.push(PushCode.insertLog, null, a);

    }

    public void pushLog(Long userId, Long itemId, int numberBefore, int number, LogUserBackpackTypeEnum em,String otherUserId) {
        JSONObject a = new JSONObject();
        a.put("logType", 2);
        a.put("userId", userId);
        a.put("numberBefore", numberBefore);
        a.put("number", number);
        a.put("itemId", itemId);
        a.put("em", em);
        a.put("otherUserId",otherUserId);
        Push.push(PushCode.insertLog, null, a);

    }

    //玩家增加道具数量
    @Transactional
    public int addItemNumber(Long userId, Long itemId, int number, LogUserBackpackTypeEnum em, int type, int beforeNumber, Long id) {
        int a = 0;
        if (type == 0) {
            //没有该道具 向背包种添加
            a = addBackpackInfo(itemId, number, userId);
        } else {
            Map<String, Object> params = new HashedMap<String, Object>();
            params.put("userId", userId);
            params.put("itemId", itemId);
            params.put("id", id);
            params.put("number", number);
            params.put("tableName", Backpack.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
            a = execute("addItemNumber", params);
        }
        pushLog(userId, itemId, beforeNumber, number, em);
        return a;
    }

    @Transactional
    public int addItemNumber(Long userId, Long itemId, int number, LogUserBackpackTypeEnum em, int type, int beforeNumber, Long id,String fromUserId) {
        int a = 0;
        if (type == 0) {
            //没有该道具 向背包种添加
            a = addBackpackInfo(itemId, number, userId);
        } else {
            Map<String, Object> params = new HashedMap<String, Object>();
            params.put("userId", userId);
            params.put("itemId", itemId);
            params.put("id", id);
            params.put("number", number);
            params.put("tableName", Backpack.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
            a = execute("addItemNumber", params);
        }
        pushLog(userId, itemId, beforeNumber, number, em,fromUserId);
        return a;
    }
    @Transactional
    public void batchUpdateBackpack(List<Map<String, Object>> backpacks, long userId) {
        if (backpacks.size() > 0) {
            execute("batchUpdateBackpack", backpacks);
        }
    }


    @Transactional
    public void deletedOneMonthNoLogin(List<User> users) {
        for (User user : users) {
            Map<String, Object> params = new HashedMap<>();
            params.put("userId", user.getId());
            params.put("tableName", Backpack.tablePrefix + user.getId().toString().charAt(user.getId().toString().length() - 1));
            execute("deletedOneMonthNoLogin", params);
        }
    }

    public Long getWfsbNumber(){
        long number = 0L;
        for (int i = 0; i < 10; i++) {
            try {
                Map<String, Object> params = new HashedMap<>();
                params.put("tableName", Backpack.tablePrefix + i);
                long getWfsbNumber = count("getWfsbNumber", params);
                number+=getWfsbNumber;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return number;
    }

}
