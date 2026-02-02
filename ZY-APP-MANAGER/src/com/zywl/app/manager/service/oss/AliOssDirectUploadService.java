package com.zywl.app.manager.service.oss;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Author: lzx
 * @Create: 2026/1/3
 * @Version: V1.2 OSS功能迁移
 * @Description: 阿里云 OSS 直传签名/资源访问服务（基础能力，供多业务复用）
 */
@Slf4j
@Service
public class AliOssDirectUploadService {

    @Autowired
    private ManagerConfigService managerConfigService;

    private static final String KEY_ENDPOINT = "endpoint";
    private static final String KEY_BUCKET = "bucket";
    private static final String KEY_ACCESS_KEY_ID = "accessKeyId";
    private static final String KEY_ACCESS_KEY_SECRET = "accessKeySecret";
    private static final String KEY_DIR_PREFIX = "dirPrefix";
    private static final String KEY_PUBLIC_DOMAIN = "publicDomain";
    private static final String KEY_EXPIRE_SECONDS = "expireSeconds";
    private static final String KEY_MAX_SIZE = "maxSize";

    private static final String KEY_BIZ_RULES = "bizRules";
    private static final String KEY_ALLOW_SUFFIX = "allowSuffix";

    private static final String KEY_READ_MODE = "readMode";
    private static final String KEY_READ_EXPIRE_SECONDS = "readExpireSeconds";

    private static final String READ_MODE_PUBLIC = "public";
    private static final String READ_MODE_PRIVATE = "private";

    private static final String DEFAULT_DIR_PREFIX = "yssy";
    private static final long DEFAULT_EXPIRE_SECONDS = 300L;
    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024L;
    // 7天
    private static final long DEFAULT_READ_EXPIRE_SECONDS = 7 * 24 * 3600L;

    private static final int DEFAULT_ARRAY_MIN = 1;
    private static final int DEFAULT_ARRAY_MAX = 10;

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static final DateTimeFormatter ISO_8601_UTC = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    // 简单缓存
    private static final long CFG_CACHE_MS = 5_000L;
    private volatile JSONObject cachedCfg;
    private volatile long cachedAtMs;

    public JSONObject buildPostPolicy(String bizDir, Long userId, String suffix) {
        return buildPostPolicy(bizDir, userId, suffix, null, null);
    }
    /**
     * OSS 直传签名入口
     * 生成给前端直传所需的字段, 并对maxSize/expireSeconds做服务端的裁剪;同时返回readableUrl用于前端展示
     * **/
    public JSONObject buildPostPolicy(String bizDir, Long userId, String suffix, Long requestMaxSize, Long requestExpireSeconds) {
        JSONObject cfg = getAndCheckCfg();

        String biz = normalizeBiz(bizDir);
        String normalizedSuffix = normalizeSuffix(suffix);

        // 可选 suffix 白名单
        checkSuffixAllowed(cfg, biz, normalizedSuffix);

        String dir = generateObjectDir(cfg, biz, userId);
        String fileName = generateUniqueFileName(normalizedSuffix);
        String objectKey = dir + fileName;

        long maxSize = resolveMaxSize(cfg, biz, requestMaxSize);
        long expireSeconds = resolveExpireSeconds(cfg, biz, requestExpireSeconds);

        long expireEpoch = System.currentTimeMillis() / 1000 + expireSeconds;
        String expiration = ISO_8601_UTC.format(Instant.ofEpochSecond(expireEpoch));

        String bucket = cfg.getString(KEY_BUCKET);
        String successActionStatus = "200";

        String policyBase64 = createPolicyBase64(expiration, bucket, dir, maxSize, successActionStatus);
        String signature = computeSignature(cfg.getString(KEY_ACCESS_KEY_SECRET), policyBase64);

        String host = buildHostInternal(cfg.getString(KEY_ENDPOINT), bucket);
        String urlPrefix = buildUrlPrefix(cfg, host);

        JSONObject resp = new JSONObject();
        resp.put("accessId", cfg.getString(KEY_ACCESS_KEY_ID));
        resp.put("policy", policyBase64);
        resp.put("signature", signature);
        resp.put("dir", dir);
        resp.put("fileName", fileName);
        resp.put("objectKey", objectKey);
        resp.put("host", host);
        resp.put("urlPrefix", urlPrefix);
        resp.put("finalUrl", urlPrefix + "/" + objectKey);
        resp.put("expire", expireEpoch);
        resp.put("maxSize", maxSize);
        resp.put("success_action_status", successActionStatus);

        // 资源读取模式, public=裸链接可直接访问；private=展示时返回签名GET链接托底
        resp.put("readMode", resolveReadMode(cfg));
        resp.put("readableUrl", toReadableUrl(objectKey, null));

        return resp;
    }


    /**
     * 规范化并强校验 OSS 资源地址 输入完整URL或 objectKey
     */
    public String canonicalizeAndCheckOssUrl(String raw, String fieldName, boolean allowEmpty) {
        String fn = StringUtils.hasText(fieldName) ? fieldName : "url";

        if (!StringUtils.hasText(raw)) {
            if (allowEmpty) return null;
            throw new RuntimeException(fn + "不能为空");
        }

        String value = raw.trim();

        // 完整URL的话必须校验host合法
        if (value.startsWith("http://") || value.startsWith("https://")) {
            checkOssUrlHostAllowed(value, fn);
            return value;
        }

        // objectKey的话urlPrefix
        String urlPrefix = buildUrlPrefix();
        Assert.hasText(urlPrefix, "OSS 配置缺失，无法生成" + fn + "地址");

        while (value.startsWith("/")) value = value.substring(1);
        return urlPrefix + "/" + value;
    }

    /**
     * 校验用户传入的 OSS 完整 URL 是否属于本系统允许的 OSS 域名
     */
    public void checkOssUrlHostAllowed(String url, String fieldName) {
        JSONObject cfg = getAndCheckCfg();

        String bucket = cfg.getString(KEY_BUCKET);
        String standardHost = buildHostInternal(cfg.getString(KEY_ENDPOINT), bucket);
        String urlPrefix = buildUrlPrefix(cfg, standardHost);

        String allowedHost1 = null;
        String allowedHost2 = null;
        try {
            if (StringUtils.hasText(urlPrefix)) allowedHost1 = URI.create(urlPrefix).getHost();
            if (StringUtils.hasText(standardHost)) allowedHost2 = URI.create(standardHost).getHost();
        } catch (Exception ignored) {
        }

        String inputHost;
        try {
            inputHost = URI.create(url).getHost();
        } catch (Exception e) {
            throw new RuntimeException(fieldName + "地址格式错误");
        }

        if (!StringUtils.hasText(inputHost)) {
            throw new RuntimeException(fieldName + "地址格式错误");
        }

        boolean ok1 = allowedHost1 != null && inputHost.equalsIgnoreCase(allowedHost1);
        boolean ok2 = allowedHost2 != null && inputHost.equalsIgnoreCase(allowedHost2);

        if (!ok1 && !ok2) {
            throw new RuntimeException(fieldName + "非法，请上传至官方服务器");
        }
    }

    /**
     * 将资源地址转换为 可展示 URL
     * **/
    public String toReadableUrl(String urlOrKey, Long requestReadExpireSeconds) {
        if (!StringUtils.hasText(urlOrKey)) return null;

        // 先规范化
        String canonical = canonicalizeAndCheckOssUrl(urlOrKey, "url", true);
        if (!StringUtils.hasText(canonical)) return null;

        JSONObject cfg = getAndCheckCfg();
        String mode = resolveReadMode(cfg);

        if (READ_MODE_PUBLIC.equalsIgnoreCase(mode)) {
            return canonical;
        }

        // private的话就 需要签名 GET
        String objectKey = extractObjectKey(canonical);
        long expireSeconds = resolveReadExpireSeconds(cfg, requestReadExpireSeconds);
        return buildSignedGetUrl(objectKey, expireSeconds);
    }

    /**
     * 将 JSON 数组字符串或者单个字符串 统一转为 可展示URL数组 JSON字符串
     * **/
    public String toReadableUrlArrayJsonString(String jsonArrOrSingle, Long requestReadExpireSeconds) {
        if (!StringUtils.hasText(jsonArrOrSingle)) return jsonArrOrSingle;

        JSONArray array = parseJsonArrayOrSingle(jsonArrOrSingle, false, "urlArray");
        for (int i = 0; i < array.size(); i++) {
            String raw = array.getString(i);
            if (StringUtils.hasText(raw)) {
                array.set(i, toReadableUrl(raw, requestReadExpireSeconds));
            }
        }
        return array.toJSONString();
    }

    /**
     * 通用数组字段校验与规范化
     */
    public String normalizeAndCheckUrlArrayJsonString(String jsonArrOrSingle,
                                                      String fieldName,
                                                      Integer min,
                                                      Integer max) {
        String fn = StringUtils.hasText(fieldName) ? fieldName : "urlArray";

        int minVal = (min == null || min <= 0) ? DEFAULT_ARRAY_MIN : min;
        int maxVal = (max == null || max <= 0) ? DEFAULT_ARRAY_MAX : max;
        if (maxVal < minVal) maxVal = minVal;

        if (!StringUtils.hasText(jsonArrOrSingle)) {
            throw new RuntimeException(fn + "不能为空");
        }

        JSONArray array = parseJsonArrayOrSingle(jsonArrOrSingle, true, fn);

        if (array.isEmpty()) throw new RuntimeException(fn + "至少需要" + minVal + "项");
        if (array.size() < minVal || array.size() > maxVal) {
            throw new RuntimeException(fn + "数量范围必须是" + minVal + "~" + maxVal);
        }

        for (int i = 0; i < array.size(); i++) {
            String val = array.getString(i);
            if (!StringUtils.hasText(val)) {
                throw new RuntimeException(fn + "第" + (i + 1) + "项为空");
            }
            array.set(i, canonicalizeAndCheckOssUrl(val, fn, false));
        }
        return array.toJSONString();
    }


    /**
     *生成 OSS private 模式下的 GET 签名 URL
     */
    public String buildSignedGetUrl(String objectKey, long expireSeconds) {
        JSONObject cfg = getAndCheckCfg();
        String bucket = cfg.getString(KEY_BUCKET);

        String accessKeyId = cfg.getString(KEY_ACCESS_KEY_ID);
        String accessKeySecret = cfg.getString(KEY_ACCESS_KEY_SECRET);

        String host = buildHostInternal(cfg.getString(KEY_ENDPOINT), bucket);

        long expires = System.currentTimeMillis() / 1000 + Math.max(60, expireSeconds);
        String resource = "/" + bucket + "/" + objectKey;

        String stringToSign = "GET\n\n\n" + expires + "\n" + resource;
        String signature = hmacSha1Base64(accessKeySecret, stringToSign);

        String encodedSig;
        try {
            encodedSig = URLEncoder.encode(signature, "UTF-8");
        } catch (Exception e) {
            encodedSig = signature;
        }

        return host + "/" + objectKey + "?OSSAccessKeyId=" + accessKeyId + "&Expires=" + expires + "&Signature=" + encodedSig;
    }

    /**
     * 获取 OSS 资源访问前缀 用于将 objectKey 拼接成完整 URL。
     * **/
    public String buildUrlPrefix() {
        JSONObject cfg = getAndCheckCfg();
        String host = buildHostInternal(cfg.getString(KEY_ENDPOINT), cfg.getString(KEY_BUCKET));
        return buildUrlPrefix(cfg, host);
    }

    /**
     * 获取当前配置下的标准 OSS host 用于直传上传目标或私有读签名 URL 生成。
     * **/
    public String buildHost() {
        JSONObject cfg = getAndCheckCfg();
        return buildHostInternal(cfg.getString(KEY_ENDPOINT), cfg.getString(KEY_BUCKET));
    }

    /**
     * 数组字符串或单字符串解析为 JSONArray
     * **/
    private JSONArray parseJsonArrayOrSingle(String jsonArrOrSingle, boolean strict, String fieldName) {
        String s = jsonArrOrSingle.trim();
        try {
            if (s.startsWith("[")) {
                JSONArray arr = JSONArray.parseArray(s);
                return arr == null ? new JSONArray() : arr;
            }
            JSONArray one = new JSONArray();
            one.add(s);
            return one;
        } catch (Exception e) {
            if (strict) {
                throw new RuntimeException(fieldName + "格式错误");
            }
            JSONArray one = new JSONArray();
            one.add(s);
            return one;
        }
    }

    /**
     * 从完整 URL 中提取 objectKey
     * **/
    private String extractObjectKey(String fullUrl) {
        if (!StringUtils.hasText(fullUrl)) return null;
        String v = fullUrl.trim();
        if (!(v.startsWith("http://") || v.startsWith("https://"))) {
            while (v.startsWith("/")) v = v.substring(1);
            return v;
        }
        URI uri = URI.create(v);
        String path = uri.getPath();
        if (!StringUtils.hasText(path)) return null;
        while (path.startsWith("/")) path = path.substring(1);
        return path;
    }

    /**
     * 解析资源读模式 readMode 配置
     * **/
    private String resolveReadMode(JSONObject cfg) {
        String mode = cfg.getString(KEY_READ_MODE);
        if (!StringUtils.hasText(mode)) return READ_MODE_PUBLIC;
        mode = mode.trim().toLowerCase(Locale.ROOT);
        if (READ_MODE_PRIVATE.equals(mode)) return READ_MODE_PRIVATE;
        return READ_MODE_PUBLIC;
    }

    /**
     * 解析 private 读模式下签名链接有效期
     * **/
    private long resolveReadExpireSeconds(JSONObject cfg, Long req) {
        long base = cfg.getLongValue(KEY_READ_EXPIRE_SECONDS);
        if (base <= 0) base = DEFAULT_READ_EXPIRE_SECONDS;
        if (req == null || req <= 0) return base;
        return Math.min(base, req);
    }

    /**
     * 文件后缀规范化
     * **/
    private String normalizeBiz(String bizDir) {
        String biz = bizDir;
        if (!StringUtils.hasText(biz)) biz = "misc";
        biz = biz.trim().toLowerCase(Locale.ROOT);
        biz = biz.replaceAll("[^a-z0-9_\\-]", "");
        if (!StringUtils.hasText(biz)) biz = "misc";
        return biz;
    }

    private String normalizeSuffix(String suffix) {
        if (!StringUtils.hasText(suffix)) return null;
        String s = suffix.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith(".")) s = s.substring(1);
        s = s.replaceAll("[^a-z0-9]", "");
        if (!StringUtils.hasText(s)) return null;
        return s;
    }

    /**
     * 按配置的 bizRules/allowSuffix 校验文件后缀是否允许上传
     * **/
    private void checkSuffixAllowed(JSONObject cfg, String biz, String suffix) {
        JSONObject rules = cfg.getJSONObject(KEY_BIZ_RULES);
        if (rules == null || rules.isEmpty()) return;
        JSONObject bizRule = rules.getJSONObject(biz);
        if (bizRule == null || bizRule.isEmpty()) return;

        JSONArray allow = bizRule.getJSONArray(KEY_ALLOW_SUFFIX);
        if (allow == null || allow.isEmpty()) return;

        if (!StringUtils.hasText(suffix)) {
            throw new RuntimeException("文件类型不允许");
        }

        boolean ok = false;
        for (int i = 0; i < allow.size(); i++) {
            String t = String.valueOf(allow.get(i)).trim().toLowerCase(Locale.ROOT);
            if (suffix.equals(t)) {
                ok = true;
                break;
            }
        }
        if (!ok) throw new RuntimeException("文件类型不允许");
    }

    /**
     * 生成上传对象目录前缀 dir
     * **/
    private String generateObjectDir(JSONObject cfg, String biz, Long userId) {
        String dirPrefix = cfg.getString(KEY_DIR_PREFIX);
        if (!StringUtils.hasText(dirPrefix)) dirPrefix = DEFAULT_DIR_PREFIX;

        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new Date());
        return dirPrefix + "/" + biz + "/" + date + "/" + userId + "/";
    }

    /**
     * 生成随机文件名 并拼接后缀。避免重名覆盖与可预测路径。
     * **/
    private String generateUniqueFileName(String suffix) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (!StringUtils.hasText(suffix)) return uuid;
        return uuid + "." + suffix;
    }

    /**
     * 计算最终允许上传的最大文件大小
     * **/
    private long resolveMaxSize(JSONObject cfg, String biz, Long reqMaxSize) {
        long base = cfg.getLongValue(KEY_MAX_SIZE);
        if (base <= 0) base = DEFAULT_MAX_SIZE;

        JSONObject rules = cfg.getJSONObject(KEY_BIZ_RULES);
        if (rules != null) {
            JSONObject bizRule = rules.getJSONObject(biz);
            if (bizRule != null && bizRule.containsKey(KEY_MAX_SIZE)) {
                long v = bizRule.getLongValue(KEY_MAX_SIZE);
                if (v > 0) base = v;
            }
        }
        if (reqMaxSize == null || reqMaxSize <= 0) return base;
        return Math.min(base, reqMaxSize);
    }

    /**
     * 计算最终直传签名有效期
     * **/
    private long resolveExpireSeconds(JSONObject cfg, String biz, Long reqExpire) {
        long base = cfg.getLongValue(KEY_EXPIRE_SECONDS);
        if (base <= 0) base = DEFAULT_EXPIRE_SECONDS;

        JSONObject rules = cfg.getJSONObject(KEY_BIZ_RULES);
        if (rules != null) {
            JSONObject bizRule = rules.getJSONObject(biz);
            if (bizRule != null && bizRule.containsKey(KEY_EXPIRE_SECONDS)) {
                long v = bizRule.getLongValue(KEY_EXPIRE_SECONDS);
                if (v > 0) base = v;
            }
        }
        if (reqExpire == null || reqExpire <= 0) return base;
        return Math.min(base, reqExpire);
    }

    /**
     * 构建并 Base64 编码 OSS Post Policy
     * 限制上传 bucket
     * 限制 key 必须以 dir 前缀开头
     * 限制 content-length-range
     * 指定 success_action_status
     * **/
    private String createPolicyBase64(String expiration, String bucket, String dir, long maxSize, String successActionStatus) {
        JSONObject policy = new JSONObject();
        policy.put("expiration", expiration);

        JSONArray conditions = new JSONArray();

        JSONArray contentLen = new JSONArray();
        contentLen.add("content-length-range");
        contentLen.add(0);
        contentLen.add(maxSize);
        conditions.add(contentLen);

        JSONObject bucketObj = new JSONObject();
        bucketObj.put("bucket", bucket);
        conditions.add(bucketObj);

        JSONArray startsWith = new JSONArray();
        startsWith.add("starts-with");
        startsWith.add("$key");
        startsWith.add(dir);
        conditions.add(startsWith);

        JSONObject successObj = new JSONObject();
        successObj.put("success_action_status", successActionStatus);
        conditions.add(successObj);

        policy.put("conditions", conditions);
        String json = policy.toJSONString();
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 对 Base64 policy 进行 HmacSHA1 签名并返回 Base64 结果，生成 OSS 直传所需 Signature 字段。
     * **/
    private String computeSignature(String accessKeySecret, String policyBase64) {
        if (!StringUtils.hasText(accessKeySecret)) {
            throw new RuntimeException("OSS accessKeySecret 未配置");
        }
        return hmacSha1Base64(accessKeySecret, policyBase64);
    }

    /**
     * 通用 HmacSHA1(Base64) 工具方法。用于 policy 签名或 GET 签名计算，内部统一异常转换为运行时错误。
     * **/
    private String hmacSha1Base64(String secret, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA1_ALGORITHM));
            byte[] signData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signData);
        } catch (Exception e) {
            throw new RuntimeException("签名生成失败");
        }
    }

    /**
     * 构建标准 OSS host：
     * **/
    private String buildHostInternal(String endpoint, String bucket) {
        Assert.hasText(endpoint, "OSS endpoint 未配置");
        Assert.hasText(bucket, "OSS bucket 未配置");

        endpoint = endpoint.trim();
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            if (endpoint.contains(bucket + ".")) {
                return endpoint;
            }
            return endpoint.replace("://", "://" + bucket + ".");
        }
        return "https://" + bucket + "." + endpoint;
    }

    /**
     * 计算资源访问前缀 urlPrefix：优先使用 publicDomain（便于切 CDN/域名），否则使用标准 host。
     * **/
    private String buildUrlPrefix(JSONObject cfg, String standardHost) {
        String publicDomain = cfg.getString(KEY_PUBLIC_DOMAIN);
        if (StringUtils.hasText(publicDomain)) {
            return publicDomain.trim();
        }
        return standardHost;
    }

    /**
     * 加载并强校验 OSS 配置
     * **/
    private JSONObject getAndCheckCfg() {
        long now = System.currentTimeMillis();
        if (cachedCfg != null && (now - cachedAtMs) <= CFG_CACHE_MS) {
            return cachedCfg;
        }

        String cfgStr = managerConfigService.getString(Config.ALIYUN_OSS_CONFIG);
        Assert.hasText(cfgStr, "系统未配置 OSS 参数: t_config." + Config.ALIYUN_OSS_CONFIG);

        JSONObject cfg;
        try {
            cfg = JSONObject.parseObject(cfgStr);
        } catch (Exception e) {
            throw new RuntimeException("OSS 配置 JSON 格式解析错误");
        }

        Assert.hasText(cfg.getString(KEY_ENDPOINT), "OSS 配置缺失 endpoint");
        Assert.hasText(cfg.getString(KEY_BUCKET), "OSS 配置缺失 bucket");
        Assert.hasText(cfg.getString(KEY_ACCESS_KEY_ID), "OSS 配置缺失 accessKeyId");
        Assert.hasText(cfg.getString(KEY_ACCESS_KEY_SECRET), "OSS 配置缺失 accessKeySecret");

        cachedCfg = cfg;
        cachedAtMs = now;
        return cfg;
    }
}

