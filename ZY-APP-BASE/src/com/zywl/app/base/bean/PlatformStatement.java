package com.zywl.app.base.bean;

import com.zywl.app.base.BaseBean;

import java.math.BigDecimal;



public class PlatformStatement extends BaseBean {

    //
    private Integer id;
    //
    private String ymd;
    //
    private BigDecimal subPrize = BigDecimal.ZERO;
    //
    private BigDecimal subSendMail = BigDecimal.ZERO;
    //
    private BigDecimal subMailFee = BigDecimal.ZERO;
    //
    private BigDecimal subTradingBuy = BigDecimal.ZERO;
    //
    private BigDecimal subAskBuy = BigDecimal.ZERO;
    //
    private BigDecimal subGameBetDts = BigDecimal.ZERO;

    private BigDecimal subGameBetFood = BigDecimal.ZERO;
    //
    private BigDecimal subSign = BigDecimal.ZERO;
    //
    private BigDecimal subRefineSpeed = BigDecimal.ZERO;
    //
    private BigDecimal subPet = BigDecimal.ZERO;
    //
    private BigDecimal subStudySkill = BigDecimal.ZERO;
    //
    private BigDecimal subShop = BigDecimal.ZERO;
    //
    private BigDecimal subGuild = BigDecimal.ZERO;
    //
    private BigDecimal subCash = BigDecimal.ZERO;
    //
    private BigDecimal subBuyCoin = BigDecimal.ZERO;
    //
    private BigDecimal subExchange = BigDecimal.ZERO;
    //
    private BigDecimal addPirze = BigDecimal.ZERO;
    //
    private BigDecimal addDemo = BigDecimal.ZERO;
    //
    private BigDecimal addSign = BigDecimal.ZERO;
    //
    private BigDecimal addSell = BigDecimal.ZERO;
    //
    private BigDecimal addCancelAskBuy = BigDecimal.ZERO;
    //
    private BigDecimal addCashFail = BigDecimal.ZERO;

    private BigDecimal addDailyTask = BigDecimal.ZERO;
    //
    private BigDecimal addReceiveIncome = BigDecimal.ZERO;
    //
    private BigDecimal addReceiveMail = BigDecimal.ZERO;
    //
    private BigDecimal addGameWinDts = BigDecimal.ZERO;

    private BigDecimal addGameWinFood = BigDecimal.ZERO;
    //
    private BigDecimal addSellSys = BigDecimal.ZERO;
    private BigDecimal addSellSys2 = BigDecimal.ZERO;
    private BigDecimal addSellSys3 = BigDecimal.ZERO;
    //
    private BigDecimal addInvite = BigDecimal.ZERO;
    //
    private BigDecimal addGuildSend = BigDecimal.ZERO;

    private BigDecimal addAchievement = BigDecimal.ZERO;

    private BigDecimal subConvert = BigDecimal.ZERO;

    private BigDecimal addConvert = BigDecimal.ZERO;

    private BigDecimal addC3ConvertRmb = BigDecimal.ZERO;

    private BigDecimal subC3ConvertRmb = BigDecimal.ZERO;

    private BigDecimal addFriendPlayGame = BigDecimal.ZERO;
    private BigDecimal addPlayGame = BigDecimal.ZERO;

    private BigDecimal subNs = BigDecimal.ZERO;

    private BigDecimal addNs = BigDecimal.ZERO;

    private BigDecimal subNh = BigDecimal.ZERO;

    private BigDecimal addNh = BigDecimal.ZERO;

    private BigDecimal subMagic = BigDecimal.ZERO;

    private BigDecimal addMagic = BigDecimal.ZERO;

    private BigDecimal addDzGame = BigDecimal.ZERO;

    private BigDecimal subDzGame = BigDecimal.ZERO;

    private BigDecimal addRedGame = BigDecimal.ZERO;

    private BigDecimal subRedGame = BigDecimal.ZERO;

    private BigDecimal subRedZd = BigDecimal.ZERO;

    private BigDecimal addDtsRebate = BigDecimal.ZERO;

    private BigDecimal addDts2 = BigDecimal.ZERO;

    private BigDecimal subDts2 = BigDecimal.ZERO;


    private BigDecimal subCq = BigDecimal.ZERO;

    private BigDecimal addCq = BigDecimal.ZERO;
    private BigDecimal subCreateXm = BigDecimal.ZERO;
    private BigDecimal subContribution = BigDecimal.ZERO;
    private BigDecimal subBuyHead = BigDecimal.ZERO;
    private BigDecimal subUpdateName = BigDecimal.ZERO;
    private BigDecimal addAncientC2 = BigDecimal.ZERO;
    private BigDecimal addAncientC5 = BigDecimal.ZERO;
    private BigDecimal subJoinAncient = BigDecimal.ZERO;
    private BigDecimal subRefresh = BigDecimal.ZERO;

    private BigDecimal addSellSysMagic = BigDecimal.ZERO;

    private BigDecimal addCave = BigDecimal.ZERO;

    private BigDecimal subAddFlag = BigDecimal.ZERO;

    private BigDecimal addBt = BigDecimal.ZERO;

    private BigDecimal subBt = BigDecimal.ZERO;

    private BigDecimal subTicket = BigDecimal.ZERO;

    private BigDecimal subShopMagic = BigDecimal.ZERO;

    private BigDecimal allOutPut= BigDecimal.ZERO;

    private  BigDecimal allExpend= BigDecimal.ZERO;

    private BigDecimal allMagicOutPut= BigDecimal.ZERO;

    private  BigDecimal allMagicExpend= BigDecimal.ZERO;

    private  BigDecimal addReceivePop= BigDecimal.ZERO;
    private  BigDecimal subSendGreetingCard= BigDecimal.ZERO;
    private  BigDecimal addReceiveAchievement= BigDecimal.ZERO;
    private  BigDecimal addFromItem= BigDecimal.ZERO;
    private  BigDecimal addDispatch= BigDecimal.ZERO;
    private  BigDecimal addWander= BigDecimal.ZERO;
    private  BigDecimal addDice= BigDecimal.ZERO;

    private BigDecimal subGift= BigDecimal.ZERO;

    private BigDecimal subVip= BigDecimal.ZERO;

    private BigDecimal subAfk= BigDecimal.ZERO;

    private BigDecimal subGameEscort= BigDecimal.ZERO;

    private BigDecimal addGameEscort= BigDecimal.ZERO;

    private BigDecimal subBuyPass= BigDecimal.ZERO;

    private BigDecimal addFriendBuy= BigDecimal.ZERO;

    private BigDecimal addPetBuy= BigDecimal.ZERO;

    private BigDecimal addAnima= BigDecimal.ZERO;

    private BigDecimal subMine= BigDecimal.ZERO;

    private BigDecimal subXhmj= BigDecimal.ZERO;

    private BigDecimal addXhmj= BigDecimal.ZERO;

    private BigDecimal subDgs= BigDecimal.ZERO;

    private BigDecimal addDgs= BigDecimal.ZERO;

    private BigDecimal subPit = BigDecimal.ZERO;

    private BigDecimal   addPit = BigDecimal.ZERO;

    public BigDecimal getSubDgs() {
        return subDgs;
    }

    public void setSubDgs(BigDecimal subDgs) {
        this.subDgs = subDgs;
    }

    public BigDecimal getAddDgs() {
        return addDgs;
    }

    public void setAddDgs(BigDecimal addDgs) {
        this.addDgs = addDgs;
    }

    public BigDecimal getSubMine() {
        return subMine;
    }

    public void setSubMine(BigDecimal subMine) {
        this.subMine = subMine;
    }

    public BigDecimal getSubBuyPass() {
        return subBuyPass;
    }

    public void setSubBuyPass(BigDecimal subBuyPass) {
        this.subBuyPass = subBuyPass;
    }

    public BigDecimal getAddFriendBuy() {
        return addFriendBuy;
    }

    public void setAddFriendBuy(BigDecimal addFriendBuy) {
        this.addFriendBuy = addFriendBuy;
    }

    public BigDecimal getAddPetBuy() {
        return addPetBuy;
    }

    public void setAddPetBuy(BigDecimal addPetBuy) {
        this.addPetBuy = addPetBuy;
    }

    public BigDecimal getSubGameEscort() {
        return subGameEscort;
    }

    public void setSubGameEscort(BigDecimal subGameEscort) {
        this.subGameEscort = subGameEscort;
    }

    public BigDecimal getAddGameEscort() {
        return addGameEscort;
    }

    public void setAddGameEscort(BigDecimal addGameEscort) {
        this.addGameEscort = addGameEscort;
    }

    public BigDecimal getSubAfk() {
        return subAfk;
    }

    public void setSubAfk(BigDecimal subAfk) {
        this.subAfk = subAfk;
    }

    public BigDecimal getSubGift() {
        return subGift;
    }

    public void setSubGift(BigDecimal subGift) {
        this.subGift = subGift;
    }

    public BigDecimal getSubVip() {
        return subVip;
    }

    public void setSubVip(BigDecimal subVip) {
        this.subVip = subVip;
    }

    public BigDecimal getAddReceivePop() {
        return addReceivePop;
    }

    public void setAddReceivePop(BigDecimal addReceivePop) {
        this.addReceivePop = addReceivePop;
    }

    public BigDecimal getSubSendGreetingCard() {
        return subSendGreetingCard;
    }

    public void setSubSendGreetingCard(BigDecimal subSendGreetingCard) {
        this.subSendGreetingCard = subSendGreetingCard;
    }

    public BigDecimal getAddReceiveAchievement() {
        return addReceiveAchievement;
    }

    public void setAddReceiveAchievement(BigDecimal addReceiveAchievement) {
        this.addReceiveAchievement = addReceiveAchievement;
    }

    public BigDecimal getAddFromItem() {
        return addFromItem;
    }

    public void setAddFromItem(BigDecimal addFromItem) {
        this.addFromItem = addFromItem;
    }

    public BigDecimal getAddDispatch() {
        return addDispatch;
    }

    public void setAddDispatch(BigDecimal addDispatch) {
        this.addDispatch = addDispatch;
    }

    public BigDecimal getAddWander() {
        return addWander;
    }

    public void setAddWander(BigDecimal addWander) {
        this.addWander = addWander;
    }

    public BigDecimal getAddDice() {
        return addDice;
    }

    public void setAddDice(BigDecimal addDice) {
        this.addDice = addDice;
    }

    public BigDecimal getSubShopMagic() {
        return subShopMagic;
    }

    public void setSubShopMagic(BigDecimal subShopMagic) {
        this.subShopMagic = subShopMagic;
    }

    public BigDecimal getSubTicket() {
        return subTicket;
    }

    public void setSubTicket(BigDecimal subTicket) {
        this.subTicket = subTicket;
    }

    public BigDecimal getAddCave() {
        return addCave;
    }

    public void setAddCave(BigDecimal addCave) {
        this.addCave = addCave;
    }

    public BigDecimal getSubAddFlag() {
        return subAddFlag;
    }

    public void setSubAddFlag(BigDecimal subAddFlag) {
        this.subAddFlag = subAddFlag;
    }

    public BigDecimal getAddBt() {
        return addBt;
    }

    public void setAddBt(BigDecimal addBt) {
        this.addBt = addBt;
    }

    public BigDecimal getSubBt() {
        return subBt;
    }

    public void setSubBt(BigDecimal subBt) {
        this.subBt = subBt;
    }

    public BigDecimal getAddAncientC2() {
        return addAncientC2;
    }

    public void setAddAncientC2(BigDecimal addAncientC2) {
        this.addAncientC2 = addAncientC2;
    }

    public BigDecimal getAddAncientC5() {
        return addAncientC5;
    }

    public void setAddAncientC5(BigDecimal addAncientC5) {
        this.addAncientC5 = addAncientC5;
    }

    public BigDecimal getAddSellSysMagic() {
        return addSellSysMagic;
    }

    public void setAddSellSysMagic(BigDecimal addSellSysMagic) {
        this.addSellSysMagic = addSellSysMagic;
    }

    public BigDecimal getAllMagicOutPut() {
        return allMagicOutPut;
    }

    public void setAllMagicOutPut(BigDecimal allMagicOutPut) {
        this.allMagicOutPut = allMagicOutPut;
    }

    public BigDecimal getAllMagicExpend() {
        return allMagicExpend;
    }

    public void setAllMagicExpend(BigDecimal allMagicExpend) {
        this.allMagicExpend = allMagicExpend;
    }

    public BigDecimal getSubCreateXm() {
        return subCreateXm;
    }

    public void setSubCreateXm(BigDecimal subCreateXm) {
        this.subCreateXm = subCreateXm;
    }

    public BigDecimal getSubContribution() {
        return subContribution;
    }

    public void setSubContribution(BigDecimal subContribution) {
        this.subContribution = subContribution;
    }

    public BigDecimal getSubBuyHead() {
        return subBuyHead;
    }

    public void setSubBuyHead(BigDecimal subBuyHead) {
        this.subBuyHead = subBuyHead;
    }

    public BigDecimal getSubUpdateName() {
        return subUpdateName;
    }

    public void setSubUpdateName(BigDecimal subUpdateName) {
        this.subUpdateName = subUpdateName;
    }


    public BigDecimal getSubJoinAncient() {
        return subJoinAncient;
    }

    public void setSubJoinAncient(BigDecimal subJoinAncient) {
        this.subJoinAncient = subJoinAncient;
    }

    public BigDecimal getSubRefresh() {
        return subRefresh;
    }

    public void setSubRefresh(BigDecimal subRefresh) {
        this.subRefresh = subRefresh;
    }

    public BigDecimal getAllOutPut() {
        return allOutPut;
    }

    public void setAllOutPut(BigDecimal allOutPut) {
        this.allOutPut = allOutPut;
    }

    public BigDecimal getAllExpend() {
        return allExpend;
    }

    public void setAllExpend(BigDecimal allExpend) {
        this.allExpend = allExpend;
    }

    public BigDecimal getAddPlayGame() {
        return addPlayGame;
    }

    public void setAddPlayGame(BigDecimal addPlayGame) {
        this.addPlayGame = addPlayGame;
    }

    public BigDecimal getAddC3ConvertRmb() {
        return addC3ConvertRmb;
    }

    public void setAddC3ConvertRmb(BigDecimal addC3ConvertRmb) {
        this.addC3ConvertRmb = addC3ConvertRmb;
    }

    public BigDecimal getSubC3ConvertRmb() {
        return subC3ConvertRmb;
    }

    public void setSubC3ConvertRmb(BigDecimal subC3ConvertRmb) {
        this.subC3ConvertRmb = subC3ConvertRmb;
    }

    public BigDecimal getSubConvert() {
        return subConvert;
    }

    public void setSubConvert(BigDecimal subConvert) {
        this.subConvert = subConvert;
    }

    public BigDecimal getAddConvert() {
        return addConvert;
    }

    public void setAddConvert(BigDecimal addConvert) {
        this.addConvert = addConvert;
    }

    /**
     * 设置：
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取：
     */
    public Integer getId() {
        return id;
    }

    /**
     * 设置：
     */
    public void setYmd(String ymd) {
        this.ymd = ymd;
    }

    /**
     * 获取：
     */
    public String getYmd() {
        return ymd;
    }

    /**
     * 设置：
     */
    public void setSubPrize(BigDecimal subPrize) {
        this.subPrize = subPrize;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubPrize() {
        return subPrize;
    }

    /**
     * 设置：
     */
    public void setSubSendMail(BigDecimal subSendMail) {
        this.subSendMail = subSendMail;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubSendMail() {
        return subSendMail;
    }

    /**
     * 设置：
     */
    public void setSubMailFee(BigDecimal subMailFee) {
        this.subMailFee = subMailFee;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubMailFee() {
        return subMailFee;
    }

    /**
     * 设置：
     */
    public void setSubTradingBuy(BigDecimal subTradingBuy) {
        this.subTradingBuy = subTradingBuy;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubTradingBuy() {
        return subTradingBuy;
    }

    /**
     * 设置：
     */
    public void setSubAskBuy(BigDecimal subAskBuy) {
        this.subAskBuy = subAskBuy;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubAskBuy() {
        return subAskBuy;
    }


    /**
     * 设置：
     */
    public void setSubSign(BigDecimal subSign) {
        this.subSign = subSign;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubSign() {
        return subSign;
    }

    /**
     * 设置：
     */
    public void setSubRefineSpeed(BigDecimal subRefineSpeed) {
        this.subRefineSpeed = subRefineSpeed;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubRefineSpeed() {
        return subRefineSpeed;
    }

    /**
     * 设置：
     */
    public void setSubPet(BigDecimal subPet) {
        this.subPet = subPet;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubPet() {
        return subPet;
    }

    /**
     * 设置：
     */
    public void setSubStudySkill(BigDecimal subStudySkill) {
        this.subStudySkill = subStudySkill;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubStudySkill() {
        return subStudySkill;
    }

    /**
     * 设置：
     */
    public void setSubShop(BigDecimal subShop) {
        this.subShop = subShop;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubShop() {
        return subShop;
    }

    /**
     * 设置：
     */
    public void setSubGuild(BigDecimal subGuild) {
        this.subGuild = subGuild;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubGuild() {
        return subGuild;
    }

    /**
     * 设置：
     */
    public void setSubCash(BigDecimal subCash) {
        this.subCash = subCash;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubCash() {
        return subCash;
    }

    /**
     * 设置：
     */
    public void setSubBuyCoin(BigDecimal subBuyCoin) {
        this.subBuyCoin = subBuyCoin;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubBuyCoin() {
        return subBuyCoin;
    }

    /**
     * 设置：
     */
    public void setSubExchange(BigDecimal subExchange) {
        this.subExchange = subExchange;
    }

    /**
     * 获取：
     */
    public BigDecimal getSubExchange() {
        return subExchange;
    }

    /**
     * 设置：
     */
    public void setAddPirze(BigDecimal addPirze) {
        this.addPirze = addPirze;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddPirze() {
        return addPirze;
    }

    /**
     * 设置：
     */
    public void setAddDemo(BigDecimal addDemo) {
        this.addDemo = addDemo;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddDemo() {
        return addDemo;
    }

    /**
     * 设置：
     */
    public void setAddSign(BigDecimal addSign) {
        this.addSign = addSign;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddSign() {
        return addSign;
    }

    /**
     * 设置：
     */
    public void setAddSell(BigDecimal addSell) {
        this.addSell = addSell;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddSell() {
        return addSell;
    }

    /**
     * 设置：
     */
    public void setAddCancelAskBuy(BigDecimal addcancelAskBuy) {
        this.addCancelAskBuy = addcancelAskBuy;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddCancelAskBuy() {
        return addCancelAskBuy;
    }

    /**
     * 设置：
     */
    public void setAddCashFail(BigDecimal addCashFail) {
        this.addCashFail = addCashFail;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddCashFail() {
        return addCashFail;
    }

    /**
     * 设置：
     */
    public void setAddReceiveIncome(BigDecimal addReceiveIncome) {
        this.addReceiveIncome = addReceiveIncome;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddReceiveIncome() {
        return addReceiveIncome;
    }

    /**
     * 设置：
     */
    public void setAddReceiveMail(BigDecimal addReceiveMail) {
        this.addReceiveMail = addReceiveMail;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddReceiveMail() {
        return addReceiveMail;
    }

    public BigDecimal getSubGameBetDts() {
        return subGameBetDts;
    }

    public void setSubGameBetDts(BigDecimal subGameBetDts) {
        this.subGameBetDts = subGameBetDts;
    }

    public BigDecimal getSubGameBetFood() {
        return subGameBetFood;
    }

    public void setSubGameBetFood(BigDecimal subGameBetFood) {
        this.subGameBetFood = subGameBetFood;
    }

    public BigDecimal getAddGameWinDts() {
        return addGameWinDts;
    }

    public void setAddGameWinDts(BigDecimal addGameWinDts) {
        this.addGameWinDts = addGameWinDts;
    }

    public BigDecimal getAddGameWinFood() {
        return addGameWinFood;
    }

    public void setAddGameWinFood(BigDecimal addGameWinFood) {
        this.addGameWinFood = addGameWinFood;
    }

    /**
     * 设置：
     */
    public void setAddSellSys(BigDecimal addSellSys) {
        this.addSellSys = addSellSys;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddSellSys() {
        return addSellSys;
    }

    /**
     * 设置：
     */
    public void setAddInvite(BigDecimal addInvite) {
        this.addInvite = addInvite;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddInvite() {
        return addInvite;
    }

    /**
     * 设置：
     */
    public void setAddGuildSend(BigDecimal addGuildSend) {
        this.addGuildSend = addGuildSend;
    }

    /**
     * 获取：
     */
    public BigDecimal getAddGuildSend() {
        return addGuildSend;
    }

    public BigDecimal getAddAchievement() {
        return addAchievement;
    }

    public void setAddAchievement(BigDecimal addAchievement) {
        this.addAchievement = addAchievement;
    }

    public BigDecimal getAddFriendPlayGame() {
        return addFriendPlayGame;
    }

    public void setAddFriendPlayGame(BigDecimal addFriendPlayGame) {
        this.addFriendPlayGame = addFriendPlayGame;
    }

    public BigDecimal getAddDailyTask() {
        return addDailyTask;
    }

    public void setAddDailyTask(BigDecimal addDailyTask) {
        this.addDailyTask = addDailyTask;
    }

    public BigDecimal getSubNs() {
        return subNs;
    }

    public void setSubNs(BigDecimal subNs) {
        this.subNs = subNs;
    }

    public BigDecimal getAddNs() {
        return addNs;
    }

    public void setAddNs(BigDecimal addNs) {
        this.addNs = addNs;
    }

    public BigDecimal getAddSellSys2() {
        return addSellSys2;
    }

    public void setAddSellSys2(BigDecimal addSellSys2) {
        this.addSellSys2 = addSellSys2;
    }

    public BigDecimal getAddSellSys3() {
        return addSellSys3;
    }

    public void setAddSellSys3(BigDecimal addSellSys3) {
        this.addSellSys3 = addSellSys3;
    }

    public BigDecimal getSubNh() {
        return subNh;
    }

    public void setSubNh(BigDecimal subNh) {
        this.subNh = subNh;
    }

    public BigDecimal getAddNh() {
        return addNh;
    }

    public void setAddNh(BigDecimal addNh) {
        this.addNh = addNh;
    }

    public BigDecimal getSubMagic() {
        return subMagic;
    }

    public void setSubMagic(BigDecimal subMagic) {
        this.subMagic = subMagic;
    }

    public BigDecimal getAddMagic() {
        return addMagic;
    }

    public void setAddMagic(BigDecimal addMagic) {
        this.addMagic = addMagic;
    }

    public BigDecimal getAddDzGame() {
        return addDzGame;
    }

    public void setAddDzGame(BigDecimal addDzGame) {
        this.addDzGame = addDzGame;
    }

    public BigDecimal getSubDzGame() {
        return subDzGame;
    }

    public void setSubDzGame(BigDecimal subDzGame) {
        this.subDzGame = subDzGame;
    }

    public BigDecimal getAddRedGame() {
        return addRedGame;
    }

    public void setAddRedGame(BigDecimal addRedGame) {
        this.addRedGame = addRedGame;
    }

    public BigDecimal getSubRedGame() {
        return subRedGame;
    }

    public void setSubRedGame(BigDecimal subRedGame) {
        this.subRedGame = subRedGame;
    }

    public void init(){
        allExpend=subShop.add(subTradingBuy).add(subGift).add(subPit).add(subRedGame).subtract(subRedZd);
        allOutPut=addDailyTask.add(addAchievement).add(addSellSys).add(addReceiveMail).add(addDice).add(addWander).add(addSell).add(addReceiveIncome).add(addPit).add(addRedGame);
        allMagicOutPut = BigDecimal.ZERO.add(addSellSysMagic).add(addAncientC5);
        allMagicExpend = BigDecimal.ZERO.add(subCreateXm).add(subBuyHead).add(subUpdateName).add(subJoinAncient).add(subRefresh).add(subContribution).add(subAddFlag).subtract(subShopMagic);
    }

    public BigDecimal getAddDtsRebate() {
        return addDtsRebate;
    }

    public void setAddDtsRebate(BigDecimal addDtsRebate) {
        this.addDtsRebate = addDtsRebate;
    }

    public BigDecimal getAddDts2() {
        return addDts2;
    }

    public void setAddDts2(BigDecimal addDts2) {
        this.addDts2 = addDts2;
    }

    public BigDecimal getSubDts2() {
        return subDts2;
    }

    public void setSubDts2(BigDecimal subDts2) {
        this.subDts2 = subDts2;
    }

    public BigDecimal getSubCq() {
        return subCq;
    }

    public void setSubCq(BigDecimal subCq) {
        this.subCq = subCq;
    }

    public BigDecimal getAddCq() {
        return addCq;
    }

    public void setAddCq(BigDecimal addCq) {
        this.addCq = addCq;
    }

    public BigDecimal getSubXhmj() {
        return subXhmj;
    }

    public void setSubXhmj(BigDecimal subXhmj) {
        this.subXhmj = subXhmj;
    }

    public BigDecimal getAddXhmj() {
        return addXhmj;
    }

    public void setAddXhmj(BigDecimal addXhmj) {
        this.addXhmj = addXhmj;
    }

    public BigDecimal getAddAnima() {
        return addAnima;
    }

    public void setAddAnima(BigDecimal addAnima) {
        this.addAnima = addAnima;
    }
}
