package com.zywl.app.manager.service.oss;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Config;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.manager.service.manager.ManagerConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * @Author: lzx
 * @Create: 2026/1/8
 * @Version: V2.2 (Final Full)
 * @Description: 阿里云 OSS 直传签名服务
 */
@Slf4j
@Service
public class AliOssDirectUploadService {

    @Autowired
    private ManagerConfigService managerConfigService;

    // 常量定义
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final String DEFAULT_DIR_PREFIX = "yssy";
    private static final long DEFAULT_EXPIRE_SECONDS = 300L;
    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024L;

    private static final String KEY_ENDPOINT = "endpoint";
    private static final String KEY_BUCKET = "bucket";
    private static final String KEY_ACCESS_KEY_ID = "accessKeyId";
    private static final String KEY_ACCESS_KEY_SECRET = "accessKeySecret";
    private static final String KEY_EXPIRE_SECONDS = "expireSeconds";
    private static final String KEY_MAX_SIZE = "maxSize";
    private static final String KEY_DIR_PREFIX = "dirPrefix";
    private static final String KEY_PUBLIC_DOMAIN = "publicDomain";

    // 线程安全的日期格式化器 (ISO 8601)
    private static final DateTimeFormatter ISO_8601_UTC =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneOffset.UTC);

    /**
     * 构建 OSS 直传 Policy 和签名
     */
    public JSONObject buildPostPolicy(String bizDir, Long userId, String fileSuffix) {
        JSONObject cfg = getAndCheckCfg();

        String dir = generateObjectDir(cfg, bizDir, userId);
        String fileName = generateUniqueFileName(fileSuffix);
        String objectKey = dir + fileName;
        long expireEpoch = System.currentTimeMillis() / 1000 +
                cfg.getLongValue(KEY_EXPIRE_SECONDS, DEFAULT_EXPIRE_SECONDS);

        String expiration = ISO_8601_UTC.format(Instant.ofEpochSecond(expireEpoch));
        long maxSize = cfg.getLongValue(KEY_MAX_SIZE, DEFAULT_MAX_SIZE);
        String bucket = cfg.getString(KEY_BUCKET);

        String policyBase64 = createPolicyBase64(expiration, bucket, dir, maxSize);
        String signature = computeSignature(cfg.getString(KEY_ACCESS_KEY_SECRET), policyBase64);

        String host = buildHostInternal(cfg.getString(KEY_ENDPOINT), bucket);
        String urlPrefix = buildUrlPrefix(cfg, host);

        JSONObject result = new JSONObject();
        result.put("accessId", cfg.getString(KEY_ACCESS_KEY_ID));
        result.put("policy", policyBase64);
        result.put("signature", signature);
        result.put("dir", dir);
        result.put("fileName", fileName);
        result.put("objectKey", objectKey);
        result.put("host", host);
        result.put("urlPrefix", urlPrefix);
        result.put("expire", expireEpoch);
        result.put("maxSize", maxSize);
        return result;
    }

    /**
     * 获取 OSS 配置 (供外部调用校验)
     * ManagerBountyService 依赖此方法
     */
    public JSONObject getOssConfig() {
        try {
            return getAndCheckCfg();
        } catch (Exception e) {
            log.warn("OSS Config Check Failed: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取标准 Host (供外部调用校验)
     * ManagerBountyService 依赖此方法
     */
    public String buildHost() {
        JSONObject cfg = getAndCheckCfg();
        return buildHostInternal(cfg.getString(KEY_ENDPOINT), cfg.getString(KEY_BUCKET));
    }

    /**
     * 获取 URL 前缀
     * ManagerBountyService 依赖此方法
     */
    public String buildUrlPrefix() {
        JSONObject cfg = getAndCheckCfg();
        String host = buildHostInternal(cfg.getString(KEY_ENDPOINT), cfg.getString(KEY_BUCKET));
        return buildUrlPrefix(cfg, host);
    }

    // ================= Private Helper Methods =================

    private JSONObject getAndCheckCfg() {
        String cfgStr = managerConfigService.getString(Config.ALIYUN_OSS_CONFIG);
        Assert.hasText(cfgStr, "系统未配置 OSS 参数: t_config." + Config.ALIYUN_OSS_CONFIG);

        JSONObject cfg;
        try {
            cfg = JSONObject.parseObject(cfgStr);
        } catch (Exception e) {
            throw new RuntimeException("OSS 配置 JSON 格式解析错误");
        }

        if (!StringUtils.hasText(cfg.getString(KEY_ENDPOINT)) ||
                !StringUtils.hasText(cfg.getString(KEY_BUCKET)) ||
                !StringUtils.hasText(cfg.getString(KEY_ACCESS_KEY_ID)) ||
                !StringUtils.hasText(cfg.getString(KEY_ACCESS_KEY_SECRET))) {
            throw new RuntimeException("OSS 配置不完整: endpoint/bucket/ak/sk 缺失");
        }
        return cfg;
    }

    private String generateObjectDir(JSONObject cfg, String bizDir, Long userId) {
        String prefix = cfg.getString(KEY_DIR_PREFIX);
        if (!StringUtils.hasText(prefix)) {
            prefix = DEFAULT_DIR_PREFIX;
        }
        String dateStr = DateUtil.format9(new Date());
        String safeBiz = StringUtils.hasText(bizDir) ? bizDir.trim() : "misc";
        String uidStr = (userId == null) ? "0" : String.valueOf(userId);

        return String.format("%s/%s/%s/%s/", prefix, safeBiz, dateStr, uidStr);
    }

    private String generateUniqueFileName(String fileSuffix) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String suffix = "";
        if (StringUtils.hasText(fileSuffix)) {
            suffix = fileSuffix.trim();
            if (!suffix.startsWith(".")) {
                suffix = "." + suffix;
            }
        }
        return uuid + suffix;
    }

    private String createPolicyBase64(String expiration, String bucket, String dir, long maxSize) {
        JSONObject policyJson = new JSONObject();
        policyJson.put("expiration", expiration);

        JSONArray conditions = new JSONArray();
        JSONArray contentLenRange = new JSONArray();
        contentLenRange.add("content-length-range");
        contentLenRange.add(0);
        contentLenRange.add(maxSize);
        conditions.add(contentLenRange);

        JSONObject bucketCond = new JSONObject();
        bucketCond.put("bucket", bucket);
        conditions.add(bucketCond);

        JSONArray keyStartWith = new JSONArray();
        keyStartWith.add("starts-with");
        keyStartWith.add("$key");
        keyStartWith.add(dir);
        conditions.add(keyStartWith);

        policyJson.put("conditions", conditions);

        return Base64.getEncoder().encodeToString(
                policyJson.toJSONString().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String computeSignature(String accessKeySecret, String content) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(new SecretKeySpec(accessKeySecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA1_ALGORITHM));
            byte[] signData = mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signData);
        } catch (Exception e) {
            throw new RuntimeException("OSS 签名计算失败", e);
        }
    }

    private static String buildHostInternal(String endpoint, String bucket) {
        if (!StringUtils.hasText(endpoint) || !StringUtils.hasText(bucket)) {
            return "";
        }
        String ep = endpoint.trim();
        String bk = bucket.trim();

        if (ep.startsWith("http://") || ep.startsWith("https://")) {
            String scheme = ep.split("://")[0];
            String domain = ep.split("://")[1];
            return scheme + "://" + bk + "." + domain;
        }
        return "https://" + bk + "." + ep;
    }

    private static String buildUrlPrefix(JSONObject cfg, String ossHost) {
        String publicDomain = cfg.getString(KEY_PUBLIC_DOMAIN);
        if (StringUtils.hasText(publicDomain)) {
            return publicDomain.trim().replaceAll("/+$", "");
        }
        return ossHost;
    }
}