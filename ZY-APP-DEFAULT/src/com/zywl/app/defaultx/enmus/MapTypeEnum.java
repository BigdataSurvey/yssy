package com.zywl.app.defaultx.enmus;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.math.BigDecimal;

/**
 *   遗迹大分类
 * 
 * @author DOE
 *
 */
public enum MapTypeEnum {

	MAP1(1, new BigDecimal("2")),
	MAP2(2, new BigDecimal("10")),
	MAP3(3, new BigDecimal("20"));
	private Integer type;

	private BigDecimal value;

	private MapTypeEnum(Integer type, BigDecimal value) {
		this.type = type;
		this.value = value;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}



}
