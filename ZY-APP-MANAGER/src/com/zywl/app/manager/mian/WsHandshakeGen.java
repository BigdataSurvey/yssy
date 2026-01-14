package com.zywl.app.manager.mian;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.util.DesUtil;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * WebSocket 连接命令
 * 1.在redis的t:app:user:ws:937223- 拿到wsPrivateKey和wsid
 * 2.输入到main参数里运行
 * 3.得到WS URL
 * 2.下载 https://github.com/vi/websocat/releases 中的 websocat.x86_64-pc-windows-gnu.exe //可以换成最新版本，然后重命名为websocat.exe 之后放在cmd默认路径 C:\Users\Administrator
 * 3.之后再cmd..运行
 * .\websocat "ws://127.0.0.1:8083/ZY-APP-SERVER/APPServer/z3id8JYIIEDC0iMaAHyDATBj0g2RZQLj/Gph92wFw%252BLDCBTkd%252FZGCujthOHcotXqPpKrq6N%252FpP6CBfAZCYfpV1CLK9cml4vW90BhdYFjChE0%253D"
 * @author lzx
 * @version V1.0
 * @since 2026/1/13
 */
public class WsHandshakeGen {

    /**
     * WebSocket 服务端基础地址
     * 例如：ws://127.0.0.1:8083/ZY-APP-SERVER/APPServer
     */
    private static final String SERVER_BASE_URL = "ws://127.0.0.1:8083/ZY-APP-SERVER/APPServer";

    /**
     * t:app:user:ws:...的wsid
     */
    private static final String WS_ID = "NhGxT8PDPl4xZnVhAvSXsW4u9T0bmaNm";

    /**
     * Redis 中的PrivateKey
     */
    private static final String WS_PRIVATE_KEY = "krBpavMZKt9rUTtGqiOcvHF6JKPQ1POQ";

    public static void main(String[] args) {
        try {
            // 构建鉴权数据对象
            JSONObject authObj = new JSONObject();
            authObj.put("wsPrivateKey", WS_PRIVATE_KEY);

            //  DES 加密
            String encrypted = DesUtil.encrypt(authObj.toJSONString(), WS_PRIVATE_KEY);

            // URL 编码
            String encodedOnce = URLEncoder.encode(encrypted, StandardCharsets.UTF_8.name());
            String encodedTwice = URLEncoder.encode(encodedOnce, StandardCharsets.UTF_8.name());

            // URL
            String fullUrl = String.format("%s/%s/%s", SERVER_BASE_URL, WS_ID, encodedTwice);

            printDebugInfo(encrypted, encodedTwice);

            String command = ".\\websocat -v \"" + fullUrl + "\"";

            System.out.println("================= Execution Command =================");
            System.out.println(command);
            System.out.println("=====================================================");

        } catch (Exception e) {
            System.err.println("生成 WebSocket 连接命令时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printDebugInfo(String encrypted, String encoded) {
        System.out.println("------------------ Debug Info ------------------");
        System.out.println("WSID      : " + WS_ID);
        System.out.println("Encrypted : " + encrypted);
        System.out.println("Encoded   : " + encoded);
        System.out.println("------------------------------------------------");
        System.out.println();
    }
}