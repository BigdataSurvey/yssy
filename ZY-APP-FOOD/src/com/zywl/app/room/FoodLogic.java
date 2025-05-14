package com.zywl.app.room;

import java.util.ArrayList;
import java.util.Iterator;

public class FoodLogic {
	private static  int maxcard = 50;
	private static int maxvalue = 30;
//	private static int maxvalue = 100;
	private static int[] foods = {1,2,3,4,5,6,7,8,9,10};
	private static ArrayList<Integer> foodarr = new ArrayList<Integer>();
	
	public static void Init() {
//		if(foodarr.size() > 0) {
//			foodarr.clear();
//		}
//		for(int i=0; i<maxcard; i++) {
//			int idx = (int) (Math.random() * 10);
//			foodarr.add(foods[idx]);
//		}
	}
	
	public static int dispatchOne(int round) {
//		Iterator<Integer> iterator = foodarr.iterator();
//		while (iterator.hasNext()) {
//			Integer integer = (Integer) iterator.next();
//			iterator.remove();
//			return integer;
//		}
//		return -1;
		int i =  (int) (Math.random() * 1000);
		if(round > 2) {
			return i % 10 + 1;
		}
		if(i < 650) {
			return i % 5 + 6;
		}

		return i % 10 + 1;
	}
	
	public static int calcScore(ArrayList<Integer> cards) {
		int result = 0;
		for (Integer card : cards) {
			result += card;
		}
		result = result > maxvalue ? 0 : result;
		return result;
	}
	
}
