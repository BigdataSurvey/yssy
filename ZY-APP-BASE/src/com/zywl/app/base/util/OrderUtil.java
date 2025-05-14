package com.zywl.app.base.util;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * @Author ns
 * @Date 2023/3/25 16:24
 * @Version 1.0
 */
public class OrderUtil {

    public static  String getOrder5Number(){
        String time = DateUtil.getCurrent3();
        String randomString = RandomStringUtils.randomNumeric(5);
        return time+randomString;
    }

    public static  String getOrder3Number(){
        String time = DateUtil.getCurrent3();
        String randomString = RandomStringUtils.randomNumeric(3);
        return time+randomString;
    }

    
    public static  String getOrder32Number(){
        String time = DateUtil.getCurrent3();
        String randomString = RandomStringUtils.randomNumeric(18);
        return time+randomString;
    }
    
    
    public static  String getBatchOrder32Number(){
        String time = DateUtil.getCurrent3();
        String randomString = RandomStringUtils.randomNumeric(16)+"Zy";
        return time+randomString;
    }

    
    public static String get6NumberCode(){
    	return RandomStringUtils.randomNumeric(6);
    }
    



}
