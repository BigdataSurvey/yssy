package com.zywl.app.base.bean.vo.card;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GamingCard extends BaseBean {

    private int lv;

    private Long skillId;

    private Long cardId;
    private String name;

    private String context;

    private String pic;

    private double hp;

    private double maxHp;

    private double atk;


    private double def;


    private double chc;

    private double chcImpact;

    private double defChc;

    private double speed;

    private double htt;

    private double dodge;

    private double afb;

    private double combo;

    private double leech;

    private double quality;

    private int ragePoint;

    private int cardType;

    private int index;

    private int mp;

    private Set<Integer> buff = new HashSet<>();

    private JSONObject buffInfo = new JSONObject();

    private List<Integer> deBuff = new ArrayList<>();

    public Set<Integer> getBuff() {
        return buff;
    }

    public void setBuff(Set<Integer> buff) {
        this.buff = buff;
    }

    public List<Integer> getDeBuff() {
        return deBuff;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDeBuff(List<Integer> deBuff) {
        this.deBuff = deBuff;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
        if (this.mp>100){
            this.mp=100;
        }
    }

    public GamingCard(String name, int hp, int atk, int def, double chc, double chcImpact, double defChc, double speed, double htt, double dodge, double afb, double combo, double leech,long cardId) {
        this.name = name;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.chc = chc;
        this.chcImpact = chcImpact;
        this.defChc = defChc;
        this.speed = speed;
        this.htt = htt;
        this.dodge = dodge;
        this.afb = afb;
        this.combo = combo;
        this.leech = leech;
        this.cardId=cardId;
    }

    public GamingCard(String name, int hp, int atk, int def, double chc, double chcImpact, double defChc, double speed, double htt, double dodge, double afb, double combo, double leech,long cardId,int index) {
        this.name = name;
        this.hp = hp;
        this.atk = atk;
        this.def = def;
        this.chc = chc;
        this.chcImpact = chcImpact;
        this.defChc = defChc;
        this.speed = speed;
        this.htt = htt;
        this.dodge = dodge;
        this.afb = afb;
        this.combo = combo;
        this.leech = leech;
        this.cardId=cardId;
        this.index = index;
    }

    public GamingCard(){

    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
        if (this.hp>maxHp && this.maxHp!=0){
            this.hp=maxHp;
        }
    }

    public double getAtk() {
        return atk;
    }

    public void setAtk(double atk) {
        this.atk = atk;
    }

    public double getDef() {
        return def;
    }

    public void setDef(double def) {
        this.def = def;
    }

    public double getChc() {
        return chc;
    }

    public void setChc(double chc) {
        this.chc = chc;
    }

    public double getChcImpact() {
        return chcImpact;
    }

    public void setChcImpact(double chcImpact) {
        this.chcImpact = chcImpact;
    }

    public double getDefChc() {
        return defChc;
    }

    public void setDefChc(double defChc) {
        this.defChc = defChc;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getHtt() {
        return htt;
    }

    public void setHtt(double htt) {
        this.htt = htt;
    }

    public double getDodge() {
        return dodge;
    }

    public void setDodge(double dodge) {
        this.dodge = dodge;
    }

    public double getAfb() {
        return afb;
    }

    public void setAfb(double afb) {
        this.afb = afb;
    }

    public double getCombo() {
        return combo;
    }

    public void setCombo(double combo) {
        this.combo = combo;
    }

    public double getLeech() {
        return leech;
    }

    public void setLeech(double leech) {
        this.leech = leech;
    }

    public double getQuality() {
        return quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public int getRagePoint() {
        return ragePoint;
    }

    public void setRagePoint(int ragePoint) {
        this.ragePoint = ragePoint;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public double getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(double maxHp) {
        this.maxHp = maxHp;
    }

    public JSONObject getBuffInfo() {
        return buffInfo;
    }

    public void setBuffInfo(JSONObject buffInfo) {
        this.buffInfo = buffInfo;
    }

    @Override
    public String toString() {
        return "GamingCard{" +
                ", name='" + name + '\'' +

                '}';
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }
}
