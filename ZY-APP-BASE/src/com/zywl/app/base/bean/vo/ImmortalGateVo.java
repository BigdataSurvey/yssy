package com.zywl.app.base.bean.vo;

import java.math.BigDecimal;
import java.util.List;

public class ImmortalGateVo {

    private String immortalGateName;

    private String immortalGateDesc;

    private Long immortalGateImageId;

    private Integer immortalGateLevel;

    private Integer immortalGateNowExp;

    private Integer immortalGateMaxExp;

    private Long immortalGateFighting;

    private Integer immortalGateMemberNum;

    private Integer immortalGateMemberMax;

    private BigDecimal immortalGateBalance;

    private Long immortalGatePower;

    private List<ImmortalGateMemberVo> vos;

    public Long getImmortalGateImageId() {
        return immortalGateImageId;
    }

    public void setImmortalGateImageId(Long immortalGateImageId) {
        this.immortalGateImageId = immortalGateImageId;
    }

    public Long getImmortalGatePower() {
        return immortalGatePower;
    }

    public void setImmortalGatePower(Long immortalGatePower) {
        this.immortalGatePower = immortalGatePower;
    }

    public List<ImmortalGateMemberVo> getVos() {
        return vos;
    }



    public void setVos(List<ImmortalGateMemberVo> vos) {
        this.vos = vos;
    }

    public String getImmortalGateName() {
        return immortalGateName;
    }

    public void setImmortalGateName(String immortalGateName) {
        this.immortalGateName = immortalGateName;
    }

    public String getImmortalGateDesc() {
        return immortalGateDesc;
    }

    public void setImmortalGateDesc(String immortalGateDesc) {
        this.immortalGateDesc = immortalGateDesc;
    }

    public Integer getImmortalGateLevel() {
        return immortalGateLevel;
    }

    public void setImmortalGateLevel(Integer immortalGateLevel) {
        this.immortalGateLevel = immortalGateLevel;
    }

    public Integer getImmortalGateNowExp() {
        return immortalGateNowExp;
    }

    public void setImmortalGateNowExp(Integer immortalGateNowExp) {
        this.immortalGateNowExp = immortalGateNowExp;
    }

    public Integer getImmortalGateMaxExp() {
        return immortalGateMaxExp;
    }

    public void setImmortalGateMaxExp(Integer immortalGateMaxExp) {
        this.immortalGateMaxExp = immortalGateMaxExp;
    }

    public Long getImmortalGateFighting() {
        return immortalGateFighting;
    }

    public void setImmortalGateFighting(Long immortalGateFighting) {
        this.immortalGateFighting = immortalGateFighting;
    }

    public Integer getImmortalGateMemberNum() {
        return immortalGateMemberNum;
    }

    public void setImmortalGateMemberNum(Integer immortalGateMemberNum) {
        this.immortalGateMemberNum = immortalGateMemberNum;
    }

    public Integer getImmortalGateMemberMax() {
        return immortalGateMemberMax;
    }

    public void setImmortalGateMemberMax(Integer immortalGateMemberMax) {
        this.immortalGateMemberMax = immortalGateMemberMax;
    }

    public BigDecimal getImmortalGateBalance() {
        return immortalGateBalance;
    }

    public void setImmortalGateBalance(BigDecimal immortalGateBalance) {
        this.immortalGateBalance = immortalGateBalance;
    }

}
