package com.zywl.app.manager.service.kafka;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.defaultx.annotation.KafkaProducer;
import com.zywl.app.manager.context.KafkaEventContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class KafkaProducerAspect {

    private static final Log logger = LogFactory.getLog(KafkaProducerAspect.class);

    @Around("@annotation(kafkaProducer)")
    public Object handleKafkaProducer(ProceedingJoinPoint joinPoint, KafkaProducer kafkaProducer) throws Throwable {
        // 执行目标方法
        Object result = joinPoint.proceed();

        // 获取注解中的信息
        String topic = kafkaProducer.topic();
        String event = kafkaProducer.event();
        boolean sendParams = kafkaProducer.sendParams();
        // 构造消息内容
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Business: ").append(event).append("; ");
        // 如果需要发送参数
        JSONObject args = new JSONObject();
        if (sendParams) {
            if (event.equals(KafkaEventContext.ADD_REWARD)){
                args.put("userId",joinPoint.getArgs()[0]);
                args.put("reward", joinPoint.getArgs()[1]);
            }else{
                args = (JSONObject)joinPoint.getArgs()[1];
            }
        }
        JSONObject sendMsg = new JSONObject();
        sendMsg.put("eventType",event);
        sendMsg.put("data",args);
        // 如果需要发送返回值
        if (result != null) {
            messageBuilder.append("Result: ").append(result);
        }
        // 发送消息到 Kafka
        KafkaProducerUtil.sendMessage(topic, sendMsg);
        return result;
    }
}
