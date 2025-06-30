package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.UserPower;
import com.zywl.app.base.bean.card.UserCheckpointTopVo;
import com.zywl.app.base.bean.vo.*;
import com.zywl.app.base.bean.vo.card.UserTowerVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//排行榜
@Service
public class TopService extends BaseService {
    //人气榜
    public static JSONArray POPULAR_TOP = new JSONArray();
    //仙晶排行榜
    public static JSONArray PVP = new JSONArray();
    //试炼之塔排行榜
    public static JSONArray TOWER_TOP = new JSONArray();

    public static JSONArray TOP_4 = new JSONArray();

    public static List<UserPower> userPowers = new ArrayList<>();

    public static JSONArray TOP_5 = new JSONArray();
    //仙门排行榜
    public static JSONArray TOP_6 = new JSONArray();

    public static JSONArray TOP_FRIEND = new JSONArray();

    public static JSONArray TOP_VIP = new JSONArray();


    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private UserPowerService userPowerService;


    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private UserService userService;


    @Autowired
    private UserVipService userVipService;


    public void updateTop1Info() {




        long time = System.currentTimeMillis();
        TOP_5.clear();
        List<OneJuniorNumTopVo> toByJuniorNum = userStatisticService.findToByJuniorNum();
        for (OneJuniorNumTopVo oneJuniorNumTopVo : toByJuniorNum) {
            oneJuniorNumTopVo.setNum(oneJuniorNumTopVo.getNum()+oneJuniorNumTopVo.getNum2());
        }
        toByJuniorNum.sort(((o1,o2) -> {
            //从小到大
            return o2.getNum() - o1.getNum();//此处定义比较规则，o2.age-o1.age即为从大到小
        }));
        TOP_5.addAll(toByJuniorNum);
        logger.info("更新好友排行榜--用时：" + (System.currentTimeMillis() - time));



        time = System.currentTimeMillis();
        TOP_VIP.clear();
        List<VipTopVo> vipTopVos = userVipService.findTopByVip();
        TOP_VIP.addAll(vipTopVos);
        logger.info("更新vip榜--用时：" + (System.currentTimeMillis() - time));
    }

}
