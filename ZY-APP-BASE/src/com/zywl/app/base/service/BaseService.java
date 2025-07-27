package com.zywl.app.base.service;

import com.zywl.app.base.Base;
import com.zywl.app.base.bean.hongbao.RedEnvelope;
import com.zywl.app.base.util.Async;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseService extends Base {

	protected final Log logger = LogFactory.getLog(getClass());

	public static Async async(){ 
		return new Async(10 * 1000);
	}
	public static Map<String, Object> objectToMap(Object obj) {
		Map<String, Object> map = new HashMap<>();
		if (obj == null) {
			return map;
		}

		Class<?> clazz = obj.getClass();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			field.setAccessible(true);  // 设置为可访问
			try {
				map.put(field.getName(), field.get(obj));
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		return map;
	}

	@Override
	protected Log logger() {
		return logger;
	}


}
