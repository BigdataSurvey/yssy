package com.zywl.app.manager.mian;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.manager.ManagerCapitalService;
import com.zywl.app.manager.service.manager.ManagerGameBaseService;
import com.zywl.app.manager.service.manager.ManagerGameFarmService;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.zywl.app.manager.service.manager.ManagerMailService;
import com.zywl.app.defaultx.enmus.ItemIdEnum;

import java.lang.reflect.Field;

/**
 * @author lzx
 * 本地Debug工具类，手动调用不同Manager方法。
 * ❌❌❌ todo:::  如果要运行测试就要注释AliPayCashService的初始化信息; 因为不知道为啥加载不到支付宝的证书文件 FileNotFoundException: File '/alipayRootCert.crt' does not exist
 */
public class SpringInDebug {
    // todo 不这样私有静态加载空指针..
    private static ClassPathXmlApplicationContext ctx;
    private static ManagerSocketServer fakeSocket;

    static {
        try {
            //todo:${redis.pool.maxIdle}是一个占位符在spring-redis.xml... 直接注册PropertyPlaceholderConfigurer;不然报错: For input string: "${redis.pool.maxIdle}".
            System.setProperty("redis.pool.maxIdle",   "8");
            System.setProperty("redis.pool.maxTotal",  "50");
            System.setProperty("redis.pool.minIdle",   "0");
            System.setProperty("redis.host",           "127.0.0.1");
            System.setProperty("redis.port",           "6379");
            System.setProperty("redis.timeout",        "2000");

            //todo 项目配置文件不配置加载不到汇报错：No qualifying bean of type 'com.zywl.app.manager.service.manager.card.ManagerSignService' available
            //不写找不到包就会无法实例化该bean
            String[] cfgs = {
                    "classpath:application.xml",
                    "classpath:application-db.xml",
                    "classpath:application-mybatis.xml",
                    "classpath:spring-redis.xml"
            };
            ctx = new ClassPathXmlApplicationContext(cfgs);
            ctx.registerShutdownHook();

            // 反射,把 ctx 注入到 SpringUtil.applicationContext 不然报错
            Field f = SpringUtil.class.getDeclaredField("applicationContext");
            f.setAccessible(true);
            f.set(null, ctx);

            //todo: 使用Spring容器获取的实例，不能去new 不然报错: No qualifying bean of type 'com.zywl.app.manager.socket.ManagerSocketServer' available  java.lang.NullPointerException at com.zywl.app.defaultx.util.SpringUtil.getService(SpringUtil.java:45)
            try {
                fakeSocket = ctx.getBean(ManagerSocketServer.class);
            } catch (NoSuchBeanDefinitionException e) {
                // Spring中没有Bean定义 就手动new.
                fakeSocket = new ManagerSocketServer() {
                    private final String id = "DEBUG-" + java.util.UUID.randomUUID();
                    @Override
                    public String getId() { return id; }
                };
            }

        } catch (Exception e) {
            throw new RuntimeException("初始化SpringContext失败", e);
        }
    }


    /**
     * 合成道具（种子 3 合 1 调试）
     */
    public static void synInTest() {
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();

            params.put("userId", 937223L);

            params.put("itemId", 1102);

            params.put("number", 10);

            JSONObject resp = (JSONObject) svc.syn(fakeSocket, params);
            System.out.println("=== 道具合成 测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * 背包测试
     * **/
    public static void backpackTest() {
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 928364);
            JSONObject resp = svc.backpack(fakeSocket, params);
            System.out.println("=== 背包测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 背包道具出售系统测试
     * **/
    public static void sellItemToSysTest() {
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 928364);
            params.put("itemId", 16);
            params.put("num", 1000);
            JSONObject resp = (JSONObject) svc.sellItemToSys(fakeSocket, params);
            System.out.println("=== 背包道具出售系统测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 邮件发送（转赠）测试
     *
     * 场景：
     *  - fromUserId 给 toUserId 转赠指定道具（目前要求是 CORE_POINT 核心积分）
     *  - 生成一封带附件的邮件，收件人稍后通过 userReadMail 领取
     *
     * 使用前请根据自己本地数据修改：
     *  - FROM_USER_ID  : 发送人 user_id
     *  - TO_USER_ID    : 收件人 user_id
     *  - TO_USER_NO    : 收件人 user_no（游戏编号）
     */
    public static void sendMailTest() {
        // TODO：这里改成你本地真实存在的两个用户
        long   FROM_USER_ID = 937223L;
        long   TO_USER_ID   = 937226L;
        String TO_USER_NO   = "43626293";

        try {
            ManagerMailService svc = ctx.getBean(ManagerMailService.class);

            JSONObject params = new JSONObject();
            // 发送人 / 收件人
            params.put("userId", FROM_USER_ID);
            params.put("toUserId", TO_USER_ID);
            params.put("toUserNo", TO_USER_NO);

            // 转赠的道具：目前我们约定只能转 CORE_POINT（核心积分）
            params.put("itemId", ItemIdEnum.CORE_POINT.getValue());

            // 转赠数量：这里先写个 100，注意要满足 TRANSFER_SILL 起赠门槛
            params.put("amount", 100);

            // 邮件标题 / 内容（可以让前端传，这里直接写死测试文案）
            params.put("title",   "【测试】好友转赠核心积分");
            params.put("context", "本邮件为本地Debug测试用，转赠 100 核心积分，请在游戏内勿当真~");

            JSONObject resp = svc.sendMail(fakeSocket, params);
            System.out.println("=== 发送邮件（转赠）测试返回 ===");
            System.out.println(resp.toJSONString());

            // 一般 resp 会包含 mailId / amount / itemId
            // 你可以记下 mailId，下面单封领取的时候用
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 邮件领取测试 - 全部领取（mailId = 0）
     *
     * 场景：
     *  - 指定一个用户，领取他当前“所有可领取的邮件附件”
     *  - 用于配合 sendMailTest() 测试收件方到账情况
     */
    public static void readMailAllTest() {
        // TODO：改成“收件方”的 user_id（上面 TO_USER_ID）
        long USER_ID = 937226L;

        try {
            ManagerMailService svc = ctx.getBean(ManagerMailService.class);

            JSONObject params = new JSONObject();
            params.put("userId", USER_ID);
            // mailId = 0 表示“全部领取”，与真实 userReadMail 逻辑一致
            params.put("mailId", 0L);

            JSONObject resp = svc.userReadMail(fakeSocket, params);
            System.out.println("=== 领取邮件（全部）测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 邮件领取测试 - 单封领取（指定 mailId）
     *
     * 使用方法：
     *  - 先执行 sendMailTest()，看控制台返回的 mailId
     *  - 把下面的 MAIL_ID 改成那个值，再执行本方法
     */
    public static void readMailOneTest() {
        // TODO：改成收件人的 user_id（同上）和你要测试的 mailId
        long USER_ID = 937226L;
        long MAIL_ID = 123456L;  // 把这里改成 sendMailTest 返回里的 mailId

        try {
            ManagerMailService svc = ctx.getBean(ManagerMailService.class);

            JSONObject params = new JSONObject();
            params.put("userId", USER_ID);
            params.put("mailId", MAIL_ID);

            JSONObject resp = svc.userReadMail(fakeSocket, params);
            System.out.println("=== 领取邮件（单封）测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户土地信息
     */
    public static void myFarmInfoInTest() {
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();

            params.put("userId", 937223L);

            JSONObject resp = (JSONObject) svc.getMyFarmInfo(fakeSocket, params);
            System.out.println("=== 用户土地信息 测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户土地种植
     */
    public static void plantInTest() {
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("landIndex",8);
            params.put("seedItemId",1305);
            params.put("userId", 937223L);

            JSONObject resp = (JSONObject) svc.plant(fakeSocket, params);
            System.out.println("=== 用户土地种植 测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户种地收割 单个/一键
     */
    public static void harvestInTest() {
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 937223L);
            params.put("landIndex",-1);
            JSONObject resp = (JSONObject) svc.harvest(fakeSocket, params);
            System.out.println("=== 用户种地收割 测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 用户解锁/购买土地
     */
    public static void unlockLandInTest() {
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 937223L);
                params.put("landIndex",8);
            JSONObject resp = (JSONObject) svc.unlockLand(fakeSocket, params);
            System.out.println("=== 用户解锁/购买土地 测试返回 ===");
            System.out.println(resp.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 商城信息 037
     */
    public static void shopInfo() {
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 937223L);
            params.put("type",1);
            Object respObj = svc.shopInfo(fakeSocket, params);
            System.out.println("=== 商城信息 测试返回 ===");
            System.out.println(JSONObject.toJSONString(respObj));
            System.out.println(JSONObject.toJSONString(respObj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public static void main(String[] args) {
        //炼制测试
        ///synInTest();;

        //背包测试
        //backpackTest();

        //出售背包道具给系统 测试
        //sellItemToSysTest();;

        //发送邮件
        //sendMailTest();
        //{"mailId":133,"amount":100,"itemId":"1001"}

        //领取全部邮件
        //readMailAllTest();

        //用户土地信息
        myFarmInfoInTest();

        //用户土地种植
        //plantInTest();

        //用户种地收割
        //harvestInTest();

        //用户解锁/购买土地 测试返回
        //unlockLandInTest();

        //商城信息
        //shopInfo();
    }



}



