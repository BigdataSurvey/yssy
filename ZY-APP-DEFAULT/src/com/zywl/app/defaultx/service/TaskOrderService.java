package com.zywl.app.defaultx.service;

import com.zywl.app.base.bean.TaskOrder;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class TaskOrderService extends DaoService {

	public TaskOrderService() {
		super("TaskOrderMapper");
		// TODO Auto-generated constructor stub
	}


	private static final Log logger = LogFactory.getLog(TaskOrderService.class);
	
	
	
	@Transactional
	public Long addOrder(String orderNo,String sql,String server,int type,Long userId,BigDecimal amount,int capitalType) {
		TaskOrder taskOrder = new TaskOrder();
		taskOrder.setOrderNo(orderNo);
		taskOrder.setServer(server);
		taskOrder.setSql(sql);
		taskOrder.setType(type);
		taskOrder.setActiveTime(DateUtil.getTimeByOneMin());
		taskOrder.setCreateTime(new Date());
		taskOrder.setUpdateTime(new Date());
		taskOrder.setUserId(userId);
		taskOrder.setAmount(amount);
		taskOrder.setCapitalType(capitalType);
		save(taskOrder);
		return taskOrder.getId();
		
	}
	
	@Transactional
	public int updateTaskOrder(int status,String sql,String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("status", status);
		params.put("sql", sql);
		params.put("orderNo", orderNo);
		int a = execute("updateOrder", params);
		if (a<1) {
			throwExp("异常！");
		}
		return a;
	}
	
	
	public TaskOrder findOrderByOrderNo(String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo", orderNo);
		return findOne(params);
	}
	
	public TaskOrder findNoStatusOrderByOrderNo(String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo", orderNo);
		return (TaskOrder) findOne("findNoStatusOrderByOrderNo",params);
	}
	
	@Transactional
	public int deleteOrderByOrderNo(String orderNo) {
		Map<String, Object> params = new HashedMap<String, Object>();
		params.put("orderNo", orderNo);
		return delete(params);
	}
	
	@Transactional
	public int deleteByTimer() {
		return delete(null);
	}
	
	public List<TaskOrder> findErrorOrder(){
		return findList("findErrorOrder", null);
	}

	@Override
	protected Log logger() {
		return logger;
	}
	
	
}
