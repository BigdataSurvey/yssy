package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

public class TradingRecordVo extends BaseBean {

	private Long itemId;

	private Integer itemNumber;

	private BigDecimal amount;

	private Integer type;

	private Date createTime;

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Integer getItemNumber() {
		return itemNumber;
	}

	public void setItemNumber(Integer itemNumber) {
		this.itemNumber = itemNumber;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public TradingRecordVo() {

	}

	public TradingRecordVo(Long itemId, Integer itemNumber, BigDecimal amount,BigDecimal fee, Integer type, Date createTime) {
		super();
		this.itemId = itemId;
		this.itemNumber = itemNumber;
		this.amount = amount.subtract(fee);
		this.type = type;
		this.createTime = createTime;
	}

}
