<@compress single_line=true>
var consoleLog = false;
if(window.localStorage && '1' == window.localStorage.getItem("consoleLog")) {
    consoleLog = true;
};
var controllerProvider = null;
var LIVEAPP = angular.module('LIVE-APP', [
    'ui.router',
    'oc.lazyLoad',
    'toastr',
    'ngTable'
    ], function($controllerProvider){
        controllerProvider = $controllerProvider;
    });
LIVEAPP.config(function(toastrConfig) {
    angular.extend(toastrConfig, {
        closeButton : false,
        debug : false,
        positionClass : 'toast-top-center',
        onclick : null,
        showDuration : 300,
        hideDuration : 1000,
        timeOut : 6000,
        extendedTimeOut : 1000,
        showEasing : 'swing',
        hideEasing : 'linear',
        showMethod : 'fadeIn',
        hideMethod : 'fadeOut',
        newestOnTop : false,
        closeButton : true
    });
});
LIVEAPP.filter('orderObjectBy', function() {
    return function(items, field, reverse) {
      var filtered = [];
      angular.forEach(items, function(item) {
        filtered.push(item);
      });
      filtered.sort(function (a, b) {
        return (a[field] > b[field] ? 1 : -1);
      });
      if(reverse) filtered.reverse();
      return filtered;
    };
});

angular.module('LIVE-APP')
    .run(['$templateCache', function($templateCache) {
        'use strict';
    }]);
<#if '${User.roleId}' =='ad02969f3c2e400db79105843cbed44e'>
var menuTree = [{id: 'Home', name: '总览', icon: 'ti-home'},
    {id: 'Business', name: '游戏管理', icon: 'ti-view-grid', hasChild: true, children: [

        {id: 'UserManager', name: '玩家管理'},
        {id: 'PlayerManager', name: '主角系统'},
        {id: 'TreasureInfo', name: '资产详情'},
        {id: 'BackpackInfo', name: '背包详情'},
        /*{id: 'PetInfo', name: '灵兽系统'},
        {id: 'JSXMManager', name: '仙门系统'},*/
        {id: 'GuildManager', name: '公会系统'},
        {id: 'GuildMemberManager', name: '公会成员'},
        {id: 'ChannelManager', name: '渠道审核'},
        {id: 'ShopManager', name: '店长申请'},
        {id: 'EmailManager', name: '邮件列表'},
        {id: 'SendMail', name: '发送邮件'},
        {id: 'GoodNoManager', name: '靓号管理'},
            {id: 'CarouselManager', name: '店长道具管理'},
    ]},
    {id: 'Rank', name: '排行榜', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'TreasureRank', name: '资产排行'},
            {id: 'ItemRank', name: '文房四宝排行'}
    ]},
    {id: 'Trans', name: '交易行管理', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'TransactionInfo', name: '交易行信息'},
        {id: 'MakeOrder', name: '生成订单'},
    ]},
    {id: 'Order', name: '提现充值', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'RechargeInfo', name: '充值记录'},
        {id: 'DrawHistory', name: '提现记录'},
        {id: 'DrawExam', name: '提现审核'}
    ]},
    {id: 'Total', name:'统计信息', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'PetAnalysis', name:'书境分布'},
        /*{id: 'GodSeatAnalysis', name:'矿产分布'}*/
        /*{id: 'SkillAnalysis', name:'技能分布'}*/
    ]},
    {id: 'statement', name: '报表管理', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'PlatformStatementManager', name: '游戏每日报表'},
            {id: 'GiftStatementManager', name: '礼包每日报表'},
        /*{id: 'userStatement', name: '玩家每日报表'},
        {id: 'miJingStatement', name: '秘境每日报表'}*/
    ]},
    {id: 'log', name:'日志管理', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'TreasureLog', name:'资产日志'},
        {id: 'ItemLog', name:'道具日志'},
        {id: 'DTSLog', name: '渡劫日志'},
        {id: 'JiuXianLog', name: '酒仙日志'},
        {id: 'BanLoginLog', name: '封号日志'}
    ]},
    {id: 'shumei', name:'数美设置', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'ShuMeiRule', name:'数美规则'}
    ]},

    {id: 'System', name: '系统管理', icon: 'ti-settings', hasChild: true, children: [
        {id: 'EmployeeManager', name: '员工管理'},
        {id: 'SystemConfig', name: '系统配置'},
        {id: 'VersionManager', name: '版本管理'},
        {id: 'AdminLog', name: '管理员日志'}
        /*{id: 'NoticeManager', name: '系统公告'}*/
    ]}
];
<#if User?has_content && User.id == 'bGRrYZPKDBpzkFR3f5RerKt4BPLZnZ9n'>
menuTree[3].children.push({id: 'UserWarning', name: '用户预警'});
</#if>
<#elseif '${User.roleId}' =='fafa9fa225644438937feb755d5eae2f'>
var menuTree = [{id: 'Home', name: '总览', icon: 'ti-home'},
    {id: 'Business', name: '游戏管理', icon: 'ti-view-grid', hasChild: true, children: [
        {id: 'UserManager', name: '玩家管理'},
        {id: 'PlayerManager', name: '主角系统'},
        {id: 'TreasureInfo', name: '资产详情'},
        {id: 'BackpackInfo', name: '背包详情'},
        /*{id: 'PetInfo', name: '灵兽系统'},*/
        {id: 'EmailManager', name: '邮件列表'}
    ]},
    {id: 'Rank', name: '排行榜', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'TreasureRank', name: '资产排行'},
            {id: 'ItemRank', name: '文房四宝排行'}
    ]},
    {id: 'Order', name: '提现充值', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'RechargeInfo', name: '充值记录'},
        {id: 'DrawHistory', name: '提现记录'}
    ]},
    {id: 'Total', name:'统计信息', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'PetAnalysis', name:'书境分布'},
       /* {id: 'GodSeatAnalysis', name:'神座分布'}*/
        /*{id: 'SkillAnalysis', name:'技能分布'}*/
    ]},
    {id: 'statement', name: '报表管理', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'PlatformStatementManager', name: '游戏每日报表'},
         {id: 'GiftStatementManager', name: '礼包每日报表'},
        /*{id: 'userStatement', name: '玩家每日报表'},
        {id: 'miJingStatement', name: '秘境每日报表'}*/
    ]},
    {id: 'log', name:'日志信息', icon: 'ti-layers-alt', hasChild: true, children: [
        {id: 'TreasureLog', name:'资产日志'},
        {id: 'ItemLog', name:'道具日志'},
        {id: 'DTSLog', name: '渡劫日志'},
        {id: 'JiuXianLog', name: '酒仙日志'},
        {id: 'BanLoginLog', name: '封号日志'}
    ]}
];
</#if>
window._defualtMenuId = menuTree[0].id;
angular.module('LIVE-APP').constant('menuTree', menuTree);
</@compress>