package com.zywl.app.base.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * @author wenqi5
 * 
 */
public class GZIPUtils {

    public static final String GZIP_ENCODE_UTF_8 = "UTF-8";

    /**
     * 字符串压缩为GZIP字节数组
     * 
     * @param str
     * @return
     */
    public static byte[] compress(String str) {
        return compress(str, GZIP_ENCODE_UTF_8);
    }

    /**
     * 字符串压缩为GZIP字节数组
     * 
     * @param str
     * @param encoding
     * @return
     */
    public static byte[] compress(String str, String encoding) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
        		GZIPOutputStream gzip = new GZIPOutputStream(out)){
            gzip.write(str.getBytes(encoding));
            gzip.finish();
            return out.toByteArray();
        } catch (IOException e) {
        	return null;
        }
    }

    /**
     * 
     * @param bytes
     * @return
     */
    public static String uncompressToString(byte[] bytes) {
        return uncompressToString(bytes, GZIP_ENCODE_UTF_8);
    }

    /**
     * 
     * @param bytes
     * @param encoding
     * @return
     */
    public static String uncompressToString(byte[] bytes, String encoding) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        		GZIPInputStream ungzip = new GZIPInputStream(in);){
            byte[] buffer = new byte[1024];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
            return out.toString(encoding);
        } catch (Exception e) {
        	return null;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String str = "{\"code\":\"007005\",\"data\":{\"liveInfo\":{\"play\":1,\"liveNo\":3678601,\"display\":1,\"recommend\":0,\"cityId\":\"MEoorcae\",\"pushStream\":\"8c72ee0f816cbb9568e97fd23a4d0f64\",\"title\":\"野外作战11111\",\"userId\":\"ox2mXCwfiPP5sVol56MzciExeETmJ4KN\",\"playUrl\":\"rtmp://1019.lssplay.aodianyun.com/mitao/8c72ee0f816cbb9568e97fd23a4d0f64\",\"pushURL\":\"rtmp://41396.lsspublish.aodianyun.com/mitao/8c72ee0f816cbb9568e97fd23a4d0f64\",\"cityName\":\"保定市\",\"createTime\":1564215275000,\"closeTime\":1564754061000,\"playTime\":1564757650746,\"talk\":1,\"id\":\"bgXROLZe1bImpXd9SndIU5IbYtR2OxPG\"},\"liveOnlineTopUserInfo\":[{\"onlineTopUserAmount\":2656.00,\"onlineTopUserInfo\":{\"playerLevel\":1,\"viewerLevel\":9,\"phone\":\"137****2355\",\"sex\":1,\"nickname\":\"哈哈\",\"photo\":\"http://192.168.1.115:8080/data/photo/g3sTtRarGexg0DFF4q5CI9LgQwmyOvpb.jpg?1563893412751\",\"id\":\"g3sTtRarGexg0DFF4q5CI9LgQwmyOvpb\",\"playerStatus\":1}},{\"joinTime\":1564761581292,\"onlineTopUserInfo\":{\"playerLevel\":1,\"viewerLevel\":1,\"phone\":\"132****0001\",\"sex\":1,\"nickname\":\"阿里咯哦哦\",\"id\":\"HnSaqbwKLqJ2qPRafjN2HjhfPSKj9QQf\",\"playerStatus\":0}}],\"playerUserInfo\":{\"playerLevel\":1,\"viewerLevel\":1,\"phone\":\"137****2351\",\"sex\":1,\"nickname\":\"狗\",\"photo\":\"http://192.168.1.115:8080/data/photo/ox2mXCwfiPP5sVol56MzciExeETmJ4KN.jpg?1563978881475\",\"id\":\"ox2mXCwfiPP5sVol56MzciExeETmJ4KN\",\"playerStatus\":1},\"walletInfo\":{\"availableAmount\":0.0000,\"createTime\":1564747666000,\"lockAmount\":0.0000,\"currency\":\"GOLD\",\"id\":\"pQwdfj6CLhOZcJmq318JsVT7MRy66tk4\",\"totalSpendAmount\":0.0000,\"totalIncomeAmount\":0.0000,\"totalRealIncomeAmount\":0.0000,\"userId\":\"HnSaqbwKLqJ2qPRafjN2HjhfPSKj9QQf\"},\"defaultSysChat\":{\"message\":\"为了营造一个良好的直播环境，请大家文明聊天，遵守平台相关规则、规定。不要轻易添加聊天区域出现的私人联系方式，谨防上当受骗。\"},\"liveId\":\"bgXROLZe1bImpXd9SndIU5IbYtR2OxPG\",\"liveOnlineUserNum\":2,\"liveWeekTopTotalAmount\":0,\"fans\":{\"createTime\":1564759409081,\"fansUserId\":\"HnSaqbwKLqJ2qPRafjN2HjhfPSKj9QQf\",\"id\":\"tUT2UNlPmiBno6gJ2zSoCwgqshMyQ2OV\",\"userId\":\"ox2mXCwfiPP5sVol56MzciExeETmJ4KN\"}},\"id\":\"GEPQODJAALJTZSUUPAXHZGRZKWUYYWZY\",\"locale\":\"zh_CN\",\"push\":false,\"requestTime\":\"2019-08-02 23:59:41\",\"responseTime\":\"2019-08-02 23:59:41\",\"success\":true}";
        byte[] compress = compress(str);
        ByteBuffer wrap = ByteBuffer.wrap(compress);
        byte[] array = wrap.array();
        System.out.println(str.length());
        System.out.println(array.length);
    }
}
