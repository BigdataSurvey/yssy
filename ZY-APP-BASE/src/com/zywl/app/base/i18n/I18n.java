package com.zywl.app.base.i18n;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zywl.app.base.bean.SystemLocale;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class I18n {
	
	public static final String DEFAULT_LOCALE = SystemLocale.DEFAULT_LOCALE;
	
	public static final String BASE_NAME = I18n.class.getPackage().getName()+".fxbtg";
	
	public static final Set<String> LOCALS = Sets.newConcurrentHashSet(Lists.newArrayList("zh_CN", "en_US"));
	
	private static final Map<String, ResourceBundle> LOCAL_CACHE = new ConcurrentHashMap<String, ResourceBundle>();

	public static Locale displayName2Locale(String displayName){
		if(displayName == null){
			return null;
		}
		String[] split = displayName.split("_");
		if(split.length == 2){
			return new Locale(split[0], split[1]);
		}else{
			return new Locale(split[0]);
		}
	}

	public static synchronized String getString(String localeCode, String key, String defaultVal){
		String val = getString(displayName2Locale(localeCode), key);
		return val == null ? defaultVal : val;
	}
	
	public static synchronized String getString(String localeCode, String key){
		if(localeCode == null){
			localeCode = DEFAULT_LOCALE;
//			localeCode = "en_US";
		}
		return getString(displayName2Locale(localeCode), key);
	}
	
	public static synchronized String getString(Locale locale, String key){
		ResourceBundle resourceBundle = LOCAL_CACHE.get(locale.getDisplayName());
		if(resourceBundle == null){
			LOCAL_CACHE.put(locale.getDisplayName(), resourceBundle = ResourceBundle.getBundle(BASE_NAME, locale));
		}
		if(resourceBundle.containsKey(key)){
			return resourceBundle.getString(key);
		}
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(getString("zh_CN", "ChanPinBuCunZai"));
	}
}
