package com.zywl.app.base.bean.vo.card;

import com.zywl.app.base.BaseBean;

public class CardVo extends BaseBean {


    private Long id;

    private String name;

    private String context;

    private String pic;

    private int hp;

    private int atk;


    private int def;


    private double chc;

    private double chcImpact;
    private double defChc;

    private double speed;

    private double htt;

    private double dodge;

    private double afb;

    private double combo;

    private double leech;

    private int quality;
    private Long artifactId;

    private int factions;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getAtk() {
        return atk;
    }

    public void setAtk(int atk) {
        this.atk = atk;
    }

    public int getDef() {
        return def;
    }

    public void setDef(int def) {
        this.def = def;
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

    public int getFactions() {
        return factions;
    }

    public void setFactions(int factions) {
        this.factions = factions;
    }

    public double getChcImpact() {
        return chcImpact;
    }

    public Long getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(Long artifactId) {
        this.artifactId = artifactId;
    }

    public void setChcImpact(double chcImpact) {
        this.chcImpact = chcImpact;
    }
}
