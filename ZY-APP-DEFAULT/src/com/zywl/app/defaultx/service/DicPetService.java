package com.zywl.app.defaultx.service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.zywl.app.base.bean.DicPet;
import com.zywl.app.base.bean.UserJoy;
import com.zywl.app.base.bean.card.DicFarm;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: lzx
 * @Create: 2025/12/29
 * @Version: V1.0
 * @Description: 养宠全局配置Service
 * @Task:
 */
public class DicPetService  extends DaoService {

    public DicPetService() {
        super("DicPetMapper");
    }
    /**
     * 查询养宠配置
     */
    @Transactional(readOnly = true)
    public List<DicPet> findAll() {
        return findList("findAll", null);
    }

}

