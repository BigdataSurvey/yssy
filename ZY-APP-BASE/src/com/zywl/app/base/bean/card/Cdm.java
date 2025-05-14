package com.zywl.app.base.bean.card;

import com.zywl.app.base.BaseBean;

import java.util.Date;

public class Cdm extends BaseBean {

    private Long id;

    private Date dmTime;

    private String tips;

    private String dm;

    private String answer;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDmTime() {
        return dmTime;
    }

    public void setDmTime(Date dmTime) {
        this.dmTime = dmTime;
    }

    public String getDm() {
        return dm;
    }

    public void setDm(String dm) {
        this.dm = dm;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }
}
