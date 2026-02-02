package com.zywl.app.manager.service.manager;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.User;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.UserCacheService;
import com.zywl.app.manager.context.MessageCodeContext;
import com.zywl.app.manager.service.oss.AliOssDirectUploadService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Map;

/**
 * @Author: lzx
 * @Create: 2026/1/3
 * @Version: V1.0
 * @Description: OSS 直传/资源服务
 * @Task: 040 (MessageCodeContext.OSS)
 */
@Service
@ServiceClass(code = MessageCodeContext.OSS)
public class ManagerOssService extends BaseService {

    @Autowired
    private AliOssDirectUploadService aliOssDirectUploadService;
    @Autowired
    private UserCacheService userCacheService;

    /**
     * 获取 OSS 直传签名
     * 040001
     */
    @ServiceMethod(code = "001", description = "OSS-直传签名")
    public JSONObject getOssDirectUploadPolicy(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        String biz = params.getString("biz");
        if (!StringUtils.hasText(biz)) {
            biz = "bounty";
        }
        String suffix = params.getString("suffix");

        Long maxSize = params.getLong("maxSize");
        Long expireSeconds = params.getLong("expireSeconds");

        return aliOssDirectUploadService.buildPostPolicy(biz, userId, suffix, maxSize, expireSeconds);
    }

    /**
     * 规范化并校验 OSS 资源 URL
     * 040002
     */
    @ServiceMethod(code = "002", description = "OSS-规范化资源URL")
    public JSONObject canonicalizeOssUrl(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        String url = params.getString("url");
        String fieldName = params.getString("fieldName");
        Boolean allowEmpty = params.getBoolean("allowEmpty");
        if (!StringUtils.hasText(fieldName)) {
            fieldName = "url";
        }
        boolean allow = allowEmpty != null && allowEmpty;

        String canonical = aliOssDirectUploadService.canonicalizeAndCheckOssUrl(url, fieldName, allow);

        JSONObject resp = new JSONObject();
        resp.put("url", canonical);
        resp.put("objectKey", extractObjectKeyPublic(canonical));
        return resp;
    }

    /**
     * 生成“可展示 URL”
     * 040003
     */
    @ServiceMethod(code = "003", description = "OSS-获取可展示URL")
    public JSONObject getReadableUrl(ManagerSocketServer socket, JSONObject params) {
        checkNull(params);
        checkNull(params.get("userId"));
        Long userId = params.getLong("userId");
        loadAndCheckUser(userId);

        Long readExpireSeconds = params.getLong("readExpireSeconds");

        // 批量 keys
        JSONArray keys = params.getJSONArray("keys");
        if (keys != null && !keys.isEmpty()) {
            JSONArray urls = new JSONArray();
            for (int i = 0; i < keys.size(); i++) {
                String input = keys.getString(i);
                if (!StringUtils.hasText(input)) {
                    urls.add(null);
                    continue;
                }
                String readable = aliOssDirectUploadService.toReadableUrl(input, readExpireSeconds);
                urls.add(readable);
            }
            JSONObject resp = new JSONObject();
            resp.put("urls", urls);
            return resp;
        }

        // 单个 url/key
        String url = params.getString("url");
        String key = params.getString("key");

        String input = StringUtils.hasText(url) ? url : key;
        if (!StringUtils.hasText(input)) {
            throwExp("url或key不能为空");
        }

        String readableUrl = aliOssDirectUploadService.toReadableUrl(input, readExpireSeconds);
        String canonical = aliOssDirectUploadService.canonicalizeAndCheckOssUrl(input, "url", false);

        JSONObject resp = new JSONObject();
        resp.put("url", readableUrl);
        resp.put("canonicalUrl", canonical);
        resp.put("objectKey", extractObjectKeyPublic(canonical));
        return resp;
    }

    /**
     * 从完整 URL 或 objectKey 中提取 objectKey 不依赖 AliOssDirectUploadService 的 private 方法
     */
    private String extractObjectKeyPublic(String fullUrlOrKey) {
        if (!StringUtils.hasText(fullUrlOrKey)) return null;

        String v = fullUrlOrKey.trim();
        // 如果不是 URL，认为就是 objectKey
        if (!(v.startsWith("http://") || v.startsWith("https://"))) {
            while (v.startsWith("/")) v = v.substring(1);
            return v;
        }

        try {
            URI uri = URI.create(v);
            String path = uri.getPath();
            if (!StringUtils.hasText(path)) return null;
            while (path.startsWith("/")) path = path.substring(1);
            return path;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加载用户信息并校验是否存在
     */
    private User loadAndCheckUser(Long userId) {
        Map<Long, User> users = userCacheService.loadUsers(userId);
        User user = (users != null) ? users.get(userId) : null;
        if (user == null) {
            throwExp("用户不存在");
        }
        return user;
    }
}
