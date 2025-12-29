package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.PbxWeekSummary;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/28
 * @Version: V1.0
 * @Description: PBX 周榜汇总（周维度聚合+快照） Service
 * 对应表：t_pbx_week_summary
 */

@Service
public class PbxWeekSummaryService extends DaoService {

    private static final Log logger = LogFactory.getLog(PbxWeekSummaryService.class);

    public PbxWeekSummaryService() {
        super("PbxWeekSummaryMapper");
    }

    /**
     * 新增周榜汇总记录
     */
    @Transactional
    public void insert(PbxWeekSummary pbxWeekSummary) {
        getBaseDao().execute(mapperSpace, "insert", pbxWeekSummary);
    }

    /**
     * 忽略重复键新增
     */
    @Transactional
    public int insertIgnore(PbxWeekSummary summary) {
        return execute("insertIgnore", summary);
    }

    /**
     * 更新周榜汇总记录
     */
    @Transactional
    public int update(PbxWeekSummary summary) {
        return execute("update", summary);
    }

    /**
     * 按 ID 查询记录
     */
    @Transactional(readOnly = true)
    public PbxWeekSummary findById(Long id) {
        return (PbxWeekSummary) findOne("findById", id);
    }

    /**
     * 按 WeekKey 查询记录 (忽略 gameId，基于单表单游戏假设)
     * @param weekKey "yyyy-MM-dd"
     */
    @Transactional(readOnly = true)
    public PbxWeekSummary findByWeekKey(String weekKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("weekKey", weekKey);
        return (PbxWeekSummary) findOne("findByWeekKey", params);
    }

    /**
     * 按 GameId + WeekKey 查询记录
     */
    @Transactional(readOnly = true)
    public PbxWeekSummary findByGameWeek(int gameId, String weekKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        map.put("weekKey", weekKey);
        return (PbxWeekSummary) findOne("findByGameWeek", map);
    }

    /**
     * 按 GameId + WeekKey 查询记录并加行锁 (FOR UPDATE)
     * 用于并发更新汇总数据时防止冲突
     */
    @Transactional
    public PbxWeekSummary findByGameWeekForUpdate(int gameId, String weekKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        map.put("weekKey", weekKey);
        return (PbxWeekSummary) findOne("findByGameWeekForUpdate", map);
    }
}