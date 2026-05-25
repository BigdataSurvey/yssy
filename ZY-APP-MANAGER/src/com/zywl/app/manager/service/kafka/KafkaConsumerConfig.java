package com.zywl.app.manager.service.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;

public class KafkaConsumerConfig {

    public static Properties getConsumerProperties(String bootstrapServers, String groupId) {
        Properties kafkaProperties = new Properties();
        kafkaProperties.put("bootstrap.servers", bootstrapServers);
        return getConsumerProperties(kafkaProperties, groupId);
    }

    public static Properties getConsumerProperties(Properties kafkaProperties, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getProperty("bootstrap.servers"));
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

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
