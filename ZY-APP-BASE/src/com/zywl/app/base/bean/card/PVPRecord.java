package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;


public class PVPRecord extends BaseBean {

    private Long id;

    private String orderNo;

    private Long userId;

    private String headImg;

    private String name;

    private String userNo;

    private int result;

    private Long targetUserId;

    private String targetHeadImg;

    private String targetName;

    private String targetUserNo;

    private byte[] context;

    private String record;

    private int score;

    private int targetScore;

    private Date createTime;

    private Date updateTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getHeadImg() {
        return headImg;
    }

    public void setHeadImg(String headImg) {
        this.headImg = headImg;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public byte[] getContext() {
        return context;
    }

    public void setContext(byte[] context) {
        this.context = context;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public int getTargetScore() {
        return targetScore;
    }

    public void setTargetScore(int targetScore) {
        this.targetScore = targetScore;
    }
}
