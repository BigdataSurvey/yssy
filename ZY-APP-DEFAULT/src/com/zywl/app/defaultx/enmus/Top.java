package com.zywl.app.defaultx.enmus;

/**
 *   榜单
 * 
 * @author DOE
 *
 */
public enum Top {

	capital_2("货币排行榜", 1 * 24), week("周榜", 7 * 24), month("月榜", 30 * 24);
	
	private String name;

	private int hour;
	
	private Top(String name, int hour) {
		this.name = name;
		this.hour = hour;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}
}
