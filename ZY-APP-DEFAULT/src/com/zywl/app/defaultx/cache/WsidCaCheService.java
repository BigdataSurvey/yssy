package com.zywl.app.defaultx.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.constant.RedisKeyConstant;
import com.zywl.app.base.i18n.I18n;
import com.zywl.app.base.bean.WS;
import com.zywl.app.base.bean.WsidBean;
import com.zywl.app.defaultx.cache.impl.RedisService;
import com.zywl.app.defaultx.service.WSService;


@Service
public class WsidCaCheService extends RedisService{
	
	
	public static Map<String, String> USER_WS_CACHE = new ConcurrentHashMap<String,String>();
	
	@Autowired
	private WSService wsService;

	
	
	public void setWsid(WsidBean wsidBean){
		wsidBean.setTimeout(System.currentTimeMillis() + WsidBean.TIMEOUT);
		if (null!=wsidBean) {
			set(RedisKeyConstant.APP_WSID+wsidBean.getWsid(), wsidBean,86400*7);
			setNumber(RedisKeyConstant.APP_USER_WSID+wsidBean.getUserId()+"-", wsidBean.getWsid(),86400*7);
		}
	}
	
	public WsidBean getWsidByUserId(Long userId) {
		WsidBean wsidBean = get(RedisKeyConstant.APP_USER_WSID+userId+"-",WsidBean.class);
		if (wsidBean==null) {
			wsidBean = wsService.findWsByUserId(userId);
			if (wsidBean!=null) {
				changeWsidLocale(wsidBean, null);
				setWsid(wsidBean);
			}
		}
		return wsidBean;
		
	}
	
	public WsidBean getWsid(String wsid){
		WsidBean wsBean = get(RedisKeyConstant.APP_WSID+wsid,WsidBean.class);
			if (wsBean==null) {
				wsBean = wsService.findWsByWsId(wsid);
				if (wsBean!=null) {
					changeWsidLocale(wsBean, null);
					setWsid(wsBean);
				}
			}
			return wsBean;
	}
	
	public void removeWsid(String wsid){
		wsid = wsid.substring(1,wsid.length()-1);
		String key = RedisKeyConstant.APP_WSID+wsid;

		del(key);
	}
	public void removeUserWs(Long userId){
		String wsid = get(RedisKeyConstant.APP_USER_WSID+userId+"-");
		if (wsid!=null) {
			del(RedisKeyConstant.APP_USER_WSID+userId+"-");
			removeWsid(wsid);
		}
	}
	
	public static void changeWsidLocale(WsidBean wsidBean, String locale){
		JSONObject attr = wsidBean.getAttr();
		if(attr == null){
			wsidBean.setAttr(attr = new JSONObject());
		}
		if(locale == null){
			locale = I18n.DEFAULT_LOCALE;
		}
		attr.put("locale", locale);
	}
	
	public static String getWsidLocale(WsidBean wsidBean){
		if(wsidBean.getAttr() == null){
			changeWsidLocale(wsidBean, null);
		}
		return wsidBean.getAttr().getString("locale");
	}

	
}
