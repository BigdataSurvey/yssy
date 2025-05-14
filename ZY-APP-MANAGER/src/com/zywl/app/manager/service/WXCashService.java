package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.CommandBuilder;
import com.live.app.ws.util.Push;
import com.wechat.pay.java.service.transferbatch.TransferBatchService;
import com.wechat.pay.java.service.transferbatch.model.*;
import com.zywl.app.base.bean.BatchCashRecord;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.bean.UserCapital;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.defaultx.cache.UserCapitalCacheService;
import com.zywl.app.defaultx.enmus.CashOrderErrorMessageEnum;
import com.zywl.app.defaultx.enmus.CashStatusTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;
import com.zywl.app.defaultx.service.BatchCashRecordService;
import com.zywl.app.defaultx.service.CashRecordService;
import com.zywl.app.defaultx.service.UserCapitalService;
import com.zywl.app.defaultx.service.UserService;
import com.zywl.app.manager.service.manager.ManagerSocketService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shadow.com.wechat.pay.java.core.Config;
import shadow.com.wechat.pay.java.core.RSAAutoCertificateConfig;
import shadow.com.wechat.pay.java.core.exception.ServiceException;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class WXCashService extends BaseService {

    private static final Log logger = LogFactory.getLog(WXCashService.class);

    //大收藏家
    //public static String appid = "wx7bc1ec5bc59705f3";

    //易云仙舍
    public static String appid = "wx938564a42cf4f4cb";

    public static String merchantId = "1623381453";
    /** 商户API私钥路径 */
    /**
     * 商户证书序列号
     */
    public static String merchantSerialNumber = "5598EA37D9FB0CB357619537291731966E1CA4DF";
    /**
     * 商户APIV3密钥
     */
    public static String apiV3Key = "HENANzongyiwangluokejigs20220314";
    public static TransferBatchService service;

    @Autowired
    private BatchCashRecordService batchCashRecordService;

    @Autowired
    private UserCacheService userCacheService;

    @Autowired
    private CashRecordService cashRecordService;

    @Autowired
    private UserCapitalService userCapitalService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserCapitalCacheService userCapitalCacheService;

    @Autowired CheckAchievementService checkAchievementService;

    @Autowired
    private ManagerSocketService managerSocketService;


    private final static Object lock = new Object();

    public static Config config = null;


    public static List<String> batchCashRecord = new ArrayList<String>();

    public static List<String> cashOrderNos = new ArrayList<String>();




    /**
     * 初始化内存信息
     */
    public void initCache() {
        logger.info("加载批量提现订单");
        List<BatchCashRecord> batchOrders = batchCashRecordService.findNoResponseOrder();
        for (BatchCashRecord record : batchOrders) {
            batchCashRecord.add(record.getOrderNo());
        }
        logger.info("加载批量提现订单完成");


        logger.info("加载审核通过但未推送的提现订单");
        List<CashRecord> cashRecords = cashRecordService.findSingleOrderByBatchOrderNo();
        for (CashRecord record : cashRecords) {
            cashOrderNos.add(record.getOrderNo());
        }
        logger.info("加载审核通过但未推送的提现订单完成");
    }

    //@PostConstruct
    public void _construct() {
        setConfig();
        initCache();
    }

    @Transactional
    public void setConfig() {
        synchronized (lock) {
            String wxkeypath = this.getClass().getClassLoader().getResource("apiclient_key.pem").getPath();
            logger.info("配置微信商户");
            config = new RSAAutoCertificateConfig.Builder().merchantId(merchantId).privateKeyFromPath(wxkeypath)
                    .merchantSerialNumber(merchantSerialNumber).apiV3Key(apiV3Key).build();
            logger.info("配置微信商户完成");
            logger.info("开始监控批量提现订单");
            new Timer("监控批量提现订单").schedule(new TimerTask() {
                public void run() {
                    try {
                        if (batchCashRecord.size() <= 0) {
                            return;
                        }
                        List<BatchCashRecord> batchOrders = batchCashRecordService.findNoResponseOrder();
                        if (batchOrders.size() <= 0) {
                            return;
                        }
                        logger.info("检测到批量提现订单数量：" + batchOrders.size());
                        for (BatchCashRecord batchCashOrderRecord : batchOrders) {
                            getInfo(batchCashOrderRecord.getOrderNo(), 0, 20);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }, 1000L, 10000L);
        }
        synchronized (lock) {

            new Timer("监控未提交至微信的提现订单").schedule(new TimerTask() {
                public void run() {
                    try {
                        // 获取20条数据
                        if (cashOrderNos.size() <= 0) {
                            return;
                        }
                        List<CashRecord> cashRecords = cashRecordService.findSingleOrderByBatchOrderNo();
                        if (cashRecords.size() <= 0) {
                            return;
                        }
                        logger.info("检测到未推送的提现订单数量：" + cashRecords.size());
                        // 生成批量订单 插入数据库 同时推送至微信
                        String orderNo = OrderUtil.getBatchOrder32Number();
                        BigDecimal totalAmount = BigDecimal.ZERO;
                        for (CashRecord cashRecord : cashRecords) {
                            BigDecimal realAmount = cashRecord.getAmount().subtract(cashRecord.getFee());
                            totalAmount = totalAmount.add(realAmount);
                        }
                        String name = "玩家提现";
                        String remark = "批量提现订单，数量：" + cashRecords.size();
                        batchCashRecordService.addBatchCashOrderAndUpdateCashRecord(orderNo, cashRecords, totalAmount, name,
                                remark);
                        batchCashRecord.add(orderNo);
                        wxCash(cashRecords, orderNo, name, remark,
                                new BigDecimal(totalAmount.toString()).multiply(new BigDecimal("100")).longValue(),
                                cashRecords.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
            }, 1000L, 10000L);
        }
    }

    @Transactional
    public void wxCash(List<CashRecord> cashRecords, String batchOrderNo, String batchName, String mark, Long amount,
                       int totalNum) {
        service = new TransferBatchService.Builder().config(config).build();
        InitiateBatchTransferRequest initiateBatchTransferRequest = new InitiateBatchTransferRequest();
        initiateBatchTransferRequest.setAppid(appid);
        initiateBatchTransferRequest.setOutBatchNo(batchOrderNo);
        initiateBatchTransferRequest.setBatchName(batchName);
        initiateBatchTransferRequest.setBatchRemark(mark);
        initiateBatchTransferRequest.setTotalAmount(amount);
        initiateBatchTransferRequest.setTotalNum(totalNum);
        List<TransferDetailInput> transferDetailListList = new ArrayList<>();
        for (CashRecord cashRecord : cashRecords) {
            TransferDetailInput transferDetailInput = new TransferDetailInput();
            transferDetailInput.setOutDetailNo(cashRecord.getOrderNo());
            transferDetailInput.setTransferAmount(
                    new BigDecimal(cashRecord.getReceivedAmount().toString()).multiply(new BigDecimal("100")).longValue());
            transferDetailInput.setTransferRemark(cashRecord.getRemark());
            transferDetailInput.setOpenid(cashRecord.getOpenId());
            transferDetailInput.setUserName(cashRecord.getRealName());
            transferDetailListList.add(transferDetailInput);
        }
        initiateBatchTransferRequest.setTransferDetailList(transferDetailListList);
        InitiateBatchTransferResponse response = null;
        try {
            response = service.initiateBatchTransfer(initiateBatchTransferRequest);
        } catch (ServiceException e) {
            logger.error(e.getErrorCode());
            logger.error(e.getErrorMessage());
            throwExp(e.getErrorMessage());
            return;
        }
        logger.info(response);
        logger.info("batchId:" + response.getBatchId() + "-batchStatus:" + response.getBatchStatus() + "-outBatchNo:"
                + response.getOutBatchNo() + "-createTime:" + response.getCreateTime());
    }



    @Transactional
    public void getInfo(String batchOrderNo, int start, int limit) {
        service = new TransferBatchService.Builder().config(config).build();
        GetTransferBatchByOutNoRequest request = new GetTransferBatchByOutNoRequest();
        request.setOutBatchNo(batchOrderNo);
        request.setNeedQueryDetail(false);
        request.setOffset(start);
        request.setLimit(limit);
        TransferBatchEntity response = null;
        try {
            response = service.getTransferBatchByOutNo(request);
        } catch (ServiceException e) {
            if (e.getErrorCode().equals(CashOrderErrorMessageEnum.NOT_FOUND.getName())){
                BatchCashRecord record = batchCashRecordService.findByOrderNo(batchOrderNo);
                if (record!=null && (System.currentTimeMillis()- record.getCreateTime().getTime())/1000/60>60){
                    batchCashRecordService.updateCashOrderToFinshed(batchOrderNo, 0, e.getErrorCode(), e.getErrorMessage());
                    List<CashRecord> cashRecords = cashRecordService.findCashRecordByBatchOrderNo(batchOrderNo);
                    for (CashRecord cashRecord : cashRecords) {
                        UserCapital capital = userCapitalCacheService.getUserCapitalCacheByType(cashRecord.getUserId(),
                                UserCapitalTypeEnum.rmb.getValue());
                        String errorMessage = e.getErrorMessage();
                        cashRecordService.cashRecordFail(cashRecord.getOrderNo(), errorMessage);
                        userCapitalService.subUserOccupyBalanceByCashFail(cashRecord.getAmount(), cashRecord.getUserId(),
                                capital.getBalance(), capital.getOccupyBalance(), cashRecord.getOrderNo(), cashRecord.getId());
                        if (errorMessage.equals("NAME_NOT_CORRECT")){
                            //实名不一致 直接修改为未实名认证状态 并且没有收益状态
                            userService.updateUserNoPassIdCard(cashRecord.getUserId());
                            managerSocketService.noPassAuthUser(cashRecord.getUserId());
                        }
                        UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(cashRecord.getUserId(),UserCapitalTypeEnum.rmb.getValue());
                        JSONObject pushData = new JSONObject();
                        pushData.put("userId", cashRecord.getUserId());
                        pushData.put("capitalType", UserCapitalTypeEnum.rmb.getValue());
                        pushData.put("balance", userCapital.getBalance());
                        Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(cashRecord.getUserId()), pushData);
                        cashOrderNos.remove(cashRecord.getOrderNo());
                    }
                    batchCashRecord.remove(batchOrderNo);
                    e.printStackTrace();
                    throwExp(e.getErrorMessage());
                }
                return;

            }

        }
        logger.info("查询批量转账订单:" + batchOrderNo);
        logger.info("批量转账订单结果:" + JSONObject.from(response));
        TransferBatchGet transferBatchGet = response.getTransferBatch();
        if (transferBatchGet.getBatchStatus().equals("FINISHED")) {
            // 首先更新batchRecord 为已完成
            batchCashRecordService.updateCashOrderToFinshed(batchOrderNo, transferBatchGet.getSuccessNum(), null, null);
            List<CashRecord> cashRecords = cashRecordService.findCashRecordByBatchOrderNo(batchOrderNo);
            // 批量转账已完成 查询是否有失败的订单
            if (transferBatchGet.getFailNum() > 0) {
                // 轮询去查询订单明细
                for (CashRecord cashRecord : cashRecords) {
                    GetTransferDetailByOutNo(cashRecord);
                }
            } else {
                // 全部转账成功 批量更新为成功
                cashRecordService.batchUpdateSuccess(batchOrderNo);
                // 扣除玩家冻结余额
                for (CashRecord cashRecord : cashRecords) {
                    //移除内存数据
                    User user = userCacheService.getUserInfoById(cashRecord.getUserId());
                    if (user.getIsCash()==0) {
                         userService.updateIsCash(user.getId());
                    }
                    cashOrderNos.remove(cashRecord.getOrderNo());
                    UserCapital capital = userCapitalCacheService.getUserCapitalCacheByType(cashRecord.getUserId(),
                            UserCapitalTypeEnum.rmb.getValue());
                    userCapitalService.subUserOccupyBalanceByCashSuccess(cashRecord.getUserId(), capital.getBalance(),
                            capital.getOccupyBalance(), cashRecord.getOrderNo(), cashRecord.getAmount(),
                            cashRecord.getId());
                }
            }
        } else {
            logger.info(transferBatchGet.getOutBatchNo() + "转账未完成，当前转账状态：" + transferBatchGet.getBatchStatus());
        }

    }

    @Transactional
    public void GetTransferDetailByOutNo(CashRecord cashRecord) {
        service = new TransferBatchService.Builder().config(config).build();
        GetTransferDetailByOutNoRequest request = new GetTransferDetailByOutNoRequest();
        request.setOutDetailNo(cashRecord.getOrderNo());
        request.setOutBatchNo(cashRecord.getBatchOrderNo());
        TransferDetailEntity response = null;
        try {
            response = service.getTransferDetailByOutNo(request);
        } catch (ServiceException e) {
            e.printStackTrace();
            logger.error("单号：" + cashRecord.getOrderNo() + "微信提现明细错误：" + e.getErrorMessage());
            return;
        }
        UserCapital capital = userCapitalCacheService.getUserCapitalCacheByType(cashRecord.getUserId(),
                UserCapitalTypeEnum.rmb.getValue());
        cashOrderNos.remove(cashRecord.getOrderNo());
        if (response.getDetailStatus().equals("FAIL")) {
            // 订单转账失败，查询失败原因 更新数据库 移除缓存
            String errorCode = response.getFailReason().toString();
            String errorMessage = CashOrderErrorMessageEnum.getOrderStatusEnum(errorCode).getValue();
            cashRecordService.cashRecordFail(cashRecord.getOrderNo(), errorMessage);
            userCapitalService.subUserOccupyBalanceByCashFail(cashRecord.getAmount(), cashRecord.getUserId(),
                    capital.getBalance(), capital.getOccupyBalance(), cashRecord.getOrderNo(), cashRecord.getId());
            if (errorCode.equals("NAME_NOT_CORRECT")){
                //实名不一致 直接修改为未实名认证状态 并且没有收益状态
                userService.updateUserNoPassIdCard(cashRecord.getUserId());
                managerSocketService.noPassAuthUser(cashRecord.getUserId());
            }
            UserCapital userCapital = userCapitalCacheService.getUserCapitalCacheByType(cashRecord.getUserId(),UserCapitalTypeEnum.rmb.getValue());
            JSONObject pushData = new JSONObject();
            pushData.put("userId", cashRecord.getUserId());
            pushData.put("capitalType", UserCapitalTypeEnum.rmb.getValue());
            pushData.put("balance", userCapital.getBalance());
            Push.push(PushCode.updateUserCapital, managerSocketService.getServerIdByUserId(cashRecord.getUserId()), pushData);

        } else if (response.getDetailStatus().equals("SUCCESS")) {
            User user = userCacheService.getUserInfoById(cashRecord.getUserId());
            if ( user.getIsCash()==0) {
                userService.updateIsCash(user.getId());
            }
            cashRecordService.cashRecordSuccess(cashRecord.getOrderNo(), CashStatusTypeEnum.SUCCESS.getName());
            userCapitalService.subUserOccupyBalanceByCashSuccess(cashRecord.getUserId(), capital.getBalance(),
                    capital.getOccupyBalance(), cashRecord.getOrderNo(), cashRecord.getAmount(), cashRecord.getId());

        }

    }


}
