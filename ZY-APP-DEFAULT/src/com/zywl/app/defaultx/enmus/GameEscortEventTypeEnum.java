package com.zywl.app.defaultx.enmus;

/**
 *   广告位
 * 
 * @author DOE
 *
 */
public enum GameEscortEventTypeEnum {

	//劝降
	EVENT1("劝降",1),
	//进攻
	EVENT2("进攻",2),
	//绕过
	EVENT3("绕过",3),
	;

	private String index;

	private int  count;

	private GameEscortEventTypeEnum(String index, int count) {
		this.index = index;
		this.count = count;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}


}
