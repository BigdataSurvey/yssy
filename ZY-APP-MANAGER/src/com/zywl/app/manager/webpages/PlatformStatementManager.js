<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var dataMap_log = {};
        var statusDic = {'0': '禁用', '1': '启用'};
        var statusColorDic = {'0': 'text-danger', '1': 'text-success'};
        var sexDic = {'1': '男','0': '女'};
		var authenticationDic={'1':'已实名','0':'未实名'};
        var liveManagerDic = {'1': '是','0': '否'};
        var playerStatusDic = {'0': '非主播', '1': '正常', '2': '审核中', '3': '拒绝', '4': '禁用', '5': '重新审核'};
        var playerStatusColorDic = {'0': 'text-muted', '1': '', '2': 'text-primary', '3': 'text-danger', '4': 'text-gray', '5': 'text-primary'};
        var playerStatusEditDic = {'0': '非主播', '1': '正常', '4': '禁用', '5': '重新审核'};
        var onlineDic = {'0': '离线', '1': '在线'};
        var amountTypeDic = {'15': '添加普通金币', '16': '扣除普通金币', '17': '添加可提现金币', '18': '扣除可提现金币'};
        var playerLiveTypeDic = {'1': '直播', '2': '热门', '4': '帅哥'};
        var playerLiveTypeColorDic = {'1': '', '2': '', '4': ''};
        var switchStatusMap = {};
        var switchLiveManagerMap = {};
        var baseTable = null;
        var baseTable_log = null;
        var gaodeMap = null;
        var currentMarker = null;
        me.defaultFeeTpl = {};
        me.hideLocation = true;
        if(storageService.get('_hideLocation') == 0) {
            me.hideLocation = false;
        }
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                scrollY: '50vh',
                scrollX: true,
                scrollCollapse: true,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "ymd", title: '日期', render: datatableUtil.renderNormal},
                    {data: "allOutPut", title: '今日总产出', render: datatableUtil.renderNormal},
                    {data: "allExpend", title: '今日总消耗', render: datatableUtil.renderNormal},
                    {data: "addDice", title: '捐赠产出通宝', render: datatableUtil.renderNormal},
                    {data: "addDailyTask", title: '每日任务增加', render: datatableUtil.renderNormal},
                    {data: "addReceiveAchievement", title: '领取成就奖励', render: datatableUtil.renderNormal},
                    {data: "addWander", title: '商城产出通宝', render: datatableUtil.renderNormal},
                    {data: "addSellSys", title: '背包卖出道具', render: datatableUtil.renderNormal},
                    {data: "addSell", title: '交易行出售获得', render: datatableUtil.renderNormal},
                    {data: "addReceiveIncome", title: '领取收益', render: datatableUtil.renderNormal},
                    {data: "subShop", title: '商店购买', render: datatableUtil.renderNormal},
                    {data: "subTradingBuy", title: '交易行购买消耗', render: datatableUtil.renderNormal},
                    {data: "subGift", title: '购买每日礼包', render: datatableUtil.renderNormal},
                    {data: "subMine", title: '开通书境消耗', render: datatableUtil.renderNormal},
                    {data: "subDts2", title: '倩女幽魂参与', render: datatableUtil.renderNormal},
                    {data: "addDts2", title: '倩女幽魂获得', render: datatableUtil.renderNormal},
                    {data: "subNh", title: '躲雨参与', render: datatableUtil.renderNormal},
                    {data: "addNh", title: '躲雨获得', render: datatableUtil.renderNormal},
                    {data:"subDzGame",title: '报名打坐',render: datatableUtil.renderNormal},
                    {data:"addDzGame",title: '领取打坐',render: datatableUtil.renderNormal},
                    {data:"subDgs",title: '打怪参与',render: datatableUtil.renderNormal},
                    {data:"addDgs",title: '打怪获得',render: datatableUtil.renderNormal}







                    ],
                    drawCallback: function(){
                        loadOpearte();
                    },
                    ajax: function (data, callback, settings) {
                        loadData(data, callback, settings);
                    }
            });
        };
        /*请求数据*/
        function loadData(data, callback, settings) {
            if(me.loading) return;
            var searchData = {
                page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                limit: settings._iDisplayLength,
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                if(_name == 'status' || _name == 'playerStatus') {
                    if(_value && _value.length > 0) {
                        _value = _value.join(',');
                    } else {
                        _value = null;
                    }
                }
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            if(searchData.userNo && !amountUtil.testInt(searchData.userNo)) {
                toastr.warning('蜜桃号必须为数字', '温馨提示');
                return false;
            }
            me.loading = true;
            websocketService.request('019001', searchData, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.list || [];
                    for(var i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i]; 
                    }
                    me.defaultFeeTpl = result.defaultFee;
                    var resultData = {
                        draw: data.draw,
                        recordsTotal: result.count,
                        recordsFiltered: result.count,
                        data: dataList
                    };
                    callback(resultData);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                me.loading = false;
                $scope.$apply();
            });
        };
        /*加载操作*/
        function loadOpearte() {
            $('#${menuId}_baseTable').find('.switch-status').each(function(i, r){
                var $this = $(r);
                switchStatusMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-status').siblings('.switchery')
            .on('click', function(e){
                var $this = $(this).siblings('.switch-status');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchStatusMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    alertUtil.alert({
                        html: '即将变更状态为<span class="ml-1 ' + (_newState == 1 ? 'text-success' : 'text-danger') + '">' + (_newState == 1 ? '启用' : '禁用') + '</span> ？',
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, status: _newState};
                            websocketService.request('010005', submitData, function(command){
                                if(command.success){
                                    _record.status = _newState;
                                    datatableUtil.setSwitchery(_switchery, !_switchery.isChecked());
                                    Swal.close();
                                } else {
                                    toastr.error(command.message || '提交异常', '系统提示');
                                    Swal.hideLoading();
                                }
                                me.saving = false;
                                $scope.$apply();
                            });
                        }
                    });
                }
            });
            $('#${menuId}_baseTable').find('.switch-liveManager').each(function(i, r){
                var $this = $(r);
                switchLiveManagerMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-liveManager').siblings('.switchery')
            .on('click', function(e){
                var $this = $(this).siblings('.switch-liveManager');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchLiveManagerMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    var _newState = _switchery.isChecked() ? 0: 1;
                    var _html = '即将变更用户<span class="text-primary ml-1 mr-1">' + (_record.name || _record.nickname) + '</span>为<span class="ml-1 text-success">超管</span> ？';
                    if(_newState == 0) {
                        _html= '即将取消用户<span class="text-primary ml-1 mr-1">' + (_record.name || _record.nickname) + '</span>的<span class="ml-1 mr-1 text-danger">超管</span>权限 ？'
                    }
                    alertUtil.alert({
                        html: _html,
                        icon: 'warning',
                        confirm: function() {
                            if(me.saving) return;
                            me.saving = true;
                            var submitData = {id: _record.id, liveManager: _newState};
                            websocketService.request('010005', submitData, function(command){
                                if(command.success){
                                    _record.liveManager = _newState;
                                    datatableUtil.setSwitchery(_switchery, !_switchery.isChecked());
                                    Swal.close();
                                } else {
                                    toastr.error(command.message || '提交异常', '系统提示');
                                    Swal.hideLoading();
                                }
                                me.saving = false;
                                $scope.$apply();
                            });
                        }
                    });
                }
            });
            $('#${menuId}_content [data-toggle="tooltip"]').tooltip();
            baseTable && baseTable.columns.adjust();
            $compile($('#${menuId}_baseTable'))($scope);
            if(window.___clipboard) window.___clipboard.destroy();
            window.___clipboard = new ClipboardJS('#${menuId}_content .btn-copy');
            window.___clipboard.on('success', function(e) {
                toastr.success('复制成功');
                e.clearSelection();
            });
        };
        /*初始化表格*/
        function initTalbe_log() {
            baseTable_log = datatableUtil.init('#${menuId}_baseTable_log', {
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "name", title: '名称', render: datatableUtil.renderNormal},
                    {data: "nickname", title: '昵称', render: datatableUtil.renderNormal},
                    {data: "phone", title: '联系电话', render: datatableUtil.renderNormal},
                    {data: "sex", title: '性别', render: function(data, type, row, setting){
                        return data == null ? '-' :sexDic[data];
                    }},
                    {data: "playerStatus", title: '主播状态', render: function(data, type, row, setting){
                        return data == null ? '-' :'<span class="' + playerStatusColorDic[data] + '">' + playerStatusDic[data] + '</span>';
                    }},
                    {data: "status", title: '状态', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-status" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "photo", title: '自拍照', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a href="' + data + '" data-toggle="lightbox" data-title="自拍照" data-lightbox="'+ row.id +'"><img src="' + data + '" class="td-img"></a>';
                    }},
                    {data: "card", title: '手持证件照', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a href="' + data + '" data-toggle="lightbox" data-title="手持证件照" data-lightbox="'+ row.id +'"><img src="' + data + '" class="td-img"></a>';
                    }},
                    {data: "registIp", title: '注册IP', render: datatableUtil.renderNormal},
                    {data: "registTime", title: '注册时间', render: datatableUtil.renderTime},
                    {data: "lastLoginTime", title: '最后登录时间', render: datatableUtil.renderTime},
                    {data: "lastLoginIp", title: '最后登录IP', render: datatableUtil.renderNormal},
                    {data: "playerStatusMark", title: '主播状态变更原因', render: datatableUtil.renderNormal},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>'
                            + '<button class="btn waves-effect waves-light btn-info btn-sm mr-2" ng-click="${menuId}.mapRow(\'' + row.id + '\')"><i class="ti-map-alt"></i>定位</button>';
                            /*
                            + '<button class="btn waves-effect waves-light btn-purple btn-sm mr-2" ng-click="${menuId}.logRow(\'' + row.id + '\')"><i class="ti-map-alt"></i>流水</button>';
                            */
                        return _opearHtml;
                    }},
                    ],
                    drawCallback: function(){
                        baseTable_log && baseTable_log.columns.adjust();
                        $compile($('#${menuId}_baseTable_log'))($scope);
                    },
                    ajax: function (data, callback, settings) {
                        loadData_log(data, callback, settings);
                    }
            });
        };
        /*请求数据*/
        function loadData_log(data, callback, settings) {
            /*TODO:测试代码预留*/
            if(!me.currentTpl || !me.currentTpl.id || 1 == 1) {
                var resultData = {
                    draw: data.draw,
                    recordsTotal: 0,
                    recordsFiltered: 0,
                    data: []
                };
                callback(resultData);
                return false;
            } else {
                if(me.loading) return;
                me.loading = true;
                var searchData = {
                        page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                        limit: settings._iDisplayLength,
                        userId: me.currentTpl.id
                };
                websocketService.request('', searchData, function(command){
                    if(command.success){
                        var result = command.data || {};
                        var dataList = result.list || [];
                        for(var i = 0; i < dataList.length; i++) {
                            dataMap_log[dataList[i].id] = dataList[i]; 
                        }
                        var resultData = {
                                draw: data.draw,
                                recordsTotal: result.count,
                                recordsFiltered: result.count,
                                data: dataList
                        };
                        callback(resultData);
                    } else {
                        toastr.error(command.message || '查询异常', '系统提示');
                    }
                    me.loading = false;
                    $scope.$apply();
                });
            }
        };
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /*查看定位*/
        me.mapRow = function(id) {
            var record = dataMap[id];
            if(record && record.locationX && record.locationY) {
                me.currentTpl = record;
                me.currentTpl._title = record.phone + ' - ' + (record.nickname || '');
                var gps = [record.locationX, record.locationY];
                AMap.convertFrom(gps, 'gps', function (status, result) {
                    if (result.info === 'ok') {
                        var lnglats = result.locations;
                        currentMarker = new AMap.Marker({
                            position: new AMap.LngLat(lnglats[0].Q, lnglats[0].P),
                            title: me.currentTpl._title
                        });
                        gaodeMap.add(currentMarker);
                        gaodeMap.setCenter([record.locationX, record.locationY]);
                        gaodeMap.setZoom(12); 
                        $('#${menuId}_mapModal').modal('show').on('hide.bs.modal', function(){
                            if(currentMarker) {
                                gaodeMap.remove(currentMarker);
                                currentMarker = null;
                            }
                        });
                    } else {
                        toastr.warning('定位失败', '温馨提示');
                    }
                });
            } else {
                toastr.warning('暂无定位信息', '温馨提示');
            }
        };
        /*修改*/
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.phone + ' - ' + (record.nickname || '');
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    if(key == 'playerGiftFee' || key == 'playerTicketFee' || key == 'playerChargeFee' || key == 'playerGuardFee'){
                        record[key] = record[key] == -1 ? null : record[key]; 
                    }
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_rowModal').modal('show');
            }
        };
        /*金币校正*/
        me.amountRow = function(id) {
            var record = dataMap[id];
            if(record) {
                me.saving = true;
                websocketService.request('017001', {userId: record.id}, function(command){
                    if(command.success){
                        var result = command.data || {};
                        me.currentTpl = record;
                        me.currentTpl._title = record.phone + ' - ' + (record.nickname || '');
                        me.currentTpl._amountTpl = result;
                        me.currentTpl._amount = amountUtil.formatCurrency(parseInt(result.availableAmount));
                        me.currentTpl._income = amountUtil.formatCurrency(parseInt(result.totalRealIncomeAmount));
                        me.currentTpl._errorMsg = null;
                        me.currentTpl._walletId = result.id;
                        me.currentTpl.$type = 'amount';
                        $('#${menuId}_formEdit_amount').find('input[name],select[name],textarea[name]').val('').trigger('change');
                        $('#${menuId}_rowModal_amount').modal('show');
                    } else {
                        toastr.error(command.message || '查询异常', '系统提示');
                    }
                    me.saving = false;
                    $scope.$apply();
                });
            }
        };
        /*查看流水*/
        me.logRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.saving = true;
                websocketService.request('017001', {userId: record.id}, function(command){
                    if(command.success){
                        var result = command.data || {};
                        me.currentTpl = record;
                        me.currentTpl._title = record.phone + ' - ' + (record.nickname || '');
                        me.currentTpl._amountTpl = result;
                        me.currentTpl._errorMsg = null;
                        me.currentTpl._walletId = result.id;
                        me.currentTpl.$type = 'log';
                        $('#${menuId}_list').addClass('d-none');
                        $('#${menuId}_log').removeClass('d-none');
                        baseTable_log.ajax.reload();
                    } else {
                        toastr.error(command.message || '查询异常', '系统提示');
                    }
                    me.saving = false;
                    $scope.$apply();
                });
            }
        };
        /*返回至列表*/
        me.backList = function(){
            me.currentTpl = null;
            $('#${menuId}_list').removeClass('d-none');
            $('#${menuId}_log').addClass('d-none');
        };
        /*刷新流水*/
        me.loadData_log = function() {
            baseTable_log.ajax.reload();
        };
        function testFee(data) {
            var result = false;
            if(data != null && data !='' && data != -1) {
                data = parseFloat(data);
                if(!isNaN(data) && data >= 0.1 && data <= 1){
                    result = true;
                }
            } else {
                result = true;
            }
            return result;
        }
        /*修改*/
        me.saveData = function() {
            if(me.saving) return;
            var submitData = {
                id: me.currentTpl.id
            };
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.nickname) {
                toastr.warning('请输入昵称', '温馨提示');
                return false;
            }
            if(submitData.playerStatus == 4 || submitData.playerStatus == 5) {
                if(!submitData.playerStatusMark) {
                    toastr.warning('请输入主播状态变更原因', '温馨提示');
                    return false;
                }
            } else {
                submitData.playerStatusMark = '';
            }
            me.saving = true;
            websocketService.request('010005', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload(null, false);
                    me.currentTpl = null;
                    $('#${menuId}_rowModal').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        /*资产校正*/
        me.saveData_amount = function() {
            if(me.saving) return;
            var submitData = {
                id: me.currentTpl._walletId
            };
            $('#${menuId}_formEdit_amount').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.type) {
                toastr.warning('请选择操作类型', '温馨提示');
                return false;
            }
            if(!submitData.mark) {
                toastr.warning('请输入备注', '温馨提示');
                return false;
            }
            if(!amountUtil.testInt(submitData.amount)) {
                toastr.warning('金币格式不合法', '温馨提示');
                return false;
            }
            me.saving = true;
            websocketService.request('017002', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload(null, false);
                    me.currentTpl = null;
                    $('#${menuId}_formEdit_amount').find('input[name],select[name],textarea[name]').val('').trigger('change');
                    $('#${menuId}_rowModal_amount').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        me.showLocation = function(){
            me.hideLocation = false;
            storageService.set('_hideLocation', '0');
            $scope.$apply();
        };
        /* 性别 */
        function loadSexDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in sexDic) {
                strHtml +='<option value="' + key + '">' + sexDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="sex"]').html(strHtml).trigger('change');
        };
        /* 状态 */
        function loadStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in statusDic) {
                strHtml +='<option value="' + key + '">' + statusDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="status"]').html(strHtml).trigger('change');
        };
        /* 主播状态 */
        function loadPlayerStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in playerStatusDic) {
                strHtml +='<option value="' + key + '">' + playerStatusDic[key] + '</option>';
            }
            $('#${menuId}_search select[name="playerStatus"]').html(strHtml).trigger('change');
        };
        /* 主播状态：修改 */
        function loadPlayerStatusEditDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in playerStatusEditDic) {
                strHtml +='<option value="' + key + '">' + playerStatusEditDic[key] + '</option>';
            }
            $('#${menuId}_rowModal select[name="playerStatus"]').html(strHtml).trigger('change');
        };
        /* 超管 */
        function loadLiveManagerDicDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in liveManagerDic) {
                strHtml +='<option value="' + key + '">' + liveManagerDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="liveManager"]').html(strHtml).trigger('change');
        };
        /* 在线状态 */
        function loadLiveOnlineDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in onlineDic) {
                strHtml +='<option value="' + key + '">' + onlineDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="online"]').html(strHtml).trigger('change');
        };
        /* 资产校正类型 */
        function loadAmountTypeDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in amountTypeDic) {
                strHtml +='<option value="' + key + '">' + amountTypeDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="type"]').html(strHtml).trigger('change');
        };
        /* 频道 */
        function loadPlayerLiveTypeDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in playerLiveTypeDic) {
                strHtml +='<option value="' + key + '">' + playerLiveTypeDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="playerLiveType"]').html(strHtml).trigger('change');
        };
        function initPage() {
            loadSexDic();
            loadStatusDic();
            loadPlayerStatusDic();
            loadPlayerStatusEditDic();
            loadLiveManagerDicDic();
            loadLiveOnlineDic();
            loadAmountTypeDic();
            loadPlayerLiveTypeDic();
            /*日期查询*/
            datatableUtil.datepicker(true);
            initTalbe();
            initTalbe_log();
            gaodeMap = new AMap.Map('${menuId}_map');
            $('#${menuId}_form_palyerStatus').on('change', function(){
                var _val = $(this).val();
                if(_val == 4 || _val == 5){
                    me.currentTpl._noMark = false;
                    $('#${menuId}_form_palyerStatus_mark').removeAttr('disabled');
                } else {
                    $('#${menuId}_form_palyerStatus_mark').attr('disabled', 'disabled');
                }
            });
            /*伸缩表格头*/
            $(window).on('resize', function(){
                baseTable && baseTable.columns.adjust();
                baseTable_log && baseTable_log.columns.adjust();
            });
        };
        initPage();
        
        
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>