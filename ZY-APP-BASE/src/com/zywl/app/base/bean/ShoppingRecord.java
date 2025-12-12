package com.zywl.app.base.bean;
import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商城购买记录表
 * **/
public class ShoppingRecord extends BaseBean{
	/**
	 * 自增主键ID
	 * **/
	private Long id;
	/**
	 * 玩家Id
	 * **/
	private Long userId;
	/**
	 * 购买的道具商品ID
	 * **/
	private Long itemId;
	/**
	 * 本次购买的商品数量
	 * **/
	private Integer number;
	/**
	 * 本次实际支付总额：单价price * number
	 * **/
	private BigDecimal amount;
	/**
	 * 商店类型 关联 dic_shop_type.shop_type
	 * **/
	private int shopType;
	/**
	 * 支付使用的资产类型：当 use_item_id 属于资产货币时存对应资产枚举
	 * **/
	private int capitalType;
	/**
	 * 商城订单号
	 * **/
	private String orderNo;
	
	private Date createTime;

	public int getShopType() {
		return shopType;
	}

	public void setShopType(int shopType) {
		this.shopType = shopType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getItemId() {
		return itemId;
	}

	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getCapitalType() {
		return capitalType;
	}

	public void setCapitalType(int capitalType) {
		this.capitalType = capitalType;
	}
}
