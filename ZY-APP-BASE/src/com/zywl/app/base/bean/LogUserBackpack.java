package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class LogUserBackpack extends BaseBean{
	
	public final static String tablePrefix = "log_user_backpack_";
	
	private Long id;
	
	private Long userId;
	
	private Long itemId;
	
	private int numberBefore;
	
	private int number;
	
	private int numberAfter;
	
	private String mark;
	
	private int type;
	
	private Date createTime;
	
	private Date updateTime;

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

	public int getNumberBefore() {
		return numberBefore;
	}

	public void setNumberBefore(int numberBefore) {
		this.numberBefore = numberBefore;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getNumberAfter() {
		return numberAfter;
	}

	public void setNumberAfter(int numberAfter) {
		this.numberAfter = numberAfter;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}
	

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	

}
