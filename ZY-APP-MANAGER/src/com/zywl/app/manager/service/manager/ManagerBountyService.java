package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.BountyTask;
import com.zywl.app.base.bean.BountyTaskOrder;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.enmus.BountyStatusEnum;
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.BountyFeePoolService;
import com.zywl.app.defaultx.service.BountyTaskOrderService;
import com.zywl.app.defaultx.service.BountyTaskService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.service.oss.AliOssDirectUploadService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.*;

/**
 * @Author: lzx
 * @Create: 2026/1/3
 * @Version: V1.4
 * @Description: 悬赏任务核心业务逻辑层
 * @Task: 039 (MessageCodeContext.BOUNTY_TASK)
 */
@Slf4j
@Service
@ServiceClass(code = MessageCodeContext.BOUNTY_TASK)
public class ManagerBountyService extends BaseService {
    // 任务状态：1-上架中
    private static final int TASK_STATUS_ONLINE = 1;
    // 任务状态：2-已取消/下架
    private static final int TASK_STATUS_CANCEL = 2;

    // 申诉处理状态：0-无申诉
    private static final int APPEAL_STATUS_NONE = 0;
    // 申诉处理状态：1-申诉处理中
    private static final int APPEAL_STATUS_DOING = 1;
    // 申诉处理状态：2-申诉已处理
    private static final int APPEAL_STATUS_DONE = 2;

    @Autowired
    private UserCacheService userCacheService;
    @Autowired
    private PlayGameService playGameService;
    @Autowired
    private ManagerConfigService managerConfigService;
    @Autowired
    private UserCapitalService userCapitalService;
    @Autowired
    private BountyTaskService bountyTaskService;
    @Autowired
    private BountyTaskOrderService bountyTaskOrderService;
    @Autowired
    private BountyFeePoolService bountyFeePoolService;
    @Autowired
    private AliOssDirectUploadService aliOssDirectUploadService;

    /**
     * 悬赏任务-大厅列表查询
     * 获取全服悬赏任务列表，支持排序（最新/高价/热度）与关键词搜索。
     */
    @ServiceMethod(code = "001", description = "悬赏任务-大厅列表")
    public JSONObject listTasks(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));

        Integer orderType = params.getInteger("orderType");
        String keyword = params.getString("keyword");

        int pageNo = params.getIntValue("pageNo");
        int pageSize = params.getIntValue("pageSize");
        if (pageNo <= 0) pageNo = 1;
        if (pageSize <= 0) pageSize = 10;
        if (pageSize > 50) pageSize = 50;

        int offset = (pageNo - 1) * pageSize;

        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("status", TASK_STATUS_ONLINE);
        queryMap.put("orderType", orderType);
        queryMap.put("keyword", keyword);
        queryMap.put("offset", offset);
        queryMap.put("limit", pageSize);

        List<BountyTask> taskList = bountyTaskService.findListByConditions(queryMap);

        Map<String, Object> countQuery = new HashMap<>();
        countQuery.put("status", TASK_STATUS_ONLINE);
        countQuery.put("keyword", keyword);
        int total = bountyTaskService.countByConditions(countQuery);

        JSONArray resultArray = new JSONArray();
        if (taskList != null) {
            for (BountyTask task : taskList) {
                JSONObject item = new JSONObject();
                item.put("taskId", task.getId());
                item.put("taskTitle", task.getTaskTitle());
                item.put("taskName", task.getTaskName());
                item.put("unitPrice", task.getUnitPrice());
                item.put("quotaTotal", task.getQuotaTotal());
                item.put("quotaRemain", task.getQuotaRemain());
                item.put("joinCount", task.getJoinCount());
                item.put("finishCount", task.getFinishCount());
                item.put("takeLimitHours", task.getTakeLimitHours());
                item.put("createTime", task.getCreateTime());
                resultArray.add(item);
            }
        }
        JSONObject response = new JSONObject();
        response.put("list", resultArray);
        response.put("total", total);
        response.put("pageNo", pageNo);
        response.put("pageSize", pageSize);
        return response;
    }

    /**
     * 悬赏任务-获取任务详情
     * 同时返回我在该任务下的最新订单状态。
     */
    @ServiceMethod(code = "002", description = "悬赏任务-任务详情")
    public JSONObject getTaskDetail(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");

        BountyTaskOrder myOrder = findLatestOrder(taskId, userId);
        if (myOrder != null) {
            // 查看详情时顺便检查订单是否超时
            refreshTimeoutIfNeeded(task, myOrder);
        }

        // 如果任务已下架，并且查看者不是发布者本人，且没有进行中的订单，就禁止查看
        if (task.getStatus() != TASK_STATUS_ONLINE
                && !Objects.equals(task.getUserId(), userId)
                && myOrder == null) {
            throwExp("任务已下架");
        }

        JSONObject response = new JSONObject();
        response.put("task", beanToJson(task));
        response.put("myOrder", myOrder == null ? null : orderToJson(myOrder));
        response.put("serverTime", System.currentTimeMillis());
        return response;
    }

    /**
     * 悬赏任务-发布任务
     * 创建新任务，冻结发布者资金（总赏金+手续费），并将手续费转入公共奖池。
     */
    @Transactional
    @ServiceMethod(code = "003", description = "悬赏任务-发布")
    public JSONObject publishTask(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        checkNull(params.get("taskName"), params.get("taskTitle"), params.get("taskDesc"),
                params.get("unitPrice"), params.get("quotaTotal"), params.get("takeLimitHours"),
                params.get("downloadImgs"), params.get("idTip"));

        String taskName = params.getString("taskName");
        String taskTitle = params.getString("taskTitle");
        String taskDesc = params.getString("taskDesc");
        String taskSteps = params.getString("taskSteps");
        String idTip = params.getString("idTip");

        BigDecimal unitPrice = params.getBigDecimal("unitPrice");
        Integer quotaTotal = params.getInteger("quotaTotal");
        Integer takeLimitHours = params.getInteger("takeLimitHours");

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) throwExp("单价错误");
        if (quotaTotal == null || quotaTotal <= 0) throwExp("名额错误");
        if (takeLimitHours == null || takeLimitHours < 1 || takeLimitHours > 72) throwExp("接单时限范围1~72小时");

        // 资源链接归OSS
        String videoUrl = canonicalizeAndCheckOssUrl(params.getString("videoUrl"), "videoUrl", true);
        String downloadImgs = normalizeAndCheckImgArray(params.getString("downloadImgs"), "downloadImgs", 1, 9);

        // 费用计算
        String feeRateStr = managerConfigService.getString(Config.BOUNTY_FEE_RATE);
        if (!StringUtils.hasText(feeRateStr)) throwExp("手续费配置缺失");
        BigDecimal feeRate = new BigDecimal(feeRateStr);
        // 托管赏金 = 单价 * 总名额
        BigDecimal prepay = unitPrice.multiply(new BigDecimal(quotaTotal));
        // 手续费
        BigDecimal feeAmount = prepay.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        // 总扣款
        BigDecimal totalPay = prepay.add(feeAmount);

        BountyTask task = new BountyTask();
        task.setUserId(userId);
        task.setTaskName(taskName);
        task.setTaskTitle(taskTitle);
        task.setTaskDesc(taskDesc);
        task.setTaskSteps(taskSteps);
        task.setVideoUrl(videoUrl);
        task.setDownloadImgs(downloadImgs);
        task.setIdTip(idTip);

        task.setUnitPrice(unitPrice);
        task.setQuotaTotal(quotaTotal);
        task.setQuotaRemain(quotaTotal);
        task.setTakeLimitHours(takeLimitHours);

        task.setJoinCount(0);
        task.setFinishCount(0);
        task.setStatus(TASK_STATUS_ONLINE);

        task.setCapitalType(UserCapitalTypeEnum.hxjf.getValue());
        task.setEscrowAmount(prepay);
        task.setFeeRate(feeRate);
        task.setFeeAmount(feeAmount);

        Long taskId = bountyTaskService.addTask(task);

        // 执行扣款业务
        String orderNo = "BOUNTY_PUB_" + taskId;
        userCapitalService.subUserBalanceByBountyPublish(
                userId, totalPay, UserCapitalTypeEnum.hxjf.getValue(), orderNo, taskId, LogCapitalTypeEnum.bounty_publish_pay
        );

        // 手续费注入奖池
        bountyFeePoolService.initIfAbsent();
        bountyFeePoolService.addPoolCents(toCents(feeAmount));

        JSONObject response = new JSONObject();
        response.put("taskId", taskId);
        response.put("prepay", prepay);
        response.put("feeAmount", feeAmount);
        response.put("totalPay", totalPay);
        return response;
    }

    /**
     * 悬赏任务-取消任务
     * 发布者主动下架任务，将剩余名额对应的托管资金退回到发布者账户，手续费不退
     */
    @Transactional
    @ServiceMethod(code = "004", description = "悬赏任务-取消任务")
    public JSONObject cancelTask(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");
        if (!Objects.equals(task.getUserId(), userId)) throwExp("无权限");
        if (task.getStatus() != TASK_STATUS_ONLINE) throwExp("任务已下架");

        Integer remain = task.getQuotaRemain();
        if (remain == null) remain = 0;

        // 计算应退款金额 = 单价 * 剩余名额
        BigDecimal refund = task.getUnitPrice().multiply(new BigDecimal(remain));

        // 更新任务状态
        task.setStatus(TASK_STATUS_CANCEL);
        task.setQuotaRemain(0);

        // 扣减数据库中的托管金记录
        if (task.getEscrowAmount() != null && refund.compareTo(BigDecimal.ZERO) > 0) {
            task.setEscrowAmount(task.getEscrowAmount().subtract(refund));
            if (task.getEscrowAmount().compareTo(BigDecimal.ZERO) < 0) {
                task.setEscrowAmount(BigDecimal.ZERO);
            }
        }
        bountyTaskService.updateTask(task);

        // 执行资产退还
        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            JSONArray rewards = new JSONArray();
            JSONObject reward = new JSONObject();
            reward.put("type", 1);
            reward.put("id", UserCapitalTypeEnum.hxjf.getValue());
            reward.put("number", refund);
            rewards.add(reward);
            playGameService.addReward(userId, rewards, LogCapitalTypeEnum.bounty_cancel_refund, LogUserBackpackTypeEnum.bounty);
        }

        JSONObject response = new JSONObject();
        response.put("taskId", taskId);
        response.put("refund", refund);
        response.put("status", TASK_STATUS_CANCEL);
        return response;
    }

    /**
     * 悬赏任务-用户接单
     * 用户领取任务，生成订单，扣减任务剩余名额。
     */
    @Transactional
    @ServiceMethod(code = "005", description = "悬赏任务-接单")
    public JSONObject takeTask(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");
        loadAndCheckUser(userId);

        String orderNo = "BOUNTY_" + System.currentTimeMillis() + "_" + userId + "_" + taskId;
        BountyTask task = bountyTaskService.findById(taskId);

        if (task == null) throwExp("任务不存在");
        if (task.getStatus() != TASK_STATUS_ONLINE) throwExp("任务已下架");
        if (task.getQuotaRemain() == null || task.getQuotaRemain() <= 0) throwExp("名额已满");

        BountyTaskOrder latestOrder = findLatestOrder(taskId, userId);
        if (latestOrder != null) {
            refreshTimeoutIfNeeded(task, latestOrder);
            if (isOrderAlive(latestOrder)) {
                throwExp("你已接过该任务");
            }
        }

        Date now = new Date();
        BountyTaskOrder order = new BountyTaskOrder();
        order.setTaskId(taskId);
        order.setPublisherUserId(task.getUserId());
        order.setUserId(userId);
        order.setUnitPrice(task.getUnitPrice());
        order.setCapitalType(task.getCapitalType());
        // 设置状态为进行中
        order.setStatus(BountyStatusEnum.DOING.getCode());
        order.setTakeTime(now);
        // 计算截止时间
        order.setDeadlineTime(addHours(now, task.getTakeLimitHours()));
        order.setOrderNo(orderNo);
        order.setAppealStatus(APPEAL_STATUS_NONE);
        Long orderId = bountyTaskOrderService.addOrder(order);

        // 更新任务统计数据 参与数+1，剩余名额-1
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("taskId", taskId);
        updateMap.put("joinCountDelta", 1);
        updateMap.put("quotaRemainDelta", -1);
        bountyTaskService.updateCounts(updateMap);

        JSONObject response = new JSONObject();
        response.put("orderId", orderId);
        response.put("deadlineTime", order.getDeadlineTime());
        return response;
    }

    /**
     * 悬赏任务-取消接单 (用户放弃任务)
     * 用户主动取消进行中的任务，如果是上架状态则恢复名额。
     */
    @Transactional
    @ServiceMethod(code = "006", description = "悬赏任务-取消接单")
    public JSONObject cancelOrder(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");

        BountyTaskOrder order = findLatestOrder(taskId, userId);
        if (order == null) throwExp("未找到接单记录");

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");

        refreshTimeoutIfNeeded(task, order);

        if (order.getStatus() == BountyStatusEnum.TIMEOUT.getCode()) {
            throwExp("订单已超时");
        }
        if (order.getStatus() != BountyStatusEnum.DOING.getCode()) {
            throwExp("当前状态不可取消");
        }

        order.setStatus(BountyStatusEnum.CANCEL.getCode());
        order.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(order);

        // 任务仍在上架时恢复名额
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("taskId", taskId);
        if (task.getStatus() == TASK_STATUS_ONLINE) {
            updateMap.put("quotaRemainDelta", 1);
        }
        bountyTaskService.updateCounts(updateMap);

        JSONObject response = new JSONObject();
        response.put("orderId", order.getId());
        response.put("status", BountyStatusEnum.CANCEL.getCode());
        return response;
    }

    /**
     * 悬赏任务-提交任务材料
     * 用户完成任务后提交截图凭证，状态变更为“待审核”。
     */
    @Transactional
    @ServiceMethod(code = "007", description = "悬赏任务-完成任务提交材料")
    public JSONObject submitOrder(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"), params.get("submitImgs"), params.get("submitUserId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");

        BountyTaskOrder order = findLatestOrder(taskId, userId);
        if (order == null) throwExp("未找到接单记录");

        refreshTimeoutIfNeeded(task, order);
        if (order.getStatus() == BountyStatusEnum.TIMEOUT.getCode()) throwExp("订单已超时");
        if (order.getStatus() != BountyStatusEnum.DOING.getCode()) throwExp("当前状态不可提交");

        // 校验提交的图片
        String submitImgs = normalizeAndCheckImgArray(params.getString("submitImgs"), "submitImgs", 1, 10);
        String submitUserId = params.getString("submitUserId");

        // 更新订单信息
        order.setSubmitImgs(submitImgs);
        order.setSubmitUserId(submitUserId);
        order.setSubmitTime(new Date());
        order.setStatus(BountyStatusEnum.SUBMIT.getCode());
        order.setUpdateTime(new Date());

        // 重置申诉状态
        order.setAppealStatus(APPEAL_STATUS_NONE);
        order.setAppealReason(null);
        order.setAppealTime(null);

        bountyTaskOrderService.updateOrder(order);

        JSONObject response = new JSONObject();
        response.put("orderId", order.getId());
        response.put("status", BountyStatusEnum.SUBMIT.getCode());
        return response;
    }

    /**
     * 悬赏任务-被驳回后重新提交
     * 用户在被驳回后，修改材料再次提交审核。
     */
    @Transactional
    @ServiceMethod(code = "008", description = "悬赏任务-重新提交")
    public JSONObject resubmitOrder(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"), params.get("submitImgs"), params.get("submitUserId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");

        BountyTaskOrder order = findLatestOrder(taskId, userId);
        if (order == null) throwExp("未找到订单");
        if (order.getStatus() != BountyStatusEnum.REJECT.getCode()) throwExp("当前状态不可重新提交");

        refreshTimeoutIfNeeded(task, order);
        if (order.getStatus() == BountyStatusEnum.TIMEOUT.getCode()) throwExp("订单已超时");

        String submitImgs = normalizeAndCheckImgArray(params.getString("submitImgs"), "submitImgs", 1, 10);
        String submitUserId = params.getString("submitUserId");

        // 重新进入待审核状态
        order.setStatus(BountyStatusEnum.SUBMIT.getCode());
        order.setSubmitImgs(submitImgs);
        order.setSubmitUserId(submitUserId);
        order.setSubmitTime(new Date());
        order.setUpdateTime(new Date());

        // 清除上一轮的驳回信息
        order.setRejectReason(null);
        order.setAuditTime(null);

        bountyTaskOrderService.updateOrder(order);

        JSONObject response = new JSONObject();
        response.put("orderId", order.getId());
        response.put("status", BountyStatusEnum.SUBMIT.getCode());
        return response;
    }

    /**
     * 悬赏任务-发起申诉
     * 用户对驳回结果不满意，发起申诉。
     */
    @Transactional
    @ServiceMethod(code = "009", description = "悬赏任务-申诉")
    public JSONObject appealOrder(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"), params.get("appealReason"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");
        String appealReason = params.getString("appealReason");

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");

        BountyTaskOrder order = findLatestOrder(taskId, userId);
        if (order == null) throwExp("未找到订单");
        if (order.getStatus() != BountyStatusEnum.REJECT.getCode()) throwExp("仅未通过订单可申诉");

        refreshTimeoutIfNeeded(task, order);
        if (order.getStatus() == BountyStatusEnum.TIMEOUT.getCode()) throwExp("订单已超时");

        // 状态流转:已驳回 -> 申诉中
        order.setStatus(BountyStatusEnum.APPEAL.getCode());
        order.setAppealStatus(APPEAL_STATUS_DOING);
        order.setAppealReason(appealReason);
        order.setAppealTime(new Date());
        order.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(order);

        JSONObject response = new JSONObject();
        response.put("orderId", order.getId());
        response.put("status", BountyStatusEnum.APPEAL.getCode());
        return response;
    }

    /**
     * 悬赏任务-我的接单列表查询
     * 根据前端 Tab 页签获取对应的订单列表。
     */
    @ServiceMethod(code = "010", description = "悬赏任务-我的接单列表")
    public JSONObject myOrders(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");

        Integer tab = params.getInteger("tab");
        int pageNo = params.getIntValue("pageNo");
        int pageSize = params.getIntValue("pageSize");
        if (pageNo <= 0) pageNo = 1;
        if (pageSize <= 0) pageSize = 10;
        if (pageSize > 50) pageSize = 50;
        int offset = (pageNo - 1) * pageSize;

        // 通过枚举策略获取该Tab对应的状态码列表
        List<Integer> statusList = BountyStatusEnum.getCodesByTab(tab);

        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("userId", userId);
        queryMap.put("statuses", statusList);
        queryMap.put("offset", offset);
        queryMap.put("limit", pageSize);

        List<BountyTaskOrder> list = bountyTaskOrderService.findListByConditions(queryMap);

        JSONArray resultArray = new JSONArray();
        if (list != null) {
            for (BountyTaskOrder order : list) {
                // 检查关联任务的状态，刷新超时情况
                BountyTask task = bountyTaskService.findById(order.getTaskId());
                if (task != null) {
                    refreshTimeoutIfNeeded(task, order);
                }
                resultArray.add(orderToJson(order));
            }
        }

        JSONObject response = new JSONObject();
        response.put("list", resultArray);
        response.put("pageNo", pageNo);
        response.put("pageSize", pageSize);
        return response;
    }

    /**
     * 悬赏任务-我的发布列表查询
     * 查询当前用户发布的所有任务。
     */
    @ServiceMethod(code = "011", description = "悬赏任务-我的发布列表")
    public JSONObject myPublish(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");

        int pageNo = params.getIntValue("pageNo");
        int pageSize = params.getIntValue("pageSize");
        if (pageNo <= 0) pageNo = 1;
        if (pageSize <= 0) pageSize = 10;
        if (pageSize > 50) pageSize = 50;
        int offset = (pageNo - 1) * pageSize;

        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("userId", userId);
        queryMap.put("offset", offset);
        queryMap.put("limit", pageSize);

        List<BountyTask> list = bountyTaskService.findListByConditions(queryMap);

        JSONArray resultArray = new JSONArray();
        if (list != null) {
            for (BountyTask task : list) {
                resultArray.add(beanToJson(task));
            }
        }

        JSONObject response = new JSONObject();
        response.put("list", resultArray);
        response.put("pageNo", pageNo);
        response.put("pageSize", pageSize);
        return response;
    }

    /**
     * 悬赏任务-我发布的待审核列表
     * 查询所有待我审核（或申诉中）的订单。
     */
    @ServiceMethod(code = "012", description = "悬赏任务-我发布的待审核列表")
    public JSONObject pendingAudit(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");

        int pageNo = params.getIntValue("pageNo");
        int pageSize = params.getIntValue("pageSize");
        if (pageNo <= 0) pageNo = 1;
        if (pageSize <= 0) pageSize = 10;
        if (pageSize > 50) pageSize = 50;
        int offset = (pageNo - 1) * pageSize;

        Map<String, Object> queryMap = new HashMap<>();
        // 查询提交待审或者申诉中的状态
        List<Integer> statuses = Arrays.asList(
                BountyStatusEnum.SUBMIT.getCode(),
                BountyStatusEnum.APPEAL.getCode()
        );

        queryMap.put("publisherUserId", userId);
        queryMap.put("statuses", statuses);
        queryMap.put("offset", offset);
        queryMap.put("limit", pageSize);

        List<BountyTaskOrder> list = bountyTaskOrderService.findListByConditions(queryMap);
        JSONArray resultArray = new JSONArray();
        if (list != null) {
            for (BountyTaskOrder order : list) {
                resultArray.add(orderToJson(order));
            }
        }
        JSONObject response = new JSONObject();
        response.put("list", resultArray);
        response.put("pageNo", pageNo);
        response.put("pageSize", pageSize);
        return response;
    }

    /**
     * 悬赏任务-发布者审核通过
     * 发布者认可提交内容，订单完成，系统将托管赏金发放给接单用户。
     */
    @Transactional
    @ServiceMethod(code = "013", description = "悬赏任务-审核通过")
    public JSONObject auditApprove(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("orderId"));
        Long userId = params.getLong("userId");
        Long orderId = params.getLong("orderId");

        BountyTaskOrder order = bountyTaskOrderService.findById(orderId);
        if (order == null) throwExp("订单不存在");
        if (!Objects.equals(order.getPublisherUserId(), userId)) throwExp("无权限");
        // 只能审核 待审核、申诉中 的订单
        if (order.getStatus() != BountyStatusEnum.SUBMIT.getCode() && order.getStatus() != BountyStatusEnum.APPEAL.getCode()) {
            throwExp("订单状态不可审核");
        }

        boolean isAppeal = (order.getStatus() == BountyStatusEnum.APPEAL.getCode());
        if (isAppeal) {
            if (order.getAppealStatus() == null || order.getAppealStatus() != APPEAL_STATUS_DOING) {
                throwExp("申诉状态不可处理");
            }
        }

        BountyTask task = bountyTaskService.findById(order.getTaskId());
        if (task == null) throwExp("任务不存在");
        // 校验托管金是否充足
        if (task.getEscrowAmount() == null || task.getEscrowAmount().compareTo(order.getUnitPrice()) < 0) {
            throwExp("托管金不足");
        }

        // 发放奖励给接单者
        JSONArray rewards = new JSONArray();
        JSONObject reward = new JSONObject();
        reward.put("type", 1);
        reward.put("id", String.valueOf(order.getCapitalType()));
        reward.put("number", order.getUnitPrice());
        rewards.add(reward);
        playGameService.addReward(order.getUserId(), rewards, LogCapitalTypeEnum.bounty_order_reward, LogUserBackpackTypeEnum.bounty);

        Date now = new Date();
        // 更新订单状态为已完成
        order.setStatus(BountyStatusEnum.DONE.getCode());
        order.setAuditTime(now);
        order.setUpdateTime(now);

        // 如果是申诉通过，记录处理信息
        if (isAppeal) {
            order.setAppealStatus(APPEAL_STATUS_DONE);
            order.setAppealHandleUserId(userId);
            order.setAppealHandleTime(now);
            order.setAppealHandleReason(params.getString("appealHandleReason"));
        }
        // 审核通过后不再保留驳回原因
        order.setRejectReason(null);
        bountyTaskOrderService.updateOrder(order);

        // 更新任务完成数
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("taskId", task.getId());
        updateMap.put("finishCountDelta", 1);
        bountyTaskService.updateCounts(updateMap);

        // 扣除任务托管金余额
        task.setEscrowAmount(task.getEscrowAmount().subtract(order.getUnitPrice()));
        if (task.getEscrowAmount().compareTo(BigDecimal.ZERO) < 0) task.setEscrowAmount(BigDecimal.ZERO);
        bountyTaskService.updateTask(task);

        JSONObject response = new JSONObject();
        response.put("orderId", orderId);
        response.put("status", BountyStatusEnum.DONE.getCode());
        return response;
    }

    /**
     * 悬赏任务-发布者审核驳回
     * 订单变更为已驳回，等待用户重新提交或申诉。
     */
    @Transactional
    @ServiceMethod(code = "014", description = "悬赏任务-审核驳回")
    public JSONObject auditReject(ManagerSocketServer socket, JSONObject params) {
        Date now = new Date();
        checkNull(params);
        checkNull(params.get("userId"), params.get("orderId"), params.get("rejectReason"));
        Long userId = params.getLong("userId");
        Long orderId = params.getLong("orderId");
        String rejectReason = params.getString("rejectReason");
        rejectReason = rejectReason.trim();

        BountyTaskOrder order = bountyTaskOrderService.findById(orderId);
        if (order == null) throwExp("订单不存在");
        if (!Objects.equals(order.getPublisherUserId(), userId)) throwExp("无权限");

        if (order.getStatus() != BountyStatusEnum.SUBMIT.getCode() && order.getStatus() != BountyStatusEnum.APPEAL.getCode()) {
            throwExp("订单状态不可驳回");
        }

        boolean isAppeal = (order.getStatus() == BountyStatusEnum.APPEAL.getCode());
        if (isAppeal) {
            if (order.getAppealStatus() == null || order.getAppealStatus() != APPEAL_STATUS_DOING) {
                throwExp("申诉状态不可处理");
            }
            // 申诉被驳回
            order.setAppealStatus(APPEAL_STATUS_DONE);
            order.setAppealHandleUserId(userId);
            order.setAppealHandleTime(now);
            order.setAppealHandleReason(rejectReason);
        }

        // 状态变更为已驳回
        order.setStatus(BountyStatusEnum.REJECT.getCode());
        order.setRejectReason(rejectReason);
        order.setAuditTime(now);
        order.setUpdateTime(now);
        bountyTaskOrderService.updateOrder(order);

        JSONObject response = new JSONObject();
        response.put("orderId", orderId);
        response.put("status", BountyStatusEnum.REJECT.getCode());
        response.put("rejectReason", rejectReason);
        return response;
    }

    /**
     * 悬赏任务-获取 OSS 直传签名
     * 前端获取上传策略后直接将文件上传到阿里云
     */
    @ServiceMethod(code = "015", description = "悬赏任务-OSS直传签名")
    public JSONObject getOssDirectUploadPolicy(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        String biz = params.getString("biz");
        if (!StringUtils.hasText(biz)) {
            biz = "bounty";
        }
        String suffix = params.getString("suffix");
        return aliOssDirectUploadService.buildPostPolicy(biz, userId, suffix);
    }



    /**
     * OSS URL 校验
     */
    private String canonicalizeAndCheckOssUrl(String raw, String fieldName, boolean allowEmpty) {
        if (!StringUtils.hasText(raw)) {
            if (allowEmpty) return null;
            throwExp(fieldName + "不能为空");
            return null;
        }

        String value = raw.trim();
        if (value.startsWith("http://") || value.startsWith("https://")) {
            //校验 URL Host 是否合法
            checkOssUrlHostAllowed(value, fieldName);
            return value;
        }

        String urlPrefix = aliOssDirectUploadService.buildUrlPrefix();
        if (!StringUtils.hasText(urlPrefix)) {
            throwExp("OSS 配置缺失，无法生成" + fieldName + "地址");
            return null;
        }
        while (value.startsWith("/")) value = value.substring(1);
        return urlPrefix + "/" + value;
    }

    /**
     * 校验 URL Host 是否合法
     */
    private void checkOssUrlHostAllowed(String url, String fieldName) {
        JSONObject ossConfig = aliOssDirectUploadService.getOssConfig();
        if (ossConfig == null || ossConfig.isEmpty()) return;

        String urlPrefix = aliOssDirectUploadService.buildUrlPrefix();
        String standardHost = aliOssDirectUploadService.buildHost();

        String allowedHost1 = null;
        String allowedHost2 = null;
        try {
            if (StringUtils.hasText(urlPrefix)) allowedHost1 = URI.create(urlPrefix).getHost();
            if (StringUtils.hasText(standardHost)) allowedHost2 = URI.create(standardHost).getHost();
        } catch (Exception ignored) {
        }

        String inputHost;
        try {
            inputHost = URI.create(url).getHost();
        } catch (Exception e) {
            throwExp(fieldName + "地址格式错误");
            return;
        }

        if (inputHost == null) {
            throwExp(fieldName + "地址格式错误");
            return;
        }

        boolean match1 = allowedHost1 != null && inputHost.equalsIgnoreCase(allowedHost1);
        boolean match2 = allowedHost2 != null && inputHost.equalsIgnoreCase(allowedHost2);

        if (!match1 && !match2) {
            throwExp(fieldName + "非法，请上传至官方服务器");
        }
    }

    /**
     * 解析并校验图片数组字符串
     */
    private String normalizeAndCheckImgArray(String jsonArrStr, String fieldName, int min, int max) {
        if (!StringUtils.hasText(jsonArrStr)) {
            throwExp(fieldName + "不能为空");
            return null;
        }

        JSONArray array;
        String s = jsonArrStr.trim();
        try {
            if (!s.startsWith("[")) {
                array = new JSONArray();
                array.add(s);
            } else {
                array = JSONArray.parseArray(s);
            }
        } catch (Exception e) {
            throwExp(fieldName + "格式错误");
            return null;
        }

        if (array == null || array.isEmpty()) throwExp(fieldName + "至少需要" + min + "张");
        if (array.size() < min || array.size() > max) {
            throwExp(fieldName + "数量范围必须是" + min + "~" + max);
        }
        for (int i = 0; i < array.size(); i++) {
            String val = array.getString(i);
            if (!StringUtils.hasText(val)) {
                throwExp(fieldName + "第" + (i + 1) + "项为空");
            }
            array.set(i, canonicalizeAndCheckOssUrl(val, fieldName, false));
        }
        return array.toJSONString();
    }

    /**
     * 加载用户信息并校验是否存在
     */
    private User loadAndCheckUser(Long userId) {
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = (users != null) ? users.get(userId) : null;
        if (user == null) {
            throwExp("用户不存在");
        }
        return user;
    }

    /**
     * 查找用户最近一次对该任务的订单
     */
    private BountyTaskOrder findLatestOrder(Long taskId, Long userId) {
        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("taskId", taskId);
        queryMap.put("userId", userId);
        queryMap.put("limit", 1);
        List<BountyTaskOrder> list = bountyTaskOrderService.findListByConditions(queryMap);
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    /**
     * 判断订单是否处于活跃状态
     * 活跃状态包括：进行中、提交待审核、申诉中。
     * 已结束状态包括：已完成、已取消、已超时。
     */
    private boolean isOrderAlive(BountyTaskOrder order) {
        if (order == null || order.getStatus() == null) return false;
        if (order.getStatus() == BountyStatusEnum.DONE.getCode()) return false;
        if (order.getStatus() == BountyStatusEnum.CANCEL.getCode()) return false;
        if (order.getStatus() == BountyStatusEnum.TIMEOUT.getCode()) return false;

        if (order.getStatus() == BountyStatusEnum.SUBMIT.getCode()) return true;

        if (order.getDeadlineTime() != null && order.getDeadlineTime().before(new Date())) {
            return false;
        }
        return true;
    }

    /**
     * 检查并刷新订单超时状态
     * 如果当前时间超过订单截止时间，强制将状态置为“已超时”并释放名额。
     */
    private void refreshTimeoutIfNeeded(BountyTask task, BountyTaskOrder order) {
        if (task == null || order == null || order.getStatus() == null) return;

        // 只有 进行中、已驳回、申诉中 状态受时间限制
        if (order.getStatus() != BountyStatusEnum.DOING.getCode()
                && order.getStatus() != BountyStatusEnum.REJECT.getCode()
                && order.getStatus() != BountyStatusEnum.APPEAL.getCode()) {
            return;
        }
        if (order.getDeadlineTime() == null) return;
        if (!order.getDeadlineTime().before(new Date())) return;

        // 标记超时
        order.setStatus(BountyStatusEnum.TIMEOUT.getCode());
        order.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(order);

        // 释放任务名额
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("taskId", task.getId());
        if (task.getStatus() != null && task.getStatus() == TASK_STATUS_ONLINE) {
            updateMap.put("quotaRemainDelta", 1);
        }
        bountyTaskService.updateCounts(updateMap);
    }

    /**
     * 金额单位转换
     */
    private long toCents(BigDecimal amount) {
        if (amount == null) return 0L;
        return amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
    }

    /**
     * bean 转换 JSON
     */
    private JSONObject beanToJson(BountyTask task) {
        JSONObject json = new JSONObject();
        json.put("taskId", task.getId());
        json.put("userId", task.getUserId());
        json.put("taskTitle", task.getTaskTitle());
        json.put("taskName", task.getTaskName());
        json.put("taskDesc", task.getTaskDesc());
        json.put("taskSteps", task.getTaskSteps());
        json.put("videoUrl", task.getVideoUrl());
        json.put("downloadImgs", task.getDownloadImgs());
        json.put("idTip", task.getIdTip());
        json.put("unitPrice", task.getUnitPrice());
        json.put("quotaTotal", task.getQuotaTotal());
        json.put("quotaRemain", task.getQuotaRemain());
        json.put("takeLimitHours", task.getTakeLimitHours());
        json.put("status", task.getStatus());
        json.put("joinCount", task.getJoinCount());
        json.put("finishCount", task.getFinishCount());
        json.put("capitalType", task.getCapitalType());
        json.put("escrowAmount", task.getEscrowAmount());
        json.put("feeAmount", task.getFeeAmount());
        json.put("feeRate", task.getFeeRate());
        json.put("createTime", task.getCreateTime());
        json.put("updateTime", task.getUpdateTime());
        return json;
    }

    /**
     * bean 转换 JSON
     */
    private JSONObject orderToJson(BountyTaskOrder order) {
        JSONObject json = new JSONObject();
        json.put("orderId", order.getId());
        json.put("taskId", order.getTaskId());
        json.put("publisherUserId", order.getPublisherUserId());
        json.put("userId", order.getUserId());
        json.put("unitPrice", order.getUnitPrice());
        json.put("status", order.getStatus());
        json.put("takeTime", order.getTakeTime());
        json.put("deadlineTime", order.getDeadlineTime());
        json.put("submitTime", order.getSubmitTime());
        json.put("submitImgs", order.getSubmitImgs());
        json.put("submitUserId", order.getSubmitUserId());
        json.put("rejectReason", order.getRejectReason());
        json.put("appealStatus", order.getAppealStatus());
        json.put("appealReason", order.getAppealReason());
        json.put("appealTime", order.getAppealTime());
        json.put("appealHandleReason", order.getAppealHandleReason());
        json.put("appealHandleTime", order.getAppealHandleTime());
        json.put("appealHandleUserId", order.getAppealHandleUserId());
        return json;
    }

    /**
     * 当前时间 + N小时
     */
    private Date addHours(Date now, Integer hours) {
        if (now == null || hours == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.HOUR_OF_DAY, hours);
        return calendar.getTime();
    }
}