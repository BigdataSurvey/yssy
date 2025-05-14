package com.zywl.app.base.util;

public class MapHelper {
	
	/**
	 * 地球半径单位km
	 */
	private static double EarthRadius = 6378.137;


	/**
	 *
	 * @param firstLatitude   第一个坐标的纬度
	 * @param firstLongitude  第一个坐标的经度
	 * @param secondLatitude  第二个坐标的纬度
	 * @param secondLongitude 第二个坐标的经度
	 * @return 返回两点之间的距离，单位：公里/千米
	 */
	public static double getDistance(double firstLatitude, double firstLongitude, double secondLatitude, double secondLongitude) {
		double radiansAX = Math.toRadians(firstLatitude); // A经弧度
        double radiansAY = Math.toRadians(firstLongitude); // A纬弧度
        double radiansBX = Math.toRadians(secondLatitude); // B经弧度
        double radiansBY = Math.toRadians(secondLongitude); // B纬弧度
 
        // 公式中“cosβ1cosβ2cos（α1-α2）+sinβ1sinβ2”的部分，得到∠AOB的cos值
        double cos = Math.cos(radiansAY) * Math.cos(radiansBY) * Math.cos(radiansAX - radiansBX)
                + Math.sin(radiansAY) * Math.sin(radiansBY);
//        System.out.println("cos = " + cos); // 值域[-1,1]
        double acos = Math.acos(cos); // 反余弦值
//        System.out.println("acos = " + acos); // 值域[0,π]
//        System.out.println("∠AOB = " + Math.toDegrees(acos)); // 球心角 值域[0,180]
        return EarthRadius * acos;
	}
	
	public static void main(String[] args) {
		System.out.println(getDistance(116.360099, 39.999056, 116.360665,39.999091));
	}
}
