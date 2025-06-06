package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserVip;
import com.zywl.app.base.bean.vo.DSTopVo;
import com.zywl.app.base.bean.vo.VipTopVo;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.VipLevelTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserVipService extends DaoService {

    public UserVipService() {
        super("UserVipMapper");
    }

    @Transactional
    public UserVip addUserVip(Long userId){
        UserVip userVip = new UserVip();
        userVip.setUserId(userId);
        userVip.setVipLevel(0);
        userVip.setRechargeAmount(BigDecimal.ZERO);
        userVip.setCreateTime(new Date());
        save(userVip);
        return userVip;
    }


    @Transactional
    public void updateUserVipInfo(UserVip userVip) {
       execute("updateUserVip",userVip);
    }

    public UserVip findRechargeAmountByUserId(Long userId) {
        Map<String,Object> parameters = new HashedMap<>();
        parameters.put("userId", userId);
        UserVip userVip = (UserVip) findOne("findRechargeAmountByUserId", parameters);
        if (userVip==null){
            return addUserVip(userId);
        }
        return userVip;

    }


    public List<VipTopVo> findTopByVip(){
        return findList("findTopByVip",null);
    }
}
