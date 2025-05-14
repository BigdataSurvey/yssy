package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.UserPower;
import com.zywl.app.base.bean.card.UserCheckpointTopVo;
import com.zywl.app.base.bean.vo.CapitalTopVo;
import com.zywl.app.base.bean.vo.DSTopVo;
import com.zywl.app.base.bean.vo.OneJuniorNumTopVo;
import com.zywl.app.base.bean.vo.UserPowerVo;
import com.zywl.app.base.bean.vo.card.UserTowerVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserPowerService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.defaultx.service.UserStatisticService;
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

    public static JSONArray TOP_DS = new JSONArray();


    @Autowired
    private UserStatisticService userStatisticService;

    @Autowired
    private UserPowerService userPowerService;


    @Autowired
    private UserCapitalService userCapitalService;


    @Autowired
    private UserService userService;




    public void updateTop1Info() {


      /*  time = System.currentTimeMillis();
        CAPITAL_TOP_2.clear();
        List<CapitalTopVo> capitalTop1 = userCapitalService.findCapitalTop(UserCapitalTypeEnum.currency_3.getValue());
        CAPITAL_TOP_2.addAll(capitalTop1);
        logger.info("更新仙晶排行榜--用时：" + (System.currentTimeMillis() - time));*/

        long time = System.currentTimeMillis();

         time = System.currentTimeMillis();
        TOP_4.clear();

        logger.info("更新战力排行榜--用时：" + (System.currentTimeMillis() - time));


        time = System.currentTimeMillis();
        TOP_5.clear();
        List<OneJuniorNumTopVo> toByJuniorNum = userStatisticService.findToByJuniorNum();
        for (OneJuniorNumTopVo oneJuniorNumTopVo : toByJuniorNum) {
            oneJuniorNumTopVo.setNum(oneJuniorNumTopVo.getNum()+oneJuniorNumTopVo.getNum2());
            if (oneJuniorNumTopVo.getRoleId()==3){
                oneJuniorNumTopVo.setIsPopular(1);
            }
            if (oneJuniorNumTopVo.getRoleId()==2){
                oneJuniorNumTopVo.setIsPopular(2);
            }
        }
        toByJuniorNum.sort(((o1,o2) -> {
            //从小到大
            return o2.getNum() - o1.getNum();//此处定义比较规则，o2.age-o1.age即为从大到小
        }));
        TOP_5.addAll(toByJuniorNum);
        logger.info("更新好友排行榜--用时：" + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();


        time = System.currentTimeMillis();
        TOP_FRIEND.clear();
        List<CapitalTopVo> capitalTopVos = userStatisticService.findTop();
        for (CapitalTopVo capitalTopVo : capitalTopVos) {
            if (capitalTopVo.getRoleId()==3){
                capitalTopVo.setIsPopular(1);
            }
            if (capitalTopVo.getRoleId()==2){
                capitalTopVo.setIsPopular(2);
            }
        }
        TOP_FRIEND.addAll(capitalTopVos);
        logger.info("更新友情值排行榜--用时：" + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        TOP_DS.clear();
        List<DSTopVo> dsTopVos = userService.findTopByDs();
        for (DSTopVo dsTopVo : dsTopVos) {
            if (dsTopVo.getRoleId()==3){
                dsTopVo.setIsPopular(1);
            }
            if (dsTopVo.getRoleId()==2){
                dsTopVo.setIsPopular(2);
            }
        }
        List<DSTopVo> dsTopVos2 = userService.findTopByDl();
        for (DSTopVo dsTopVo : dsTopVos2) {
            if (dsTopVo.getRoleId()==3){
                dsTopVo.setIsPopular(1);
            }
            if (dsTopVo.getRoleId()==2){
                dsTopVo.setIsPopular(2);
            }
        }
        Collections.shuffle(dsTopVos);
        Collections.shuffle(dsTopVos2);
        TOP_DS.addAll(dsTopVos);
        TOP_DS.addAll(dsTopVos2);

        logger.info("更新至尊榜--用时：" + (System.currentTimeMillis() - time));
    }

}
