package com.zywl.app.base.bean;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author: lzx
 * @Create: 2025/12/28
 * @Version: V1.0
 * @Description: 周榜结算事件
    t_pbx_week_settle_event
 */
@Data
public class PbxWeekSettleEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 游戏ID（PBX=12）
     */
    private Integer gameId;

    /**
     * 周Key（周一日期：yyyy-MM-dd）
     */
    private String weekKey;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 周榜名次（1-10）
     */
    private Integer rank;

    /**
     * 该用户本周投注额（分）
     */
    private Long betCents;

    /**
     * 分润比例（如0.30）
     */
    private BigDecimal rate;

    /**
     * 该用户应派奖金额（分）
     */
    private Long awardCents;

    /**
     * 币种（固定1002）
     */
    private Integer capitalType;

    /**
     * 幂等单号（建议 PBX_WEEK_RANK_ + weekKey）
     */
    private String orderNo;

    /**
     * 资金日志枚举
     */
    private Integer em;

    /**
     * 日志归属表名
     */
    private String tableName;

    /**
     * 发奖状态：0待发 1成功 2失败
     */
    private Integer status;

    /**
     * 失败原因（失败时记录）
     */
    private String failMsg;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getWeekKey() {
        return weekKey;
    }

    public void setWeekKey(String weekKey) {
        this.weekKey = weekKey;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Long getBetCents() {
        return betCents;
    }

    public void setBetCents(Long betCents) {
        this.betCents = betCents;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public Long getAwardCents() {
        return awardCents;
    }

    public void setAwardCents(Long awardCents) {
        this.awardCents = awardCents;
    }

    public Integer getCapitalType() {
        return capitalType;
    }

    public void setCapitalType(Integer capitalType) {
        this.capitalType = capitalType;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Integer getEm() {
        return em;
    }

    public void setEm(Integer em) {
        this.em = em;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFailMsg() {
        return failMsg;
    }

    public void setFailMsg(String failMsg) {
        this.failMsg = failMsg;
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