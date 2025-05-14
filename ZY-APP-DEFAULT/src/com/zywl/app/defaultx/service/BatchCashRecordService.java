package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.BatchCashRecord;
import com.zywl.app.base.bean.CashRecord;
import com.zywl.app.defaultx.dbutil.DaoService;
import com.zywl.app.defaultx.enmus.BatchCashTypeEnum;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
public class BatchCashRecordService extends DaoService{
	
	private static final Log logger = LogFactory.getLog(BatchCashRecordService.class);

	
	
	@Autowired
	private CashRecordService cashRecordService;
	
	

	public BatchCashRecordService() {
		super("BatchCashRecordMapper");
	}
	
	@Transactional
	public void addBatchCashOrderAndUpdateCashRecord(String orderNo,List<CashRecord> cashRecords,BigDecimal totalAmount,String name,String remark) {
		BatchCashRecord batchCashOrderRecord = new BatchCashRecord();
		batchCashOrderRecord.setBatchName(name);
		batchCashOrderRecord.setBatchRemark(remark);
		batchCashOrderRecord.setOrderNo(orderNo);
		batchCashOrderRecord.setTotalAmount(totalAmount);
		batchCashOrderRecord.setTotalNum(cashRecords.size());
		batchCashOrderRecord.setStatus(BatchCashTypeEnum.UNFINISHED.getValue());
		batchCashOrderRecord.setCreateTime(new Date());
		batchCashOrderRecord.setUpdateTime(new Date());
		save(batchCashOrderRecord);
		int a=cashRecordService.batchUpdateRecordToSubmit(cashRecords, orderNo);
		if (a<1) {
			throwExp("更新微信提现订单失败");
		}
		
	}
	@Transactional
	public int updateCashOrderToFinshed(String batchOrderNo,int successNum,String code,String message) {
		Map<String, Object> params = new HashedMap<>();
		params.put("batchOrderNo",batchOrderNo);
		params.put("status", BatchCashTypeEnum.FINISHED.getValue());
		params.put("successNum", successNum);
		params.put("code", code);
		params.put("message",message);
		params.put("remark", "批量提现订单已完成");
		return execute("updateCashOrderToFinshed", params);
	}
	
	@Transactional
	public int updateCashOrder(String orderNo,int status,String mark,BigDecimal receivedAmount) {
		Map<String, Object> params=new HashedMap<>();
		params.put("orderNo", orderNo);
		params.put("status", status);
		params.put("mark", mark);
		params.put("receivedAmount", receivedAmount);
		return update(params);
	}
	
	public List<BatchCashRecord> findNoSubmitOrder() {
		Map<String, Object> params = new HashedMap<>();
		params.put("status",0);
		return findByConditions(params);
	}
	public List<BatchCashRecord> findNoResponseOrder() {
		Map<String, Object> params = new HashedMap<>();
		params.put("status",1);
		return findByConditions(params);
	}

	public BatchCashRecord findByOrderNo(String orderNo){
		Map<String, Object> params = new HashedMap<>();
		params.put("orderNo",orderNo);
		return (BatchCashRecord) findOne("findByOrderNo",params);
	}
	
	
	

}
