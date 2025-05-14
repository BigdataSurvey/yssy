package com.zywl.app.defaultx.enmus;

import java.math.BigDecimal;

/**
 *   灵气树灵气来源
 * 
 * @author ns
 *
 */
public enum AnimaTreeFromEnum {

	lv_11("等级11", new BigDecimal("0.1")),
	lv_21("等级21", new BigDecimal("1")),
	lv_31("等级31", new BigDecimal("5")),
	lv_41("等级41", new BigDecimal("10")),
	lv_51("等级51", new BigDecimal("30")),
	exempt_ad("开通免广告", new BigDecimal("10")),
	mp("恢复灵气", new BigDecimal("0.4")),
	egg("领取1个神秘蛋",new BigDecimal("0.00005")),
	egg2("领取1个神秘蛋",new BigDecimal("0.00002")),
	cave("领取洞府收益",new BigDecimal("0.1")),
	prize_draw("探宝",new BigDecimal("0.1")),
	magic("魔晶幻化",new BigDecimal("0.05")),
	MINE_1("矿场1使用锄头",new BigDecimal("1")),
	MINE_2("矿场2使用锄头",new BigDecimal("2")),
	MINE_3("矿场3使用锄头",new BigDecimal("3")),
	MINE_4("矿场4使用锄头",new BigDecimal("4"));

	private String name;

	private BigDecimal value;

	private AnimaTreeFromEnum(String name, BigDecimal value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public static BigDecimal getLvAnimal(int lv){
		if (lv==11){
			return lv_11.getValue();
		} else if (lv==21) {
			return lv_21.getValue();
		}else if (lv==31) {
			return lv_31.getValue();
		}else if (lv==41) {
			return lv_41.getValue();
		}else if (lv==51) {
			return lv_51.getValue();
		}
		return null;
	}

	public static BigDecimal getMineAnimal(Long mineId){
		if (mineId<2){
			return MINE_1.getValue();
		} else if (mineId<4) {
			return MINE_2.getValue();
		}else if (mineId<6) {
			return MINE_3.getValue();
		}else {
			return MINE_4.getValue();
		}
	}
	
	
}
