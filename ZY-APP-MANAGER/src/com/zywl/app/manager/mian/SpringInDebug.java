package com.zywl.app.manager.mian;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.defaultx.util.SpringUtil;
import com.zywl.app.manager.service.manager.*;
import com.zywl.app.manager.socket.ManagerSocketServer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
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
    private static final Long MY_USER_ID = 937223L;
    private static final Long FRIEND_USER_ID = 928765L;

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
    public static void shopInfoInTest() {
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", 937223L);
            params.put("type",1);
            Object respObj = svc.shopInfo(fakeSocket, params);
            System.out.println("=== 商城信息 测试返回 ===");
            System.out.println(JSONObject.toJSONString(respObj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 商城购买 035
     */
    public static void buyInTest() {
        try {
            ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
            JSONObject params = new JSONObject();
            params.put("userId", MY_USER_ID);
            params.put("id",3);
            params.put("number",2);
            params.put("type",1);
            Object respObj = svc.buy(fakeSocket, params);
            System.out.println("=== 商城购买 测试返回 ===");
            System.out.println(JSONObject.toJSONString(respObj));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 001 查询我的欢乐值与可兑气球数量 */
    public static void testJoy001_getMyJoyInfo() {
        ManagerJoyService joyService = ctx.getBean(ManagerJoyService.class);

        JSONObject params = new JSONObject();
        params.put("userId", MY_USER_ID);

        System.out.println("========== [JOY 001] getMyJoyInfo ==========");
        JSONObject result = joyService.getMyJoyInfo(null, params);
        System.out.println(result == null ? "null" : result.toJSONString());
    }

    /** 002 兑换气球（欢乐值 -> 气球道具入背包） */
    private static void testJoy002_exchangeJoyToBalloon() {
        ManagerJoyService joyService = ctx.getBean(ManagerJoyService.class);

        JSONObject params = new JSONObject();
        params.put("userId", MY_USER_ID);
        JSONObject result = joyService.exchangeJoyToBalloon(null, params);

        System.out.println("========== [JOY 002] exchangeJoyToBalloon ==========");
        try {
            System.out.println(result == null ? "null" : result.toJSONString());
        } catch (Exception e) {
            System.out.println("==============================[JOY 002] EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** 003 查看某个好友对我的欢乐值贡献 */
    private static void testJoy003_getFriendJoyContrib() {
        ManagerJoyService joyService = ctx.getBean(ManagerJoyService.class);

        if (FRIEND_USER_ID == null || FRIEND_USER_ID <= 0) {
            System.out.println("========== [JOY 003] SKIP: FRIEND_USER_ID 未设置 ==========");
            return;
        }
        JSONObject params = new JSONObject();
        params.put("userId", MY_USER_ID);
        params.put("friendUserId", FRIEND_USER_ID);
        JSONObject result = joyService.getFriendJoyContrib(null, params);
        System.out.println(result == null ? "null" : result.toJSONString());
        System.out.println("========== ========== [JOY 003] getFriendJoyContrib ==========");

    }

    /**
     * distributeJoy 调试：
     * - 会沿 triggerUserId 的 parentId 链路，给 1~5 代上级入账
     * - 使用 eventId + receiverUserId 做幂等：重复调用不会重复入账
     *
     * 注意：
     * - 如果你用 MY_USER_ID 作为 triggerUserId，那收益会入到 MY_USER_ID 的上级，不会入到 MY_USER_ID 自己
     * - 若你要让 MY_USER_ID 获得收益，用“MY_USER_ID 的下级用户”作为 triggerUserId
     */
    private static void testJoyDistributeJoy() {
        ManagerJoyService joyService = ctx.getBean(ManagerJoyService.class);

        // 测试用户的下级用户ID
        Long triggerUserId = 937226L;
        int itemQuality = 2;
        String sourceType = "FARM_HARVEST";

        String eventId = "DEBUG_" + sourceType + "_" + UUID.randomUUID();

        System.out.println("========== [JOY] distributeJoy ==========");
        try {
            joyService.distributeJoy(triggerUserId, itemQuality, eventId, sourceType);
            System.out.println("[========== ========== ========== JOY] distributeJoy OK. eventId=" + eventId);
        } catch (Exception e) {
            System.out.println("[JOY] distributeJoy EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static final String TEST_GUILD_NAME = "A工会测试";
    // 计划招募人数（用于“质押=人数*单价”的版本；旧代码会忽略该字段）
    private static final Integer TEST_NEED_MEMBER_NUMBER = 20;
    private static Long TEST_GUILD_ID = null;

    /**
     * 查询某个用户最新的一条“待审核/已通过”等指定状态的公会记录 id
     * （如果你的实现只允许一人一会，可直接取第一条；这里取最大的 id 更稳妥）
     */
    private static Long queryLatestGuildId(Long userId, Integer status) {
        com.zywl.app.defaultx.service.GuildService guildService = ctx.getBean(com.zywl.app.defaultx.service.GuildService.class);
        java.util.Map<String, Object> q = new java.util.HashMap<>();
        q.put("userId", userId);
        if (status != null) {
            q.put("status", status);
        }
        java.util.List<com.zywl.app.base.bean.Guild> list = guildService.findByConditions(q);
        if (list == null || list.isEmpty()) {
            return null;
        }
        long max = 0;
        for (com.zywl.app.base.bean.Guild g : list) {
            if (g != null && g.getId() != null && g.getId() > max) {
                max = g.getId();
            }
        }
        return max == 0 ? null : max;
    }

    /**
     * 001：获取公会列表（MANAGER: ManagerGuildService.getGuilds）
     */
    private static void guildGetListTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);
        JSONObject p = new JSONObject();
        p.put("userId", MY_USER_ID);
        System.out.println("========== [GUILD][001 获取公会列表] 测试 ==========");
        try {
            JSONObject r = guildService.getGuilds(fakeSocket, p);
            System.out.println(r == null ? "null" : r.toJSONString());
            System.out.println("[GUILD][001] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][001][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 002：创建公会（MANAGER: ManagerGuildService.createGuild）
     */
    private static void guildCreateTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);

        JSONObject p = new JSONObject();
        p.put("userId", MY_USER_ID);
        p.put("guildName", TEST_GUILD_NAME);
        p.put("needMemberNumber", TEST_NEED_MEMBER_NUMBER); // 新需求字段；旧实现会忽略
        System.out.println("========== [GUILD][002 创建公会] 测试 ==========");
        try {
            JSONObject r = guildService.createGuild(fakeSocket, p);
            System.out.println(r == null ? "null" : r.toJSONString());

            // 兼容：如果 createGuild 不回传 guildId，则从 DB 查一条最新记录作为后续测试用
            Long gid = null;
            if (r != null && r.getLong("guildId") != null) {
                gid = r.getLong("guildId");
            } else {
                // 优先按“审核中 status=2”查；如果查不到，再不带 status 查
                gid = queryLatestGuildId(MY_USER_ID, 2);
                if (gid == null) {
                    gid = queryLatestGuildId(MY_USER_ID, null);
                }
            }
            TEST_GUILD_ID = gid;
            System.out.println("[GUILD][002] guildId = " + TEST_GUILD_ID);
            System.out.println("[GUILD][002] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][002][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 审核通过（非 WS 指令）：ManagerGuildService.passApplyGuild(dataId, userId)
     * 仅当你实现了“创建公会需审核”时使用。
     */
    private static void guildApproveTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);
        System.out.println("========== [GUILD][审核通过] 测试 ==========");
        try {
            if (TEST_GUILD_ID == null) {
                TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, 2);
            }
            if (TEST_GUILD_ID == null) {
                throw new RuntimeException("未找到待审核公会记录（status=2），请先执行 guildCreateTest()");
            }
            guildService.passApplyGuild(TEST_GUILD_ID, MY_USER_ID);
            System.out.println("[GUILD][审核通过] guildId=" + TEST_GUILD_ID + " OK");
        } catch (Exception e) {
            System.out.println("[GUILD][审核通过][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 审核拒绝（非 WS 指令）：ManagerGuildService.refuseApplyGuild(dataId, userId)
     * 仅当你实现了“创建公会需审核”时使用。
     */
    private static void guildRefuseTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);
        System.out.println("========== [GUILD][审核拒绝] 测试 ==========");
        try {
            if (TEST_GUILD_ID == null) {
                TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, 2);
            }
            if (TEST_GUILD_ID == null) {
                throw new RuntimeException("未找到待审核公会记录（status=2），请先执行 guildCreateTest()");
            }
            guildService.refuseApplyGuild(TEST_GUILD_ID, MY_USER_ID);
            System.out.println("[GUILD][审核拒绝] guildId=" + TEST_GUILD_ID + " OK");
        } catch (Exception e) {
            System.out.println("[GUILD][审核拒绝][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 003：公会详情（MANAGER: ManagerGuildService.getGuildInfo）
     */
    private static void guildInfoTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);

        JSONObject p = new JSONObject();
        p.put("userId", MY_USER_ID);
        if (TEST_GUILD_ID == null) {
            TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
        }
        p.put("guildId", TEST_GUILD_ID);
        System.out.println("========== [GUILD][003 公会详情] 测试 ==========");
        try {
            Object r = guildService.getGuildInfo(fakeSocket, p);
            System.out.println(r == null ? "null" : r.toString());
            System.out.println("[GUILD][003] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][003][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 004：添加成员（MANAGER: ManagerGuildService.addGuildMember）
     */
    private static void guildAddMemberTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);

        if (TEST_GUILD_ID == null) {
            TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
        }
        JSONObject p = new JSONObject();
        p.put("guildId", TEST_GUILD_ID);
        p.put("userId", 853859);      // 被邀请加入的成员
        p.put("createUserId", FRIEND_USER_ID);    // 会长/邀请人
        p.put("memberRoleId",4);
        System.out.println("========== [GUILD][004 添加成员] 测试 ==========");
        try {
            JSONObject r = guildService.addGuildMember(fakeSocket, p);
            System.out.println(r == null ? "null" : r.toJSONString());
            System.out.println("[GUILD][004] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][004][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 005：成员列表（MANAGER: ManagerGuildService.myGuild）
     */
    private static void guildMemberListTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);

        if (TEST_GUILD_ID == null) {
            TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
        }
        JSONObject p = new JSONObject();
        p.put("guildId", TEST_GUILD_ID);
        p.put("userId", MY_USER_ID);
        System.out.println("========== [GUILD][005 成员列表] 测试 ==========");
        try {
            JSONObject r = guildService.myGuild(fakeSocket, p);
            System.out.println(r == null ? "null" : r.toJSONString());
            System.out.println("[GUILD][005] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][005][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 为了测试 007（发放佣金/领取），需要先人为给某个成员塞一点 profitBalance。
     * 这里直接调用 DEFAULT 的 GuildMemberService.addProfitBalance(userId, amount)
     */
    private static void guildAddProfitBalanceForTest() {
        com.zywl.app.defaultx.service.GuildMemberService memberService = ctx.getBean(com.zywl.app.defaultx.service.GuildMemberService.class);
        System.out.println("========== [GUILD][准备数据] 给成员增加 profitBalance ==========");
        try {
            memberService.addProfitBalance(FRIEND_USER_ID, new java.math.BigDecimal("100"));
            System.out.println("[GUILD][准备数据] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][准备数据][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 007：发放佣金（MANAGER: ManagerGuildService.receive）
     * - 注意：当前实现是“把 member.profitBalance 转给 operatorUserId（操作人/会长）”
     */
    private static void guildReceiveTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);

        if (TEST_GUILD_ID == null) {
            TEST_GUILD_ID = queryLatestGuildId(MY_USER_ID, null);
        }
        JSONObject p = new JSONObject();
        p.put("guildId", TEST_GUILD_ID);
        p.put("userId", FRIEND_USER_ID);       // 被发放/被清零 profitBalance 的成员
        p.put("operatorUserId", MY_USER_ID);   // 实际入账的人（会长）
        System.out.println("========== [GUILD][007 发放佣金] 测试 ==========");
        try {
            Object r = guildService.receive(fakeSocket, p);
            System.out.println(r == null ? "null" : r.toString());
            System.out.println("[GUILD][007] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][007][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 009：修改比例（MANAGER: ManagerGuildService.updateRate）
     */
    private static void guildUpdateRateTest() {
        ManagerGuildService guildService = ctx.getBean(ManagerGuildService.class);

        JSONObject p = new JSONObject();
        p.put("userId", FRIEND_USER_ID);
        p.put("rate", "8"); // 这里传字符串也可以，被 getBigDecimal 解析
        System.out.println("========== [GUILD][009 修改比例] 测试 ==========");
        try {
            JSONObject r = guildService.updateRate(fakeSocket, p);
            System.out.println(r == null ? "null" : r.toJSONString());
            System.out.println("[GUILD][009] OK");
        } catch (Exception e) {
            System.out.println("[GUILD][009][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //养宠物信息
    private static void getPetInfoTest() {
        ManagerGamePetService getPetService = ctx.getBean(ManagerGamePetService.class);
        JSONObject params = new JSONObject();
        params.put("userId", MY_USER_ID);
        System.out.println("========== [获取养宠信息] 测试 ==========");
        try {
            JSONObject result = getPetService.getPetInfo(null, params);
            System.out.println(result == null ? "null" : result.toJSONString());
            System.out.println("[========== ========== ========== 获取养宠信息 OK" );
        } catch (Exception e) {
            System.out.println("[获取养宠信息][=====================异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //购买宠物
    private static void getPayPetTest() {
        ManagerGamePetService getPetService = ctx.getBean(ManagerGamePetService.class);
        JSONObject params = new JSONObject();
        params.put("userId", MY_USER_ID);
        params.put("buyCount", 2);
        System.out.println("========== [购买养宠] 测试 ==========");
        try {
            JSONObject result = getPetService.buyLion(null, params);
            System.out.println(result == null ? "null" : result.toJSONString());
            System.out.println("[========== ========== ========== 购买养宠 OK" );
        } catch (Exception e) {
            System.out.println("[购买养宠][=====================异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //喂养宠物
    private static void getFeedLionTest() {
        ManagerGamePetService getPetService = ctx.getBean(ManagerGamePetService.class);
        JSONObject params = new JSONObject();
        params.put("userId", MY_USER_ID);
        params.put("feedTimes", 1);
        System.out.println("========== [喂养宠物] 测试 ==========");
        try {
            JSONObject result = getPetService.feedLion(null, params);
            System.out.println(result == null ? "null" : result.toJSONString());
            System.out.println("[========== ========== ========== 喂养宠物 OK" );
        } catch (Exception e) {
            System.out.println("[喂养宠物][=====================异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }


    //领取宠物收益信息
    private static void getClaimYieldTest() {
        ManagerGamePetService getPetService = ctx.getBean(ManagerGamePetService.class);
        JSONObject params = new JSONObject();
        params.put("userId", MY_USER_ID);
        System.out.println("========== [领取养宠产出] 测试 ==========");
        try {
            JSONObject result = getPetService.claimYield(null, params);
            System.out.println(result == null ? "null" : result.toJSONString());
            System.out.println("[========== ========== ========== 领取养宠产出 OK" );
        } catch (Exception e) {
            System.out.println("[领取养宠产出][=====================异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void unlockLv3Test() {
        ManagerGamePetService svc = ctx.getBean(ManagerGamePetService.class);
        JSONObject p = new JSONObject();
        p.put("userId", 928765L);     // 你的1代上级
        p.put("unlockLevel", 3);
        System.out.println("========== [解锁3代分润] 测试 ==========");
        try {
            JSONObject r = svc.unlockDividendLevel(null, p);
            System.out.println(r == null ? "null" : r.toJSONString());
            System.out.println("[========== 解锁3代分润 OK");
        } catch (Exception e) {
            System.out.println("[解锁3代分润][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 排行榜测试
    private static void getTopTest() {
        ManagerGameBaseService svc = ctx.getBean(ManagerGameBaseService.class);
        JSONObject p = new JSONObject();
        p.put("userId", MY_USER_ID);
        // 1邀请拉新/2VIP/7资产消耗
        p.put("type", 7);
        p.put("capitalType", 0);
        System.out.println("========== [排行榜] 测试 ==========");
        try {
            JSONObject r = svc.getTop(null, p);
            System.out.println(r == null ? "null" : r.toJSONString());
            System.out.println("[========== 排行榜 OK");
        } catch (Exception e) {
            System.out.println("[排行榜][异常]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // ===================== 公会 Guild 模块 =====================
        // 一键跑完整流程：
        // guildFullFlowTest();

        //种子合成
        // synInTest();;

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
        //myFarmInfoInTest();

        //用户土地种植
        //plantInTest();

        //用户种地收割
        //harvestInTest();

        //用户解锁/购买土地 测试返回
        //unlockLandInTest();

        //商城信息
        //shopInfoInTest();

        //商城购买
        //buyInTest();

        //查询我的欢乐值与可兑气球数量
        //testJoy001_getMyJoyInfo();

        //兑换气球
        //testJoy002_exchangeJoyToBalloon();

        // 查看好友对我的贡献值
        //testJoy003_getFriendJoyContrib();


        // 分配欢乐值
        //testJoyDistributeJoy();

        //工会列表
        guildGetListTest();

        //创建工会
        //guildCreateTest();
        //邀请成员
        //guildAddMemberTest();

        //工会详情
        //testGuild();

        //获取购买宠物信息
        //getPetInfoTest();

        //购买宠物
        //getPayPetTest();

        //喂养宠物
        //getFeedLionTest();

        //领取宠物产出
        //getClaimYieldTest();

        //解锁3代分润
        //unlockLv3Test();

        //排行榜测试‘’
        //getTopTest();
    }





}



