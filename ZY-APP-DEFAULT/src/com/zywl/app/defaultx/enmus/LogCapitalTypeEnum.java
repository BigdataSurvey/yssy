package com.zywl.app.defaultx.enmus;

/**
 * 用户资产变动类型
 *
 * @author DOE
 */
public enum LogCapitalTypeEnum {

    pirze_draw("抽奖扣除", 1),
    demo("试玩", 2),
    friend_transfer("好友转赠", 3),
    transfer_friend("邮件支援好友", 4),
    sign_in("签到", 5),
    sell("易市出售", 6),
    buy("易市购买", 7),
    cancel_askbuy("取消求购", 8),
    askbuy("发布求购", 9),
    askbuy_sucess("扣除求购预付", 10),
    user_cash("余额提现扣除", 11),
    cash_succrss("提现成功", 12),
    cash_fail("提现失败返还", 13),
    balance_convert("货币转换", 14),
    book("藏书阁领取", 15),
    receive_income("传承奖励", 16),
    play_game("试玩获得", 17),
    mail("邮件领取", 18),
    rollback("资产回退", 19),
    game_bet("参加倩女幽魂", 20),
    game_bet_win("倩女幽魂获得", 21),
    to_lottery("世界聊天", 22),
    lottery_to("秘境转出", 23),
    lottery_to_forzen("秘境转出冻结", 26),
    task_forzen("秘境转入冻结", 24),
    task_success("秘境转入成功", 25),
    exchange("兑换商品", 26),
    sign("补签", 27),
    sign_reward("签到奖励", 28),
    achievement_reward("新手任务奖励", 29),
    send_mail("发送贺卡手续费", 30),
    up_lv("购买经验扣除", 31),
    attack_boss("攻击boss", 32),
    offline_money("领取离线收益", 33),
    buy_pl("购买疲劳值", 34),
    skill("释放技能", 35),
    receive_total_sign_reward("累签奖励", 36),
    refine("炼丹", 37),
    refine_speed("炼丹加速", 38),
    get_pet("合成/升级 灵兽", 39),
    get_buff("合成/升级 底座", 40),
    sell_sys("卖出道具", 41),
    study_skill("领悟/升级 技能", 42),
    shopping("游戏商城", 43),
    receive_invite("活动奖励", 44),
    guild("公会质押",45),
    guild_receive("公会发放",46),
    buy_coin("购买铜钱",47),
    daily_task("每日任务",48),
    friend_play_game("好友试玩收益",49),
    c3_convert("灵石兑换仙晶",50),
    c3_to_rmb("仙晶兑换余额",51),
    cash_channel_income("提取渠道收益",52),
    game_bet_food("参加我是酒仙",53),
    game_bet_win_food("我是酒仙获得",54),
    buy_user_no("购买靓号",55),
    update_idCard("修改实名信息",56),
    ios_test("充值获得",57),
    cancel_bet("参与失败返还",58),
    food_cancel_bet("酒仙返还", 59),
    game_bet_ns("攻击年兽", 60),
    game_win_ns("攻击年兽获得", 61),
    game_kill_ns("年兽最后一击", 66),
    open_box("开启年兽宝箱",62),
    sell_sys2("背包卖出", 63),
    sell_sys3("背包卖出", 64),
    pirze_draw_reward("奖池获得", 65),
    game_bet_nh("参加趋亭避雨游戏", 76),
    game_bet_win_nh("趋亭避雨游戏获得", 67),
    add_magic("投入魔晶",70),
    receive_magic("魔幻阁领取",71),

    dz_dk_lq_reward("打坐获得", 68),
    dz_kc_reward("打坐报名",69),
    checkout_magic("魔幻阁领取",71),
    magic_convert("魔晶幻化",72),
    get_magic_convert("魔晶幻化获得",73),
    send_red_package("发红包",74),

    lq_red_package("抢红包",75),
    game_bet_dts2("游玩倩女幽魂", 77),
    game_bet_win_dts2("倩女幽魂奖励", 78),
    dts_rank_rebate("游园奖励",79),
    cave_prize_draw("宝箱抽奖",80),
    game_bet_sg("参与签与签寻", 81),
    game_bet_win_sg("签与签寻获得", 82),
    create_jsxm_gate("创建仙门扣除",85),
    contribution_jsxm_zj("仙门捐献扣除",86),
    buy_jsxm_image("仙门头像扣除",87),
    update_jsxm_name("修改仙门名称",88),
    jsxm_xm_reward("仙脉产出分配",89),
    join_ancient("副本扫荡",90),
    ancient_get_c2("遗迹获得灵石",91),
    ancient_get_c5("遗迹获得魔晶",93),
    refresh("刷新天赋",92),
    sell_sys_magic("背包卖出(材料)", 94),
    add_flag("补充阵旗",95),
    game_bet_win_bt("试炼宝塔获得", 96),
    game_bet_bt("参与试炼宝塔", 97),
    receive_cave("洞府领取",98),
    exchange_ticket("兑换抽奖券",99),
    shopping_magic("商城购买道具(魔晶)", 100),


    buy_pvp_item("购买挑战令",1001),
    open_mine("开通书境",1002),
    receive_pop("领取人气宝箱奖励",1003),
    send_greeting_card("赠送贺卡",1004),
    add_reward("奖励获得",1005),
    update_coin("铜钱变动",1006),
    receive_achievement("成就奖励",1006),
    from_item("使用道具",1007),
    dispatch("悬赏任务",1008),
    wander("云游天下",1009),
    dice("捐赠获得",1010),
    buy_gift("购买每日礼包",1011),
    buy_vip("开通会员",1012),
    afk("快速作战",1013),
    game_bet_escort("参与护送游戏",1014),
    game_escort_win("护送游戏获得",1015),
    buy_pass("购买通行证",1016),
    friend_buy("友情商店购买",1017),
    pet_buy("战马商城购买",1018),
    receive_anima("领取友情值", 1019),
    game_bet_cards("参与虚妄迷局",1020),

    game_cards_win("虚妄迷局获得",1021),

    bug_role_gift("购买角色",1022),
    donate_item("捐赠道具",1023),
    VIP_RECEIVE("vip领取",1024),
    yyb_winning("打怪中奖",1026),
    SHOPPING_GET("商城获得",1025)
    ;

    private String name;

    private int value;

    private LogCapitalTypeEnum(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public void setHour(int value) {
        this.value = value;
    }

    public static LogCapitalTypeEnum getEm(int emValue){
        LogCapitalTypeEnum[] values = LogCapitalTypeEnum.values();
        for (LogCapitalTypeEnum value : values) {
            if (value.getValue()==emValue){
                return value;
            }
        }
        return null;
    }
}
