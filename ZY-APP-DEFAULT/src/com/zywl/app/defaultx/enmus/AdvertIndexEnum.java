package com.zywl.app.defaultx.enmus;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

/**
 *   广告位
 * 
 * @author DOE
 *
 */
public enum AdvertIndexEnum {

	afkBox(10,6),
	//获快速挂机
	afkReward(1,2l),
	//刷新悬赏任务
	refreshDispatch(2,3l),
	//抽卡
	getCard_1(3,3l),
	getCard_2(4,1l),
	//副本扫荡
	fb_1(5,2l),
	fb_2(6,2l),
	fb_3(7,2l),
	fb_4(8,2l),
	tower(9,2l);
	private int index;
	
	private long count;
	
	private AdvertIndexEnum(int index, long count) {
		this.index = index;
		this.count = count;
	}

	public int getIndex() {
		return index;
	}

	
	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public void setIndex(int index) {
		this.index = index;
	}


	public static String getAdName(int index){
		AdvertIndexEnum[] values = AdvertIndexEnum.values();
		for (AdvertIndexEnum value : values) {
			if (value.getIndex()==index){
				return value.toString();
			}
		}
		return null;
	}
	
}
