package com.zywl.app.base.util;

import java.util.*;

public class GameNicknameGenerator {
    // 单例随机数（保证随机性和性能）
    private static final Random RANDOM = new Random();

    // ===================== 多风格核心词库（6大风格，大幅扩充，贴合LOL）=====================
    // 【前缀库】1-2字符（纯中文/数字+字母/纯字母，覆盖所有风格，适配拼接）
    private static final List<String> PREFIX_LIST = new ArrayList<String>() {{
        // 原有基础：霸气竞技+休闲百搭
        add("寒风");add("烈阳");add("星空");add("月影");add("白愈");add("暴躁");add("温柔");add("狂野");
        add("极速");add("暗夜");add("黎明");add("电竞");add("网吧");add("孤勇");add("王牌");add("上校");
        add("少女");add("少年");add("余生");add("5S");add("66");add("8K");add("9M");add("Ou");add("Xtn");
        add("Bu");add("Yx");add("Zl");add("Qn");add("Sj");add("1V");add("2G");add("3A");add("7D");
        add("A");add("B");add("X");add("Y");add("Z");add("Q");add("W");add("M");
        // 新增5风格前缀
        add("摆烂");add("躺平");add("摆烂");add("摆烂");add("软萌");add("奶萌");add("甜妹");add("奶盖");
        add("野区");add("中路");add("下路");add("上单");add("辅神");add("温酒");add("执笔");add("听风");
        add("观月");add("沙雕");add("搞笑");add("憨憨");add("呆毛");add("摆烂");add("00");add("5G");
        add("Nn");add("Ln");add("Rn");add("甜弟");add("萌系");add("峡谷");add("王者");add("青铜");
        add("白银");add("黄金");add("钻石");add("星耀");add("墨染");add("枕雪");add("寻梅");add("搞怪");
        add("逗比");add("菜鸡");add("坑神");add("混子");add("Fc");add("Gc");add("Hh");add("6Y");
    }};

    // 【主体库】2-3字符（核心记忆点，6大风格全覆盖，适配4-6字符拼接）
    private static final List<String> MAIN_LIST = new ArrayList<String>() {{
        // 原有基础：霸气竞技+休闲百搭
        add("狼王");add("战神");add("王者");add("狂飙");add("千斩");add("不败");add("无双");add("法王");
        add("野王");add("兵王");add("狙神");add("剑神");add("拳皇");add("小王");add("小队");add("大千");
        add("小仙");add("小魔");add("团子");add("奶盖");add("憨憨");add("呆呆");add("萌萌");add("希望星");
        add("不眠夜");add("浮生若");add("云中月");add("风里行");add("梦里寻");add("开黑团");add("组队侠");
        add("路人王");add("队友宝");
        // 新增1：摆烂躺平风
        add("摆烂王");add("躺平侠");add("混分怪");add("摸鱼仔");add("摆烂魂");add("躺平魂");add("划水怪");
        add("摆烂帝");add("躺平仙");add("摸鱼神");
        // 新增2：软萌可爱风
        add("小团子");add("奶盖酱");add("小奶猫");add("小奶狗");add("软糖妹");add("软糖弟");add("萌团子");
        add("呆毛君");add("憨憨酱");add("奶萌仔");add("小泡芙");add("小蛋挞");
        // 新增3：热血电竞风（贴合LOL峡谷/位置）
        add("守塔神");add("越塔王");add("拿龙帝");add("抢龙神");add("推塔怪");add("支援侠");add("补刀神");
        add("游走帝");add("Carry王");add("收割神");add("反野王");add("控龙神");add("带飞哥");add("带飞姐");
        // 新增4：文艺意境风
        add("揽星河");add("赴山海");add("听风吟");add("观雨落");add("枕星河");add("渡余生");add("寻清欢");
        add("赴清欢");add("书半生");add("画余生");add("温酒行");add("执笔书");
        // 新增5：搞怪趣味风
        add("菜鸡仔");add("坑神弟");add("逗比哥");add("沙雕姐");add("憨憨哥");add("呆毛姐");add("下饭神");
        add("送头怪");add("空大帝");add("闪现撞墙");add("技能空了");add("瞎溜达");
    }};

    // 【后缀库】0-1字符（轻修饰，不突兀，覆盖所有风格，空字符占比高保证拼接灵活）
    private static final List<String> SUFFIX_LIST = new ArrayList<String>() {{
        add("博");add("梦");add("轩");add("儿");add("子");add("吖");add("呢");add("兮");add("也");add("哥");
        add("姐");add("弟");add("妹");add("酱");add("爷");add("仙");add("神");add("帝");add("侠");add("仔");
        add("君");add("宝");add("喵");add("汪");add("");add("");add("");add("");add("");add("");
        add("");add("");add("");add("");add("");add("");add("");add("");add("");add("");
    }};

    /**
     * 生成单个LOL风格昵称（核心方法，严格4-6字符）
     * @return 4-6字符的多风格LOL昵称
     */
    public static String generateLOLNickname() {
        String nickname;
        // 循环拼接，直到满足4-6字符（过滤极端组合，保证符合游戏规则）
        do {
            String prefix = PREFIX_LIST.get(RANDOM.nextInt(PREFIX_LIST.size()));
            String main = MAIN_LIST.get(RANDOM.nextInt(MAIN_LIST.size()));
            String suffix = SUFFIX_LIST.get(RANDOM.nextInt(SUFFIX_LIST.size()));
            nickname = new StringBuilder().append(prefix).append(main).append(suffix).toString();
        } while (nickname.length() < 4 || nickname.length() > 6);
        return nickname;
    }

    /**
     * 批量生成LOL昵称（自动去重+保留顺序，性能最优）
     * @param count 生成数量（>0）
     * @return 去重后的昵称列表
     * @throws IllegalArgumentException 非法参数异常
     */
    public static List<String> generateBatchLOLNickname(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("生成数量必须大于0，当前传入：" + count);
        }
        // LinkedHashSet：自动去重 + 保留添加顺序，哈希表实现，判断重复效率O(1)
        Set<String> nicknameSet = new LinkedHashSet<>();
        while (nicknameSet.size() < count) {
            nicknameSet.add(generateLOLNickname());
        }
        // 转List返回，符合调用方常规使用习惯
        return new ArrayList<>(nicknameSet);
    }

    // 测试主方法：直接运行看效果，6大风格随机生成
    public static void main(String[] args) {
        // 生成5个单个昵称，测试风格多样性
        System.out.println("【随机5个单个昵称】：");
        for (int i = 0; i < 5; i++) {
            System.out.println((i+1) + ". " + generateLOLNickname());
        }

        // 批量生成30个昵称（自动去重，6大风格全覆盖）
        System.out.println("\n【批量30个多风格LOL昵称】：");
        List<String> batchNicks = generateBatchLOLNickname(30);
        for (int i = 0; i < batchNicks.size(); i++) {
            System.out.printf("%d. %s%n", i+1, batchNicks.get(i));
        }
    }
}