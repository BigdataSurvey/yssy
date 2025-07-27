package com.zywl.app.manager.context;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.zywl.app.base.constant.KafkaTopicContext;
import com.zywl.app.base.util.PropertiesUtil;
import com.zywl.app.defaultx.APP;
import com.zywl.app.defaultx.service.BatchCashRecordService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.SocketLogService;
import com.zywl.app.manager.service.kafka.KafkaConsumerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

import java.util.Collections;
import java.util.Properties;

/**
 * 上下文监听
 *
 * @author FXBTG Doe.
 */
public class ManagerContext implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(ManagerContext.class);
    public void contextDestroyed(ServletContextEvent contextEvent) {
        APP.shutdown();
    }

    public void contextInitialized(ServletContextEvent contextEvent) {
        APP.run();
        PropertiesUtil propertiesUtil = new PropertiesUtil("kafka.properties");
        Thread t = new Thread(() -> {
            SpringUtil.getService(SocketLogService.class).connectLog();
        }, "connectLogServer");
//        t.start();
        Thread t2 = new Thread(() -> {
            KafkaConsumerService consumerService = new KafkaConsumerService(propertiesUtil.get("bootstrap.servers"), "red","Consumer-1");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down Kafka consumer...");
                consumerService.shutdown();
            }));
            consumerService.start();
            logger.info("KafkaConsumerStart===============GroupId:a");
        }, "consumerServiceA");
        t2.start();
        Thread t3 = new Thread(() -> {
            KafkaConsumerService consumerService = new KafkaConsumerService(propertiesUtil.get("bootstrap.servers"), "red","Consumer-2");
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down Kafka consumer...");
                consumerService.shutdown();
            }));
            consumerService.start();
            logger.info("KafkaConsumerStart===============GroupId:a");
        }, "consumerServiceB");
        t3.start();

    }
}
