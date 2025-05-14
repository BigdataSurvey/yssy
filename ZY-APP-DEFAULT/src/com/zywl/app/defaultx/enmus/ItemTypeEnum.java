package com.zywl.app.defaultx.enmus;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 *   道具大分类
 * 
 * @author DOE
 *
 */
public enum ItemTypeEnum {

	danyao("丹药", 1), 
	yaocao("药草", 2);
	private String name;

	private int value;
	
	private ItemTypeEnum(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValie(int value) {
		this.value = value;
	}
	
	
	public static String getName(int value) {
		ItemTypeEnum[] ems = ItemTypeEnum.values();
		for (ItemTypeEnum gameTypeEnum : ems) {
			if (gameTypeEnum.getValue()==value) {
				return gameTypeEnum.name.toString();
			}
		}
		return null;
	}
	
	public static JSONArray getType() {
		JSONArray array = new JSONArray();
		
		ItemTypeEnum[] ems = ItemTypeEnum.values();
		for (ItemTypeEnum em : ems) {
			JSONObject obj = new JSONObject();
			obj.put("type", em.getValue());
			obj.put("name", em.getName());
			array.add(obj);
		}
		
		return array;
	}
	
}
