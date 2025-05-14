package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.LogUserCapital;
import com.zywl.app.base.bean.vo.LogUserCapitalVo;
import com.zywl.app.base.util.BeanUtils;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.GameTypeEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class LogUserCapitalService extends DaoService {

    private static final Log logger = LogFactory.getLog(LogUserCapitalService.class);

    public LogUserCapitalService() {
        super("LogUserCapitalMapper");
        // TODO Auto-generated constructor stub
    }


    @Transactional
    @Async
    public void addLogUserCapital(int type, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, BigDecimal amount, LogCapitalTypeEnum em, String orderNo, Long sourceDataId, String tableName) {
        LogUserCapital logUserCapital = new LogUserCapital();

        if (type == 1) {
            //只动余额
            logUserCapital.setBalanceAfter(amount.add(balanceBefore));
            logUserCapital.setOccupyBalanceAfter(occupyBalanceBefore);
        } else if (type == 2) {
            //只动冻结
            logUserCapital.setBalanceAfter(balanceBefore);
            logUserCapital.setOccupyBalanceAfter(amount.add(occupyBalanceBefore));
        } else {
            //一加一减
            logUserCapital.setBalanceAfter(amount.add(balanceBefore));
            logUserCapital.setOccupyBalanceAfter(occupyBalanceBefore.subtract(amount));
        }
        logUserCapital.setOccupyBalanceBefore(occupyBalanceBefore);
        logUserCapital.setLogType(em.getValue());
        logUserCapital.setUserId(userId);
        logUserCapital.setCapitalType(capitalType.toString());
        logUserCapital.setBalanceBefore(balanceBefore);
        logUserCapital.setAmount(amount);
        logUserCapital.setMark(em.getName());
        logUserCapital.setSourceNo(orderNo);
        logUserCapital.setSourceDataId(sourceDataId);
        logUserCapital.setSourceTableName(tableName);
        logUserCapital.setSourceType(em.getName());
        logUserCapital.setCreateTime(new Date());
        logUserCapital.setUpdateTime(new Date());
        Map params = objectToMap(logUserCapital);
        params.put("tableName", LogUserCapital.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        insert(params);
    }


    @Transactional
    public void addLogUserCapital(int type, Long userId, Integer capitalType, BigDecimal balanceBefore, BigDecimal occupyBalanceBefore, BigDecimal amount, LogCapitalTypeEnum em, String orderNo, Long sourceDataId, String tableName, int gameId) {
        LogUserCapital logUserCapital = new LogUserCapital();

        if (type == 1) {
            //只动余额
            logUserCapital.setBalanceAfter(amount.add(balanceBefore));
            logUserCapital.setOccupyBalanceAfter(occupyBalanceBefore);
        } else if (type == 2) {
            //只动冻结
            logUserCapital.setBalanceAfter(balanceBefore);
            logUserCapital.setOccupyBalanceAfter(amount.add(occupyBalanceBefore));
        } else {
            //一加一减
            logUserCapital.setBalanceAfter(amount.add(balanceBefore));
            logUserCapital.setOccupyBalanceAfter(occupyBalanceBefore.subtract(amount));
        }
        logUserCapital.setOccupyBalanceBefore(occupyBalanceBefore);
        logUserCapital.setLogType(em.getValue());
        logUserCapital.setUserId(userId);
        logUserCapital.setCapitalType(capitalType.toString());
        logUserCapital.setBalanceBefore(balanceBefore);
        logUserCapital.setAmount(amount);
        logUserCapital.setMark(GameTypeEnum.getName(gameId) + em.getName());
        logUserCapital.setSourceNo(orderNo);
        logUserCapital.setSourceDataId(sourceDataId);
        logUserCapital.setSourceTableName(tableName);
        logUserCapital.setSourceType(em.getName());
        logUserCapital.setCreateTime(new Date());
        logUserCapital.setUpdateTime(new Date());
        JSONObject params = (JSONObject) JSON.toJSON(logUserCapital);
        params.put("tableName", LogUserCapital.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        insert(params);
    }


    @Transactional
    public void batchInsert(JSONObject data) {
        JSONArray arr = new JSONArray();
        Set<String> set = data.keySet();
        for (String key : set) {
            LogUserCapital logUserCapital = new LogUserCapital();
            JSONObject log = data.getJSONObject(key);
            if (log.getIntValue("type") == 1) {
                //只动余额
                logUserCapital.setBalanceAfter(log.getBigDecimal("amount").add(log.getBigDecimal("balanceBefore")));
                logUserCapital.setOccupyBalanceAfter(log.getBigDecimal("occupyBalanceBefore"));
            } else if (log.getIntValue("type") == 2) {
                //只动冻结
                logUserCapital.setBalanceAfter(log.getBigDecimal("balanceBefore"));
                logUserCapital.setOccupyBalanceAfter(log.getBigDecimal("amount").add(log.getBigDecimal("occupyBalanceBefore")));
            } else {
                //一加一减
                logUserCapital.setBalanceAfter(log.getBigDecimal("amount").add(log.getBigDecimal("balanceBefore")));
                logUserCapital.setOccupyBalanceAfter(log.getBigDecimal("occupyBalanceBefore").subtract(log.getBigDecimal("amount")));
            }
            logUserCapital.setOccupyBalanceBefore(log.getBigDecimal("occupyBalanceBefore"));
            logUserCapital.setLogType(log.getInteger("logType"));
            logUserCapital.setUserId(log.getLong("userId"));
            logUserCapital.setCapitalType(log.getString("capitalType"));
            logUserCapital.setBalanceBefore(log.getBigDecimal("balanceBefore"));
            logUserCapital.setAmount(log.getBigDecimal("amount"));
            logUserCapital.setMark(GameTypeEnum.getName(log.getIntValue("gameId")) + log.getString("reamrk"));
            logUserCapital.setSourceNo(log.getString("orderNo"));
            logUserCapital.setSourceDataId(log.getLong("dataId"));
            logUserCapital.setSourceTableName(log.getString("tableName"));
            logUserCapital.setSourceType(log.getString("reamrk"));
            logUserCapital.setCreateTime(new Date());
            logUserCapital.setUpdateTime(new Date());
            JSONObject params = (JSONObject) JSON.toJSON(logUserCapital);
            Long userId = log.getLong("userId");
            params.put("tableName", LogUserCapital.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
            arr.add(params);
        }
        execute("batchInsert", arr);
    }


    public List<LogUserCapitalVo> getLog(Long userId, int capitalType, int start, int limit,int type) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        params.put("tableName", LogUserCapital.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        params.put("capitalType", capitalType);
        params.put("start", start * limit);
        params.put("limit", limit);
        params.put("type",type);
        List<LogUserCapital> list = findByConditions(params);
        List<LogUserCapitalVo> vos = new ArrayList<LogUserCapitalVo>();
        for (LogUserCapital logUserCapital : list) {
            LogUserCapitalVo vo = new LogUserCapitalVo();
            BeanUtils.copy(logUserCapital, vo);
            vos.add(vo);
        }
        return vos;
    }

    public List<LogUserCapital> findSellLqg(Long userId){
        JSONObject params = new JSONObject();
        params.put("tableName", LogUserCapital.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        params.put("userId",userId);
        return findList("findSellLqg",params);
    }

    public List<LogUserCapital> findByDz(Long userId,Date createTime){
        JSONObject params = new JSONObject();
        params.put("tableName", LogUserCapital.tablePrefix + userId.toString().charAt(userId.toString().length() - 1));
        params.put("userId",userId);
        params.put("createTime", DateUtil.getDateByM(createTime,-60) );
        params.put("endTime",DateUtil.getDateByM(createTime,60));
        return findList("findByDz",params);
    }
}
