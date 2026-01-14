package com.zywl.app.manager.mian;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.util.DesUtil;
import com.zywl.app.base.util.GZIPUtils;
import com.zywl.app.base.util.UID;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * 本地联调 WebSocket 客户端
 * 避免 javax.websocket 等库自动对 URI 再次编码导致服务端 400 错误。
 */
public class AppWsLocalTestClient {

    private static final String DEFAULT_MANAGER_BASE = "http://127.0.0.1:8080/ZY-APP-MANAGER";
    private static final String DEFAULT_ACCESS_TOKEN = "1";
    private static final String DEFAULT_OPEN_ID = "1";
    private static final String DEFAULT_VERSION_ID = "1";
    private static final String DEFAULT_LOCALE = "zh_CN";

    private static final String PROP_WS_OVERRIDE = "ws.override";
    private static final String PROP_CMD_CODE = "cmd.code";
    private static final String PROP_CMD_DATA = "cmd.data";

    private static final int SOCKET_READ_TIMEOUT = 15000;

    public static void main(String[] args) {
        try {
            runClient();
        } catch (Exception e) {
            System.err.println("[CLIENT] Fatal Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runClient() throws Exception {
        String managerBase = System.getProperty("manager.base", DEFAULT_MANAGER_BASE);
        String accessToken = System.getProperty("accessToken", DEFAULT_ACCESS_TOKEN);
        String openId = System.getProperty("openId", DEFAULT_OPEN_ID);
        String versionId = System.getProperty("versionId", DEFAULT_VERSION_ID);
        String locale = System.getProperty("locale", DEFAULT_LOCALE);

        String cmdCode = System.getProperty(PROP_CMD_CODE, "004001");
        // gameId
        String cmdDataStr = System.getProperty(PROP_CMD_DATA, "{\"gameId\":12}");

        // HTTP 登录
        System.out.println("[CLIENT] HTTP login -> " + managerBase + "/wxLoginOauth?accessToken=...");
        LoginResult login = httpLogin(managerBase, accessToken, openId, versionId, locale);

        System.out.println("[CLIENT] Login Success:");
        System.out.println("    userId         = " + login.userId);
        System.out.println("    wsid           = " + login.wsid);
        System.out.println("    authServerHost = " + login.authServerHost);

        // 连接 WebSocket
        RawWsClient ws = connectWebSocket(login);

        // 发送业务指令
        JSONObject data = JSON.parseObject(cmdDataStr);
        sendStrictFrontendMessage(ws, login, cmdCode, data);

        // 等待回包
        System.out.println("[CLIENT] Listening... (Ctrl+C to exit)");
        ws.awaitClose();
    }

    private static LoginResult httpLogin(String managerBase, String accessToken, String openId, String versionId, String locale) throws Exception {
        String url = managerBase + "/wxLoginOauth"
                + "?accessToken=" + urlEncodeOnce(accessToken)
                + "&openId=" + urlEncodeOnce(openId)
                + "&versionId=" + urlEncodeOnce(versionId)
                + "&locale=" + urlEncodeOnce(locale);

        String resp = httpGet(url);
        JSONObject root = JSON.parseObject(resp);
        if (root.getIntValue("code") != 1) {
            throw new RuntimeException("HTTP login failed: " + root.getString("message"));
        }

        JSONObject dataObj = root.getJSONObject("data");
        JSONObject wsInfo = dataObj.getJSONObject("wsInfo");

        LoginResult lr = new LoginResult();
        // 从wsInfo取userId
        lr.userId = wsInfo.getString("userId");
        lr.wsid = wsInfo.getString("wsid");
        lr.wsPrivateKey = wsInfo.getString("wsPrivateKey");
        lr.authServerHost = wsInfo.getString("authServerHost");

        if (isBlank(lr.wsid) || isBlank(lr.wsPrivateKey) || isBlank(lr.authServerHost)) {
            throw new RuntimeException("wsInfo missing required fields");
        }
        return lr;
    }

    private static RawWsClient connectWebSocket(LoginResult login) throws Exception {
        HostAndPath hp = parseAuthServerHost(login.authServerHost);
        String wsOverride = System.getProperty(PROP_WS_OVERRIDE);
        if (!isBlank(wsOverride)) {
            HostAndPort hop = parseHostPort(wsOverride.trim());
            hp.host = hop.host;
            hp.port = hop.port;
        }

        String shakePlain = "{\"wsPrivateKey\":\"" + login.wsPrivateKey + "\"}";
        String shakeEncrypted = DesUtil.encrypt(shakePlain, login.wsPrivateKey);
        String shakeOnce = urlEncodeOnce(shakeEncrypted);
        String shakeTwice = urlEncodeOnce(shakeOnce);

        List<String> shakeCandidates = Arrays.asList(shakeTwice, shakeOnce, shakeEncrypted);
        List<String> basePathCandidates = buildBasePathCandidates(hp.basePath);

        RawWsClient ws = null;
        Exception lastEx = null;

        outerLoop:
        for (String basePath : basePathCandidates) {
            for (String shakeData : shakeCandidates) {
                String path = normalizePath(basePath) + "/" + login.wsid + "/" + shakeData;
                String uri = "ws://" + hp.host + ":" + hp.port + path;
                System.out.println("[CLIENT] WS Connecting -> " + uri);
                try {
                    ws = new RawWsClient(hp.host, hp.port, path, login.wsPrivateKey);
                    ws.connect();
                    System.out.println("[CLIENT] WS Connected OK");
                    break outerLoop;
                } catch (Exception e) {
                    lastEx = e;
                }
            }
        }

        if (ws == null || !ws.isConnected()) {
            throw new RuntimeException("Failed to connect WS. Last error: " + (lastEx != null ? lastEx.getMessage() : "unknown"));
        }
        return ws;
    }

    /**
     * 发送结构
     */
    private static void sendStrictFrontendMessage(RawWsClient ws, LoginResult login, String code, JSONObject data) throws Exception {
        if (!data.containsKey("userId")) {
            data.put("userId", login.userId);
        }
        JSONObject root = new JSONObject();
        root.put("code", code);
        root.put("data", data);
        // 序列化 -> 加密 -> 压缩
        String plain = root.toJSONString();
        String encrypted = DesUtil.encrypt(plain, login.wsPrivateKey);
        byte[] compressed = GZIPUtils.compress(encrypted);
        System.out.println("==================================================");
        System.out.println("[CLIENT] SEND (Frontend Style):");
        System.out.println("    Payload: " + plain);
        System.out.println("==================================================");
        ws.sendBinary(compressed);
    }

    private static String httpGet(String urlStr) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            int code = conn.getResponseCode();
            try (InputStream in = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream()) {
                return new String(readAllBytes(in), StandardCharsets.UTF_8);
            }
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static class RawWsClient {
        private final String host;
        private final int port;
        private final String path;
        private final String wsPrivateKey;
        private final CountDownLatch closedLatch = new CountDownLatch(1);
        private Socket socket;
        private InputStream in;
        private OutputStream out;
        private volatile boolean connected;

        RawWsClient(String host, int port, String path, String wsPrivateKey) {
            this.host = host;
            this.port = port;
            this.path = path;
            this.wsPrivateKey = wsPrivateKey;
        }

        boolean isConnected() { return connected; }

        void connect() throws Exception {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            socket.setSoTimeout(SOCKET_READ_TIMEOUT);
            in = socket.getInputStream();
            out = socket.getOutputStream();
            String secKey = Base64.getEncoder().encodeToString(randomBytes(16));
            String req = "GET " + path + " HTTP/1.1\r\n" +
                    "Host: " + host + ":" + port + "\r\n" +
                    "Upgrade: websocket\r\n" +
                    "Connection: Upgrade\r\n" +
                    "Sec-WebSocket-Key: " + secKey + "\r\n" +
                    "Sec-WebSocket-Version: 13\r\n" +
                    "User-Agent: AppWsLocalTestClient/1.0\r\n\r\n";
            out.write(req.getBytes(StandardCharsets.US_ASCII));
            out.flush();

            byte[] headerBytes = readHttpHeaderBytes(in, 64 * 1024);
            String header = new String(headerBytes, StandardCharsets.ISO_8859_1);
            if (!header.contains(" 101 ")) throw new RuntimeException("Handshake failed");
            connected = true;
            Thread t = new Thread(this::readLoop, "WS-Reader");
            t.setDaemon(true);
            t.start();
        }

        void awaitClose() throws InterruptedException { closedLatch.await(); }

        void sendBinary(byte[] payload) throws IOException {
            if (!connected) throw new IOException("WS not connected");
            sendFrame(0x2, payload);
        }

        private void sendFrame(int opcode, byte[] payload) throws IOException {
            byte[] mask = randomBytes(4);
            ByteArrayOutputStream frame = new ByteArrayOutputStream();
            frame.write(0x80 | (opcode & 0x0F));
            int len = payload.length;
            if (len <= 125) frame.write(0x80 | len);
            else if (len <= 0xFFFF) {
                frame.write(0x80 | 126);
                frame.write((len >>> 8) & 0xFF);
                frame.write(len & 0xFF);
            } else {
                frame.write(0x80 | 127);
                long l = len;
                for (int i = 7; i >= 0; i--) frame.write((int) ((l >>> (8 * i)) & 0xFF));
            }
            frame.write(mask);
            byte[] masked = new byte[len];
            for (int i = 0; i < len; i++) masked[i] = (byte) (payload[i] ^ mask[i % 4]);
            frame.write(masked);
            out.write(frame.toByteArray());
            out.flush();
        }

        private void readLoop() {
            try {
                while (connected) {
                    try {
                        Frame f = readFrame(in);
                        if (f == null) break;
                        if (f.opcode == 0x8) connected = false;
                        else if (f.opcode == 0x9) sendFrame(0xA, f.payload);
                        else if (f.opcode == 0x2) handleBinaryMessage(f.payload);
                    } catch (SocketTimeoutException e) {
                        System.out.println("[CLIENT] No data received for " + (SOCKET_READ_TIMEOUT / 1000) + "s (Idle Timeout).");
                        break;
                    }
                }
            } catch (Exception e) {
                if (connected) System.out.println("[CLIENT] readLoop error: " + e.getMessage());
            } finally {
                connected = false;
                try { if (socket != null) socket.close(); } catch (Exception ignore) {}
                closedLatch.countDown();
            }
        }

        private void handleBinaryMessage(byte[] payload) {
            try {
                String msg = GZIPUtils.uncompressToString(payload);
                if (msg != null && msg.trim().startsWith("{")) {
                    System.out.println("[CLIENT] RECV plain: " + msg);
                } else {
                    System.out.println("[CLIENT] RECV decrypt: " + DesUtil.decrypt(msg, wsPrivateKey));
                }
            } catch (Exception e) {
                System.out.println("[CLIENT] RECV decode fail: " + e.getMessage());
            }
        }
    }

    private static class Frame {
        final int opcode;
        final byte[] payload;
        Frame(int opcode, byte[] payload) { this.opcode = opcode; this.payload = payload; }
    }

    private static Frame readFrame(InputStream in) throws IOException {
        int b1 = in.read();
        if (b1 < 0) return null;
        int b2 = in.read();
        boolean fin = (b1 & 0x80) != 0;
        boolean masked = (b2 & 0x80) != 0;
        long len = (b2 & 0x7F);
        if (len == 126) {
            int bb1 = in.read(); int bb2 = in.read();
            len = ((bb1 & 0xFF) << 8) | (bb2 & 0xFF);
        } else if (len == 127) {
            len = 0;
            for (int i = 0; i < 8; i++) len = (len << 8) | (in.read() & 0xFF);
        }
        byte[] mask = null;
        if (masked) { mask = new byte[4]; readFully(in, mask); }
        byte[] payload = new byte[(int) len];
        readFully(in, payload);
        if (masked && mask != null) {
            for (int i = 0; i < payload.length; i++) payload[i] = (byte) (payload[i] ^ mask[i % 4]);
        }
        // 简化处理: 非 FIN 帧暂不拼装，仅处理单帧情况
        return new Frame(b1 & 0x0F, payload);
    }

    private static class LoginResult {
        String userId; // 新增 userId
        String wsid;
        String wsPrivateKey;
        String authServerHost;
    }

    private static class HostAndPath { String host; int port; String basePath; }
    private static class HostAndPort { String host; int port; HostAndPort(String h, int p){host=h;port=p;} }

    private static HostAndPath parseAuthServerHost(String s) {
        s = s.replaceAll("^(ws|wss|http|https)://", "");
        int slash = s.indexOf('/');
        String hostPort = (slash >= 0) ? s.substring(0, slash) : s;
        String basePath = (slash >= 0) ? s.substring(slash) : "";
        HostAndPort hp = parseHostPort(hostPort);
        HostAndPath r = new HostAndPath();
        r.host = hp.host; r.port = hp.port; r.basePath = basePath;
        return r;
    }

    private static HostAndPort parseHostPort(String s) {
        int idx = s.lastIndexOf(':');
        return new HostAndPort(s.substring(0, idx), Integer.parseInt(s.substring(idx + 1)));
    }

    private static List<String> buildBasePathCandidates(String p) {
        p = (p == null) ? "" : p.trim();
        List<String> list = new ArrayList<>();
        if (p.isEmpty()) { list.add("/APPServer"); return list; }
        list.add(p);
        int idx = p.indexOf("/APPServer");
        if (idx >= 0 && !p.substring(idx).equals(p)) list.add(p.substring(idx));
        if (!p.endsWith("/APPServer")) list.add(normalizePath(p) + "/APPServer");
        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    private static String normalizePath(String p) {
        String s = p.trim();
        if (!s.startsWith("/")) s = "/" + s;
        while (s.endsWith("/") && s.length() > 1) s = s.substring(0, s.length() - 1);
        return s;
    }

    private static String urlEncodeOnce(String s) {
        try { return URLEncoder.encode(s, "UTF-8").replace("+", "%20"); } catch (Exception e) { throw new RuntimeException(e); }
    }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static byte[] readAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[4096]; int n;
        while ((n = in.read(buf)) >= 0) bos.write(buf, 0, n);
        return bos.toByteArray();
    }
    private static void readFully(InputStream in, byte[] buf) throws IOException {
        int off = 0;
        while (off < buf.length) {
            int n = in.read(buf, off, buf.length - off);
            if (n < 0) throw new EOFException();
            off += n;
        }
    }
    private static byte[] readHttpHeaderBytes(InputStream in, int max) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int state = 0;
        while (bos.size() < max) {
            int b = in.read(); if (b < 0) break; bos.write(b);
            if (state==0 && b=='\r') state=1; else if (state==1 && b=='\n') state=2;
            else if (state==2 && b=='\r') state=3; else if (state==3 && b=='\n') break;
            else if (b!='\r') state=0;
        }
        return bos.toByteArray();
    }
    private static byte[] randomBytes(int n) { byte[] b=new byte[n]; new SecureRandom().nextBytes(b); return b; }
}