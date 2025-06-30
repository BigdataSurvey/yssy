package com.zywl.app.base.bean.vo;

import com.zywl.app.base.BaseBean;

public class GiftStatementVo extends BaseBean {

    private String time;

    private int total;

    private int pay;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPay() {
        return pay;
    }

    public void setPay(int pay) {
        this.pay = pay;
    }
}
