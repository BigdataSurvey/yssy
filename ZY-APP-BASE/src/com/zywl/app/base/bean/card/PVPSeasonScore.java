package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class PVPSeasonScore extends BaseBean {

    private Long id;

    private Long seasonId;

    private int score;

    private int rank;

    private Date seasonStartTime;

    private Date seasonEndTime;

    private Date createTime;

    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public Date getSeasonStartTime() {
        return seasonStartTime;
    }

    public void setSeasonStartTime(Date seasonStartTime) {
        this.seasonStartTime = seasonStartTime;
    }

    public Date getSeasonEndTime() {
        return seasonEndTime;
    }

    public void setSeasonEndTime(Date seasonEndTime) {
        this.seasonEndTime = seasonEndTime;
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
}
