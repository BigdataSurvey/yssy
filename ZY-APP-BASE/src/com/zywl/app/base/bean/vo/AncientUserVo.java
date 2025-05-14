package com.zywl.app.base.bean.vo;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.zywl.app.base.BaseBean;

import java.util.Date;
import java.util.List;

public class AncientUserVo extends BaseBean {

    private Long userId;

    private String userNo;

    private String name;

    private String headImgUrl;

    private int userLv;


    private Integer petType;

    private long power;



    //防御侦查次数
    private int dffCount;

    //侦查次数
    private int lookCount;

    private int attackNumber;

    private Long hakesId;

    private int hakesType;

    private Long venturesId;

    private JSONArray reward;

    private Date jionTime;

    private Date leaveTime;

    private Long dataId;

    private Date cantMoveTime;

    private List<Integer> myPetTypes;

    private int isCheckPet;

    private int dieCount;

    private int mapType;

    private Date ysEndTime;

    public int getDieCount() {
        return dieCount;
    }

    public void setDieCount(int dieCount) {
        this.dieCount = dieCount;
    }

    public int getIsCheckPet() {
        return isCheckPet;
    }

    public void setIsCheckPet(int isCheckPet) {
        this.isCheckPet = isCheckPet;
    }

    public List<Integer> getMyPetTypes() {
        return myPetTypes;
    }

    public void setMyPetTypes(List<Integer> myPetTypes) {
        this.myPetTypes = myPetTypes;
    }

    public Date getCantMoveTime() {
        return cantMoveTime;
    }

    public void setCantMoveTime(Date cantMoveTime) {
        this.cantMoveTime = cantMoveTime;
    }

    public Long getDataId() {
        return dataId;
    }

    public void setDataId(Long dataId) {
        this.dataId = dataId;
    }

    public Date getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(Date leaveTime) {
        this.leaveTime = leaveTime;
    }

    public Date getJionTime() {
        return jionTime;
    }

    public void setJionTime(Date jionTime) {
        this.jionTime = jionTime;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getPetType() {
        return petType;
    }

    public void setPetType(Integer petType) {
        this.petType = petType;
    }


    public JSONArray getReward() {
        return reward;
    }

    public void setReward(JSONArray reward) {
        this.reward = reward;
    }

    public String getUserNo() {
        return userNo;
    }

    public void setUserNo(String userNo) {
        this.userNo = userNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadImgUrl() {
        return headImgUrl;
    }

    public void setHeadImgUrl(String headImgUrl) {
        this.headImgUrl = headImgUrl;
    }

    public Long getHakesId() {
        return hakesId;
    }

    public void setHakesId(Long hakesId) {
        this.hakesId = hakesId;
    }

    public int getAttackNumber() {
        return attackNumber;
    }

    public void setAttackNumber(int attackNumber) {
        this.attackNumber = attackNumber;
    }

    public Long getVenturesId() {
        return venturesId;
    }

    public void setVenturesId(Long venturesId) {
        this.venturesId = venturesId;
    }

    public int getDffCount() {
        return dffCount;
    }

    public void setDffCount(int dffCount) {
        this.dffCount = dffCount;
    }

    public int getLookCount() {
        return lookCount;
    }

    public void setLookCount(int lookCount) {
        this.lookCount = lookCount;
    }

    public int getUserLv() {
        return userLv;
    }

    public void setUserLv(int userLv) {
        this.userLv = userLv;
    }

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    public int getHakesType() {
        return hakesType;
    }

    public void setHakesType(int hakesType) {
        this.hakesType = hakesType;
    }

    public int getMapType() {
        return mapType;
    }

    public void setMapType(int mapType) {
        this.mapType = mapType;
    }

    public Date getYsEndTime() {
        return ysEndTime;
    }

    public void setYsEndTime(Date ysEndTime) {
        this.ysEndTime = ysEndTime;
    }
}
