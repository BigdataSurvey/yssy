package com.zywl.app.manager.service;

import com.alibaba.fastjson2.JSONObject;
import com.live.app.ws.enums.PushCode;
import com.live.app.ws.util.DefaultPushHandler;
import com.live.app.ws.util.Push;
import com.zywl.app.base.bean.Version;
import com.zywl.app.base.service.BaseService;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.cache.AppConfigCacheService;
import com.zywl.app.defaultx.service.VersionService;
import com.zywl.app.manager.socket.AdminSocketServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@ServiceClass(code = "005")
public class ReleaseAppService extends BaseService {

    private static final Log logger = LogFactory.getLog(ReleaseAppService.class);

    private PropertiesUtil versionProperties;

    @Autowired
    private VersionService versionService;

    @Autowired
    PlayGameService gameService;
    @Autowired
    private AppConfigCacheService appConfigCacheService;

    @PostConstruct
    public void _construct() {
        versionProperties = new PropertiesUtil("static.properties");
        Push.addPushSuport(PushCode.updateVersion, new DefaultPushHandler());
    }

    @ServiceMethod(code = "001", description = "加载版本列表")
    public List<Version> getVersionsByType(AdminSocketServer adminSocketServer, JSONObject params) {
        checkNull(params);
        checkNull(params.get("type"));
        int type = params.getIntValue("type");
        return getVersionsByType(type);
    }

    public List<Version> getVersionsByType(int type) {
        return versionService.getVersionByType(type);
    }

    @ServiceMethod(code = "002", description = "删除版本")
    public void deleteVersion(JSONObject param) {
        checkNull(param);
        checkNull(param.get("id"));
        String versionId = param.getString("id");
        Version version = versionService.findVersionById(versionId);
        if (version != null) {
            versionService.deleteVersion(versionId);
            deleteAppFile(getFileName(versionId, version.getType()));
            pushAppUpdate(versionId);
        } else {
            throwExp("该版本不存在");
        }
    }

    @ServiceMethod(code = "003", description = "启用禁用版本")
    public String enableVersion(JSONObject param) {
        checkNull(param);
        checkNull(param.get("id"), param.get("release"));

        String versionId = param.getString("id");
        int release = param.getIntValue("release");

        Version version = versionService.findVersionById(versionId);
        if (version != null) {
            version.setRelease(release);
            switch (release) {
                case Version.RELEASE_ENABLE:
                    version.setUpdateTime(DateUtil.getCurrent0());
                    break;
                case Version.RELEASE_DISABLE:
                    version.setUpdateTime("");
                    break;
                default:
                    throwExp("请选择是否发布");
            }
            versionService.updateVersion(version);
        } else {
            throwExp("该版本不存在");
        }
        pushAppUpdate(versionId);
        return version.getUpdateTime();
    }

    @ServiceMethod(code = "004", description = "是否强制更新")
    public void disableVersion(JSONObject param) {
        checkNull(param);
        checkNull(param.get("id"), param.get("fc"));

        String versionId = param.getString("id");
        int fc = param.getIntValue("fc");

        Version version = versionService.findVersionById(versionId);
        if (version != null) {
            switch (fc) {
                case Version.FC_UPDATE:
                    version.setFc(fc);
                    break;
                case Version.NORMAL_UPDATE:
                    version.setFc(fc);
                    break;
                default:
                    throwExp("请选择是否强制更新到此版本");
            }
            versionService.updateVersion(version);
        } else {
            throwExp("该版本不存在");
        }
        pushAppUpdate(versionId);
    }

    public Version addVersion(String id, byte[] fileByte, String description, String versionName, Integer versionNo,
                              int fc, int release, int type) {
        checkNull(fileByte, description, versionName, versionNo);
        versionService.checkVersionName(null, versionName, type);
        versionService.checkVersionNo(null, versionNo, type);

        if (versionService.findVersionById(id) != null) {
            throwExp("版本ID不能重复");
        }
        String fileName = getFileName(id, type);
        String webPath = versionProperties.get("version.apk.webPath");

        Version version = new Version();
        version.setId(id);
        version.setDescription(description);
        version.setVersionName(versionName);
        version.setVersionNo(versionNo);
        version.setFc(fc);
        version.setRelease(release);
        version.setUpdateUrl(webPath + fileName);
        version.setType(type);

        versionService.addVersion(version);
        saveAppFile(fileName, fileByte);
        pushAppUpdate(id);
        return version;
    }

    public void updateVersion(String id, byte[] fileByte, String description, String versionName, Integer versionNo,
                              Integer fc, Integer release, int type) {
        checkNull(id);
        versionService.checkVersionName(id, versionName, type);
        versionService.checkVersionNo(id, versionNo, type);

        Version version = versionService.findVersionById(id);
        if (version == null) {
            throwExp("要修改的版本不存在");
        }
        String fileName = getFileName(id, type);
        String webPath = versionProperties.get("version.apk.webPath");

        version.setDescription(description);
        version.setVersionName(versionName);
        version.setVersionNo(versionNo);
        version.setFc(fc);
        version.setRelease(release);
        version.setUpdateUrl(webPath + fileName);
        version.setType(type);
        versionService.updateVersion(version);
        if (fileByte != null) {
            saveAppFile(getFileName(id, type), fileByte);
        }
        pushAppUpdate(id);
    }

    /**
     * 通知ServerAPP有更新，至于Server要不要推送至客户端，则留给Server判断
     *
     * @param id
     * @author DOE
     */
    private void pushAppUpdate(String id) {
        Push.push(PushCode.updateVersion, null, id);
    }


    private String getFileName(String id, int type) {
        return id + "." + (type == Version.TYPE_ANDROID ? Version.APP_ANDROID : Version.APP_IOS);
    }

    /**
     * 保存APP
     *
     * @param fileByte
     * @return
     * @author DOE
     */
    private void saveAppFile(String fileName, byte[] fileByte) {
        String filePath = versionProperties.get("version.apk.path");
        try {
            File file = new File(filePath + File.separator + fileName);
            if (file.exists())
                file.delete();
            FileUtils.writeByteArrayToFile(file, fileByte);
        } catch (IOException e) {
            logger.error("APK文件写入失败", e);
            throwExp("APK文件保存失败，请重新提交");
        }
    }

    public File getAppFile(String id, int type) {
        String filePath = versionProperties.get("version.apk.path");
        File file = new File(filePath + File.separator + getFileName(id, type));
        if (file.exists()) {
            return file;
        }
        return null;
    }

    private void deleteAppFile(String fileName) {
        String filePath = versionProperties.get("version.apk.path");
        File file = new File(filePath + File.separator + fileName);
        if (file.exists())
            file.delete();
    }


    @Override
    protected Log logger() {
        return logger;
    }
}
