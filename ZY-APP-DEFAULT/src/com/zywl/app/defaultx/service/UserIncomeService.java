package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserBook;
import com.zywl.app.base.bean.UserDzRecord;
import com.zywl.app.base.bean.UserIncome;
import com.zywl.app.base.bean.UserProcess;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserIncomeService extends DaoService {

    public UserIncomeService() {
        super("UserIncomeMapper");
    }

    @Transactional
    public int batchUpdate(List<UserIncome> userIncomes){
        int a = execute("batchUpdateUserIncome",userIncomes);
        if(a<1){
            throwExp("更新失败");
        }
        return a;
    }

    public List<UserIncome> findByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return  findList("findUserIncomeByUserId", params);

    }
    public List<UserIncome> findUserIncomeByUserIdAndStatus(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return  findList("findUserIncomeByUserIdAndStatus", params);

    }

}

