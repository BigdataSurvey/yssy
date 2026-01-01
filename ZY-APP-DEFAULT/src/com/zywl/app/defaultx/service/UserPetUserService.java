package com.zywl.app.defaultx.service;
import com.zywl.app.base.bean.card.DicFarm;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.base.bean.UserPetUser;
import com.zywl.app.base.bean.UserPet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: lzx
 * @Create: 2025/12/30
 * @Version: V1.0
 * @Description: 养宠用户状态Service
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
        return getBaseDao().execute(mapperSpace,"saveOrUpdate", userPetUser);
    }
}