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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class KafkaProducerUtil {
    private static final Log logger = LogFactory.getLog(KafkaProducerUtil.class);
    private static final boolean ENABLED;
    private static Producer<String, String> producer;
    private static ExecutorService kafkaThreadPool;

    static {
        PropertiesUtil propertiesUtil = new PropertiesUtil("kafka.properties");
        ENABLED = "true".equalsIgnoreCase(String.valueOf(propertiesUtil.get("kafka.enabled")));
        if (ENABLED) {
            Properties props = buildProducerProperties(propertiesUtil.getProperties());
            producer = new KafkaProducer<String, String>(props);
            kafkaThreadPool = new ThreadPoolExecutor(
                    25, 75, 60L, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>(5000),
                    new ThreadFactory() {
                        private final AtomicInteger threadNumber = new AtomicInteger(1);

                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "KafkaSenderThread-" + threadNumber.getAndIncrement());
                        }
                    },
                    (r, executor) -> {
                        if (!executor.isShutdown()) {
                            executor.getQueue().poll();
                            boolean ok = executor.getQueue().offer(r);
                            if (!ok) {
                                logger.warn("Kafka send task dropped: queue is full");
                            }
                        }
                    }
            );
            logger.info("Kafka producer initialized.");
        } else {
            logger.info("Kafka producer disabled. Set kafka.enabled=true to start it.");
        }
    }

    public static boolean isEnabled() {
        return ENABLED;
    }

    public static void sendMessage(String topic, JSONObject message) {
        if (!ENABLED || producer == null || kafkaThreadPool == null) {
            return;
        }
        kafkaThreadPool.submit(() -> {
            try {
                ProducerRecord<String, String> record = new ProducerRecord<String, String>(
                        topic, OrderUtil.getOrder3Number(), message.toJSONString());
                producer.send(record, (metadata, exception) -> {
                    if (exception == null) {
                        logger.info("Kafka sent " + message.getString("eventType") + ", offset: " + metadata.offset());
                    } else {
                        logger.error("Kafka send failed.", exception);
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to submit Kafka message.", e);
            }
        });
    }

    public static void close() {
        if (producer != null) {
            producer.close();
        }
        if (kafkaThreadPool != null) {
            kafkaThreadPool.shutdown();
        }
    }

    private static Properties buildProducerProperties(Properties kafkaProperties) {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaProperties.getProperty("bootstrap.servers"));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        copyIfPresent(kafkaProperties, props, "security.protocol");
        copyIfPresent(kafkaProperties, props, "sasl.mechanism");
        copyIfPresent(kafkaProperties, props, "sasl.jaas.config");
        copyIfPresent(kafkaProperties, props, "ssl.truststore.location");
        copyIfPresent(kafkaProperties, props, "ssl.truststore.password");
        copyIfPresent(kafkaProperties, props, "ssl.truststore.type");

        String username = kafkaProperties.getProperty("sasl.username");
        String password = kafkaProperties.getProperty("sasl.password");
        if (isNotBlank(username) && isNotBlank(password) && !props.containsKey("sasl.jaas.config")) {
            props.put("sasl.jaas.config",
                    "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" +
                            escapeJaas(username) + "\" password=\"" + escapeJaas(password) + "\";");
        }
        return props;
    }

    private static void copyIfPresent(Properties source, Properties target, String key) {
        String value = source.getProperty(key);
        if (isNotBlank(value)) {
            target.put(key, value.trim());
        }
    }

    private static boolean isNotBlank(String value) {
        return value != null && value.trim().length() > 0;
    }

    private static String escapeJaas(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
