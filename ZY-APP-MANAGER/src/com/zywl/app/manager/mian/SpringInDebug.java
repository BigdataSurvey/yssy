package com.zywl.app.manager.mian;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.Guild;
import com.zywl.app.defaultx.enmus.ItemIdEnum;
import com.zywl.app.defaultx.service.GuildMemberService;
import com.zywl.app.defaultx.service.GuildService;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.manager.*;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author lzx
 * 本地Debug工具类 - 全量完整版
 * ❌❌❌ todo:::  如果要运行测试就要注释AliPayCashService的初始化信息; 因为不知道为啥加载不到支付宝的证书文件 FileNotFoundException: File '/alipayRootCert.crt' does not exist
 */
public class SpringInDebug {
    /**
     * 上下文初始化
     */
    private static ClassPathXmlApplicationContext ctx;
    private static ManagerSocketServer fakeSocket;

    // 常用测试账号
    private static final Long MY_USER_ID = 937223L;
    private static final Long FRIEND_USER_ID = 928765L;
    private static final Long OTHER_USER_ID = 937228L;
    private static final String FRIEND_USER_NO = "76105190";

    // 公会测试相关
    private static final String TEST_GUILD_NAME = "A工会测试-Debug";
    private static final Integer TEST_NEED_MEMBER_NUMBER = 20;
    private static Long TEST_GUILD_ID = 61L;

    static {
        try {
            //todo:${redis.pool.maxIdle}是一个占位符在spring-redis.xml... 直接注册PropertyPlaceholderConfigurer;不然报错: For input string: "${redis.pool.maxIdle}".
            System.setProperty("redis.pool.maxIdle", "8");
            System.setProperty("redis.pool.maxTotal", "50");
            System.setProperty("redis.pool.minIdle", "0");
            System.setProperty("redis.host", "127.0.0.1");
            System.setProperty("redis.port", "6379");
            System.setProperty("redis.timeout", "2000");
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
                //Spring中未进行Bean定义，手动new出来
                fakeSocket = new ManagerSocketServer() {
                    private final String id = "DEBUG-" + UUID.randomUUID();
                    @Override
                    public String getId() { return id; }
                };
            }
            System.out.println("✅ Spring上下文初始化完成");
        } catch (Exception e) {
            throw new RuntimeException("❌ 初始化SpringContext失败", e);
        }
    }


    /**
     * 合成道具（种子 3 合 1）
     */
    public static void synInTest() {
        String module = "基础模块";
        String funcName = "道具合成";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("itemId", 1102);
            params.put("number", 10);
            Object resp = svc.syn(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 背包列表
     */
    public static void backpackTest() {
        String module = "基础模块";
        String funcName = "背包列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            JSONObject resp = svc.backpack(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 背包道具出售给系统
     */
    public static void sellItemToSysTest() {
        String module = "基础模块";
        String funcName = "道具回收/出售";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID); // 原代码 928364
            params.put("itemId", 16);
            params.put("num", 1000);
            JSONObject resp = (JSONObject) svc.sellItemToSys(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 商城信息
     */
    public static void shopInfoInTest() {
        String module = "商城模块";
        String funcName = "商城列表信息";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("type", 1);
            Object respObj = svc.shopInfo(fakeSocket, params);
            printResult(respObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 商城购买
     */
    public static void buyInTest() {
        String module = "商城模块";
        String funcName = "商品购买";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("id", 3);
            params.put("number", 2);
            params.put("type", 1);
            Object respObj = svc.buy(fakeSocket, params);
            printResult(respObj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 排行榜
     */
    private static void getTopTest() {
        String module = "基础模块";
        String funcName = "排行榜";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            // 1邀请拉新/2VIP/7资产消耗
            p.put("type", 7);
            p.put("capitalType", 0);
            JSONObject r = svc.getTop(null, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    // =================================================================================================================
    //                                         2. 农场种植模块 (Farm)
    // =================================================================================================================

    /**
     * 用户土地信息
     */
    public static void myFarmInfoInTest() {
        String module = "农场模块";
        String funcName = "获取土地信息";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            JSONObject resp = (JSONObject) svc.getMyFarmInfo(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 种植
     */
    public static void plantInTest() {
        String module = "农场模块";
        String funcName = "种植";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("landIndex", 8);
            params.put("seedItemId", 1305);
            JSONObject resp = (JSONObject) svc.plant(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 收割
     */
    public static void harvestInTest() {
        String module = "农场模块";
        String funcName = "收割";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("landIndex", -1);
            JSONObject resp = (JSONObject) svc.harvest(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 解锁/购买土地
     */
    public static void unlockLandInTest() {
        String module = "农场模块";
        String funcName = "解锁土地";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("landIndex", 8);
            JSONObject resp = (JSONObject) svc.unlockLand(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 兑换种子
     */
    public static void exchangeSeedInTest() {
        String module = "农场模块";
        String funcName = "兑换种子";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGameFarmService svc = ctx.getBean(ManagerGameFarmService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("seedItemId", 1101);
            params.put("number", 8);
            JSONObject resp = (JSONObject) svc.exchangeSeed(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    // =================================================================================================================
    //                                         3. 邮件模块 (Mail)
    // =================================================================================================================

    /**
     * 邮件发送（转赠）
     */
    public static void sendMailTest() {
        String module = "邮件模块";
        String funcName = "发送/转赠";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerMailService svc = ctx.getBean(ManagerMailService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("toUserId", OTHER_USER_ID);
            params.put("toUserNo", FRIEND_USER_NO);
            params.put("itemId", ItemIdEnum.CORE_POINT.getValue());
            params.put("amount", 100);
            params.put("title", "【测试】好友转赠");
            params.put("context", "本邮件为本地Debug测试用");
            JSONObject resp = svc.sendMail(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 邮件领取 - 全部
     */
    public static void readMailAllTest() {
        String module = "邮件模块";
        String funcName = "一键领取";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerMailService svc = ctx.getBean(ManagerMailService.class);
            JSONObject params = new JSONObject();
            params.put("userId", OTHER_USER_ID);
            params.put("mailId", 0L);
            JSONObject resp = svc.userReadMail(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 邮件领取 - 单封
     */
    public static void readMailOneTest() {
        long MAIL_ID = 123456L;
        String module = "邮件模块";
        String funcName = "单封领取";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerMailService svc = ctx.getBean(ManagerMailService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("mailId", MAIL_ID);
            JSONObject resp = svc.userReadMail(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    // =================================================================================================================
    //                                         4. 欢乐值模块 (Joy)
    // =================================================================================================================

    public static void testJoy001_getMyJoyInfo() {
        String module = "欢乐值模块";
        String funcName = "获取信息";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerJoyService svc = ctx.getBean(ManagerJoyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            JSONObject result = svc.getMyJoyInfo(null, params);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void testJoy002_exchangeJoyToBalloon() {
        String module = "欢乐值模块";
        String funcName = "兑换气球";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerJoyService svc = ctx.getBean(ManagerJoyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            JSONObject result = svc.exchangeJoyToBalloon(null, params);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void testJoy003_getFriendJoyContrib() {
        String module = "欢乐值模块";
        String funcName = "好友贡献查询";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerJoyService svc = ctx.getBean(ManagerJoyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("friendUserId", FRIEND_USER_ID);
            JSONObject result = svc.getFriendJoyContrib(null, params);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void testJoyDistributeJoy() {
        String module = "欢乐值模块";
        String funcName = "分发欢乐值逻辑";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerJoyService svc = ctx.getBean(ManagerJoyService.class);
            Long triggerUserId = 937226L;
            int itemQuality = 2;
            String sourceType = "FARM_HARVEST";
            String eventId = "DEBUG_" + sourceType + "_" + UUID.randomUUID();
            svc.distributeJoy(triggerUserId, itemQuality, eventId, sourceType);
            System.out.println("运行结果========= OK. eventId=" + eventId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    // =================================================================================================================
    //                                         5. 公会模块 (Guild)
    // =================================================================================================================

    // 查询最新的公会ID
    private static Long queryLatestGuildId(Long userId, Integer status) {
        GuildService guildService = ctx.getBean(GuildService.class);
        Map<String, Object> q = new HashMap<>();
        q.put("userId", userId);
        if (status != null) {
            q.put("status", status);
        }
        List<Guild> list = guildService.findByConditions(q);
        if (list == null || list.isEmpty()) return null;
        long max = 0;
        for (Guild g : list) {
            if (g != null && g.getId() != null && g.getId() > max) {
                max = g.getId();
            }
        }
        return max == 0 ? null : max;
    }

    public static void guildGetListTest() {
        String module = "公会模块";
        String funcName = "获取公会列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            JSONObject r = svc.getGuilds(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildCreateTest() {
        String module = "公会模块";
        String funcName = "创建公会";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            JSONObject p = new JSONObject();
            p.put("userId", 937232L); //账号5
            p.put("guildName", "2020测工会");
            p.put("needMemberNumber", TEST_NEED_MEMBER_NUMBER);
            JSONObject r = svc.createGuild(fakeSocket, p);
            printResult(r);

            if (r != null && r.getLong("guildId") != null) {
                TEST_GUILD_ID = r.getLong("guildId");
            } else {
                TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, 2);
                if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
            }
            System.out.println("当前测试 GuildID: " + TEST_GUILD_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildApproveTest() {
        String module = "公会模块";
        String funcName = "审核公会-通过";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, 2);
            if (TEST_GUILD_ID == null) throw new RuntimeException("无待审核公会");
            svc.passApplyGuild(TEST_GUILD_ID, MY_USER_ID);
            System.out.println("运行结果========= 审核通过成功 GuildID: " + TEST_GUILD_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildRefuseTest() {
        String module = "公会模块";
        String funcName = "审核公会-拒绝";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, 2);
            if (TEST_GUILD_ID == null) throw new RuntimeException("无待审核公会");
            svc.refuseApplyGuild(TEST_GUILD_ID, MY_USER_ID);
            System.out.println("运行结果========= 审核拒绝成功 GuildID: " + TEST_GUILD_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildInfoTest() {
        String module = "公会模块";
        String funcName = "公会详情";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            p.put("guildId", TEST_GUILD_ID);
            Object r = svc.getGuildInfo(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildAddMemberTest() {
        String module = "公会模块";
        String funcName = "直接添加成员";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
            JSONObject p = new JSONObject();
            p.put("guildId", TEST_GUILD_ID);
            p.put("userId", 853859);      // 被邀请人
            p.put("createUserId", FRIEND_USER_ID);    // 邀请人
            p.put("memberRoleId", 4);
            JSONObject r = svc.addGuildMember(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildMemberListTest() {
        String module = "公会模块";
        String funcName = "成员列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
            JSONObject p = new JSONObject();
            p.put("guildId", TEST_GUILD_ID);
            p.put("userId", MY_USER_ID);
            JSONObject r = svc.myGuild(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void guildAddProfitBalanceForTest() {
        String module = "公会模块";
        String funcName = "准备数据-增加余额";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            GuildMemberService memberService = ctx.getBean(GuildMemberService.class);
            memberService.addProfitBalance(FRIEND_USER_ID, new BigDecimal("100"));
            System.out.println("运行结果========= OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildReceiveTest() {
        String module = "公会模块";
        String funcName = "发放/领取佣金";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
            JSONObject p = new JSONObject();
            p.put("guildId", TEST_GUILD_ID);
            p.put("userId", FRIEND_USER_ID);
            p.put("operatorUserId", MY_USER_ID);
            Object r = svc.receive(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void guildUpdateRateTest() {
        String module = "公会模块";
        String funcName = "修改比例";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            JSONObject p = new JSONObject();
            p.put("userId", FRIEND_USER_ID);
            p.put("rate", "8");
            JSONObject r = svc.updateRate(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildApplyJoinTest() {
        String module = "公会模块";
        String funcName = "申请加入";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            if (TEST_GUILD_ID == null) {
                TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, 1);
                if (TEST_GUILD_ID == null) TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
            }
            if (TEST_GUILD_ID == null) throw new RuntimeException("无公会可申请");
            JSONObject p = new JSONObject();
            p.put("userId", FRIEND_USER_ID);
            p.put("guildId", TEST_GUILD_ID);
            JSONObject r = svc.applyJoinGuild(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildGetJoinApplyListTest() {
        String module = "公会模块";
        String funcName = "获取入会申请列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            JSONObject r = svc.getJoinApplyList(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void guildAuditJoinApplyTest(int pass) {
        String module = "公会模块";
        String funcName = "审核入会申请(pass=" + pass + ")";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGuildService svc = ctx.getBean(ManagerGuildService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            p.put("applyUserId", FRIEND_USER_ID);
            p.put("pass", pass);
            JSONObject r = svc.auditJoinApply(fakeSocket, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    // =================================================================================================================
    //                                         6. 宠物模块 (Pet)
    // =================================================================================================================

    public static void getPetInfoTest() {
        String module = "宠物模块";
        String funcName = "获取信息";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            JSONObject r = svc.getPetInfo(null, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void getPayPetTest() {
        String module = "宠物模块";
        String funcName = "购买宠物";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            p.put("buyCount", 2);
            JSONObject r = svc.buyLion(null, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void getFeedLionTest() {
        String module = "宠物模块";
        String funcName = "喂养";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            p.put("feedTimes", 1);
            JSONObject r = svc.feedLion(null, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void getClaimYieldTest() {
        String module = "宠物模块";
        String funcName = "领取产出";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject p = new JSONObject();
            p.put("userId", MY_USER_ID);
            JSONObject r = svc.claimYield(null, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void unlockLv3Test() {
        String module = "宠物模块";
        String funcName = "解锁3代分润";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject p = new JSONObject();
            p.put("userId", 928765L);
            p.put("unlockLevel", 3);
            JSONObject r = svc.unlockDividendLevel(null, p);
            printResult(r);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void petInviteHomeTest() {
        String module = "宠物模块";
        String funcName = "邀请主页";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            JSONObject res = svc.inviteHome(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void petInviteListTest() {
        String module = "宠物模块";
        String funcName = "邀请列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("type", 0); // 0全部 / 1未达标 / 2有效
            params.put("page", 1);
            params.put("pageSize", 20);
            JSONObject res = svc.inviteList(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void petInviterInfoTest() {
        String module = "宠物模块";
        String funcName = "邀请人信息";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            JSONObject res = svc.inviterInfo(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void petTribeHomeInfoTest() {
        String module = "宠物模块";
        String funcName = "部落信息";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            JSONObject res = svc.tribeHome(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    // =================================================================================================================
    //                                         7. 悬赏任务模块 (Bounty)
    // =================================================================================================================

    public static void getTaskListTest() {
        String module = "悬赏任务";
        String funcName = "大厅列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", FRIEND_USER_ID);
            params.put("pageNo", 1);
            params.put("pageSize", 10);
            params.put("orderType", 2);
            JSONObject res = svc.listTasks(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getTaskDetailTest() {
        String module = "悬赏任务";
        String funcName = "任务详情";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", FRIEND_USER_ID);
            params.put("taskId", 1);
            JSONObject res = svc.getTaskDetail(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void getPublishTaskTest() {
        String module = "悬赏任务";
        String funcName = "发布任务";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject pub = new JSONObject();
            pub.put("userId", MY_USER_ID);
            pub.put("taskName", "Debug-" + System.currentTimeMillis());
            pub.put("taskTitle", "Debug标题测试");
            pub.put("taskDesc", "描述");
            pub.put("taskSteps", "1.步骤一");
            pub.put("videoUrl", "http://test.video/1.mp4");
            pub.put("idTip", "请提交ID");
            pub.put("unitPrice", new BigDecimal("1.50"));
            pub.put("quotaTotal", 5);
            pub.put("takeLimitHours", 2);
            pub.put("downloadImgs", "[\"https://img.test/guide.png\"]");
            JSONObject res = svc.publishTask(null, pub);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getCancelTaskTest() {
        String module = "悬赏任务";
        String funcName = "取消任务";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("taskId", 1);
            JSONObject res = svc.cancelTask(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getTakeTaskTest() {
        String module = "悬赏任务";
        String funcName = "接单";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("taskId", 3);
            JSONObject res = svc.takeTask(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getCancelOrderTest() {
        String module = "悬赏任务";
        String funcName = "取消接单";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("taskId", 3);
            JSONObject res = svc.cancelOrder(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getSubmitOrderTest() {
        String module = "悬赏任务";
        String funcName = "提交材料";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("taskId", 3);
            params.put("submitUserId", "GameID-8888");
            params.put("submitImgs", "[\"https://oss.test/submit1.jpg\"]");
            JSONObject res = svc.submitOrder(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getResubmitOrderTest() {
        String module = "悬赏任务";
        String funcName = "重新提交";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("taskId", 3);
            params.put("submitUserId", "抖音123号");
            params.put("submitImgs", "[\"https://oss.test/fix.jpg\"]");
            JSONObject res = svc.resubmitOrder(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getAppealOrderTest() {
        String module = "悬赏任务";
        String funcName = "申诉";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("taskId", 3);
            params.put("appealReason", "我已经按要求做了");
            JSONObject res = svc.appealOrder(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    public static void getMyOrdersTest() {
        String module = "悬赏任务";
        String funcName = "我的接单列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", FRIEND_USER_ID);
            params.put("pageNo", 1);
            params.put("pageSize", 10);
            params.put("tab", 1); // 1进行中, 2待审核, 3已完成
            JSONObject res = svc.myOrders(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getMyPublishTest() {
        String module = "悬赏任务";
        String funcName = "我的发布列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("pageNo", 1);
            params.put("pageSize", 10);
            JSONObject res = svc.myPublish(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getPendingAuditTest() {
        String module = "悬赏任务";
        String funcName = "待审核列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 928762L);
            params.put("pageNo", 1);
            params.put("pageSize", 10);
            JSONObject res = svc.pendingAudit(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getAuditApproveTest() {
        String module = "悬赏任务";
        String funcName = "审核通过";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 928762L);
            params.put("orderId", 1);
            JSONObject res = svc.auditApprove(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getAuditRejectTest() {
        String module = "悬赏任务";
        String funcName = "审核驳回";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerBountyService svc = ctx.getBean(ManagerBountyService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 928762L);
            params.put("orderId", 1);
            params.put("rejectReason", "截图不清晰，请重新提交");
            JSONObject res = svc.auditReject(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    // =================================================================================================================
    //                                         8.交易行模块
    // =================================================================================================================
    private static void getTradingListingTest() {
        String module = "交易行";
        String funcName = "交易行上架";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerTradingService svc = ctx.getBean(ManagerTradingService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("number", 1);
            params.put("price", "1");
            params.put("itemId", 1201);
            JSONObject res = svc.userListingItem(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    private static void getTradingInfoTest() {
        String module = "交易行";
        String funcName = "交易行列表";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerTradingService svc = ctx.getBean(ManagerTradingService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("type", 0);
            //params.put("itemName", "玉米种子·一阶");
            params.put("page", 1);
            params.put("num", 20);
            JSONObject res = svc.getTradingInfo(null, params);
            printResult(res);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }


    // =================================================================================================================
    //                                         9.OSS
    // =================================================================================================================

    /**
     * OSS 直传签名（Policy）
     * 对应 ManagerOssService: 040001
     */
    //运行后拿到结果
/*        >>>>>>>>>> 开始本地Debug测试 <<<<<<<<<<
            =[OSS基础能力]-OSS直传签名(Policy)-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========
    运行结果========= {"accessId":"REDACTED_ALIYUN_AKID","policy":"eyJleHBpcmF0aW9uIjoiMjAyNi0wMS0xNVQwMzo1MDoxNi4wMDBaIiwiY29uZGl0aW9ucyI6W1siY29udGVudC1sZW5ndGgtcmFuZ2UiLDAsNTI0Mjg4MF0seyJidWNrZXQiOiJ5c3N5LXVzZXIifSxbInN0YXJ0cy13aXRoIiwiJGtleSIsInlzc3kvYm91bnR5LzIwMjYwMTE1LzkzNzIyMy8iXSx7InN1Y2Nlc3NfYWN0aW9uX3N0YXR1cyI6IjIwMCJ9XX0=","signature":"KFPGGCARBvNcigty1E7iS7zU4kI=","dir":"yssy/bounty/20260115/937223/","fileName":"b9a99d32d9014048ae11afada59eb2b9.jpg","objectKey":"yssy/bounty/20260115/937223/b9a99d32d9014048ae11afada59eb2b9.jpg","host":"https://yssy-user.oss-cn-beijing.aliyuncs.com","urlPrefix":"https://yssy-user.oss-cn-beijing.aliyuncs.com","finalUrl":"https://yssy-user.oss-cn-beijing.aliyuncs.com/yssy/bounty/20260115/937223/b9a99d32d9014048ae11afada59eb2b9.jpg","expire":1768449016,"maxSize":5242880,"success_action_status":"200"}
=【OSS基础能力-OSS直传签名(Policy)-测试-结束】=用时：497ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============
            >>>>>>>>>> Debug测试结束 <<<<<<<<<<*/
    //封装请求和结果：
/*    PS C:\Users\Administrator> curl.exe -i -X POST "https://yssy-user.oss-cn-beijing.aliyuncs.com" -F "key=yssy/bounty/20260115/937223/b9a99d32d9014048ae11afada59eb2b9.jpg" -F "OSSAccessKeyId=REDACTED_ALIYUN_AKID" -F "policy=eyJleHBpcmF0aW9uIjoiMjAyNi0wMS0xNVQwMzo1MDoxNi4wMDBaIiwiY29uZGl0aW9ucyI6W1siY29udGVudC1sZW5ndGgtcmFuZ2UiLDAsNTI0Mjg4MF0seyJidWNrZXQiOiJ5c3N5LXVzZXIifSxbInN0YXJ0cy13aXRoIiwiJGtleSIsInlzc3kvYm91bnR5LzIwMjYwMTE1LzkzNzIyMy8iXSx7InN1Y2Nlc3NfYWN0aW9uX3N0YXR1cyI6IjIwMCJ9XX0=" -F "Signature=KFPGGCARBvNcigty1E7iS7zU4kI=" -F "success_action_status=200" -F "file=@C:\Users\Administrator\Desktop\c8afac19f696b11624253bde98cf20c3.jpeg"
    HTTP/1.1 200 OK
    Server: AliyunOSS
    Date: Thu, 15 Jan 2026 03:49:23 GMT
    Content-Length: 0
    Connection: keep-alive
    x-oss-request-id: 696863C3BCA41832353DC28B
    ETag: "08B821169E2E9585F31F40D983A3B979"
    x-oss-hash-crc64ecma: 8973854902506338371
    Content-MD5: CLghFp4ulYXzH0DZg6O5eQ==
    x-oss-server-time: 40*/
    private static void ossGetDirectUploadPolicyTest() {
        String module = "OSS基础能力";
        String funcName = "OSS直传签名(Policy)";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerOssService svc = ctx.getBean(ManagerOssService.class);

            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);

            // 业务目录（建议传具体业务：bounty/avatar/guild/...）
            params.put("biz", "bounty");

            // 文件后缀（如果你配置了 allowSuffix 白名单，这里必须在白名单内）
            params.put("suffix", "jpg");

            // 可选：前端期望 maxSize/expireSeconds（服务端会按配置裁剪）
            params.put("maxSize", 5 * 1024 * 1024L);   // 5MB
            params.put("expireSeconds", 120L);         // 120s

            JSONObject resp = svc.getOssDirectUploadPolicy(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }

    /**
     * 规范化并校验 OSS 资源 URL
     * 对应 ManagerOssService: 040002
     */
    //拿刚才的结果在来得到URL再去请求得到结果：
    /*    =[OSS基础能力]-OSS资源URL规范化-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========
                运行结果========= {"url":"https://yssy-user.oss-cn-beijing.aliyuncs.com/yssy/bounty/20260115/937223/b9a99d32d9014048ae11afada59eb2b9.jpg"}
         =【OSS基础能力-OSS资源URL规范化-测试-结束】=用时：60ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============*/
    private static void ossCanonicalizeUrlTest() {
        String module = "OSS基础能力";
        String funcName = "OSS资源URL规范化";
        System.out.println("=[" + module + "]-" + funcName + "-测试-开始=========>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>==========");
        long start = System.currentTimeMillis();
        try {
            ManagerOssService svc = ctx.getBean(ManagerOssService.class);

            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            // 两种形式,或者传完整url也可以了
            params.put("url", "yssy/bounty/20260115/937223/b9a99d32d9014048ae11afada59eb2b9.jpg");
            // 可选：用于报错提示字段名
            params.put("fieldName", "url");
            // 可选：是否允许空（true -> 空则返回 null，不抛错）
            params.put("allowEmpty", false);

            JSONObject resp = svc.canonicalizeOssUrl(fakeSocket, params);
            printResult(resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("=【" + module + "-" + funcName + "-测试-结束】=用时：" + (System.currentTimeMillis() - start) + "ms=====>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>=============");
    }


    // =================================================================================================================
    //                                         Main
    // =================================================================================================================

    /**
     * 统一打印结果工具
     */
    private static void printResult(Object result) {
        if (result == null) {
            System.out.println("运行结果========= null");
        } else if (result instanceof JSONObject) {
            System.out.println("运行结果========= " + ((JSONObject) result).toJSONString());
        } else {
            System.out.println("运行结果========= " + JSONObject.toJSONString(result));
        }
    }

    public static void main(String[] args) {
        System.out.println(">>>>>>>>>> 开始本地Debug测试 <<<<<<<<<<");

        // --- 1. 基础模块 ---
        // synInTest();
        // backpackTest();
        // sellItemToSysTest();
        // shopInfoInTest();
        // buyInTest();
        // getTopTest();

        // --- 2. 农场模块 ---
        // myFarmInfoInTest();
        // plantInTest();
        // harvestInTest();
        // unlockLandInTest();
        // exchangeSeedInTest();

        // --- 3. 邮件模块 ---
        // sendMailTest();
        // readMailAllTest();
        // readMailOneTest();

        // --- 4. 欢乐值 ---
        // testJoy001_getMyJoyInfo();
        // testJoy002_exchangeJoyToBalloon();
        // testJoy003_getFriendJoyContrib();
        // testJoyDistributeJoy();

        // --- 5. 公会模块 ---
        // guildGetListTest();
        //guildCreateTest();
        //guildApproveTest();
        // guildRefuseTest();
        // guildInfoTest();
        // guildAddMemberTest();
        // guildMemberListTest();
        // guildAddProfitBalanceForTest();
        // guildReceiveTest();
        // guildUpdateRateTest();
        // guildApplyJoinTest();
        // guildGetJoinApplyListTest();
        // guildAuditJoinApplyTest(1);

        // --- 6. 宠物模块 ---
        // getPetInfoTest();
        // getPayPetTest();
        // getFeedLionTest();
        // getClaimYieldTest();
        // unlockLv3Test();
        // petInviteHomeTest();
        // petInviteListTest();
        // petInviterInfoTest();
        //petTribeHomeInfoTest();

        // --- 7. 悬赏任务 ---
        // getTaskListTest();
        // getTaskDetailTest();
        // getPublishTaskTest();
        // getCancelTaskTest();
        // getTakeTaskTest();
        // getCancelOrderTest();
        // getSubmitOrderTest();
        // getResubmitOrderTest();
        // getAppealOrderTest();
        // getMyOrdersTest();
        // getMyPublishTest();
        // getPendingAuditTest();
        // getAuditApproveTest();
        //getAuditRejectTest();

        //交易行
        //getTradingInfoTest();
        //getTradingListingTest();

        // --- 9. OSS基础能力 ---
        //ossGetDirectUploadPolicyTest();
        ossCanonicalizeUrlTest();
        System.out.println(">>>>>>>>>> Debug测试结束 <<<<<<<<<<");
        System.exit(0);
    }
}