package com.zywl.app.defaultx.service.card;

import com.zywl.app.base.bean.card.DicFarm;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/8
 * @Version: V1.0
 * @Description: 农场种地配置表 Service
 * @Task:
 */
@Service
public class DicFarmService extends DaoService {
        public DicFarmService() {super("DicFarmMapper");}

    /**
     * 查询农场种地配置
     */
    @Transactional(readOnly = true)
    public List<DicFarm> findAll() {
        return findList("findAll", null);
    }

    /**
     * 根据主键查询
     */
    @Transactional(readOnly = true)
    public DicFarm findOneById(Long id) {
        return (DicFarm) findOne("selectByPrimaryKey", id);
    }

    /**
     * 条件查询
     * **/
    @Transactional(readOnly = true)
    public int countByConditions(Map<String,Object> cond) {
        return (Integer) findOne("countByConditions", cond);
    }

}
