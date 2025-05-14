package com.zywl.app.base.bean.vo.card;

import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;
import com.zywl.app.base.bean.card.DicSkill;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PlayerCardInfoVo extends BaseBean {


    private Long id;

    private int lv;

    private DicSkill skillInfo;
    private Long skillId;

    private int maxLv;

    private Long userId;
    private Long cardId;

    private int cardType;

    private String pic;

    private double hp;

    private double baseHp;

    private double atk;

    private double baseAtk;


    private double def;

    private double baseDef;


    private double chc;

    private double chcImpact;
    private double defChc;

    private double speed;

    private double baseSpeed;

    private double htt;

    private double dodge;

    private double afb;

    private double combo;

    private double leech;

    private int quality;

    private int power;

    private int isFight;

    private int star;


    private Date createTime;

    private Date updateTime;

    private int isUpdate;

    private int maxStar;

    private long equA;

    private long equB;

    private long equC;

    private long equD;

    private long artifactId;

    private long petId;

    public long getEquA() {
        return equA;
    }

    public void setEquA(long equA) {
        this.equA = equA;
    }

    public long getEquB() {
        return equB;
    }

    public void setEquB(long equB) {
        this.equB = equB;
    }

    public long getEquC() {
        return equC;
    }

    public void setEquC(long equC) {
        this.equC = equC;
    }

    public long getEquD() {
        return equD;
    }

    public void setEquD(long equD) {
        this.equD = equD;
    }

    public long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(long artifactId) {
        this.artifactId = artifactId;
    }

    public int getMaxStar() {
        return maxStar;
    }

    public void setMaxStar(int maxStar) {
        this.maxStar = maxStar;
    }

    private int status;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIsUpdate() {
        return isUpdate;
    }

    public void setIsUpdate(int isUpdate) {
        this.isUpdate = isUpdate;
    }

    public static double atkCoe = 2.5;
    public static double hpCoe = 0.6;
    public static double defCoe = 1.8;
    public static double chcCoe = 1.5;
    public static double chcImpactCoe = 1.2;
    public static double defChcCoe = 1.0;
    public static double httCoe = 0;
    public static double dodgeCoe = 0;
    public static double afbCoe = 1.4;
    public static double comboCoe = 1.6;
    public static double leechCoe = 1.1;
    public static double speedCoe = 1.7;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }



    public double getChc() {
        return chc;
    }

    public void setChc(double chc) {
        this.chc = chc;
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

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public int getIsFight() {
        return isFight;
    }

    public long getPetId() {
        return petId;
    }

    public void setPetId(long petId) {
        this.petId = petId;
    }

    public void setIsFight(int isFight) {
        this.isFight = isFight;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public double getChcImpact() {
        return chcImpact;
    }

    public void setChcImpact(double chcImpact) {
        this.chcImpact = chcImpact;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    public double getBaseHp() {
        return baseHp;
    }

    public void setBaseHp(double baseHp) {
        this.baseHp = baseHp;
    }

    public double getAtk() {
        return atk;
    }

    public void setAtk(double atk) {
        this.atk = atk;
    }

    public double getBaseAtk() {
        return baseAtk;
    }

    public void setBaseAtk(double baseAtk) {
        this.baseAtk = baseAtk;
    }

    public double getDef() {
        return def;
    }

    public void setDef(double def) {
        this.def = def;
    }

    public double getBaseDef() {
        return baseDef;
    }

    public void setBaseDef(double baseDef) {
        this.baseDef = baseDef;
    }

    public int getMaxLv() {
        return maxLv;
    }

    public void setMaxLv(int maxLv) {
        this.maxLv = maxLv;
    }

    public double getBaseSpeed() {
        return baseSpeed;
    }

    public void setBaseSpeed(double baseSpeed) {
        this.baseSpeed = baseSpeed;
    }

    public void updatePower() {
        this.power = (int) (atk * atkCoe + hp * hpCoe + def * defCoe + chc * chcCoe + chcImpact * chcImpactCoe + defChc * defChcCoe + speed * speedCoe + htt * httCoe
                + dodge * dodgeCoe + afb * afbCoe + combo * comboCoe + leech * leechCoe);
    }

    public void clickUnWear() {
        this.equA = 0L;
        this.equB = 0L;
        this.equC = 0L;
        this.equD = 0L;
    }

    public void unWear(int position) {
        if (position == 1) {
            this.equA = 0L;
        } else if (position == 2) {
            this.equB = 0L;
        } else if (position == 3) {
            this.equC = 0L;
        } else {
            this.equD = 0L;
        }
    }

    public List<Long> getEquInfo(){
        List<Long> list = new ArrayList<>();
        list.add(artifactId);
        list.add(equA);
        list.add(equB);
        list.add(equC);
        list.add(equD);
        list.add(petId);
        return list;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    public DicSkill getSkillInfo() {
        return skillInfo;
    }

    public void setSkillInfo(DicSkill skillInfo) {
        this.skillInfo = skillInfo;
    }
}
