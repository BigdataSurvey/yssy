package com.zywl.app.defaultx.util;

import java.lang.reflect.Field;

import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.aop.framework.AopProxy;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Spring 工具类
 * @author june
 *
 */
public class SpringUtil {
	
	/** 应用上下文 */
	private static ClassPathXmlApplicationContext applicationContext = null;
	
	public static ClassPathXmlApplicationContext start(String config){
		return applicationContext = new ClassPathXmlApplicationContext(config);
	}
	
	public static ClassPathXmlApplicationContext getApplicationContext(){
		return applicationContext;
	}
	/**
	 * 服务名
	 * @param applicationContext
	 * @param cls
	 * @return
	 */
	public static <T> T getService(ClassPathXmlApplicationContext applicationContext, Class<T> cls){
		return applicationContext.getBean(cls);
	}
	
	/**
	 *  服务名
	 * @param sc
	 * @param cls
	 * @return
	 */
	public static <T> T getService(Class<T> cls){
		
		return applicationContext.getBean(cls);
	}
	
	public static Object getService(String bean){
		if(bean != null)
			return applicationContext.getBean(bean);
		return null;
	}
	
	/** 
	* 获取 目标对象 
	* @param proxy 代理对象 
	* @return 
	* @throws Exception 
	*/ 
	public static Object getTarget(Object proxy) throws Exception { 
		if(!AopUtils.isAopProxy(proxy)) { 
			return proxy;//不是代理对象 
		}
		if(AopUtils.isJdkDynamicProxy(proxy))
			return getJdkDynamicProxyTargetObject(proxy);
		else
			return getCglibProxyTargetObject(proxy);
	} 


	private static Object getCglibProxyTargetObject(Object proxy) throws Exception { 
		Field h = proxy.getClass().getDeclaredField("CGLIB$CALLBACK_0"); 
		h.setAccessible(true); 
		Object dynamicAdvisedInterceptor = h.get(proxy); 
		Field advised = dynamicAdvisedInterceptor.getClass().getDeclaredField("advised"); 
		advised.setAccessible(true); 
		Object target = ((AdvisedSupport)advised.get(dynamicAdvisedInterceptor)).getTargetSource().getTarget(); 
		return target;
	} 


	private static Object getJdkDynamicProxyTargetObject(Object proxy) throws Exception { 
		Field h = proxy.getClass().getSuperclass().getDeclaredField("h"); 
		h.setAccessible(true); 
		AopProxy aopProxy = (AopProxy) h.get(proxy); 
		Field advised = aopProxy.getClass().getDeclaredField("advised"); 
		advised.setAccessible(true); 
		Object target = ((AdvisedSupport)advised.get(aopProxy)).getTargetSource().getTarget(); 
		return target; 
	}

}
