package com.zywl.app.base.bean.vo;

import java.math.BigDecimal;

public class ImmortalGateMemberVo {

    private Long userId;

    private String memberName;

    private String memberImageUrl;

    private Long memberFighting;

    private BigDecimal memberContribution;

    private BigDecimal contribution;

    private Integer memberRole;

    public BigDecimal getContribution() {
        return contribution;
    }

    public void setContribution(BigDecimal contribution) {
        this.contribution = contribution;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getMemberImageUrl() {
        return memberImageUrl;
    }

    public void setMemberImageUrl(String memberImageUrl) {
        this.memberImageUrl = memberImageUrl;
    }

    public Long getMemberFighting() {
        return memberFighting;
    }

    public void setMemberFighting(Long memberFighting) {
        this.memberFighting = memberFighting;
    }

    public BigDecimal getMemberContribution() {
        return memberContribution;
    }

    public void setMemberContribution(BigDecimal memberContribution) {
        this.memberContribution = memberContribution;
    }

    public Integer getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(Integer memberRole) {
        this.memberRole = memberRole;
    }
}
