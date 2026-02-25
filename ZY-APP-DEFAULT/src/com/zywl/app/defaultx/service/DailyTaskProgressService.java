package com.zywl.app.defaultx.service;

import com.alibaba.fastjson2.JSONArray;
import com.zywl.app.base.bean.vo.UserDailyTaskVo;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.LockUtil;
import com.zywl.app.defaultx.cache.card.CardGameCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * 每日任务进度推进服务（无 Kafka 版本）
 * <p>
 * 在各小游戏"下注成功"处直接调用本服务的 pushDailyTaskByGameId 方法，
 * 即可推进对应每日任务的 schedule，达标后自动置为"可领取"，
 * 无需依赖 Kafka 消息。
 * </p>
 */
@Service
public class DailyTaskProgressService extends BaseService {

    @Autowired
    private CardGameCacheService cardGameCacheService;

    @Autowired
    private UserDailyTaskService userDailyTaskService;

    /**
     * 根据游戏 ID 推进每日任务进度
     *
     * @param userId 用户 ID
     * @param gameId 游戏 ID（对应 l_game.id）
     *               gameId=1  → 消失的兔子（DTS3）→ taskId="101"
     *               gameId=7  → 疯狂的狮子（DTS）→ taskId="102"
     *               gameId=12 → 开开乐（DTS2/PBX）→ taskId="103"
     */
    public void pushDailyTaskByGameId(Long userId, Integer gameId) {
        if (userId == null || gameId == null) return;

        String taskId;
        if (gameId == 1) {
            taskId = "101";
        } else if (gameId == 7) {
            taskId = "102";
        } else if (gameId == 12) {
            taskId = "103";
        } else {
            return;
        }

        checkAndAdvance(userId, taskId);
    }

    /**
     * 推进指定用户的指定每日任务进度 +1
     * <p>
     * 逻辑：
     * 1. 从 Redis 缓存读取用户今日任务列表
     * 2. 找到对应 taskId 的任务
     * 3. 如果任务未完成（status==0），schedule +1
     * 4. 如果 schedule >= condition，设置 status=1（可领取）
     * 5. 更新 DB 和 Redis 缓存
     * </p>
     */
    public void checkAndAdvance(Long userId, String taskId) {
        synchronized (LockUtil.getlock(userId)) {
            Map userTask = cardGameCacheService.getUserTask(userId);
            if (userTask == null || userTask.isEmpty()) {
                return;
            }

            UserDailyTaskVo task = (UserDailyTaskVo) userTask.get(taskId);
            if (task == null) {
                return;
            }

            // 仅未完成的任务才推进进度
            if (task.getStatus() == 0) {
                task.setSchedule(task.getSchedule() + 1);

                boolean reachedGoal = false;
                if (task.getSchedule() >= task.getCondition()) {
                    task.setStatus(1); // 可领取
                    reachedGoal = true;
                }

                // 更新 DB
                Collection values = userTask.values();
                userDailyTaskService.updateUserTask(userId, JSONArray.copyOf(values));

                // 更新 Redis 缓存
                cardGameCacheService.updateUserDailyTaskStatus(userId, taskId, task);

                if (reachedGoal) {
                    logger.info("[DailyTaskProgress] 用户 " + userId + " 任务 " + taskId + " 达标，可领取");
                }
            }
        }
    }
}
