package com.zywl.app.defaultx.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zywl.app.base.bean.Version;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.base.util.DateUtil;
import com.zywl.app.defaultx.dbutil.DaoService;

@Service
public class VersionService extends DaoService {

	private static final Log logger = LogFactory.getLog(VersionService.class);
	
	private static final Map<String, Version> VERSION_CACHE_DATA = new ConcurrentHashMap<String, Version>();
	
	private static final Map<Integer, List<Version>> VERSIONS_CACHE = new ConcurrentHashMap<Integer, List<Version>>();
	
	public VersionService() {
		super("VersionMapper");
	}
	
	@PostConstruct
	public void _construct(){
		try {
			reloadCache();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	}
	
	public void reloadCache(){
		try {
			logger.info("加载版本缓存");
			VERSION_CACHE_DATA.clear();
			VERSIONS_CACHE.clear();
			List<Version> versions = findAll();
			if(versions != null){
				for (Version version : versions) {
					setVersionCache(version);
				}
			}
			logger.info("加载版本缓存结束");
		}catch (Exception e){

		}

	}
	
	public void setVersionCache(Version version) {
		VERSION_CACHE_DATA.put(version.getId(), version);
		List<Version> list = VERSIONS_CACHE.get(version.getType());
		if(list == null) {
			VERSIONS_CACHE.put(version.getType(), list = new CopyOnWriteArrayList<Version>());
		}
		list.add(version);
	}
	
	public List<Version> getVersionByType(int type){
		return VERSIONS_CACHE.get(type);
	}
	
	public List<Version> getReleaseVersions(int type){
		List<Version> versions = getVersionByType(type);
		List<Version> releases = new CopyOnWriteArrayList<Version>();
		if(versions != null) {
			for (Version version : versions) {
				if(version.getRelease() == Version.RELEASE_ENABLE){
					releases.add(version);
				}
			}
		}
		return releases;
	}
	
	public Version findVersionById(String id){
		return VERSION_CACHE_DATA.get(id);
	}
	
	public void checkVersionName(String id, String versionName, int type) throws AppException{
		for (Version version : getReleaseVersions(type)) {
			if(!eq(version.getId(), id) && eq(version.getVersionName(), versionName)){
				throwExp("版本名称不能重复");
			}
		}
	}
	
	public void checkVersionNo(String id, int versionNo, int type) throws AppException{
		if(versionNo < 0){
			throwExp("版本号必须大于0");
		}
		List<Version> versions = getReleaseVersions(type);
		for (Version version : versions) {
			if(!eq(version.getId(), id) && eq(version.getVersionNo(), versionNo)){
				throwExp("版本号不能重复");
			}
		}
	}
	
	@Transactional
	public void addVersion(Version version){
		if(version.getRelease() == Version.RELEASE_ENABLE){
			version.setUpdateTime(DateUtil.getCurrent0());
		}
		insert(version);
		reloadCache();
	}
	
	@Transactional
	public void updateVersion(Version version){
		update(version);
		reloadCache();
	}
	
	@Transactional
	public void deleteVersion(String versionId){
		delete(versionId);
		reloadCache();
	}
	
	@Override
	protected Log logger() {
		return logger;
	}
}
