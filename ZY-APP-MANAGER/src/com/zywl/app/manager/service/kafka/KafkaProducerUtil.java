package com.zywl.app.manager.service.kafka;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.util.OrderUtil;
import com.zywl.app.base.util.PropertiesUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaProducerUtil {
    private static final Log logger = LogFactory.getLog(KafkaProducerUtil.class);
    private static Producer<String, String> producer;
    private static ExecutorService kafkaThreadPool; // 使用线程池执行异步任务

    static {
        logger.info("================Kafka生产者初始化配置================");
        // 配置 Kafka 生产者
        Properties props = new Properties();
        PropertiesUtil propertiesUtil = new PropertiesUtil("kafka.properties");
        props.put("bootstrap.servers", propertiesUtil.get("bootstrap.servers")); // Kafka 地址
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        logger.info("================Kafka生产者初始化配置1================");
        producer = new KafkaProducer<>(props);
        logger.info("================Kafka生产者初始化配置2================");
        kafkaThreadPool = new ThreadPoolExecutor(25, 75, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100), new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "KafkaSenderThread-" + threadNumber.getAndIncrement());
            }
        },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
        );
        logger.info("================Kafka生产者配置完成================");
    }

    public static void sendMessage(String topic, JSONObject message) {
        kafkaThreadPool.submit(() -> {
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(topic, OrderUtil.getOrder3Number(), message.toJSONString());
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        logger.info("kafka发送:  " + message.getString("eventType") + ",  offset: " + metadata.offset());
                    } else {
                        exception.printStackTrace();
                    }
                });
            } catch (Exception e) {
                // 处理异常
                logger.error("Failed to send Kafka message: " + e.getMessage());
            }
        });
    }

    public static void close() {
        producer.close();
    }
}
