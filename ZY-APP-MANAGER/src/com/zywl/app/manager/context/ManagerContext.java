package com.zywl.app.manager.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.APP;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.SocketLogService;
import com.zywl.app.manager.service.kafka.KafkaConsumerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ManagerContext implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(ManagerContext.class);

    public void contextDestroyed(ServletContextEvent contextEvent) {
        APP.shutdown();
    }

    public void contextInitialized(ServletContextEvent contextEvent) {
        APP.run();
        startLogServerConnector();

        PropertiesUtil propertiesUtil = new PropertiesUtil("kafka.properties");
        if (isEnabled(propertiesUtil.get("kafka.enabled"))) {
            startKafkaConsumer(propertiesUtil, "Consumer-1", "consumerServiceA");
            startKafkaConsumer(propertiesUtil, "Consumer-2", "consumerServiceB");
        } else {
            logger.info("Kafka consumer disabled. Set kafka.enabled=true to start it.");
        }
    }

    private void startLogServerConnector() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    SpringUtil.getService(SocketLogService.class).connectLog();
                    logger.info("LogSocket connected.");
                    break;
                } catch (Exception e) {
                    logger.warn("Connect LogServer failed, retry after 3 seconds.", e);
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }, "connectLogServer");
        thread.start();
    }

    private void startKafkaConsumer(PropertiesUtil propertiesUtil, String clientId, String threadName) {
        Thread thread = new Thread(() -> {
            try {
                String groupId = getOrDefault(propertiesUtil.get("group.id"), "red");
                final KafkaConsumerService consumerService = new KafkaConsumerService(propertiesUtil.getProperties(), groupId, clientId);
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    logger.info("Shutting down Kafka consumer: " + clientId);
                    consumerService.shutdown();
                }));
                logger.info("Kafka consumer starting. groupId=" + groupId + ", clientId=" + clientId);
                consumerService.start();
            } catch (Throwable e) {
                logger.error("Kafka consumer stopped unexpectedly. clientId=" + clientId, e);
            }
        }, threadName);
        thread.start();
    }

    private boolean isEnabled(String value) {
        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String getOrDefault(String value, String defaultValue) {
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }
        return value.trim();
    }
}
