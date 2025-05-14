package com.zywl.app.defaultx.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)  // 注解作用于方法
@Retention(RetentionPolicy.RUNTIME)  // 注解在运行时生效
public @interface KafkaProducer {
    String topic();  // 指定 Kafka 的主题
    String event();  // 业务名称，用于区分不同业务逻辑
    boolean sendParams() default false;  // 是否发送方法参数作为消息
}
