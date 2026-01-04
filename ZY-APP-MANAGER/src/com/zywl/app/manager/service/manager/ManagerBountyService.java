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
import com.zywl.app.defaultx.enmus.LogCapitalTypeEnum;
import com.zywl.app.defaultx.enmus.LogUserBackpackTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.BountyFeePoolService;
import com.zywl.app.defaultx.service.BountyTaskOrderService;
import com.zywl.app.defaultx.service.BountyTaskService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.PlayGameService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * @Author: lzx
 * @Create: 2026/1/3
 * @Version: V1.0
 * @Description: 悬赏任务 Manager Service
 * @Task: 039 (MessageCodeContext.BOUNTY_TASK)
 */
@Service
@ServiceClass(code = MessageCodeContext.BOUNTY_TASK)
public class ManagerBountyService extends BaseService {

    //上架
    private static final int TASK_STATUS_ONLINE = 1;
    //取消
    private static final int TASK_STATUS_CANCEL = 2;

    /*订单状态*/
    // 待完成/进行中
    private static final int ORDER_STATUS_DOING = 0;
    // 已提交待审核
    private static final int ORDER_STATUS_SUBMIT = 1;
    // 已完成
    private static final int ORDER_STATUS_DONE = 2;
    // 已驳回
    private static final int ORDER_STATUS_REJECT = 3;
    // 申诉中
    private static final int ORDER_STATUS_APPEAL = 4;
    // 已取消
    private static final int ORDER_STATUS_CANCEL = 5;
    // 已超时
    private static final int ORDER_STATUS_TIMEOUT = 6;

    // 申诉处理状态
    private static final int APPEAL_STATUS_NONE = 0;
    private static final int APPEAL_STATUS_DOING = 1;
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

    /**
     * 悬赏任务-大厅列表
     * **/
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

        Map<String, Object> query = new HashMap<>();
        query.put("status", TASK_STATUS_ONLINE);
        query.put("orderType", orderType);
        query.put("keyword", keyword);
        query.put("offset", offset);
        query.put("limit", pageSize);

        List<BountyTask> list = bountyTaskService.findListByConditions(query);

        Map<String, Object> countQuery = new HashMap<>();
        countQuery.put("status", TASK_STATUS_ONLINE);
        countQuery.put("keyword", keyword);
        int total = bountyTaskService.countByConditions(countQuery);

        JSONArray arr = new JSONArray();
        if (list != null) {
            for (BountyTask t : list) {
                JSONObject o = new JSONObject();
                o.put("taskId", t.getId());
                o.put("taskTitle", t.getTaskTitle());
                o.put("taskName", t.getTaskName());
                o.put("unitPrice", t.getUnitPrice());
                o.put("quotaTotal", t.getQuotaTotal());
                o.put("quotaRemain", t.getQuotaRemain());
                o.put("joinCount", t.getJoinCount());
                o.put("finishCount", t.getFinishCount());
                o.put("takeLimitHours", t.getTakeLimitHours());
                o.put("createTime", t.getCreateTime());
                arr.add(o);
            }
        }

        JSONObject res = new JSONObject();
        res.put("list", arr);
        res.put("total", total);
        res.put("pageNo", pageNo);
        res.put("pageSize", pageSize);
        return res;
    }

    /**
     * 悬赏任务-任务详情
     * **/
    @ServiceMethod(code = "002", description = "悬赏任务-任务详情")
    public JSONObject getTaskDetail(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");

        BountyTaskOrder my = findLatestOrder(taskId, userId);
        if (my != null) {
            refreshTimeoutIfNeeded(task, my);
        }

        if (task.getStatus() != TASK_STATUS_ONLINE
                && !Objects.equals(task.getUserId(), userId)
                && my == null) {
            throwExp("任务已下架");
        }

        JSONObject res = new JSONObject();
        res.put("task", beanToJson(task));
        res.put("myOrder", my == null ? null : orderToJson(my));
        res.put("serverTime", System.currentTimeMillis());
        return res;
    }

    /**
     * 悬赏任务-发布
     * **/
    @Transactional
    @ServiceMethod(code = "003", description = "悬赏任务-发布")
    public JSONObject publishTask(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        // 必填
        checkNull(params.get("taskName"), params.get("taskTitle"), params.get("taskDesc"),
                params.get("unitPrice"), params.get("quotaTotal"), params.get("takeLimitHours"),
                params.get("downloadImgs"));

        String taskName = params.getString("taskName");
        String taskTitle = params.getString("taskTitle");
        String taskDesc = params.getString("taskDesc");
        String taskSteps = params.getString("taskSteps");
        String videoUrl = params.getString("videoUrl");
        // 下载图可以是JSON串
        String downloadImgs = params.getString("downloadImgs");
        // 个人ID提示文案
        String idTip = params.getString("idTip");

        BigDecimal unitPrice = params.getBigDecimal("unitPrice");
        Integer quotaTotal = params.getInteger("quotaTotal");
        Integer takeLimitHours = params.getInteger("takeLimitHours");

        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) throwExp("单价错误");
        if (quotaTotal == null || quotaTotal <= 0) throwExp("名额错误");
        if (takeLimitHours == null || takeLimitHours < 1 || takeLimitHours > 72) throwExp("接单时限范围1~72小时");

        // 读取手续费比例
        String feeRateStr = managerConfigService.getString(Config.BOUNTY_FEE_RATE);
        if (feeRateStr == null || feeRateStr.trim().isEmpty()) throwExp("手续费配置缺失");
        BigDecimal feeRate = new BigDecimal(feeRateStr);
        // 托管金=单价*名额
        BigDecimal prepay = unitPrice.multiply(new BigDecimal(quotaTotal));
        BigDecimal feeAmount = prepay.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalPay = prepay.add(feeAmount);

        // 先写DB
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

        // 扣费
        String orderNo = "BOUNTY_PUB_" + taskId;
        userCapitalService.subUserBalanceByBountyPublish(
                userId, totalPay, UserCapitalTypeEnum.hxjf.getValue(), orderNo, taskId, LogCapitalTypeEnum.bounty_publish_pay
        );

        // 手续费进奖池
        bountyFeePoolService.initIfAbsent();
        bountyFeePoolService.addPoolCents(toCents(feeAmount));

        JSONObject res = new JSONObject();
        res.put("taskId", taskId);
        res.put("prepay", prepay);
        res.put("feeAmount", feeAmount);
        res.put("totalPay", totalPay);
        return res;
    }


    /**
     * 悬赏任务-取消任务
     * **/
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

        BigDecimal refund = task.getUnitPrice().multiply(new BigDecimal(remain));

        task.setStatus(TASK_STATUS_CANCEL);
        task.setQuotaRemain(0);

        if (task.getEscrowAmount() != null && refund.compareTo(BigDecimal.ZERO) > 0) {
            task.setEscrowAmount(task.getEscrowAmount().subtract(refund));
            if (task.getEscrowAmount().compareTo(BigDecimal.ZERO) < 0) {
                task.setEscrowAmount(BigDecimal.ZERO);
            }
        }
        bountyTaskService.updateTask(task);

        if (refund.compareTo(BigDecimal.ZERO) > 0) {
            JSONArray rewards = new JSONArray();
            JSONObject r = new JSONObject();
            r.put("type", 1);
            r.put("id", UserCapitalTypeEnum.hxjf.getValue());
            r.put("number", refund);
            rewards.add(r);
            playGameService.addReward(userId, rewards, LogCapitalTypeEnum.bounty_cancel_refund, LogUserBackpackTypeEnum.bounty);
        }

        JSONObject res = new JSONObject();
        res.put("taskId", taskId);
        res.put("refund", refund);
        res.put("status", TASK_STATUS_CANCEL);
        return res;
    }

    /**
     * 悬赏任务-接单
     * **/
    @Transactional
    @ServiceMethod(code = "005", description = "悬赏任务-接单")
    public JSONObject takeTask(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("taskId"));
        Long userId = params.getLong("userId");
        Long taskId = params.getLong("taskId");
        loadAndCheckUser(userId);

        BountyTask task = bountyTaskService.findById(taskId);
        if (task == null) throwExp("任务不存在");
        if (task.getStatus() != TASK_STATUS_ONLINE) throwExp("任务已下架");
        if (task.getQuotaRemain() == null || task.getQuotaRemain() <= 0) throwExp("名额已满");

        BountyTaskOrder latest = findLatestOrder(taskId, userId);
        if (latest != null) {
            refreshTimeoutIfNeeded(task, latest);
            if (isOrderAlive(latest)) {
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
        order.setStatus(ORDER_STATUS_DOING);
        order.setTakeTime(now);
        order.setDeadlineTime(addHours(now, task.getTakeLimitHours()));

        Long orderId = bountyTaskOrderService.addOrder(order);

        Map<String, Object> upd = new HashMap<>();
        upd.put("id", taskId);
        upd.put("joinCountDelta", 1);
        upd.put("quotaRemainDelta", -1);
        bountyTaskService.updateCounts(upd);

        JSONObject res = new JSONObject();
        res.put("orderId", orderId);
        res.put("deadlineTime", order.getDeadlineTime());
        return res;
    }

   /**
    * 悬赏任务-取消接单
    * **/
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
        if (order.getStatus() == ORDER_STATUS_TIMEOUT) {
            throwExp("订单已超时");
        }

        if (order.getStatus() != ORDER_STATUS_DOING) {
            throwExp("当前状态不可取消");
        }

        order.setStatus(ORDER_STATUS_CANCEL);
        order.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(order);

        Map<String, Object> upd = new HashMap<>();
        upd.put("id", taskId);
        upd.put("joinCountDelta", -1);
        if (task.getStatus() == TASK_STATUS_ONLINE) {
            upd.put("quotaRemainDelta", 1);
        }
        bountyTaskService.updateCounts(upd);

        JSONObject res = new JSONObject();
        res.put("orderId", order.getId());
        res.put("status", ORDER_STATUS_CANCEL);
        return res;
    }

    /**
     * 悬赏任务-提交材料
     * **/
    @Transactional
    @ServiceMethod(code = "007", description = "悬赏任务-提交材料")
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
        if (order.getStatus() == ORDER_STATUS_TIMEOUT) throwExp("订单已超时");

        if (order.getStatus() != ORDER_STATUS_DOING) throwExp("当前状态不可提交");

        String submitImgs = normalizeAndCheckImgArray(params.getString("submitImgs"), "submitImgs", 1, 10);
        String submitUserId = params.getString("submitUserId");

        order.setSubmitImgs(submitImgs);
        order.setSubmitUserId(submitUserId);
        order.setSubmitTime(new Date());
        order.setStatus(ORDER_STATUS_SUBMIT);
        order.setUpdateTime(new Date());

        order.setAppealStatus(APPEAL_STATUS_NONE);
        order.setAppealReason(null);
        order.setAppealTime(null);

        bountyTaskOrderService.updateOrder(order);

        JSONObject res = new JSONObject();
        res.put("orderId", order.getId());
        res.put("status", ORDER_STATUS_SUBMIT);
        return res;
    }

    /**
     * 悬赏任务-重新提交
     * **/
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
        if (order.getStatus() != ORDER_STATUS_REJECT) throwExp("当前状态不可重新提交");

        refreshTimeoutIfNeeded(task, order);
        if (order.getStatus() == ORDER_STATUS_TIMEOUT) throwExp("订单已超时");

        String submitImgs = normalizeAndCheckImgArray(params.getString("submitImgs"), "submitImgs", 1, 10);
        String submitUserId = params.getString("submitUserId");

        order.setStatus(ORDER_STATUS_SUBMIT);
        order.setSubmitImgs(submitImgs);
        order.setSubmitUserId(submitUserId);
        order.setSubmitTime(new Date());
        order.setUpdateTime(new Date());

        order.setRejectReason(null);
        order.setAuditTime(null);

        bountyTaskOrderService.updateOrder(order);

        JSONObject res = new JSONObject();
        res.put("orderId", order.getId());
        res.put("status", ORDER_STATUS_SUBMIT);
        return res;
    }

    /**
     * 悬赏任务-申诉
     * **/
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
        if (order.getStatus() != ORDER_STATUS_REJECT) throwExp("仅未通过订单可申诉");

        refreshTimeoutIfNeeded(task, order);
        if (order.getStatus() == ORDER_STATUS_TIMEOUT) throwExp("订单已超时");

        order.setStatus(ORDER_STATUS_APPEAL);
        order.setAppealStatus(APPEAL_STATUS_DOING);
        order.setAppealReason(appealReason);
        order.setAppealTime(new Date());
        order.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(order);

        JSONObject res = new JSONObject();
        res.put("orderId", order.getId());
        res.put("status", ORDER_STATUS_APPEAL);
        return res;
    }

    /**
     * 悬赏任务-我的接单列表
     * **/
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

        List<Integer> statusList = new ArrayList<>();
        if (tab == null || tab == 1) {
            statusList.add(ORDER_STATUS_DOING);
            statusList.add(ORDER_STATUS_SUBMIT);
            statusList.add(ORDER_STATUS_APPEAL);
        } else if (tab == 2) {
            statusList.add(ORDER_STATUS_DONE);
        } else {
            statusList.add(ORDER_STATUS_REJECT);
            statusList.add(ORDER_STATUS_TIMEOUT);
            statusList.add(ORDER_STATUS_CANCEL);
        }

        Map<String, Object> q = new HashMap<>();
        q.put("userId", userId);
        q.put("statusList", statusList);
        q.put("offset", offset);
        q.put("limit", pageSize);

        List<BountyTaskOrder> list = bountyTaskOrderService.findListByConditions(q);

        JSONArray arr = new JSONArray();
        if (list != null) {
            for (BountyTaskOrder o : list) {
                BountyTask task = bountyTaskService.findById(o.getTaskId());
                if (task != null) {
                    refreshTimeoutIfNeeded(task, o);
                }
                arr.add(orderToJson(o));
            }
        }

        JSONObject res = new JSONObject();
        res.put("list", arr);
        res.put("pageNo", pageNo);
        res.put("pageSize", pageSize);
        return res;
    }

    /**
     * 悬赏任务-我的发布列表
     * **/
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

        Map<String, Object> q = new HashMap<>();
        q.put("userId", userId);
        q.put("offset", offset);
        q.put("limit", pageSize);

        List<BountyTask> list = bountyTaskService.findListByConditions(q);

        JSONArray arr = new JSONArray();
        if (list != null) {
            for (BountyTask t : list) {
                arr.add(beanToJson(t));
            }
        }

        JSONObject res = new JSONObject();
        res.put("list", arr);
        res.put("pageNo", pageNo);
        res.put("pageSize", pageSize);
        return res;
    }

    /**
     * 悬赏任务-我发布的待审核列表
     * **/
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

        Map<String, Object> q = new HashMap<>();
        q.put("publisherUserId", userId);
        q.put("status", ORDER_STATUS_SUBMIT);
        q.put("offset", offset);
        q.put("limit", pageSize);

        List<BountyTaskOrder> list = bountyTaskOrderService.findListByConditions(q);
        JSONArray arr = new JSONArray();
        if (list != null) {
            for (BountyTaskOrder o : list) {
                arr.add(orderToJson(o));
            }
        }
        JSONObject res = new JSONObject();
        res.put("list", arr);
        res.put("pageNo", pageNo);
        res.put("pageSize", pageSize);
        return res;
    }


    /**
     * 悬赏任务-审核通过
     * **/
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
        if (order.getStatus() != ORDER_STATUS_SUBMIT) throwExp("订单状态不可审核");

        BountyTask task = bountyTaskService.findById(order.getTaskId());
        if (task == null) throwExp("任务不存在");
        if (task.getEscrowAmount() == null || task.getEscrowAmount().compareTo(order.getUnitPrice()) < 0) {
            throwExp("托管金不足");
        }

        JSONArray rewards = new JSONArray();
        JSONObject r = new JSONObject();
        r.put("type", 1);
        r.put("id", UserCapitalTypeEnum.hxjf.getValue());
        r.put("number", order.getUnitPrice());
        rewards.add(r);
        playGameService.addReward(order.getUserId(), rewards, LogCapitalTypeEnum.bounty_order_reward, LogUserBackpackTypeEnum.bounty);

        order.setStatus(ORDER_STATUS_DONE);
        order.setAuditTime(new Date());
        order.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(order);

        Map<String, Object> upd = new HashMap<>();
        upd.put("id", task.getId());
        upd.put("finishCountDelta", 1);
        bountyTaskService.updateCounts(upd);

        task.setEscrowAmount(task.getEscrowAmount().subtract(order.getUnitPrice()));
        if (task.getEscrowAmount().compareTo(BigDecimal.ZERO) < 0) task.setEscrowAmount(BigDecimal.ZERO);
        bountyTaskService.updateTask(task);

        JSONObject res = new JSONObject();
        res.put("orderId", orderId);
        res.put("status", ORDER_STATUS_DONE);
        return res;
    }

    /**
     * 悬赏任务-审核驳回
     * **/
    @Transactional
    @ServiceMethod(code = "014", description = "悬赏任务-审核驳回")
    public JSONObject auditReject(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"), params.get("orderId"), params.get("rejectReason"));
        Long userId = params.getLong("userId");
        Long orderId = params.getLong("orderId");
        String rejectReason = params.getString("rejectReason");

        BountyTaskOrder order = bountyTaskOrderService.findById(orderId);
        if (order == null) throwExp("订单不存在");
        if (!Objects.equals(order.getPublisherUserId(), userId)) throwExp("无权限");
        if (order.getStatus() != ORDER_STATUS_SUBMIT) throwExp("订单状态不可驳回");

        order.setStatus(ORDER_STATUS_REJECT);
        order.setRejectReason(rejectReason);
        order.setAuditTime(new Date());
        order.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(order);

        JSONObject res = new JSONObject();
        res.put("orderId", orderId);
        res.put("status", ORDER_STATUS_REJECT);
        res.put("rejectReason", rejectReason);
        return res;
    }


    private User loadAndCheckUser(Long userId) {
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = (users != null) ? users.get(userId) : null;
        if (user == null) {
            throwExp("用户不存在");
        }
        return user;
    }

    private BountyTaskOrder findLatestOrder(Long taskId, Long userId) {
        Map<String, Object> q = new HashMap<>();
        q.put("taskId", taskId);
        q.put("userId", userId);
        q.put("limit", 1);
        List<BountyTaskOrder> list = bountyTaskOrderService.findListByConditions(q);
        if (list == null || list.isEmpty()) return null;
        return list.get(0);
    }

    private boolean isOrderAlive(BountyTaskOrder o) {
        if (o == null || o.getStatus() == null) return false;
        if (o.getStatus() == ORDER_STATUS_DONE) return false;
        if (o.getStatus() == ORDER_STATUS_CANCEL) return false;
        if (o.getStatus() == ORDER_STATUS_TIMEOUT) return false;

        // SUBMIT-待审核 不受到任务期限deadline的影响,提交后可等待审核
        if (o.getStatus() == ORDER_STATUS_SUBMIT) return true;

        // DOING / REJECT / APPEAL 受 deadline 影响
        if (o.getDeadlineTime() != null && o.getDeadlineTime().before(new Date())) {
            return false;
        }
        return true;
    }

    /**
     * 统一超时刷新入口：
     * 若订单处于 DOING/REJECT/APPEAL 且当前时间超过 deadlineTime，则标记 TIMEOUT
     * 若任务仍在线，则释放 quotaRemain +1,舅是允许重新接单，将 joinCount -1
     * 若任务已取消，则不释放 quotaRemain  任务已下架，但是仍将 joinCount -1
     */
    private void refreshTimeoutIfNeeded(BountyTask task, BountyTaskOrder o) {
        if (task == null || o == null || o.getStatus() == null) return;

        if (o.getStatus() != ORDER_STATUS_DOING
                && o.getStatus() != ORDER_STATUS_REJECT
                && o.getStatus() != ORDER_STATUS_APPEAL) {
            return;
        }
        if (o.getDeadlineTime() == null) return;
        if (!o.getDeadlineTime().before(new Date())) return;

        o.setStatus(ORDER_STATUS_TIMEOUT);
        o.setUpdateTime(new Date());
        bountyTaskOrderService.updateOrder(o);

        Map<String, Object> upd = new HashMap<>();
        upd.put("id", task.getId());
        upd.put("joinCountDelta", -1);
        if (task.getStatus() != null && task.getStatus() == TASK_STATUS_ONLINE) {
            upd.put("quotaRemainDelta", 1);
        }
        bountyTaskService.updateCounts(upd);
    }

    private String normalizeAndCheckImgArray(String jsonArrStr, String fieldName, int min, int max) {
        if (jsonArrStr == null) throwExp(fieldName + "不能为空");
        JSONArray arr;
        try {
            arr = JSONArray.parseArray(jsonArrStr);
        } catch (Exception e) {
            throwExp(fieldName + "格式错误，必须是JSON数组字符串");
            return null;
        }
        if (arr == null || arr.isEmpty()) throwExp(fieldName + "至少需要" + min + "张");
        if (arr.size() < min || arr.size() > max) {
            throwExp(fieldName + "数量范围必须是" + min + "~" + max);
        }
        for (int i = 0; i < arr.size(); i++) {
            String v = arr.getString(i);
            if (v == null || v.trim().isEmpty()) {
                throwExp(fieldName + "第" + (i + 1) + "项为空");
            }
        }
        return arr.toJSONString();
    }

    private JSONObject beanToJson(BountyTask t) {
        JSONObject o = new JSONObject();
        o.put("taskId", t.getId());
        o.put("userId", t.getUserId());
        o.put("taskTitle", t.getTaskTitle());
        o.put("taskName", t.getTaskName());
        o.put("taskDesc", t.getTaskDesc());
        o.put("taskSteps", t.getTaskSteps());
        o.put("videoUrl", t.getVideoUrl());
        o.put("downloadImgs", t.getDownloadImgs());
        o.put("idTip", t.getIdTip());
        o.put("unitPrice", t.getUnitPrice());
        o.put("quotaTotal", t.getQuotaTotal());
        o.put("quotaRemain", t.getQuotaRemain());
        o.put("takeLimitHours", t.getTakeLimitHours());
        o.put("status", t.getStatus());
        o.put("joinCount", t.getJoinCount());
        o.put("finishCount", t.getFinishCount());
        o.put("capitalType", t.getCapitalType());
        o.put("escrowAmount", t.getEscrowAmount());
        o.put("feeAmount", t.getFeeAmount());
        o.put("feeRate", t.getFeeRate());
        o.put("createTime", t.getCreateTime());
        o.put("updateTime", t.getUpdateTime());
        return o;
    }

    private JSONObject orderToJson(BountyTaskOrder o) {
        JSONObject j = new JSONObject();
        j.put("orderId", o.getId());
        j.put("taskId", o.getTaskId());
        j.put("publisherUserId", o.getPublisherUserId());
        j.put("userId", o.getUserId());
        j.put("unitPrice", o.getUnitPrice());
        j.put("status", o.getStatus());
        j.put("takeTime", o.getTakeTime());
        j.put("deadlineTime", o.getDeadlineTime());
        j.put("submitTime", o.getSubmitTime());
        j.put("submitImgs", o.getSubmitImgs());
        j.put("submitUserId", o.getSubmitUserId());
        j.put("rejectReason", o.getRejectReason());
        j.put("appealStatus", o.getAppealStatus());
        j.put("appealReason", o.getAppealReason());
        return j;
    }

    private Date addHours(Date now, Integer h) {
        if (now == null || h == null) return null;
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.HOUR_OF_DAY, h);
        return c.getTime();
    }

    private long toCents(BigDecimal amount) {
        if (amount == null) return 0L;
        return amount.multiply(new BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).longValue();
    }

}
