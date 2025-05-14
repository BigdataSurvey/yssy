package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Role extends BaseBean {

	public final static Map<String, String> ROLES = new ConcurrentHashMap<String, String>();
	
	//管理员
	public final static String ADMIN = "ad02969f3c2e400db79105843cbed44e";
	
	//客服
	public final static String KEFU = "fafa9fa225644438937feb755d5eae2f";
	
	static{
		ROLES.put(ADMIN, "超级管理员");
		ROLES.put(KEFU, "客服");
	}
	
}
