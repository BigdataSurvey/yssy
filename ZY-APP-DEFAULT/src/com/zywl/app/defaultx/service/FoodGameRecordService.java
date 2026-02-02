package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.FoodGameRecord;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class FoodGameRecordService extends DaoService {
    public FoodGameRecordService() {
        super("FoodGameRecordMapper");
    }

    @Transactional
    public void addRecord(Long userId, Integer roomId, BigDecimal betAmount, BigDecimal winAmount, String cardInfo, int winValue, int winLose, String winInfo) {
        FoodGameRecord record = new FoodGameRecord();
        record.setBetAmount(betAmount);
        record.setUserId(userId);
        record.setRoomId(roomId);
        record.setWinAmount(winAmount);
        record.setCardInfo(cardInfo);
        record.setWinValue(winValue);
        record.setWinLose(winLose);
        record.setWinInfo(winInfo);
        record.setRecordTime(new Date());
        save(record);
    }

    @Transactional
    public void deletedThreeDayRecord(){
        Map<String, Object> params  = new HashMap<>();
        params.put("time", DateUtil.getDateByDay(-3));
        execute("deletedThreeDayRecord",params);
    }
}
