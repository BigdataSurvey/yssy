package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.PbxWeekSettleEvent;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2025/12/28
 * @Version: V1.0
 * @Description: 周榜结算事件业务逻辑层
 * 对应表：t_pbx_week_settle_event
 */
@Service
public class PbxWeekSettleEventService extends DaoService {

    private static final Log logger = LogFactory.getLog(PbxWeekSettleEventService.class);

    public PbxWeekSettleEventService() {
        super("PbxWeekSettleEventMapper");
    }

    /**
     * 新增结算记录
     * @param event 结算事件对象
     */
    @Transactional
    public void insert(PbxWeekSettleEvent event) {
        getBaseDao().execute(mapperSpace, "insert", event);
    }

    /**
     * 忽略重复键新增（通常用于幂等处理）
     * @param event 结算事件对象
     * @return 影响行数
     */
    @Transactional
    public int insertIgnore(PbxWeekSettleEvent event) {
        return execute("insertIgnore", event);
    }

    /**
     * 更新结算记录状态或信息
     * @param event 结算事件对象
     * @return 影响行数
     */
    @Transactional
    public int update(PbxWeekSettleEvent event) {
        return execute("update", event);
    }

    /**
     * 根据 WeekKey 查询该周所有用户的结算记录
     * @param weekKey 周Key (yyyy-MM-dd)
     * @return 列表按 rank 升序排列
     */
    @Transactional(readOnly = true)
    public List<PbxWeekSettleEvent> findByWeekKey(String weekKey) {
        Map<String, Object> params = new HashMap<>();
        params.put("weekKey", weekKey);
        return getBaseDao().findList(mapperSpace, "findByWeekKey", params);
    }

    /**
     * 根据 UserId 查询该用户的所有历史结算记录
     * @param userId 用户ID
     * @return 列表按创建时间倒序排列
     */
    @Transactional(readOnly = true)
    public List<PbxWeekSettleEvent> findByUserId(Long userId) {
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return getBaseDao().findList(mapperSpace, "findByUserId", params);
    }

    /**
     * 查询指定用户在指定周的结算记录
     * @param gameId 游戏ID
     * @param weekKey 周Key
     * @param userId 用户ID
     * @return 唯一记录 或 null
     */
    @Transactional(readOnly = true)
    public PbxWeekSettleEvent findByGameWeekUser(int gameId, String weekKey, long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        map.put("weekKey", weekKey);
        map.put("userId", userId);
        return (PbxWeekSettleEvent) findOne("findByGameWeekUser", map);
    }

    /**
     * 查询指定周的所有记录（显式带 gameId 维度）
     * @param gameId 游戏ID
     * @param weekKey 周Key
     * @return 列表
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<PbxWeekSettleEvent> findByGameWeek(int gameId, String weekKey) {
        Map<String, Object> map = new HashMap<>();
        map.put("gameId", gameId);
        map.put("weekKey", weekKey);
        return (List<PbxWeekSettleEvent>) (List<?>) findList("findByGameWeek", map);
    }

    /**
     * 根据幂等单号查询记录
     * 用于防止重复派奖
     * @param orderNo 唯一订单号
     * @return 记录 或 null
     */
    @Transactional(readOnly = true)
    public PbxWeekSettleEvent findByOrderNo(String orderNo) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNo", orderNo);
        return (PbxWeekSettleEvent) findOne("findByOrderNo", map);
    }
}