package com.zywl.app.defaultx.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.zywl.app.base.bean.TradingRecord;
import com.zywl.app.base.bean.vo.TradingRecordVo;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class TradingRecordService extends DaoService{
	
	
	private static final Log logger = LogFactory.getLog(TradingRecordService.class);
	
	public TradingRecordService() {
		super("TradingRecordMapper");
		// TODO Auto-generated constructor stub
	}
	
	public Long addTradingRecord(Long userId,Long tradId,Long itemId,String orderNo,BigDecimal amount,BigDecimal fee,int type,String mark,int number,BigDecimal price) {
		TradingRecord tradingRecord = new TradingRecord();
		tradingRecord.setUserId(userId);
		tradingRecord.setItemId(itemId);
		tradingRecord.setOrderNo(orderNo);
		tradingRecord.setAmount(amount);
		tradingRecord.setItemNumber(number);
		tradingRecord.setItemPrice(price);
		tradingRecord.setFee(fee);
		tradingRecord.setType(type);
		tradingRecord.setMark(mark);
		tradingRecord.setCreateTime(new Date());
		tradingRecord.setUpdateTime(new Date());
		save(tradingRecord);
		return tradingRecord.getId();
	}
	
	public List<TradingRecordVo>  getMyRecord(Long userId,int start,int limit,int type){
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("start", start*limit);
		params.put("limit", limit);
		params.put("userId", userId);
		params.put("type",type);
		List<TradingRecord> list = findByConditions(params);
		List<TradingRecordVo> result = new ArrayList<TradingRecordVo>();
		for (TradingRecord tradingRecord : list) {
			TradingRecordVo vo = new TradingRecordVo(tradingRecord.getItemId(), tradingRecord.getItemNumber(), tradingRecord.getAmount(), tradingRecord.getFee(), tradingRecord.getType(), tradingRecord.getCreateTime());
			result.add(vo);
		}
		return result;
	}
}
