package com.zywl.app.base.util;

public class CurrencyUtil {

	
	public static String getSimpleAmount(int amount) {
		String strAmount = String.valueOf(amount);
		if(amount < 100000 && amount > 9999) {
			return strAmount.substring(0, 1) + "." + strAmount.substring(1, 2) + "万";
		}
		if(amount < 100000000 && amount > 99999) {
			return amount / 10000 + "万";
		}
		return strAmount;
	}
	
	public static void main(String[] args) {
		System.out.println(getSimpleAmount(30000));
	}
}
