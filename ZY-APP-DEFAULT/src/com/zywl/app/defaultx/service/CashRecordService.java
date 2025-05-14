package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.base.bean.CashTotalInfo;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.CashStatusTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class CashRecordService extends DaoService{
	
	private static final Log logger = LogFactory.getLog(CashRecordService.class);
	
	@Autowired
	private AppConfigCacheService appConfigCacheService;
	
	@Autowired
	private LogUserCapitalService logUserCapitalService;
	
	
	

	public CashRecordService() {
		super("CashRecordMapper");
	}
	
	
	
	
	@Transactional
	public Long addCashOrder(String openId,Long userId,String userNo,String userName,String realName,String orderNo,BigDecimal amount,int type,String tel) {
		
		BigDecimal cashRate=appConfigCacheService.getCashRate();
		BigDecimal fee = amount.multiply(cashRate);
		CashRecord cashOrder = new CashRecord();
		cashOrder.setUserId(userId);
		cashOrder.setUserNo(userNo);
		cashOrder.setUserName(userName);
		cashOrder.setRealName(realName);
		cashOrder.setOpenId(openId);
		cashOrder.setOrderNo(orderNo);
		cashOrder.setAmount(amount);
		cashOrder.setRemark("颤抖吧三国提现");
		cashOrder.setReceivedAmount(BigDecimal.ZERO);
		cashOrder.setType(type);
		cashOrder.setTel(tel);
		if (amount.compareTo(new BigDecimal("0.3"))==0) {
			cashOrder.setStatus(CashStatusTypeEnum.NO_SUBMIT.getValue());
			cashOrder.setFee(BigDecimal.ZERO);
		}else {
			cashOrder.setStatus(CashStatusTypeEnum.UNAUDITED.getValue());
			cashOrder.setFee(fee);
		}
		cashOrder.setReceivedAmount(amount.subtract(cashOrder.getFee()));
		cashOrder.setCreateTime(new Date());
		cashOrder.setUpdateTime(new Date());
		save(cashOrder);
		return cashOrder.getId();
	}
	
	@Transactional
	public int updateCashOrder(String orderNo,int status,String mark,BigDecimal receivedAmount) {
		Map<String, Object> params=new HashedMap<String, Object>();
		params.put("orderNo", orderNo);
		params.put("status", status);
		params.put("mark", mark);
		params.put("receivedAmount", receivedAmount);
		return update(params);
	}
	
	public List<CashRecord> findSingleOrderByBatchOrderNo(){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("status",CashStatusTypeEnum.NO_SUBMIT.getValue());
		params.put("start", 0);
		params.put("limit", 50);
		return findByConditions(params);
	}
	@Transactional
	public void  cashRecordFail(String orderNo,String remark) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo",orderNo);
		params.put("remark",remark);
		params.put("status",CashStatusTypeEnum.FAIL.getValue());
		execute("updateFailCashOrder", params);
	}
	
	@Transactional
	public void  cashRecordSuccess(String orderNo,String remark) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo",orderNo);
		params.put("remark",remark);
		params.put("status",CashStatusTypeEnum.SUCCESS.getValue());
		execute("updateFailCashOrder", params);
	}
	@Transactional
	public int batchUpdateRecordToSubmit(List<CashRecord> cashRecords,String orderNo) {
		List<Map<String, Object>> orderList = new ArrayList<Map<String,Object>>();
		for (CashRecord record: cashRecords) {
			Map<String, Object> params = new HashedMap<String, Object>();
			params.put("id", record.getId());
			params.put("batchOrderNo", orderNo);
			params.put("status", CashStatusTypeEnum.SUBMIT.getValue());
			orderList.add(params);
		}
		return execute("updateBatch", orderList);
	}
	
	
	public List<CashRecord> findCashRecordByBatchOrderNo(String batchOrderNo){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("batchOrderNo", batchOrderNo);
		params.put("status", CashStatusTypeEnum.SUBMIT.getValue());
		return findByConditions(params);
	}
	@Transactional
	public void batchUpdateSuccess(String batchOrderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("batchOrderNo", batchOrderNo);
		params.put("status",CashStatusTypeEnum.SUCCESS.getValue());
		params.put("remark",CashStatusTypeEnum.SUCCESS.getName());
		execute("batchUpdateSuccess", params);
		
	}
	
	public List<CashRecord> findCashRecordByUserId(Long userId,int page,int num){
		Map<String, Object> params = new HashedMap<>();
		params.put("userId", userId);
		params.put("start", page*num);
		params.put("num", num);
		return findByConditions(params);
	}

	public long findCashOrderCount(Long userId){
		Map<String, Object> params = new HashedMap<>();
		params.put("userId", userId);
		params.put("startDate", DateUtil.getToDayDateBegin());
		return count("countByConditions",params);
	}

	public void updateStatus(int id, int status, String mark) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("id",id);
		params.put("status",status);
		if(mark != null && !mark.isEmpty()) {
			params.put("remark", mark);
		}
		execute("updateStatus", params);
	}

	public List<CashTotalInfo> findWaitTotalInfo() {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("status",0);
		return findList("findTotalInfo", params);
	}

	public List<CashTotalInfo> findCashTotalInfo() {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("status",3);
		return findList("findTotalInfo", params);
	}

}
