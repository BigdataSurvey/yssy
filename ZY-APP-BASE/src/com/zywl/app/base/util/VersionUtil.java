package com.zywl.app.base.util;

public class VersionUtil {
	
	public static long version2Long(String versionNum){
		
		String[] split = versionNum.split("\\.");
		String result = "";
		for (String string : split) {
			result += addZeroForNum(string, 3);
		}
		return Long.parseLong(result);
	}
	
	/**
	 * @param versionNum1 1
	 * @param versionNum2 2
	 * @return
	 * 1 > 2 return 1 <br>
	 * 1 < 2 return -1 <br>
	 * 1 = 2 return 0 <br>
	 */
	public static int compare(String versionNum1, String versionNum2){
		long long1 = version2Long(versionNum1);
		long long2 = version2Long(versionNum2);
		if(long1 == long2){
			return 0;
		}
		return long1 > long2 ? 1 : -1;
	}
	
	public static String addZeroForNum(String str, int strLength) {
	    int strLen = str.length();
	    if (strLen < strLength) {
	        while (strLen < strLength) {
	            StringBuffer sb = new StringBuffer();
//	            sb.append("0").append(str);// 左补0
	            sb.append(str).append("0");//右补0
	            str = sb.toString();
	            strLen = str.length();
	        }
	    }
	    return str;
	}
	
	public static void main(String[] args) {
		
		System.out.println(updateV2("v1.1.4"));
	}
	
	
	public static String updateV3(String version) {
		if (!version.startsWith("v")) {
			return null;
		}
		version = version.substring(1, version.length());
		version = version.replace(".", "-");
		String[] a = version.split("-");
		int v1=Integer.parseInt(a[0]) ;
		int v2=Integer.parseInt(a[1]) ;
		int v3=Integer.parseInt(a[2]) ;
		v3+=1;
		return "v"+v1+"."+v2+"."+v3;
	}
	
	
	public static String updateV2(String version) {
		if (!version.startsWith("v")) {
			return null;
		}
		version = version.substring(1, version.length());
		version = version.replace(".", "-");
		String[] a = version.split("-");
		int v1=Integer.parseInt(a[0]) ;
		int v2=Integer.parseInt(a[1]) ;
		int v3=Integer.parseInt(a[2]) ;
		v2+=1;
		v3=0;
		return "v"+v1+"."+v2+"."+v3;
	}
	
	public static String updateV1(String version) {
		if (!version.startsWith("v")) {
			return null;
		}
		version = version.substring(1, version.length());
		version = version.replace(".", "-");
		String[] a = version.split("-");
		int v1=Integer.parseInt(a[0]) ;
		int v2=Integer.parseInt(a[1]) ;
		int v3=Integer.parseInt(a[2]) ;
		v1+=1;
		v2=0;
		v3=0;
		return "v"+v1+"."+v2+"."+v3;
	}
	
	
}
