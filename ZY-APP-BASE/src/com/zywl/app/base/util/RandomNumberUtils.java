package com.zywl.app.base.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomNumberUtils {

	public synchronized static long create(int length) {
		int min = (int) Math.pow(10, length - 1);
		int max = (int) Math.pow(10, length) - 1;
		Random random = new Random();
		return (random.nextInt(max) % (max - min + 1) + min);
	}
	
	public static void main(String[] args) {
		Set<Long> set = new HashSet<Long>();
		long t1 = System.currentTimeMillis();
		do {
			long create = create(6);
			if(!set.contains(create) && !RegexUtil.isLiangHao(create+"")) {
//				System.out.println(create);
				set.add(create);
			}
		} while (set.size() < 800000);
		System.out.println(System.currentTimeMillis() - t1);
	}
}
