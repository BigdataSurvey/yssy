package com.zywl.app.server.context;


import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.bean.card.DicSkill;
import com.zywl.app.base.bean.vo.card.GamingCard;
import com.zywl.app.base.bean.vo.card.GamingCardAttacker;
import com.zywl.app.base.bean.vo.card.GamingCardTarget;
import com.zywl.app.defaultx.enmus.BattleEventEnum;
import com.zywl.app.defaultx.enmus.CardBuffEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/*
  数值公式计算
 */


public class FormulaContext {


    public static final Map<String, DicSkill> SKILL_INFO = new ConcurrentHashMap<>();
    static final Random RANDOM = new Random();

    public static DicSkill getSkill(Long skillId) {
        if (skillId == null) {
            return null;
        }
        if (SKILL_INFO.containsKey(skillId.toString())) {
            return SKILL_INFO.get(skillId.toString());
        } else {
            return null;
        }
    }

    //设暴击数值为 n，假设基础暴击概率为 0.05（5%）
    //0.05*(1+暴击率/5)/(1+抗暴/100)
    public static boolean isChc(double n, double defChc) {
        double a = 0.7 * (1 - defChc / (n + defChc));
        double i = RANDOM.nextDouble();
        return a > i;
    }


    //设命中数值为 M，闪避数值为 E，基础命中率为 P（比如可以设为 0.7，即初始有 70% 的命中率），最终命中率为 R。
    //R = P × (1 + M/1000) / (1 + E/1000)。
    //这个公式的含义是：
    //基础命中率 P 乘以一个与命中数值 M 相关的因子（1 + M/1000），表示命中数值对命中率的提升作用，命中数值越高，这个因子越大，命中率提升越多，但增速逐渐减缓（因为除以了一个较大的数）。
    //同时除以一个与闪避数值 E 相关的因子（1 + E/1000），表示闪避数值对命中率的降低作用，闪避数值越高，这个因子越大，命中率降低越多，但同样增速逐渐减缓。
    public static boolean isHtt(double htt, double dodge) {
        double r = 1 * (1 - dodge / (htt + dodge));
        double i = RANDOM.nextDouble();
        return r > i;
    }


    //设原始反击值为 f，最终反击概率为 p。
    //p = 0.5×f/(100 + f)。
    public static boolean isAfb(double afb) {
        double p = 0.5 * afb / (100 + afb);
        double i = RANDOM.nextDouble();
        return p > i;

    }

    //设原始连击值为 c，最终连击概率为 p。
    //p = 0.6×c/(100 + c)。
    //这个公式中，随着连击值 c 的增加，连击概率 p 会逐渐增大，但增速逐渐减缓。当连击值 c 趋于无穷大时，连击概率 p 会趋近于最高概率 60。
    public static boolean isCombo(double combo) {
        double p = 0.5 * combo / (100 + combo);
        double i = RANDOM.nextDouble();
        return p > i;
    }


    /**
     * 1.本次攻击是否触发技能 1是 2不是
     * 1-1,。根据技能走单独的方法
     * 2-1.攻击是否miss
     * 2-2.本次攻击是否触发连击，判断本次攻击对方扣血多少
     * 2-3。本次攻击计算吸血多少
     * 2-4.本次攻击是否被对方反击，自己扣血多少
     */


    public static JSONArray getCardsInfo(List<GamingCardAttacker> playerCards, List<GamingCardTarget> otherCards) {
        JSONArray cardsInfo = new JSONArray();
        for (GamingCardAttacker playerCard : playerCards) {
            JSONObject o = new JSONObject();
            o.put("index", playerCard.getIndex());
            o.put("maxHp", playerCard.getHp());
            o.put("maxMp", 100);
            cardsInfo.add(o);
        }
        for (GamingCardTarget playerCard : otherCards) {
            JSONObject o = new JSONObject();
            o.put("index", playerCard.getIndex());
            o.put("maxHp", playerCard.getHp());
            o.put("maxMp", 100);
            cardsInfo.add(o);
        }
        return cardsInfo;
    }


    public static JSONObject updateCardStatusByBuff(List<GamingCardAttacker> playerCards, List<GamingCardTarget> otherCards) {
        JSONObject returnInfo = new JSONObject();
        JSONArray zhiliaoInfo = new JSONArray();
        JSONArray zhongduInfo = new JSONArray();
        JSONArray liuxueInfo = new JSONArray();
        JSONArray xuanyunInfo = new JSONArray();
        JSONArray mianshangInfo = new JSONArray();
        JSONArray gongjiInfo = new JSONArray();
        JSONArray zhuoshaoInfo = new JSONArray();
        for (GamingCard card : playerCards) {
            Set<Integer> buffs = card.getBuff();
            JSONObject buffInfo = card.getBuffInfo();
            Iterator<Integer> iterator = buffs.iterator();
            while (iterator.hasNext()) {
                int buff = iterator.next();
                if (buff == CardBuffEnum.zhiLiao.getValue() && buffInfo.containsKey("zhiliao")) {
                    JSONObject zhiliao = buffInfo.getJSONObject("zhiliao");
                    int roundsNumber = zhiliao.containsKey("roundsNumber") ? zhiliao.getInteger("roundsNumber") : 2;
                    int hp = zhiliao.containsKey("hp") ? zhiliao.getIntValue("hp") : 100;
                    card.setHp(card.getHp() + hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    zhiliao.put("hp", hp);
                    zhiliao.put("roundsNumber", roundsNumber);
                    buffInfo.put("zhiliao", zhiliao);
                    if (roundsNumber == 0) {
                        buffInfo.remove("zhiliao");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", hp);
                    zhiliaoInfo.add(info);
                }
                if (buff == CardBuffEnum.zhongDu.getValue() && buffInfo.containsKey("zhongdu")) {
                    JSONObject zhongDu = buffInfo.getJSONObject("zhongdu");
                    int roundsNumber = zhongDu.containsKey("roundsNumber") ? zhongDu.getInteger("roundsNumber") : 2;
                    int hp = zhongDu.containsKey("hp") ? zhongDu.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    zhongDu.put("roundsNumber", roundsNumber);
                    buffInfo.put("zhongdu", zhongDu);
                    if (roundsNumber == 0) {
                        buffInfo.remove("zhongdu");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    zhongduInfo.add(info);
                }
                if (buff == CardBuffEnum.liuXue.getValue() && buffInfo.containsKey("liuxue")) {
                    JSONObject liuxue = buffInfo.getJSONObject("liuxue");
                    int roundsNumber = liuxue.containsKey("roundsNumber") ? liuxue.getInteger("roundsNumber") : 2;
                    int hp = liuxue.containsKey("hp") ? liuxue.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    liuxue.put("roundsNumber", roundsNumber);
                    buffInfo.put("liuxue", liuxue);
                    if (roundsNumber == 0) {
                        buffInfo.remove("liuxue");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    liuxueInfo.add(info);
                }
                if (buff == CardBuffEnum.xuanYun.getValue() && buffInfo.containsKey("xuanyun")) {
                    JSONObject xuanyun = buffInfo.getJSONObject("xuanyun");
                    int roundsNumber = xuanyun.containsKey("roundsNumber") ? xuanyun.getInteger("roundsNumber") : 2;
                    xuanyun.put("roundsNumber", roundsNumber);
                    buffInfo.put("xuanyun", xuanyun);
                    if (roundsNumber == 0) {
                        buffInfo.remove("xuanyun");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", 0);
                    xuanyunInfo.add(info);
                }
                if (buff == CardBuffEnum.zhuoShao.getValue() && buffInfo.containsKey("zhuoshao")) {
                    JSONObject zhuoshao = buffInfo.getJSONObject("zhuoshao");
                    int roundsNumber = zhuoshao.containsKey("roundsNumber") ? zhuoshao.getInteger("roundsNumber") : 2;
                    int hp = zhuoshao.containsKey("hp") ? zhuoshao.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    zhuoshao.put("roundsNumber", roundsNumber);
                    buffInfo.put("zhuoshao", zhuoshao);
                    if (roundsNumber == 0) {
                        buffInfo.remove("zhuoshao");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    zhuoshaoInfo.add(info);
                }
                if (buff == CardBuffEnum.mianShang.getValue() && buffInfo.containsKey("mianshang")) {
                    JSONObject mianshang = buffInfo.getJSONObject("mianshang");
                    int roundsNumber = mianshang.containsKey("roundsNumber") ? mianshang.getInteger("roundsNumber") : 2;
                    int hp = mianshang.containsKey("hp") ? mianshang.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    mianshang.put("roundsNumber", roundsNumber);
                    buffInfo.put("mianshang", mianshang);
                    if (roundsNumber == 0) {
                        buffInfo.remove("mianshang");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    mianshangInfo.add(info);
                }
                if (buff == CardBuffEnum.gongJi.getValue() && buffInfo.containsKey("gongji")) {
                    JSONObject gongji = buffInfo.getJSONObject("gongji");
                    int roundsNumber = gongji.containsKey("roundsNumber") ? gongji.getInteger("roundsNumber") : 2;
                    int hp = gongji.containsKey("hp") ? gongji.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    gongji.put("roundsNumber", roundsNumber);
                    buffInfo.put("gongji", gongji);
                    if (roundsNumber == 0) {
                        buffInfo.remove("gongji");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    gongjiInfo.add(info);
                }
            }
        }

        for (GamingCard card : otherCards) {
            Set<Integer> buffs = card.getBuff();
            JSONObject buffInfo = card.getBuffInfo();
            Iterator<Integer> iterator = buffs.iterator();
            while (iterator.hasNext()) {
                int buff = iterator.next();
                if (buff == CardBuffEnum.zhiLiao.getValue() && buffInfo.containsKey("zhiliao")) {
                    JSONObject zhiliao = buffInfo.getJSONObject("zhiliao");
                    int roundsNumber = zhiliao.containsKey("roundsNumber") ? zhiliao.getInteger("roundsNumber") : 2;
                    int hp = zhiliao.containsKey("hp") ? zhiliao.getIntValue("hp") : 100;
                    card.setHp(card.getHp() + hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    zhiliao.put("hp", hp);
                    zhiliao.put("roundsNumber", roundsNumber);
                    buffInfo.put("zhiliao", zhiliao);
                    if (roundsNumber == 0) {
                        buffInfo.remove("zhiliao");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", hp);
                    zhiliaoInfo.add(info);
                }
                if (buff == CardBuffEnum.zhongDu.getValue() && buffInfo.containsKey("zhongdu")) {
                    JSONObject zhongDu = buffInfo.getJSONObject("zhongdu");
                    int roundsNumber = zhongDu.containsKey("roundsNumber") ? zhongDu.getInteger("roundsNumber") : 2;
                    int hp = zhongDu.containsKey("hp") ? zhongDu.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    zhongDu.put("roundsNumber", roundsNumber);
                    buffInfo.put("zhongdu", zhongDu);
                    if (roundsNumber == 0) {
                        buffInfo.remove("zhongdu");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    zhongduInfo.add(info);
                }
                if (buff == CardBuffEnum.liuXue.getValue() && buffInfo.containsKey("liuxue")) {
                    JSONObject liuxue = buffInfo.getJSONObject("liuxue");
                    int roundsNumber = liuxue.containsKey("roundsNumber") ? liuxue.getInteger("roundsNumber") : 2;
                    int hp = liuxue.containsKey("hp") ? liuxue.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    liuxue.put("roundsNumber", roundsNumber);
                    buffInfo.put("liuxue", liuxue);
                    if (roundsNumber == 0) {
                        buffInfo.remove("liuxue");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    liuxueInfo.add(info);
                }
                if (buff == CardBuffEnum.xuanYun.getValue() && buffInfo.containsKey("xuanyun")) {
                    JSONObject xuanyun = buffInfo.getJSONObject("xuanyun");
                    int roundsNumber = xuanyun.containsKey("roundsNumber") ? xuanyun.getInteger("roundsNumber") : 2;
                    xuanyun.put("roundsNumber", roundsNumber);
                    buffInfo.put("xuanyun", xuanyun);
                    if (roundsNumber == 0) {
                        buffInfo.remove("xuanyun");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", 0);
                    xuanyunInfo.add(info);
                }
                if (buff == CardBuffEnum.zhuoShao.getValue() && buffInfo.containsKey("zhuoshao")) {
                    JSONObject zhuoshao = buffInfo.getJSONObject("zhuoshao");
                    int roundsNumber = zhuoshao.containsKey("roundsNumber") ? zhuoshao.getInteger("roundsNumber") : 2;
                    int hp = zhuoshao.containsKey("hp") ? zhuoshao.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    zhuoshao.put("roundsNumber", roundsNumber);
                    buffInfo.put("zhuoshao", zhuoshao);
                    if (roundsNumber == 0) {
                        buffInfo.remove("zhuoshao");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    zhuoshaoInfo.add(info);
                }
                if (buff == CardBuffEnum.mianShang.getValue() && buffInfo.containsKey("mianshang")) {
                    JSONObject mianshang = buffInfo.getJSONObject("mianshang");
                    int roundsNumber = mianshang.containsKey("roundsNumber") ? mianshang.getInteger("roundsNumber") : 2;
                    int hp = mianshang.containsKey("hp") ? mianshang.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    mianshang.put("roundsNumber", roundsNumber);
                    buffInfo.put("mianshang", mianshang);
                    if (roundsNumber == 0) {
                        buffInfo.remove("mianshang");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    mianshangInfo.add(info);
                }
                if (buff == CardBuffEnum.gongJi.getValue() && buffInfo.containsKey("gongji")) {
                    JSONObject gongji = buffInfo.getJSONObject("gongji");
                    int roundsNumber = gongji.containsKey("roundsNumber") ? gongji.getInteger("roundsNumber") : 2;
                    int hp = gongji.containsKey("hp") ? gongji.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    gongji.put("roundsNumber", roundsNumber);
                    buffInfo.put("gongji", gongji);
                    if (roundsNumber == 0) {
                        buffInfo.remove("gongji");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    gongjiInfo.add(info);
                }
            }
        }

        returnInfo.put("zhiliao", zhiliaoInfo);
        returnInfo.put("liuxue", liuxueInfo);
        returnInfo.put("zhuoshao", zhuoshaoInfo);
        returnInfo.put("zhongdu", zhongduInfo);
        returnInfo.put("xuanyun", xuanyunInfo);
        returnInfo.put("mianshang", mianshangInfo);
        returnInfo.put("gongji", gongjiInfo);
        return returnInfo;
    }


    public static void updateCardXuanYunBuff(List<GamingCardAttacker> playerCards, List<GamingCardTarget> otherCards) {
        JSONArray xuanyunInfo = new JSONArray();
        JSONArray mianshangInfo = new JSONArray();
        JSONArray gongjiInfo = new JSONArray();
        for (GamingCard card : playerCards) {
            Set<Integer> buffs = card.getBuff();
            JSONObject buffInfo = card.getBuffInfo();
            Iterator<Integer> iterator = buffs.iterator();
            while (iterator.hasNext()) {
                int buff = iterator.next();
                if (buff == CardBuffEnum.xuanYun.getValue() && buffInfo.containsKey("xuanyun")) {
                    JSONObject xuanyun = buffInfo.getJSONObject("xuanyun");
                    int roundsNumber = xuanyun.containsKey("roundsNumber") ? xuanyun.getInteger("roundsNumber") : 2;
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    xuanyun.put("roundsNumber", roundsNumber);
                    buffInfo.put("xuanyun", xuanyun);
                    if (roundsNumber == 0) {
                        buffInfo.remove("xuanyun");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", 0);
                    xuanyunInfo.add(info);
                }
                if (buff == CardBuffEnum.mianShang.getValue() && buffInfo.containsKey("mianshang")) {
                    JSONObject mianshang = buffInfo.getJSONObject("mianshang");
                    int roundsNumber = mianshang.containsKey("roundsNumber") ? mianshang.getInteger("roundsNumber") : 2;
                    int hp = mianshang.containsKey("hp") ? mianshang.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    mianshang.put("roundsNumber", roundsNumber);
                    buffInfo.put("mianshang", mianshang);
                    if (roundsNumber == 0) {
                        buffInfo.remove("mianshang");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    mianshangInfo.add(info);
                }
                if (buff == CardBuffEnum.gongJi.getValue() && buffInfo.containsKey("gongji")) {
                    JSONObject gongji = buffInfo.getJSONObject("gongji");
                    int roundsNumber = gongji.containsKey("roundsNumber") ? gongji.getInteger("roundsNumber") : 2;
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    gongji.put("roundsNumber", roundsNumber);
                    buffInfo.put("gongji", gongji);
                    if (roundsNumber == 0) {
                        buffInfo.remove("gongji");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", 0);
                    gongjiInfo.add(info);
                }
            }
        }

        for (GamingCard card : otherCards) {
            Set<Integer> buffs = card.getBuff();
            JSONObject buffInfo = card.getBuffInfo();
            Iterator<Integer> iterator = buffs.iterator();
            while (iterator.hasNext()) {
                int buff = iterator.next();
                if (buff == CardBuffEnum.xuanYun.getValue() && buffInfo.containsKey("xuanyun")) {
                    JSONObject xuanyun = buffInfo.getJSONObject("xuanyun");
                    int roundsNumber = xuanyun.containsKey("roundsNumber") ? xuanyun.getInteger("roundsNumber") : 2;
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    xuanyun.put("roundsNumber", roundsNumber);
                    buffInfo.put("xuanyun", xuanyun);
                    if (roundsNumber == 0) {
                        buffInfo.remove("xuanyun");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", 0);
                    xuanyunInfo.add(info);
                }
                if (buff == CardBuffEnum.mianShang.getValue() && buffInfo.containsKey("mianshang")) {
                    JSONObject mianshang = buffInfo.getJSONObject("mianshang");
                    int roundsNumber = mianshang.containsKey("roundsNumber") ? mianshang.getInteger("roundsNumber") : 2;
                    int hp = mianshang.containsKey("hp") ? mianshang.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    mianshang.put("roundsNumber", roundsNumber);
                    buffInfo.put("mianshang", mianshang);
                    if (roundsNumber == 0) {
                        buffInfo.remove("mianshang");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    mianshangInfo.add(info);
                }
                if (buff == CardBuffEnum.gongJi.getValue() && buffInfo.containsKey("gongji")) {
                    JSONObject gongji = buffInfo.getJSONObject("gongji");
                    int roundsNumber = gongji.containsKey("roundsNumber") ? gongji.getInteger("roundsNumber") : 2;
                    int hp = gongji.containsKey("hp") ? gongji.getIntValue("hp") : 100;
                    card.setHp(card.getHp() - hp);
                    if (roundsNumber - 1 == 0) {
                        roundsNumber -= 1;
                        iterator.remove();
                    } else {
                        roundsNumber -= 1;
                    }
                    gongji.put("roundsNumber", roundsNumber);
                    buffInfo.put("gongji", gongji);
                    if (roundsNumber == 0) {
                        buffInfo.remove("gongji");
                    }
                    JSONObject info = new JSONObject();
                    info.put("index", card.getIndex());
                    info.put("hp", -hp);
                    gongjiInfo.add(info);
                }
            }
        }
    }

    public static JSONArray getCardBuffInfo(List<GamingCardAttacker> playerCards, List<GamingCardTarget> otherCards) {
        JSONArray buffInfo = new JSONArray();
        for (GamingCardAttacker playerCard : playerCards) {
            JSONObject o = new JSONObject();
            if (playerCard.getHp() <= 0) {
                continue;
            }
            o.put("index", playerCard.getIndex());
            Set<Integer> buff = playerCard.getBuff();
            Set<Integer> a = new HashSet<>();
            a.addAll(buff);
            o.put("buff", a);
            buffInfo.add(o);
        }

        for (GamingCardTarget playerCard : otherCards) {
            JSONObject o = new JSONObject();
            o.put("index", playerCard.getIndex());
            Set<Integer> buff = playerCard.getBuff();
            Set<Integer> a = new HashSet<>();
            a.addAll(buff);
            o.put("buff", a);
            buffInfo.add(o);
        }
        return buffInfo;
    }


    public static Map<String, Integer> getMyCardIndexMap(List<GamingCardAttacker> playerCards) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            if (playerCards.size() >= i) {
                if (playerCards.get(i - 1) == null) {
                    continue;
                }
                map.put(playerCards.get(i - 1).getCardId().toString(), i);
                playerCards.get(i - 1).setIndex(i);
            }
        }

        return map;
    }

    public static Map<String, Integer> getOtherCardIndexMap(List<GamingCardTarget> otherCards) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 6; i <= 10; i++) {
            if (otherCards.size() >= i - 5) {
                if (otherCards.get(i - 6) != null) {
                    map.put(otherCards.get(i - 6).getCardId().toString(), i);
                    otherCards.get(i - 6).setIndex(i);
                }
            }
        }
        return map;
    }

    public static List<GamingCardAttacker> getPlayerCards(List<GamingCardAttacker> playerCardss) {
        List<GamingCardAttacker> list = new ArrayList<>();
        for (int i = 0; i < playerCardss.size(); i++) {
            if (playerCardss.get(i) != null) {
                list.add(playerCardss.get(i));
            }
        }
        return list;
    }

    public static List<GamingCardTarget> getOtherCards(List<GamingCardTarget> otherCardss) {
        List<GamingCardTarget> list = new ArrayList<>();
        for (int i = 0; i < otherCardss.size(); i++) {
            if (otherCardss.get(i) != null) {
                list.add(otherCardss.get(i));
            }
        }
        return list;
    }

    public static JSONObject battle(List<GamingCardAttacker> playerCardss, List<GamingCardTarget> otherCardss) {
        List<GamingCardAttacker> playerCards = getPlayerCards(playerCardss);
        List<GamingCardTarget> otherCards = getOtherCards(otherCardss);
        JSONArray rounds = new JSONArray();
        JSONObject result = new JSONObject();
        getMyCardIndexMap(playerCardss);
        getOtherCardIndexMap(otherCardss);
        JSONArray cardsInfo = getCardsInfo(playerCards, otherCards);
        result.put("cardInfo", cardsInfo);
        int round = 0;
        List<GamingCard> battleQueue = new ArrayList<>();
        for (GamingCardAttacker card : playerCards) {
            battleQueue.add(card);
        }
        for (GamingCardTarget card : otherCards) {
            battleQueue.add(card);
        }
        List<Boolean> playerAlive = new ArrayList<>();
        List<Boolean> otherAlive = new ArrayList<>();
        for (int i = 0; i < playerCards.size(); i++) {
            playerAlive.add(true);
        }
        for (int i = 0; i < otherCards.size(); i++) {
            otherAlive.add(true);
        }
        int isWin = 0;
        while (true) {
            JSONObject roundInfo = new JSONObject();
            boolean playerHasAlive = false;
            boolean bossHasAlive = false;
            for (Boolean alive : playerAlive) {
                if (alive) {
                    playerHasAlive = true;
                    break;
                }
            }
            for (Boolean alive : otherAlive) {
                if (alive) {
                    bossHasAlive = true;
                    break;
                }
            }
            if (!playerHasAlive || !bossHasAlive) {
                if (playerHasAlive) {
                    isWin = 1;
                }
                break;
            }
            round++;
            roundInfo.put("name", BattleEventEnum.ROUND.getValue());
            roundInfo.put("round", round);
            roundInfo.put("buff", getCardBuffInfo(playerCards, otherCards));
            JSONObject buffIndexInfo = updateCardStatusByBuff(playerCards, otherCards);
            for (GamingCard target : battleQueue) {
                // 判断生命值是否小于等于 0，如果是则设置存活状态为 false
                if (target.getHp() <= 0) {
                    target.setBuff(new HashSet<>());
                    target.setBuffInfo(new JSONObject());
                    if (target instanceof GamingCardAttacker) {
                        int playerIndex = playerCards.indexOf(target);
                        if (playerIndex != -1) {
                            playerAlive.set(playerIndex, false);
                        }
                    } else {
                        int bossIndex = otherCards.indexOf(target);
                        if (bossIndex != -1) {
                            otherAlive.set(bossIndex, false);
                        }
                    }
                }
            }
            roundInfo.putAll(buffIndexInfo);
            if (round > 50) {
                break;
            }
            //回合事件添加
            rounds.add(roundInfo);
            // 使用计数排序的思想快速确定行动顺序
//            List<GamingCard> sortGamingCard = new ArrayList<>(ba);
//            Collections.sort();
            int[] speedCount = new int[1000000]; // 假设速度最大值为 999，可根据实际情况调整
            for (GamingCard card : battleQueue) {
                speedCount[(int) card.getSpeed()]++;
            }
            battleQueue.sort((card1, card2) -> Double.compare(card2.getSpeed(), card1.getSpeed()));
                if (isWin == 1) {
                    break;
                }
            for (int i = 0;i<battleQueue.size();i++) {

                    GamingCard attacker = battleQueue.get(i);
                    playerHasAlive = false;
                    bossHasAlive = false;
                    for (Boolean alive : playerAlive) {
                        if (alive) {
                            playerHasAlive = true;
                            break;
                        }
                    }
                    for (Boolean alive : otherAlive) {
                        if (alive) {
                            bossHasAlive = true;
                            break;
                        }
                    }
                    if (!playerHasAlive || !bossHasAlive) {
                        if (playerHasAlive) {
                            isWin = 1;
                        }
                        break;
                    }
                    if (attacker.getHp() <= 0) {
                        if (attacker instanceof GamingCardAttacker) {
                            int playerIndex = playerCards.indexOf(attacker);
                            if (playerIndex != -1) {
                                playerAlive.set(playerIndex, false);
                            }
                        } else {
                            int bossIndex = otherCards.indexOf(attacker);
                            if (bossIndex != -1) {
                                otherAlive.set(bossIndex, false);
                            }
                        }
                        continue;
                    }
                    //眩晕的话进入下一个攻击目标
                    if (attacker.getBuff().contains(CardBuffEnum.xuanYun.getValue())) {
                        continue;
                    }
                    List<Object> targets;
                    int skill = 0;
                    if (attacker.getMp() >= 100) {
                        skill = 1;
                    }
                    int targetType = 1;
                    int number = 1;
                    double damageRate = 1.0;
                    int damageCount = 1;
                    JSONArray skillBuff = new JSONArray();
                    if (skill == 1) {
                        DicSkill dicSkill = getSkill(attacker.getSkillId());
                        if (dicSkill != null) {
                            number = dicSkill.getNumber();
                            targetType = dicSkill.getTargetType();
                            damageRate = dicSkill.getDamage() / 100;
                            skillBuff = dicSkill.getBuff();
                            damageCount = dicSkill.getDamageCount();
                        } else {
                            skill = 0;
                        }

                    }

                    if (attacker instanceof GamingCardAttacker) {
                        if (attacker.getCardType() == 1) {
                            targets = selectTarget(otherCards, otherAlive, number, targetType);
                        } else {
                            targetType=4;
                            targets = selectTarget(playerCards, playerAlive, number, targetType);
                        }
                    } else {
                        //攻击者是敌方
                        if (attacker.getCardType() == 1) {
                            targets = selectTarget(playerCards, playerAlive, number, targetType);
                        } else {
                            targetType=4;
                            targets = selectTarget(otherCards, otherAlive, number, targetType);
                        }

                    }
                    if (targets.size() != 0) {
                        JSONObject object = calculateDamage(attacker, targets, skill, damageRate, skillBuff, damageCount);
                        rounds.add(object);
                        for (Object o : targets) {
                            GamingCard target = (GamingCard) o;
                            // 判断生命值是否小于等于 0，如果是则设置存活状态为 false
                            if (target.getHp() <= 0) {
                                target.setBuff(new HashSet<>());
                                target.setBuffInfo(new JSONObject());
                                if (target instanceof GamingCardAttacker) {
                                    int playerIndex = playerCards.indexOf(target);
                                    if (playerIndex != -1) {
                                        playerAlive.set(playerIndex, false);
                                    }
                                } else {
                                    int bossIndex = otherCards.indexOf(target);
                                    if (bossIndex != -1) {
                                        otherAlive.set(bossIndex, false);
                                    }
                                }
                            }
                        }
                    }
                }
            updateCardXuanYunBuff(playerCards, otherCards);
        }
        JSONObject battleResult = new JSONObject();
        battleResult.put("name", BattleEventEnum.RESULT.getValue());
        battleResult.put("isWin", isWin);
        rounds.add(battleResult);
        result.put("battle", rounds);
        result.put("isWin", isWin);
        return result;
    }

    private static int applyLifeSteal(GamingCard attacker, double damageDealt) {
        return (int) (damageDealt * attacker.getLeech() / 100);
    }

    private static boolean isPlayerSideDefeated(List<Boolean> playerAlive) {
        for (boolean alive : playerAlive) {
            if (alive) {
                return false;
            }
        }
        return true;
    }

    // 提取出选择卡牌的公共方法
    public static List<Object> selectCardsByRow(List cards, List<Boolean> aliveFlags, int[] validIndices, int number, Set<Object> selected) {
        List<Object> selectedCards = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            if (aliveFlags.get(i)) {
                Object o = cards.get(i);
                int index;
                if (o instanceof GamingCardAttacker) {
                    GamingCardAttacker attacker = (GamingCardAttacker) o;
                    index = attacker.getIndex();
                } else {
                    GamingCardTarget target = (GamingCardTarget) o;
                    index = target.getIndex();
                }
                if (index > 5) {
                    index = index - 5;
                }

                final int finalIndex = index;
                if (Arrays.stream(validIndices).anyMatch(validIndex -> validIndex == finalIndex)) {
                    if (!selected.contains(cards.get(i))) {
                        selectedCards.add(cards.get(i));
                        selected.add(cards.get(i));
                        if (selectedCards.size() == number) {
                            return selectedCards; // 如果已选足够数量的卡牌，立刻返回
                        }
                    }
                }
            }
        }
        return selectedCards;
    }

    public static Map<String, Double> sortHpMap(Map<String, Double> map) {
        // 按值升序排序
        Map<String, Double> sortedMapAsc = map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()) // 默认升序
                .collect(Collectors.toMap(
                        entry -> entry.getKey(), // 使用 Lambda 提取键
                        entry -> entry.getValue(), // 使用 Lambda 提取值
                        (oldValue, newValue) -> oldValue, // 合并函数，处理键冲突
                        LinkedHashMap::new // 保证顺序
                ));
        return sortedMapAsc;
    }

    // 主选择方法
    public static List<Object> selectTarget(List cards, List<Boolean> aliveFlags, int number, int targetType) {
        List<Object> list = new ArrayList<>();
        Set<Object> selected = new HashSet<>();


        if (targetType == 4) {
            // 血量最少的目标
            Map<String, Double> map = new HashMap<>();
            for (int i = 0; i < cards.size(); i++) {
                Object o = cards.get(i);
                double hp;
                if (o instanceof GamingCardAttacker) {
                    GamingCardAttacker attacker = (GamingCardAttacker) o;
                    hp = attacker.getHp()/attacker.getMaxHp();

                } else {
                    GamingCardTarget target = (GamingCardTarget) o;
                    hp = target.getHp()/target.getMaxHp();
                }
                map.put(String.valueOf(i), hp);
            }
            Map<String, Double> sortedMapAsc = sortHpMap(map);
            for (Map.Entry<String, Double> entry : sortedMapAsc.entrySet()) {
                if (entry.getValue()>0){
                    //如果血量大于0 则选择此卡牌
                    list.add(cards.get(Integer.parseInt(entry.getKey())));
                    selected.add(cards.get(Integer.parseInt(entry.getKey())));
                }
                if (list.size() == number) {
                    return list;
                }
            }
            return list;
        }

        // 1. 选择后排卡牌
        if (targetType == 3) {
            list.addAll(selectCardsByRow(cards, aliveFlags, new int[]{3, 4, 5}, number, selected));
            // 如果已选足够卡牌，立即返回
            if (list.size() == number) {
                return list;
            }

        }



        if (targetType == 1){
            list.addAll(selectCardsByRow(cards, aliveFlags, new int[]{1,2}, number, selected));
            // 如果已选足够卡牌，立即返回
            if (list.size() == number) {
                return list;
            }
        }

        // 3. 如果是随机类型，或者前后排都不足，可以选择所有存活的卡牌
        if (targetType == 2 || list.size() < number) {
            // 获取所有存活卡牌
            List<Object> aliveCards = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                if (aliveFlags.get(i)) {
                    aliveCards.add(cards.get(i));
                }
            }

            // 如果存活卡牌数量小于 `number`，则直接返回所有存活的卡牌
            if (aliveCards.size() <= number) {
                list.addAll(aliveCards);
                return list;
            }

            // 随机打乱存活卡牌
            Collections.shuffle(aliveCards);

            // 选择剩余数量的卡牌
            for (int i = 0; i < number; i++) {
                Object card = aliveCards.get(i);
                if (!selected.contains(card)) {
                    list.add(card);
                    selected.add(card);
                }
                if (list.size() >= number) {
                    return list;
                }
            }
        }

        return list; // 返回已选定的卡牌，数量不超过 number
    }


    private static Set<Integer> addBuff(GamingCard attacker, GamingCard target, JSONArray buffs) {
        Set<Integer> nowBuff = new HashSet<>();
        for (Object o : buffs) {
            JSONObject buff = (JSONObject) o;
            int buffId = buff.getIntValue("buffId");
            int damageType = buff.getIntValue("damageType");
            int rate = buff.getIntValue("rate");
            Random random = new Random();
            int i = random.nextInt(100) + 1;
            if (rate >= i) {
                double damage = 0;
                if (damageType == 1) {
                    //攻击力百分比
                    damage = buff.getDouble("damage") / 100 * attacker.getAtk();
                } else if (damageType == 0) {
                    damage = buff.getIntValue("damage");
                } else if (damageType == 2) {
                    damage = target.getMaxHp() * buff.getDoubleValue("damage") / 100;
                } else if (damageType == 3) {
                    damage = buff.getDoubleValue("damage") / 100;
                }
                int round = buff.getIntValue("round");
                JSONObject buffInfo = new JSONObject();
                buffInfo.put("roundsNumber", round);
                buffInfo.put("hp", damage);
                if (buffId == CardBuffEnum.zhiLiao.getValue()) {
                    target.getBuff().add(buffId);
                    target.getBuffInfo().put("zhiliao", buffInfo);
                    nowBuff.add(CardBuffEnum.zhiLiao.getValue());
                } else if (buffId == CardBuffEnum.zhongDu.getValue()) {
                    target.getBuff().add(buffId);
                    target.getBuffInfo().put("zhongdu", buffInfo);
                    nowBuff.add(CardBuffEnum.zhongDu.getValue());
                } else if (buffId == CardBuffEnum.liuXue.getValue()) {
                    target.getBuff().add(buffId);
                    target.getBuffInfo().put("liuxue", buffInfo);
                    nowBuff.add(CardBuffEnum.liuXue.getValue());
                } else if (buffId == CardBuffEnum.xuanYun.getValue()) {
                    target.getBuff().add(buffId);
                    target.getBuffInfo().put("xuanyun", buffInfo);
                    nowBuff.add(CardBuffEnum.xuanYun.getValue());
                } else if (buffId == CardBuffEnum.zhuoShao.getValue()) {
                    target.getBuff().add(buffId);
                    target.getBuffInfo().put("zhuoshao", buffInfo);
                    nowBuff.add(CardBuffEnum.zhuoShao.getValue());
                } else if (buffId == CardBuffEnum.mianShang.getValue()) {
                    target.getBuff().add(buffId);
                    target.getBuffInfo().put("mianshang", buffInfo);
                    nowBuff.add(CardBuffEnum.mianShang.getValue());
                } else if (buffId == CardBuffEnum.gongJi.getValue()) {
                    target.getBuff().add(buffId);
                    target.getBuffInfo().put("gongji", buffInfo);
                    nowBuff.add(CardBuffEnum.gongJi.getValue());
                } else if (buffId ==CardBuffEnum.qingchu.getValue()) {
                    target.getBuff().clear();
                    target.getBuffInfo().clear();
                }
            }
        }
        return nowBuff;
    }

    private static JSONObject addBuff(GamingCard attacker, List targets, JSONArray buffs) {
        JSONObject buffInfo = new JSONObject();
        for (Object o : targets) {
            GamingCard target = (GamingCard) o;
            Set<Integer> set = addBuff(attacker, target, buffs);
            if (set.size() > 0) {
                buffInfo.put(String.valueOf(target.getIndex()), set);
            }
        }
        return buffInfo;
    }

    private static JSONObject calculateDamage(GamingCard attacker, List<Object> targets, int isSkill, double damageRate, JSONArray skillBuff, int damageCount) {
        JSONObject result = new JSONObject();
        JSONArray others = new JSONArray();
        JSONArray mys = new JSONArray();
        if (isSkill == 1) {
            JSONObject buffInfo = addBuff(attacker, targets, skillBuff);
            JSONObject mySkillAtk = new JSONObject();
            result.put("name", BattleEventEnum.SKILL.getValue());
            result.put("damageCount", damageCount);
            if (attacker.getCardType() == 1) {
                result.put("type", BattleEventEnum.ATTACK.getValue());
                int allDamage = 0;
                for (Object o : targets) {
                    GamingCard target = (GamingCard) o;
                    double baseDamage = (attacker.getAtk() - target.getDef()) * damageRate;
                    if (baseDamage < 0) baseDamage = 1;
                    double chcMultiplier = 1.0;
                    double hitMultiplier = 1.0;
                    boolean isHtt = isHtt(attacker.getHtt(), target.getDodge());
                    if (!isHtt) {
                        hitMultiplier = 0.0;
                    }
                    // double reduction = target.getDef() / (target.getDef() + 702);
                    // int damage = (int) (baseDamage * chcMultiplier * hitMultiplier * (1 - reduction));
                    int damage = (int) (baseDamage * chcMultiplier * hitMultiplier);
                    if (target.getBuff().contains(CardBuffEnum.mianShang.getValue())) {
                        damage = (int) (damage * 0.9);
                    }
                    if (attacker.getBuff().contains(CardBuffEnum.gongJi.getValue())) {
                        damage = (int) (damage * 1.1);
                    }
                    if (damage <=0 && isHtt) {
                        int i = RANDOM.nextInt(13) + 7;
                        damage =i ;
                    }
                    allDamage += damage;
                    JSONObject targetResult = new JSONObject();
                    targetResult.put("damage", -damage);
                    targetResult.put("buff", buffInfo.containsKey(String.valueOf(target.getIndex())) ?
                            buffInfo.get(String.valueOf(target.getIndex())) : new HashSet<>());
                    targetResult.put("index", target.getIndex());
                    targetResult.put("getMp", damage < 0 ? 25 : 0);
                    others.add(targetResult);
                    target.setHp(target.getHp() - damage);
                }
                int lifeStealAmount = applyLifeSteal(attacker, allDamage);
                attacker.setHp(attacker.getHp() + lifeStealAmount);
                mySkillAtk.put("index", attacker.getIndex());
                mySkillAtk.put("buff", new HashSet<>());
                mySkillAtk.put("getMp", -100);
                mySkillAtk.put("getHp", lifeStealAmount);
            } else {
                result.put("type", BattleEventEnum.ZHILIAO.getValue());
                int allDamage = 0;
                for (Object o : targets) {
                    GamingCard target = (GamingCard) o;
                    double chcMultiplier = 1.0;
                    int damage = (int) (attacker.getAtk() * damageRate * chcMultiplier);
                    allDamage += damage;
                    JSONObject targetResult = new JSONObject();
                    targetResult.put("damage", damage);
                    targetResult.put("buff", buffInfo.containsKey(String.valueOf(target.getIndex())) ?
                            buffInfo.get(String.valueOf(target.getIndex())) : new HashSet<>());
                    targetResult.put("index", target.getIndex());
                    targetResult.put("getMp", damage < 0 ? 25 : 0);
                    others.add(targetResult);
                    target.setHp(target.getHp() + damage);
                    if (target.getHp() > target.getMaxHp()) {
                        target.setHp(target.getMaxHp());
                    }
                }
                int lifeStealAmount = applyLifeSteal(attacker, allDamage);
                attacker.setHp(attacker.getHp() + lifeStealAmount);
                mySkillAtk.put("index", attacker.getIndex());
                Set<Integer> buff = attacker.getBuff();
                Set<Integer> newBuff = new HashSet<>();
                newBuff.addAll(buff);
                mySkillAtk.put("buff", newBuff);
                mySkillAtk.put("getMp", -100);
                mySkillAtk.put("getHp", lifeStealAmount);
            }
            attacker.setMp(0);
            mys.add(mySkillAtk);
        } else {
            result.put("name", BattleEventEnum.ATTACK.getValue());
            GamingCard target = (GamingCard) targets.get(0);
            JSONObject my = new JSONObject();
            double damage;
            JSONObject targetResult = new JSONObject();
            if (attacker.getCardType() == 1) {
                result.put("type", BattleEventEnum.ATTACK.getValue());
                double baseDamage = attacker.getAtk();
                if (baseDamage < 0) baseDamage = 1;
                double chcMultiplier = 1.0;
                int isChc = 0;
                if (isChc(attacker.getChc(), target.getDefChc())) {
                    isChc = 1;
                    chcMultiplier = attacker.getChcImpact();
                }
                double hitMultiplier = 1.0;
                boolean isHtt = isHtt(attacker.getHtt(), target.getDodge());
                if (!isHtt) {
                    hitMultiplier = 0;
                }
                //double reduction = target.getDef() / (target.getDef() + 702);
                //damage = (int) (baseDamage * chcMultiplier * hitMultiplier * (1 - reduction));
                damage = (baseDamage - target.getDef()) * chcMultiplier * hitMultiplier;
                if (target.getBuff().contains(CardBuffEnum.mianShang.getValue())) {
                    damage = (int) (damage * 0.9);
                }
                if (attacker.getBuff().contains(CardBuffEnum.gongJi.getValue())) {
                    damage = (int) (damage * 1.1);
                }
                if (damage <= 0 && isHtt) {
                    int i = RANDOM.nextInt(13) + 7;
                    damage =i ;
                }
                damage = new BigDecimal(String.valueOf(damage)).setScale(0, RoundingMode.HALF_UP).doubleValue();
                //普攻， 吸血 对手扣血
                targetResult.put("damage", -damage);
                targetResult.put("isChc", isChc);
                int lifeStealAmount = applyLifeSteal(attacker, damage);
                attacker.setHp(attacker.getHp() + lifeStealAmount);
                target.setHp(target.getHp() - damage);
                my.put("getHp", lifeStealAmount);
                targetResult.put("getMp", damage == 0 ? 0 : 25);
                target.setMp(damage == 0 ? target.getMp() : target.getMp() + 25);
            } else {
                //治疗  目标加血
                damage = attacker.getAtk();
                result.put("type", "zhiliao");
                targetResult.put("damage", damage);
                target.setHp(target.getHp() + damage);
                if (target.getHp() > target.getMaxHp()) {
                    target.setHp(target.getMaxHp());
                }
                my.put("getHp", 0);
                targetResult.put("getMp", 0);
                if (target.getHp() > target.getMaxHp()) {
                    target.setHp(target.getMaxHp());
                }
            }
            Set<Integer> nowBuff = new HashSet<>();
            targetResult.put("buff", nowBuff);
            targetResult.put("index", target.getIndex());
            my.put("index", attacker.getIndex());
            my.put("buff", new HashSet<>());
            my.put("getMp", 25);
            others.add(targetResult);
            mys.add(my);
            //是攻击型并且 没死 并且连击
            if (attacker.getCardType() == 1 && target.getHp() - damage > 0 && isCombo(attacker.getCombo())) {
                result.put("type", BattleEventEnum.COMBO.getValue());
                JSONObject my2 = new JSONObject();
                JSONObject other2 = new JSONObject();
                double chcMultiplier = 1.0;
                int chc = 0;
                if (isChc(attacker.getChc(), target.getDefChc())) {
                    chc = 1;
                    chcMultiplier = attacker.getChcImpact();
                }
                double hitMultiplier2 = 1.0;
                if (!isHtt(attacker.getHtt(), target.getDodge())) {
                    hitMultiplier2 = 0.0;
                }
                double baseDamage = attacker.getAtk();
                double reduction2 = target.getDef() / (target.getDef() + 702);
                int damage2 = (int) (baseDamage * chcMultiplier * hitMultiplier2 * (1 - reduction2));
                other2.put("damage", -damage2);
                other2.put("buff", nowBuff);
                other2.put("isChc", chc);
                other2.put("index", target.getIndex());
                other2.put("getMp", damage == 0 ? 0 : 25);
                int lifeStealAmount2 = applyLifeSteal(attacker, damage2);
                attacker.setHp(attacker.getHp() + lifeStealAmount2);
                my2.put("index", attacker.getIndex());
                my2.put("buff", new HashSet<>());
                my2.put("getMp", 0);
                my2.put("getHp", lifeStealAmount2);
                mys.add(my2);
                others.add(other2);
                target.setHp(target.getHp() - damage2);
            }
            attacker.setMp(attacker.getMp() + 25);
        }
        result.put("atk", mys);
        result.put("dam", others);
        return result;
    }


    public static void main(String[] args) {
        long a = System.currentTimeMillis();
        List<GamingCardAttacker> playerCards = new ArrayList<>();
        playerCards.add(new GamingCardAttacker("牛1", 99, 10000, 50, 500, 10, 20, 100, 800, 200, 200, 200, 0.05, 1, 1));
        playerCards.add(new GamingCardAttacker("牛2", 88, 100, 50, 500, 10, 20, 90, 800, 200, 200, 200, 0.05, 2, 2));
        playerCards.add(new GamingCardAttacker("牛3", 77, 100, 50, 500, 10, 20, 100, 800, 200, 200, 200, 0.05, 3, 3));
        playerCards.add(new GamingCardAttacker("牛4", 55, 100, 50, 500, 10, 20, 90, 800, 200, 200, 200, 0.05, 4, 4));
        playerCards.add(new GamingCardAttacker("牛5", 66, 100, 50, 500, 10, 20, 100, 800, 200, 200, 200, 0.05, 5, 5));
        List<GamingCardTarget> bossCards = new ArrayList<>();
        bossCards.add(new GamingCardTarget("张1", 9336, 300, 50, 500, 10, 20, 70, 800, 200, 200, 200, 0.05, 6, 6));
        bossCards.add(new GamingCardTarget("张2", 2000, 300, 50, 500, 10, 20, 60, 800, 200, 200, 200, 0.05, 7, 7));
        bossCards.add(new GamingCardTarget("张3", 2000, 300, 50, 500, 10, 20, 70, 800, 200, 200, 200, 0.05, 8, 8));
        bossCards.add(new GamingCardTarget("张4", 2000, 300, 50, 500, 10, 20, 60, 800, 200, 200, 200, 0.05, 9, 9));
        bossCards.add(new GamingCardTarget("张5", 2000, 300, 50, 500, 10, 20, 70, 800, 200, 200, 200, 0.05, 10, 10));
        List<Boolean> aliveFlags = new ArrayList<>(); // 假设卡牌1存活，其余死亡
        aliveFlags.add(true);
        aliveFlags.add(true);
        aliveFlags.add(true);
        aliveFlags.add(true);
        aliveFlags.add(true);
        System.out.println(selectTarget(playerCards, aliveFlags, 2, 1));
        System.out.println(selectTarget(playerCards, aliveFlags, 3, 1));
        System.out.println(selectTarget(playerCards, aliveFlags, 4, 1));
        System.out.println(selectTarget(playerCards, aliveFlags, 5, 1));
        System.out.println("=====================");
        System.out.println(selectTarget(playerCards, aliveFlags, 2, 2));
        System.out.println(selectTarget(playerCards, aliveFlags, 3, 2));
        System.out.println(selectTarget(playerCards, aliveFlags, 4, 2));
        System.out.println(selectTarget(playerCards, aliveFlags, 5, 2));
        System.out.println("=====================");
        System.out.println(selectTarget(playerCards, aliveFlags, 2, 3));
        System.out.println(selectTarget(playerCards, aliveFlags, 3, 3));
        System.out.println(selectTarget(playerCards, aliveFlags, 4, 3));
        System.out.println(selectTarget(playerCards, aliveFlags, 5, 3));
        System.out.println("=====================");
        System.out.println(selectTarget(playerCards, aliveFlags, 2, 4));
        System.out.println(selectTarget(playerCards, aliveFlags, 3, 4));
        System.out.println(selectTarget(playerCards, aliveFlags, 4, 4));
        System.out.println(selectTarget(playerCards, aliveFlags, 5, 4));

    }
}
