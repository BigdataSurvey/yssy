package com.zywl.app.defaultx.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.BalanceConvertRecord;
import com.zywl.app.base.bean.BatchCashRecord;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.CashStatusTypeEnum;
import com.zywl.app.defaultx.enmus.UserCapitalTypeEnum;


@Service
public class BalanceConvertRecordService extends DaoService{
	
	private static final Log logger = LogFactory.getLog(BalanceConvertRecordService.class);
	
	@Autowired
	private AppConfigCacheService appConfigCacheService;
	
	@Autowired
	private LogUserCapitalService logUserCapitalService;
	
	public static List<String> cashOrderNos = new ArrayList<String>();
	

	public BalanceConvertRecordService() {
		super("BalanceConvertRecordMapper");
	}
	
	
	
	@Transactional
	public Long addBalanceConvertOrder(Long userId,String orderNo,BigDecimal amount,BigDecimal amountReceived,String remark) {
		BalanceConvertRecord balanceConvertRecord = new BalanceConvertRecord();
		balanceConvertRecord.setUserId(userId);
		balanceConvertRecord.setRmbAmount(amount);
		balanceConvertRecord.setAmountReceived(amountReceived);
		balanceConvertRecord.setOrderNo(orderNo);
		balanceConvertRecord.setRemark(remark);
		balanceConvertRecord.setCreateTime(new Date());
		save(balanceConvertRecord);
		return balanceConvertRecord.getId();
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
		params.put("limit", 20);
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
	public void removeCache(String orderNo) {
		cashOrderNos.remove(orderNo);
	}

}
