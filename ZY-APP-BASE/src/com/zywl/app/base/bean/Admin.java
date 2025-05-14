package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class Admin extends BaseBean {
	
	public final static int ENABLE_STATE = 1;
	
	public final static int DISABLE_STATE = 0;

	private String id;
	
	private String username;
	
	private String password;
	
	private String name;
	
	private String photo;
	
	private String roleId;

	private Integer state;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Admin(){

	}

	public Admin(String username,String account){
		this.username=username;
		this.name=account;
	}

}
