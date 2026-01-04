package com.live.app.ws.enums;

/**
 * 推送
 * @author DOE
 *
 */
public enum PushCode {

	pushRed,
	caidengmi,

	//卡牌游戏==============================================
	addPlayerCard,
	//添加新的卡牌组
	updateRoleCard,
	//刷新全部卡牌组
	updateRoleCardAll,
	removePlayerCard,
	redPointShow,
	redPointHide,

	userAncientNotice,
	userLeaveAncient,
	updateAncientInfo,

	userJoinAncient,

	updateUserPower,
	//红包推送
	redPackageInfo,
	updateNhStatus,
	updateDts2Info,
	updateDts2Status,
	updateNhInfo,

	updateSgStatus,
	updateSgInfo,

	updateBtStatus,
	updateDgsStatus,

	updateBtInfo,
	updateDgsInfo,
	updateDnsStatus,
	updateDnsInfo,
	updatePlayer,
	updatePlayerMp,
	sendNotice,
	updateUserInfo,
	//更新广告次数
	updateAdCount,

	/**
	 * 增加报表
	 */
	addStatement,
	/**
	 * 插入log
	 */
	insertLog,

	cancelBet,

	/**
	 * 推送pl值恢复
	 */
	updatePlayerPl,
	
	/**
	 * 推送表版本更新
	 */
	updateTableVersion,


	/**
	 * 推送红点至APP
	 */
	redReminder,
	
	
	/**
	 * 更新游戏状态
	 */
	updateGameStatus,

	/**
	 * 推送游戏DIY数据
	 */
	updateGameDiyData,
	/**
	 * 消耗房间信息更新
	 */
	updateRoomDate,
	/**
	 * 离开大逃杀房间
	 */
	leaveBattleRoyale,
	
	//----------------------------大胃王
	/**
	* 大胃王房间信息
	*/
	foodGameStatus,
	/**
	* 大胃王用户坐下
	*/
	foodGameSitDown,
	/**
	* 大胃王加菜
	*/
	foodGameAddFood,
	//-----------------------------
	/**
	 *  game资产回退
	 */
	rollbackCapital,
	/**
	 * game资产修改
	 */
	updateUserCapital,
	updateUserBackpack,
	/**
	 * 同步未领取收益
	 */
	syncIncomeAmount,
	
	/**
	 * 领取收益
	 */
	receiveIncome,
	
	/**
	 * 同步服务器任务数
	 */
	syncTaskNum,
	/**
	 * 同步APP在线信息
	 */
	syncAppOnline,
	syncAppRegister,
	syncAppLogin,
	/**
	 * 同步APP离线信息
	 */
	syncAppOffline,
	
	/**
	 * 同步资产信息
	 */
	syncUserCapital,
	/**
	 * 同步APP信息更新信息
	 */
	syncAppChange,
	/**
	 * 同步首页监控信息
	 */
	syncMonitor,
	/**
	 * 同步服务器是否可用
	 */
	syncIsService,

	//推送订单
	syncTsgOrder,

	//用户注册登录
	userLogin,
	userRegist,
	/**
	 * 同步代理首页统计信息
	 */
	syncAgentMonitor,
	/**
	 * 强制app下线
	 */
	fcAppLoginOut,
	/**
	 * 系统配置修改
	 */
	updateConfig,


	
	/**
	 * 大逃杀配置修改
	 */
	updateBattleRoyaleConfig,
	/**
	 * 客服聊天推送
	 */
	chat,
	/**
	 * 文字聊天消息
	 */
	liveChat,
	/**
	 * 系统消息
	 */
	liveSysChat,
	/**
	 * 新增直播间
	 */
	liveAdd,
	/**
	 * 直播间信息更新
	 */
	liveUpdate,
	/**
	 * 开播
	 */
	liveOpen,
	/**
	 * 关播
	 */
	liveClose,
	/**
	 * 直播断线
	 */
	liveDisconnect,
	/**
	 * 直播恢复
	 */
	liveReconnect,
	/**
	 * 直播间流中断
	 */
	liveStreamDisconnect,
	/**
	 * 直播间流地址切换
	 */
	liveStreamUrlChange,
	/**
	 * 直播禁言
	 */
	liveDisableTalk,
	/**
	 * 直播允许发言
	 */
	liveEnableTalk,
	/**
	 * 加入直播室
	 */
	joinLive,
	/**
	 * 离开直播室
	 */
	leaveLive,
	/**
	 * 直播间礼物信息
	 */
	liveGift,
	/**
	 * APP版本更新
	 */
	updateVersion,
	/**
	 * 用户新增
	 */
	userAdd,
	/**
	 * 用户信息修改
	 */
	userUpdate,
	/**
	 * 入金成功
	 */
	paySuccess,
	/**
	 * 推送用户出金申请已通过
	 */
	userDrawPass,
	/**
	 * 推送拒绝用户出金申请
	 */
	userDrawReject,
	/**
	 * 推送撤销用户出金申请
	 */
	userDrawRecall,
	/**
	 * 通知server是否可使用notice通道
	 */
	noticeOpen,
	/**
	 * 系统公告
	 */
	newsAddOrUpdate,
	/**
	 * 删除系统公告
	 */
	newsDelete,
	/**
	 * 广播给各个server推送消息
	 */
	broadcastPush,
	/**
	 * 关注新增粉丝
	 */
	addFans,
	/**
	 * 取关移除粉丝
	 */
	deleteFans,
	/**
	 * 修改守护信息
	 */
	updateGuard,
	/**
	 * 新增守护信息
	 */
	addGuard,
	/**
	 * 开通守护
	 */
	openLiveGuard,
	/**
	 * 守护过期
	 */
	expireLiveGuard,
	/**
	 * 修改礼物信息
	 */
	updateGift,
	/**
	 * 新增礼物
	 */
	addGift,
	/**
	 * 新增钱包
	 */
	walletAdd,
	/**
	 * 修改钱包
	 */
	walletUpdate,
	/**
	 * 主播审核通过
	 */
	playerVerifyPass,
	/**
	 * 主播审核拒绝
	 */
	playerVerifyReject,
	/**
	 * 初始化级别
	 */
	initLevel,
	/**
	 * 修改等级
	 */
	updateLevel,
	/**
	 * 修改座驾信息
	 */
	updateDriver,
	/**
	 * 新增座驾信息
	 */
	addDriver,
	/**
	 * 购买座驾
	 */
	openUserDriver,
	/**
	 * 座驾过期
	 */
	expireUserDriver,
	/**
	 * 座驾信息更新
	 */
	updateUserDriver,
	/**
	 * 添加黑名单
	 */
	addLiveBlack,
	/**
	 * 移除黑名单
	 */
	deleteLiveBlack, 
	/**
	 * 添加直播间房管
	 */
	addLiveManager,
	/**
	 * 移除直播间房管
	 */
	deleteLiveManager, 
	/**
	 * 收到主播名片
	 */
	receiveCard,
	/**
	 * 收到主播名片直播间推送
	 */
	liveReceiveCardMsg,
	/**
	 * 直播间金币更新推送
	 */
	liveTopAmountUpdate,
	/**
	 * 家族新增
	 */
	familyAdd,
	/**
	 * 家族信息修改
	 */
	familyUpdate,
	/**
	 * 推箱子
	 */
	updatePbxInfo,
	updatePbxStatus,
	/*成为会长*/
	BECOME_GUILD_MASTER,
	/*重置身份*/
	deleteGuild,

}
