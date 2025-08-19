<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var baseTable = null;
        var markMap = {
            APP_RESOURCE_ONLINE_URL: "资源地址",
            CASH_FEE: "提现手续费",
            CONVERT_RATE: "余额兑换比例",
            GUILD_FEE: "开通公会质押价格",
            GUILD_MEMBER_FEE: "添加公会成员费用",
            MAIL_VALIDITY: "邮件有效期",
            PRIZE_DRAW_FEE: "抽奖费用",
            REFRESH_USER_CAPITAL: "更新玩家资产灵石",
            REFRESH_USER_COIN: "更新玩家铜钱缓存",
            REFRESH_USER_ITEM: "更新玩家背包缓存",
            SERVICE_STATUS: "服务器状态",
            TOP_NUMBER: "排行榜显示数量",
            TRADING_FEE: "交易行手续费",
            TRANSFER_SILL: "好友转赠起赠门槛",
            USER_NO_LENGTH: "ID长度",
            VERSION_ITEM: "物品表版本",
            VERSION_MINE:"矿场表版本",
            VERSION_ROLE:"角色表版本",
            VERSION_PET:"坐骑表版本",
            VERSION_DISPATCH:"派遣任务表版本",
            PLAYGAME_1_STATUS:"多游平台开关",
            PLAYGAME_2_STATUS:"闲玩平台开关",
            CHANNEL_FEE:"当前开通渠道需要的收益",
            CHANNEL_MAX_NUM:"渠道最大名额",
            CHANNEL_CASH_SILL:"渠道提现门槛",
            CESHITANCHUANG:"实时弹窗",
            BAI_IP:"白名单IP",
            REAL_NAME_REWARD:"实名认证奖励",
            UPDATE_GIFT:"更新礼包内容",
            IPHONE_V:"苹果版本",
            PLAYTEST_RATE:"试玩比例",
            PLAYTEST_TO_PARENT_RATE:"试玩上级返利",
            REGISTER_NUM:"限量注册名额",
            SYS_TRADING_USER_ID:"交易行上架使用的USERID",
            HOME_POPUP:"首页弹窗",
            APP_VERSION:"app版本",
            SERVER_MAX_CONNECT:"server最大连接数",
            GAME_DTS_STATUS:"大逃杀开关",
            RANK_IS_OPEN:"是否隐藏排行榜名字",
            TRAD_MIN:"交易行下限比例",
            TRAD_MAX:"交易行上限比例",
            GZS_FK:"工作室防控",
            PAY_TYPE:"支付通道 2支付宝",
            MAIL_CD:"邮件CD 分钟",
            MAIL_CD_STATUS:"是否开启邮件CD",
            ALIPAY_CASH_TYPE:"支付宝提现类型：1.支付宝ID 2手机号",
            GOOD_AD:"优质广告概率（1-100）",
            ALIPAY_MAX_NUMBER:"支付宝授权最大数量",
            CASH_LIMIT_DAY:"每日提现限额",
            CASH_LIMIT_TIPS:"提现失败提示信息",
            VIP_MONTH_PRICE:"月卡价格",
            VIP_WEEK_PRICE:"周卡价格",
            IS_REGISTER:"是否允许注册",
            TODAY_LUCKY:"优化线程数",
            VERSION_DAFUWENG:"大富翁表版本",
            LHD_STATUS:"2选1游戏状态",
            IP_USER_NUMBER:"同IP最多注册账号上限",
            IP_LOGIN_RISK:"同IP最多登录账号限制",
            CDM:"猜灯谜活动",
            GAME_SG_STATUS:"四大名著游戏状态",
            GAME_SG_RATE:"四大名著游戏消耗比例",
            QQ:"官方QQ群",
            SG_ISK:"军机情报游戏配置",
            GAME_DZ_GAME_ON:"打卡开关",
            GAME_DTS2_STATUS:"倩女幽魂游戏状态",
            GAME_DTS2_NEED_BOT:"倩女幽魂人机",
            GAME_LHD_NEED_BOT:"2杀1人机",
            PAY_NOTIFY_URL:"第三方支付回调地址",
            PAY_MERCHANT_ID:"第三方支付商户号",
            ADD_HP_WFSB:"每个文房四宝恢复体力值",
            GIFT_GAME_STATUS:"是否开放游戏币购买礼包",
            GIFT_PRICE_1_GAME:"游戏币购买礼包1价格",
            GIFT_PRICE_2_GAME:"游戏币购买礼包2价格",
            GIFT_RMB_STATUS:"是否开放RMB购买礼包",
            GIFT_PRICE_1:"RMB礼包1的价格",
            GIFT_PRICE_2:"RMB礼包2的价格",
            JZ_ITEM:"捐赠获得的道具ID",
            PAY_REDIRECT_URL:"支付后的回调地址",
            QNYH_RATE:"倩女幽魂概率",
            SIGN_REWARD:"签到奖励",
            PAY_CHANNEL:"支付通道 1  2汇付",
            PAY_NOTIFY_HF_URL:"汇付回调地址",
            VERSION_SHOP:"商店表版本",
            VV_USER_GIFT:"用户购买礼包，输入  用户id,数量",
            TEST_USER_NO:"测试账号",
            HF_SYS_ID:"汇付商户号",
            HF_RSA_PRIVATE_KEY:"汇付商户私钥",
            HF_RSA_PUBLIC_KEY:"汇付公钥",
            IS_AUTO_PAY:"是否自动打款",
            FREE_ROLE_NUM:"免费角色数量",
            ALIPAY_ONE_MONEY:"支付宝单笔转出金额",
            MZ_NEED_WHITE:"是否需要购买凭证",
            NEW_USER_TIME:"新用户注册时间",
            GAME_DTS_BOT_MONEY:"大逃杀金额",
            GAME_DTS_KILL_RATE:"大逃杀奇怪的概率",
            GAME_LHD_KILL_RATE:"2杀1奇怪的概率",
            RED_NUMBER:"红包瓜分个数",
            RED_SEND_COUNT:"踩雷发包次数",
            CHANNEL_RATE:"渠道返利比例",
            SHOW_TOP_LIST:"是否显示排行榜",
            ACTIVE1:"是否显示限时活动1",
            ACTIVE2:"是否显示限时活动2",
            ACTIVE3:"是否显示限时活动3",
            RED_RATE:"红包消耗比例",
            SHOOP_MANAGER:"店长申请通宝余额"
        };
        /*初始化表格*/
        function initTable() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bPaginate: false,
                bLengthChange: false,
                rowId: 'key',
                columns: [
                    {data: "key", title: '说明', render: function(data, type, row, setting) {
                            return markMap[data] || '-';
                        }},
                    /*{data: "key", title: 'Key11', render: datatableUtil.renderNormal},*/
                    {data: "value", title: 'Value', render: function(data, type, row, setting) {
                            var _valHtml = '<div style="white-space: pre-line;word-break: break-word;">'+data+'</div>';
                            return _valHtml;
                        }},
                    {data: "key", title: '操作', width: '160px', render: function(data, type, row, setting) {
                            var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.key + '\')"><i class="ti-slice"></i>修改</button>';
                            return _opearHtml;
                        }},
                ],
                drawCallback: function(){
                    $compile($('#${menuId}_baseTable'))($scope);
                },
                ajax: function (data, callback, settings) {
                    loadData(data, callback, settings)
                }
            });
        };
        /*请求数据*/
        function loadData(data, callback, settings) {
            if(me.loading) return;
            me.loading = true;
            websocketService.request('015001', null, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = [];
                    for(var key in result) {
                        var newRecord = {key: key, value: result[key]};
                        dataList.push(newRecord);
                        dataMap[key] = newRecord;
                    }
                    dataList.sort(compare('key'));
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: dataList.length,
                        recordsFiltered: dataList.length,
                        data: dataList
                    };
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
            });
        };
        var compare = function (prop) {
            return function (obj1, obj2) {
                var val1 = obj1[prop];
                var val2 = obj2[prop];if (val1 < val2) {
                    return -1;
                } else if (val1 > val2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /*编辑*/
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = '编辑系统配置';
                me.currentTpl._errorMsg = null;
                me.currentTpl._mark = markMap[id] || '';
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_rowModal').modal('show');
            }
        };
        /*保存*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.key || !submitData.value) {
                toastr.warning('请输入Key和Value', '温馨提示');
                return false;
            }
            me.saving = true;
            websocketService.request('015002', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_rowModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        /*初始化界面*/
        function initPage() {
            initTable();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>