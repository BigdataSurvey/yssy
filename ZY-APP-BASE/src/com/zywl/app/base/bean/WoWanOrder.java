package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;

public class WoWanOrder extends BaseBean {

    private Long id;

    private int orderId;

    private int cid;

    private String cuid;

    private String devid;

    private String adName;

    private String time;

    private BigDecimal point;

    private int atype;

    private String sign;

    private String platPoints;

    private String event;

    private String dlevel;

    private String icon;

    private String adid;

    private String phonetype;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public String getCuid() {
        return cuid;
    }

    public void setCuid(String cuid) {
        this.cuid = cuid;
    }

    public String getDevid() {
        return devid;
    }

    public void setDevid(String devid) {
        this.devid = devid;
    }

    public String getAdName() {
        return adName;
    }

    public void setAdName(String adName) {
        this.adName = adName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public BigDecimal getPoint() {
        return point;
    }

    public void setPoint(BigDecimal point) {
        this.point = point;
    }

    public int getAtype() {
        return atype;
    }

    public void setAtype(int atype) {
        this.atype = atype;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getPlatPoints() {
        return platPoints;
    }

    public void setPlatPoints(String platPoints) {
        this.platPoints = platPoints;
    }

    public String getEvents() {
        return event;
    }

    public void setEvents(String events) {
        this.event = events;
    }

    public String getDlevel() {
        return dlevel;
    }

    public void setDlevel(String dlevel) {
        this.dlevel = dlevel;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAdid() {
        return adid;
    }

    public void setAdid(String adid) {
        this.adid = adid;
    }

    public String getPhonetype() {
        return phonetype;
    }

    public void setPhonetype(String phonetype) {
        this.phonetype = phonetype;
    }

    public WoWanOrder(int orderId, int cid, String cuid, String devid, String adName, String time, BigDecimal point, int atype, String sign, String platPoints, String events, String dlevel, String icon, String adid, String phonetype) {
        this.orderId = orderId;
        this.cid = cid;
        this.cuid = cuid;
        this.devid = devid;
        this.adName = adName;
        this.time = time;
        this.point = point;
        this.atype = atype;
        this.sign = sign;
        this.platPoints = platPoints;
        this.event = events;
        this.dlevel = dlevel;
        this.icon = icon;
        this.adid = adid;
        this.phonetype = phonetype;
    }

    public WoWanOrder() {
    }
}
