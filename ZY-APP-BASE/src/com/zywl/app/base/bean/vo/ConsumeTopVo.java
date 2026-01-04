package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

/**
 * 用户资产消耗排行榜 VO
 * num：累计消耗金额（按资产类型的真实单位返回）
 */
public class ConsumeTopVo extends BaseBean {

    private Long userId;

    private String userNo;

    private String userName;

    private String userHeadImg;

    /**
     * 累计消耗金额
     */
    private BigDecimal num;

    /**
     * 资产类型（可选：空=所有资产类型汇总）
     */
    private Integer capitalType;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserHeadImg() {
        return userHeadImg;
    }

    public void setUserHeadImg(String userHeadImg) {
        this.userHeadImg = userHeadImg;
    }

    public BigDecimal getNum() {
        return num;
    }

    public void setNum(BigDecimal num) {
        this.num = num;
    }

    public Integer getCapitalType() {
        return capitalType;
    }

    public void setCapitalType(Integer capitalType) {
        this.capitalType = capitalType;
    }
}
