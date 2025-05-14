package com.zywl.app.manager.bean;

public class IDCardReportDataBean {
    /**
     * 条目编码
     */
    private byte no;
    /**
     * 内部标识
     */
    private String si;
    /**
     * 用户行为 0：下线 1：上线
     */
    private byte bt;
    /**
     * 行为发生时间 10位 单位秒
     */
    private Long ot;
    /**
     * 上报类型 0：已认证通过 2：游客
     */
    private byte ct;
    /**
     * 设备标识
     */
    private String di;
    /**
     * 用户唯一标识
     */
    private String pi;

    public byte getNo() {
        return no;
    }

    public void setNo(byte no) {
        this.no = no;
    }

    public String getSi() {
        return si;
    }

    public void setSi(String si) {
        this.si = si;
    }

    public byte getBt() {
        return bt;
    }

    public void setBt(byte bt) {
        this.bt = bt;
    }

    public Long getOt() {
        return ot;
    }

    public void setOt(Long ot) {
        this.ot = ot;
    }

    public byte getCt() {
        return ct;
    }

    public void setCt(byte ct) {
        this.ct = ct;
    }

    public String getDi() {
        return di;
    }

    public void setDi(String di) {
        this.di = di;
    }

    public String getPi() {
        return pi;
    }

    public void setPi(String pi) {
        this.pi = pi;
    }
}
