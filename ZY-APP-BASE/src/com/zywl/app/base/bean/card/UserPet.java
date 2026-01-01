package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;
import org.apache.ibatis.type.Alias;
import java.util.Date;
@Alias("CardUserPet")
public class UserPet extends BaseBean {

    private Long id;

    private Long userId;

    private Long petId;

    private Long playerCardId;

    private int lv;

    private int star;

    private double hp;

    private double atk;

    private double def;

    private double speed;

    private int power;
    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getStar() {
        return star;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public double getHp() {
        return hp;
    }

    public void setHp(double hp) {
        this.hp = hp;
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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
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

    public Long getPlayerCardId() {
        return playerCardId;
    }

    public void setPlayerCardId(Long playerCardId) {
        this.playerCardId = playerCardId;
    }

    public int getPower() {
        return power;
    }
    public static double atkCoe = 2.5;
    public static double hpCoe = 0.6;
    public static double defCoe = 1.8;
    public static double speedCoe = 1.7;
    public void updatePower() {
        this.power = (int) (atk * atkCoe + hp * hpCoe + def * defCoe +  speed * speedCoe );
    }


}
