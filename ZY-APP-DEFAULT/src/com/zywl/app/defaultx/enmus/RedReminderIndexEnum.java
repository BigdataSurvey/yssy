package com.zywl.app.defaultx.enmus;

/**
 *   红点类型
 * 
 * @author DOE
 *
 */
public enum RedReminderIndexEnum {

	achievement("成就", "5"),
	mail("邮件", "2"),
	inviteFriend("邀请好友活动","3"),
	animaTree("灵树","4"),
	dailyTask("每日任务","5");
	private String name;

	private String value;
	
	private RedReminderIndexEnum(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValie(String value) {
		this.value = value;
	}
	
	
	public static String getName(String value) {
		RedReminderIndexEnum[] ems = RedReminderIndexEnum.values();
		for (RedReminderIndexEnum gameTypeEnum : ems) {
			if (gameTypeEnum.getValue().equals(value)) {
				return gameTypeEnum.name.toString();
			}
		}
		return null;
	}
	
}
