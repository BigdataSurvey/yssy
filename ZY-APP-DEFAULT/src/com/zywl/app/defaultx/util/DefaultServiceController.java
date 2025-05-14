package com.zywl.app.defaultx.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.alibaba.fastjson2.JSON;
import com.live.app.ws.bean.Command;
import com.live.app.ws.interfacex.ServiceController;
import com.live.app.ws.socket.BaseServerSocket;
import com.zywl.app.base.Base;
import com.zywl.app.base.exp.AppException;
import com.zywl.app.defaultx.annotation.ServiceClass;
import com.zywl.app.defaultx.annotation.ServiceMethod;
import com.zywl.app.defaultx.bean.ServiceBean;

/**
 * 
 * @author DOE
 *
 */
public class DefaultServiceController extends Base implements ServiceController {
	
	private static final Log logger = LogFactory.getLog(DefaultServiceController.class);
	
	public static Map<String, ServiceBean> serviceMap = new HashMap<String, ServiceBean>();
	
	private static ServiceController controller;
	
	private DefaultServiceController(){}
	
	public static ServiceController getController(ApplicationContext context){
		if(controller == null){
			controller = new DefaultServiceController();

			try {
				Map<String, Object> classs = context.getBeansWithAnnotation(ServiceClass.class);
				for (Entry<String, Object> bean : classs.entrySet()) {
					String className = bean.getValue().toString().split("@")[0];
					Class<?> clazz = Class.forName(className);
					String classCode = clazz.getAnnotation(ServiceClass.class).code();
					Method[] methods = clazz.getMethods();

					for (Method method : methods) {
						if(method.isAnnotationPresent(ServiceMethod.class)){
							ServiceBean service = new ServiceBean();
							String methodCode = method.getAnnotation(ServiceMethod.class).code();
							String description = method.getAnnotation(ServiceMethod.class).description();
							
							service.setBean(context.getBean(bean.getKey()));
							service.setServiceCode(classCode);
							service.setMethodCode(methodCode);
							service.setMethod(method);
							service.setDescription(description);
							
							String code = classCode + methodCode;
							if(serviceMap.containsKey(code)){
								logger.error("Code重复：" + code + " " + clazz + " <-> " +serviceMap.get(code).getBean().getClass());
								System.exit(0);
							}
							logger.debug("Loading-" + description + "-" + code + "-" + clazz.toString());
							serviceMap.put(code, service);
						}
					}
				}
				logger.info("Load Serivce Success..");
			} catch (Exception e) {
				logger.error(e, e);
			}
		}
		return controller;
	}
	
	public static Map<String, ServiceBean> getServiceMap() {
		return serviceMap;
	}

	/**
	 * 获取ServiceBean的信息
	 * @author Doe.
	 * @param code 
	 * @return
	 */
	public static ServiceBean getService(String code) {
		if(isNull(code))
			return null;
		return serviceMap.get(code);
	}

	@Override
	public Object exec(BaseServerSocket serverSocket, Command command) throws Exception, AppException {
		ServiceBean service = getService(command.getCode());
		if(service == null){
			throwExp("code["+command.getCode()+"]不存在");
		}
		Object bean = service.getBean();
		Method method = service.getMethod();
		Type[] genericParameterTypes = method.getGenericParameterTypes();
		Object [] param = null;
		if(genericParameterTypes != null && genericParameterTypes.length > 0){
			param = new Object[genericParameterTypes.length];
			for (int i = 0 ;i < genericParameterTypes.length ; i++) {
				Type type = genericParameterTypes[i];
				if(type instanceof Class<?>){
					if(Command.class == (Class<?>)type){
						param[i] = command;
						continue;
					}else if(BaseServerSocket.class.isAssignableFrom((Class<?>)type)){
						param[i] = serverSocket;
						continue;
					}
				}
				param[i] = JSON.parseObject(JSON.toJSONString(command.getData()), type);
			}
		}
		try {
			return method.invoke(bean, param);
		} catch (InvocationTargetException e) {
			if(e.getTargetException() instanceof AppException){
				throw (AppException)e.getTargetException();
			}else{
				throw new Exception(e);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	protected Log logger() {
		return logger;
	}
}
