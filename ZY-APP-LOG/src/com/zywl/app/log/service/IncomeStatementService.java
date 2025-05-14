package com.zywl.app.log.service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.UserIncomeStatement;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.service.UserIncomeStatementService;
import com.zywl.app.log.socket.LogSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@ServiceClass(code = "119")
public class IncomeStatementService extends BaseService {


    public static Map<String, Boolean> hasStatement = new ConcurrentHashMap<>();

    @Autowired
    private UserIncomeStatementService userIncomeStatementService;

    @PostConstruct
    public void _construct() {
        new Timer("0点清理MAP").schedule(new TimerTask() {
            public void run() {
                hasStatement.clear();
            }
        }, DateUtil.getTomorrowBegin() - System.currentTimeMillis(), 60000 * 24);
    }


    public void addUserIncome(Long parentId, Long grandfaId, BigDecimal parentIncome, BigDecimal grandfaIncome) {
        if (parentIncome.compareTo(BigDecimal.ZERO) > 0) {
            if (!hasStatement.containsKey(parentId.toString()) || !hasStatement.get(parentId.toString())) {
                UserIncomeStatement byUserIdAndYmd = userIncomeStatementService.findByUserIdAndYmd(parentId);
                if (byUserIdAndYmd == null) {
                    userIncomeStatementService.addStatement(parentId);
                }
                hasStatement.put(parentId.toString(), true);
            }
            userIncomeStatementService.addOneIncome(parentId, parentIncome, DateUtil.format9(new Date()));
        }
        try {

            if (grandfaIncome.compareTo(BigDecimal.ZERO) > 0) {
                if (!hasStatement.containsKey(grandfaId.toString()) || !hasStatement.get(grandfaId.toString())) {
                    UserIncomeStatement byUserIdAndYmd = userIncomeStatementService.findByUserIdAndYmd(grandfaId);
                    if (byUserIdAndYmd == null) {
                        userIncomeStatementService.addStatement(grandfaId);
                    }
                    hasStatement.put(grandfaId.toString(), true);
                }
                userIncomeStatementService.addTwoIncome(grandfaId, grandfaIncome, DateUtil.format9(new Date()));
            }
        } catch (Exception e) {
        }
    }


    @Transactional
    @ServiceMethod(code = "001", description = "获取玩家收益报表")
    public Object getUserIncomeStatement(LogSocketServer socketServer, JSONObject data) {
        checkNull(data);
        checkNull(data.get("userId"));
        Long userId = data.getLong("userId");
        JSONObject result = new JSONObject();
        List<UserIncomeStatement> statements = userIncomeStatementService.findByUserId(userId);
        if (statements.size() == 7) {
            return statements;
        } else {
            List<String> list = DateUtil.oneWeekStr();
            Map<String, UserIncomeStatement> hasStrMap = new HashMap<>();
            statements.forEach(e -> hasStrMap.put(e.getYmd(), e));
            List<UserIncomeStatement> newStatements = new ArrayList<>();
            for (String s : list) {
                if (hasStrMap.containsKey(s)) {
                    newStatements.add(hasStrMap.get(s));
                } else {
                    UserIncomeStatement statement = new UserIncomeStatement();
                    statement.setUserId(userId);
                    statement.setYmd(s);
                    statement.setOneIncome(BigDecimal.ZERO);
                    statement.setTwoIncome(BigDecimal.ZERO);
                    newStatements.add(statement);
                }
            }
            return newStatements;
        }
    }
}
