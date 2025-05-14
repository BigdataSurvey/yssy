package com.zywl.app.defaultx.bean;

import java.lang.reflect.Method;

import com.zywl.app.base.BaseBean;

/**
 * 
 * @author DOE
 *
 */
public class ServiceBean extends BaseBean {
	
	private Object bean;
	
	private String serviceCode;
	
	private String methodCode;
	
//	private String methodName;
	
	private String description;
	
	private Method method;

//	private Class<?>[] paramTypes;

	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	/*public Class<?>[] getParamTypes() {
		return paramTypes;
	}
	
	public void setParamTypes(Class<?>[] paramTypes) {
		this.paramTypes = paramTypes;
	}
	
	public String getMethodName() {
		return methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}*/
	
	public String getServiceCode() {
		return serviceCode;
	}
	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}
	
	public String getMethodCode() {
		return methodCode;
	}
	
	public void setMethodCode(String methodCode) {
		this.methodCode = methodCode;
	}

	public Object getBean() {
		return bean;
	}

	public void setBean(Object bean) {
		this.bean = bean;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
