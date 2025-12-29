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
        // 启动基础上下文
        APP.run();
        PropertiesUtil propertiesUtil = new PropertiesUtil("kafka.properties");

        // 无限重试直到连接成功
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    // 尝试连接
                    SpringUtil.getService(SocketLogService.class).connectLog();
                    // 如果执行到这里没有报错，说明连接成功，打印日志并退出循环
                    System.out.println(">>> [MANAGER] LogSocket (日志服) 连接成功!");
                    break;
                } catch (Exception e) {
                    System.err.println(">>> [MANAGER] 连接 LogServer 失败 (等待Tomcat端口开放), 3秒后重试...");
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }, "connectLogServer");
        t.start();


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