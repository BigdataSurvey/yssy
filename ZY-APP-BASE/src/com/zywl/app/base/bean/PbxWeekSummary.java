package com.zywl.app.base.bean;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
/**
 * @Author: lzx
 * @Create: 2025/12/28
 * @Version: V1.0
 * @Description: PBX 周榜汇总（周维度聚合+快照）
 * 对应表：t_pbx_week_summary
 */

public class PbxWeekSummary implements Serializable{
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
     * 本周总投注（分）
     */
    private Long totalBetCents;

    /**
     * 本周总返还（返还总额，分）
     */
    private Long totalReturnCents;

    /**
     * 本周总实返（净返还 net，分）
     */
    private Long totalNetCents;

    /**
     * 本周总手续费（分）
     */
    private Long totalFeeCents;

    /**
     * 本周利润（= total_bet - total_net，分；小于0按0）
     */
    private Long profitCents;

    /**
     * 利润进入周榜奖池比例（如0.5）
     */
    private BigDecimal rankProfitPercent;

    /**
     * 本次结算新增进入周榜奖池金额（分）
     */
    private Long poolAddCents;

    /**
     * 结算后周榜奖池余额（分）
     */
    private Long poolBalanceCents;

    /**
     * Top10分润比例数组快照（JSON）
     */
    private String top10RatesJson;

    /**
     * 本次结算TopN用户派奖结果快照（JSON）
     */
    private String userListJson;

    /**
     * 是否已结算：0否1是
     */
    private Integer settled;

    /**
     * 结算时间
     */
    private Date settleTime;

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

    public Long getTotalBetCents() {
        return totalBetCents;
    }

    public void setTotalBetCents(Long totalBetCents) {
        this.totalBetCents = totalBetCents;
    }

    public Long getTotalReturnCents() {
        return totalReturnCents;
    }

    public void setTotalReturnCents(Long totalReturnCents) {
        this.totalReturnCents = totalReturnCents;
    }

    public Long getTotalNetCents() {
        return totalNetCents;
    }

    public void setTotalNetCents(Long totalNetCents) {
        this.totalNetCents = totalNetCents;
    }

    public Long getTotalFeeCents() {
        return totalFeeCents;
    }

    public void setTotalFeeCents(Long totalFeeCents) {
        this.totalFeeCents = totalFeeCents;
    }

    public Long getProfitCents() {
        return profitCents;
    }

    public void setProfitCents(Long profitCents) {
        this.profitCents = profitCents;
    }

    public BigDecimal getRankProfitPercent() {
        return rankProfitPercent;
    }

    public void setRankProfitPercent(BigDecimal rankProfitPercent) {
        this.rankProfitPercent = rankProfitPercent;
    }

    public Long getPoolAddCents() {
        return poolAddCents;
    }

    public void setPoolAddCents(Long poolAddCents) {
        this.poolAddCents = poolAddCents;
    }

    public Long getPoolBalanceCents() {
        return poolBalanceCents;
    }

    public void setPoolBalanceCents(Long poolBalanceCents) {
        this.poolBalanceCents = poolBalanceCents;
    }

    public String getTop10RatesJson() {
        return top10RatesJson;
    }

    public void setTop10RatesJson(String top10RatesJson) {
        this.top10RatesJson = top10RatesJson;
    }

    public String getUserListJson() {
        return userListJson;
    }

    public void setUserListJson(String userListJson) {
        this.userListJson = userListJson;
    }

    public Integer getSettled() {
        return settled;
    }

    public void setSettled(Integer settled) {
        this.settled = settled;
    }

    public Date getSettleTime() {
        return settleTime;
    }

    public void setSettleTime(Date settleTime) {
        this.settleTime = settleTime;
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
