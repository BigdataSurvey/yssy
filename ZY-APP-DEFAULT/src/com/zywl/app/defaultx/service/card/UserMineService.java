package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.UserMine;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserMineService extends DaoService {




    public UserMineService() {
        super("UserMineMapper");
    }

    @Transactional
    public UserMine addUserMine(Long userId,Long mineId,int days,int index,double oneReward) {
        UserMine userMine = new UserMine();
        userMine.setUserId(userId);
        userMine.setMineId(mineId);
        userMine.setLastMineTime(null);
        userMine.setCreateTime(new Date());
        userMine.setCount(days);
        userMine.setOutput(0);
        userMine.setLastOutputTime(null);
        userMine.setIsMining(0);
        userMine.setOneReward(oneReward);
        userMine.setMinEndTime(null);
        userMine.setIndex(index);
        userMine.setStatus(1);
        userMine.setAllOutput(0);
        save(userMine);
        return userMine;
    }

    public List<UserMine> findUserMineByUserId(Long userId){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("nowDate",new Date());
        return findList("findUserMineByUserId",map);
    }

    public UserMine findByUserIdAndIndex(Long userId,int index){
        Map<String,Object> map = new HashMap<>();
        map.put("userId",userId);
        map.put("index",index);
        map.put("nowDate",new Date());
        return (UserMine) findOne("findByUserIdAndIndex",map);
    }

    @Transactional
    public void updateUserMine(UserMine userMine){
        /*Map<String,Object> map = new HashMap<>();
        map.put("id",userMine.getId());
        map.put("userId",userMine.getUserId());
        map.put("mineId",userMine.getMineId());
        map.put("lastMineTime",userMine.getLastMineTime());
        map.put("lastOutputTime",userMine.getLastOutputTime());
        map.put("output",userMine.getOutput());
        map.put("isMining",userMine.getIsMining());
        map.put("minEndTime",userMine.getMinEndTime());
        map.put("index",userMine.getIndex());
        map.put("status",userMine.getStatus());*/
        execute("updateUserMin",userMine);
    }

    @Transactional
    public void batchUpdateUserMine(List<UserMine> userMines){
        if(userMines.size()>0) {
            execute("batchUpdateUserMine", userMines);
        }
    }
}