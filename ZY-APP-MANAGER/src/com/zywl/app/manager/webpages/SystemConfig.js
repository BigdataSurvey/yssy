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
            TRANSFER_FEE: "好友转增手续费",
            TRANSFER_FEE2: "好友转增手续费2",
            TRANSFER_FEE3: "好友转增手续费3",
            TRANSFER_SILL: "好友转赠起赠门槛",
            USER_NO_LENGTH: "ID长度",
            VERSION_ITEM: "物品表版本",
            VERSION_BOSS_CARD: "BOOS卡牌表版本",
            VERSION_CARD: "卡牌基础属性表版本",
            VERSION_CHECKPOINT: "主线关卡表版本",
            VERSION_EQUIPMENT: "装备表版本",
            VERSION_EQU_SYN: "装备合成表版本",
            VERSION_MINE:"矿场表版本",
            VERSION_TOWER:"试炼之塔表版本",
            VERSION_CARD_ASCEND:"卡牌升星表版本",
            SALVAGE_EXP_1:"分解1品质获得经验",
            SALVAGE_EXP_2:"分解2品质获得经验",
            SALVAGE_EXP_3:"分解3品质获得经验",
            SALVAGE_EXP_4:"分解4品质获得经验",
            SALVAGE_EXP_5:"分解5品质获得经验",
            VERSION_PET:"坐骑表版本",
            VERSION_ARTIFACT:"神兵表版本",
            VERSION_SKILL:"技能表版本",
            VERSION_DISPATCH:"派遣任务表版本",
            PLAYGAME_1_STATUS:"多游平台开关",
            PLAYGAME_2_STATUS:"闲玩平台开关",
            CHANNEL_FEE:"当前开通渠道需要的收益",
            CHANNEL_MAX_NUM:"渠道最大名额",
            CHANNEL_CASH_SILL:"渠道提现门槛",
            SALVAGE_STAR:"分解卡牌给的星星数",
            CESHITANCHUANG:"实时弹窗",
            BAI_IP:"白名单IP",
            EQUIPMENT_VERSION:"装备表版本",
            FLOWER_AMOUNT:"贺卡一朵花的价值",
            MAX_LV:"卡牌最大等级",
            CAN_BUY_HEAD_BOX:"是否可以购买头像框",
            BBSC_1:"步步生财1号价格",
            REAL_NAME_REWARD:"实名认证奖励",
            BBSC_DAYS_1:"步步生财1号天数",
            BBSC_DICE_1:"步步生财1号骰子数",
            BBSC_2:"步步生财2号价格",
            BBSC_DAYS_2:"步步生财2号天数",
            BBSC_DICE_2:"步步生财2号骰子数",
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
            PJZL_STATUS:"破军之旅游戏状态",
            XWMJ_STATUS:"虚妄迷局游戏状态",
            USER_CARD_MAX_COUNT:"玩家最大卡牌上限",
            IP_USER_NUMBER:"同IP最多注册账号上限",
            IP_LOGIN_RISK:"同IP最多登录账号限制",
            CDM:"猜灯谜活动",
            GAME_SG_STATUS:"签与签寻游戏状态",
            QQ:"官方QQ群",
            SG_ISK:"军机情报游戏配置",
            GAME_DZ_GAME_ON:"打卡开关",
            GAME_DTS2_STATUS:"倩女幽魂游戏状态"


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