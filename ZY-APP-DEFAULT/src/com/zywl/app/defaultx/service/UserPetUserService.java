package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.UserPetUser;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 养宠用户汇总状态 Service
 */
@Service
public class UserPetUserService extends DaoService {

    public UserPetUserService() {
        super("UserPetUserMapper");
    }

    public UserPetUser findByUserId(Long userId) {
        return (UserPetUser) findOne("selectByPrimaryKey", userId);
    }

    public UserPetUser lockByUserId(Long userId) {
        return (UserPetUser) findOne("selectByPrimaryKeyForUpdate", userId);
    }

    @Transactional
    public int saveOrUpdate(UserPetUser userPetUser) {
        return getBaseDao().execute(mapperSpace, "saveOrUpdate", userPetUser);
    }
}
