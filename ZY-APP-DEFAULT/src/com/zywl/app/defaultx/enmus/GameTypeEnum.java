package com.zywl.app.defaultx.enmus;

/**
 *   游戏类型
 * 
 * @author DOE
 *
 */
public enum GameTypeEnum {

	battleRoyale("单杀大逃杀", 7),
	starChange("斗转星移", 2),
	food("我是酒仙", 3),
	ns("打年兽",4),
	nh("2选1小游戏",5),
	dz("打坐",6),
	dts2("倩女幽魂",1),
	sg("算卦",8),
	bt("宝塔",9),
	dgs("打怪兽",10),
	nxq("聂小倩的故事",11)

	;
	private String name;

	private int value;
	
	private GameTypeEnum(String name, int value) {
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
		GameTypeEnum[] ems = GameTypeEnum.values();
		for (GameTypeEnum gameTypeEnum : ems) {
			if (gameTypeEnum.getValue()==value) {
				return gameTypeEnum.name.toString();
			}
		}
		return null;
	}
	
}
