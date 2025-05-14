package com.zywl.app.defaultx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 提供服务的方法注解
 * @author Doe
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceMethod {
	
	public String code();
	
	public String description() default "";
	
}
