package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;


public class PVPRecordVo extends BaseBean {

    private Long id;


    private int result;

    private Long targetUserId;

    private String targetHeadImg;

    private String targetName;

    private String targetUserNo;

    private int targetScore;

    private long power;

    private long targetPower;

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    public long getTargetPower() {
        return targetPower;
    }

    public void setTargetPower(long targetPower) {
        this.targetPower = targetPower;
    }

    private int score;

    private Date createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetHeadImg() {
        return targetHeadImg;
    }

    public void setTargetHeadImg(String targetHeadImg) {
        this.targetHeadImg = targetHeadImg;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetUserNo() {
        return targetUserNo;
    }

    public void setTargetUserNo(String targetUserNo) {
        this.targetUserNo = targetUserNo;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }
}
