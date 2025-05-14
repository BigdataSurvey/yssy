package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

public class City extends BaseBean {
	
	private String id;
	
	private Integer code;
	
	private String name;
	
	private String provinceId;

	public String getId() {
		return id;
	}

	public Integer getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProvinceId() {
		return provinceId;
	}

	public void setProvinceId(String provinceId) {
		this.provinceId = provinceId;
	}
}
