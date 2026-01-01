package com.zywl.app.defaultx.service;
import java.util.List;
import com.zywl.app.base.bean.DicPet;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author: lzx
 * @Create: 2025/12/29
 * @Version: V1.0
 * @Description: 养宠全局配置Service
 * @Task:
 */
@Service
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

