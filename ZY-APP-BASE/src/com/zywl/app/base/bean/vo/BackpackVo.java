package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class BackpackVo extends BaseBean{

	public final static String tablePrefix = "t_backpack_";
	private Long id;
	
	private Long userId;
	

	private Integer number;

	private String userNo;

	private String userName;

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

	public Integer getNumber() {
		return number;
	}

	public void setNumber(Integer number) {
		this.number = number;
	}

	public String getUserNo() {
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
