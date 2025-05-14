<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var playDic = {'0': '关播', '1': '开播'};
        var playColorDic = {'0': 'text-gray', '1': 'text-success'};
        var talkDic = {'0': '禁言', '1': '正常'};
        var talkColorDic = {'0': 'text-danger', '1': ''};
        var orderTypeDic = {1: '热度', 2: '开播时间'};
        var talkDisableTimeDic = {5: '5分钟', 10: '10分钟', 30: '半小时', 60: '一小时', 0: '永久'};
        var playerStatusDic = {'0': '非主播', '1': '正常', '2': '审核中', '3': '拒绝', '4': '禁用', '5': '重新审核'};
        var playerStatusColorDic = {'0': 'text-muted', '1': '', '2': 'text-primary', '3': 'text-danger', '4': 'text-gray', '5': 'text-primary'};
        var switchDisableMap = {};
        var switchRecommendMap = {};
        var baseTable = null;
        /*初始化表格*/
        function initTable() {
            /*加载数据*/
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "liveNo", title: '房间号', render: datatableUtil.renderNormal},
                    {data: "play", title: '开播状态', render: function(data, type, row, setting){
                        return '<span class="' + (playColorDic[data] || 'text-gray') + '">' + (playDic[data] || '关播') + '</span>';
                    }},
                    {data: "userInfo", title: '主播账号', render: function(data, type, row, setting){
                        return data == null ? '-' : (data.phone || '-');
                    }},
                    {data: "userInfo", title: '主播', render: function(data, type, row, setting){
                        return data == null ? '-' : (data.nickname || '-');
                    }},
                    {data: "userInfo", title: '主播状态', render: function(data, type, row, setting){
                        return (data == null || data.playerStatus == null) ? '-' : '<span class="' + playerStatusColorDic[data.playerStatus] + '">' + playerStatusDic[data.playerStatus] + '</span>';
                    }},
                    {data: "title", title: '标题', render: datatableUtil.renderNormal},
                    {data: "image", title: '房间封面', render: function(data, type, row, setting){
                        return data == null ? '-' : '<a href="' + data + '" data-toggle="lightbox" data-title="' + row.title + '" data-lightbox="'+ row.id +'"><img src="' + data + '" class="td-img"></a>';
                    }},
                    {data: "viewerNum", title: '观看人数', render: function(data, type, row, setting){
                        return data = null ? 0 : data;
                    }},
                    {data: "virtualNum", title: '虚拟在线倍数', render: function(data, type, row, setting){
                        return data == null ? '-' : data;
                    }},
                    {data: "talk", title: '禁言', render: function(data, type, row, setting){
                        var _html = null;
                        if(data == 0) {
                            _html = '<span class="text-danger">' + (talkDic[data] || '-');
                            if(row.talkDisableTime != null) {
                                var _time = '永久';
                                if(row.talkDisableTime > 0) {
                                    _time = $filter('date')(row.talkDisableTime, 'MM-dd HH:mm');
                                }
                                _html += '（' + _time + '）';
                            }
                            _html += '</span>';
                        }
                        return _html || '-';
                    }},
                    {data: "talkDisableMark", title: '禁言原因', render: datatableUtil.renderNormal},
                    {data: "recommend", title: '推荐', render: function(data, type, row, setting){
                        return '<input type="checkbox" class="switch-recommend" ' + (data == 1 ? 'checked': '') + ' data-id="' + row.id + '">';
                    }},
                    {data: "recommendVal", title: '推荐值', render: function(data, type, row, setting){
                        return (row.recommend ? (data || 0) : '-');
                    }},
                    {data: "cityName", title: '城市', render: datatableUtil.renderNormal},
                    {data: "playTime", title: '开播时间', render: datatableUtil.renderTime},
                    {data: "closeTime", title: '关播时间', render: datatableUtil.renderTime},
                    {data: "id", title: '操作', width: '260px', render: function(data, type, row, setting) {
                        var _opearHtml = '<button class="btn waves-effect waves-light btn-danger btn-sm mr-2 ' + (row.talk == 0 ? 'd-none' : '') + '" ng-click="${menuId}.updateRow_talk(\'' + row.id + '\')"><i class="fa fa-microphone-slash"></i>禁言</button>'
                            + '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2 ' + (row.talk == 0 ? '' : 'd-none') + '" ng-click="${menuId}.cancelTalk(\'' + row.id + '\')"><i class="fa fa-microphone"></i>取消禁言</button>'
                            + '<button class="btn waves-effect waves-light btn-info btn-sm mr-2 ' + (row.play == 1 ? '' : 'd-none') + '" ng-click="${menuId}.showPlayer(\'' + row.id + '\')"><i class="fa fa-film"></i>查看直播</button>'
                            + '<button class="btn waves-effect waves-light btn-warning btn-sm mr-2 ' + (row.userInfo.playerStatus == 1 ? '' : 'd-none') + '" ng-click="${menuId}.updateRow_player(\'' + row.id + '\')"><i class="fa fa-ban"></i>禁用</button>'
                            + '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2 ' + (row.userInfo.playerStatus == 1 ? 'd-none' : '') + '" ng-click="${menuId}.enablePlayer(\'' + row.id + '\')"><i class="fa fa-desktop"></i>启用</button>'
                            + '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow_virtualNum(\'' + row.id + '\')"><i class="ti-shield"></i>修改虚拟在线倍数</button>';
                        return _opearHtml;
                    }}
                    
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
            me.loading = true;
            var searchData = {
                page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                limit: settings._iDisplayLength,
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            if(searchData.orderType == null) searchData.orderType = 1;
            websocketService.request('007011', searchData, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.list || [];
                    for(var i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i]; 
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
        };
        /*加载操作*/
        function loadOpearte() {
            /*推荐*/
            $('#${menuId}_baseTable').find('.switch-recommend').each(function(i, r){
                var $this = $(r);
                switchRecommendMap[$this.attr('data-id')] = new Switchery($this[0], { color: '#4099ff', jackColor: '#fff' });
            });
            $('#${menuId}_baseTable').find('.switch-recommend').siblings('.switchery')
            .on('click', function(e){
                var $this = $(this).siblings('.switch-recommend');
                var _id = $this.attr('data-id');
                var _record = dataMap[_id];
                var _switchery = switchRecommendMap[_id];
                if(_record && _switchery){
                    _switchery.setPosition(true);
                    if(!_switchery.isChecked()) {
                        var record = dataMap[_id];
                        if(record) {
                            me.currentTpl = _record;
                            me.currentTpl._title = _record.liveNo + '-' + (_record.playerNickname || '');
                            me.currentTpl._errorMsg = null;
                            me.currentTpl.$type = 'edit';
                            $('#${menuId}_formEdit_recommend').find('input[name],select[name],textarea[name]').val('').trigger('change');
                            $('#${menuId}_formEdit_recommend').find('input[name="recommendVal"]').val('1').trigger('change');
                            $('#${menuId}_rowModal_recommend').modal('show');
                        }
                    } else {
                        alertUtil.alert({
                            html: '即将变取消推荐<span class="ml-1 text-danger">' + (_record.liveNo + '-' + (_record.playerNickname || '')) + '</span> ？',
                            icon: 'warning',
                            confirm: function() {
                                if(me.saving) return;
                                me.saving = true;
                                var submitData = {id: _record.id, recommend: 0, recommendVal: +(_record.recommendVal || 0)};
                                websocketService.request('007014', submitData, function(command){
                                    if(command.success){
                                        baseTable.ajax.reload();
                                        Swal.close();
                                    } else {
                                        toastr.error(command.message || '提交异常', '系统提示');
                                        Swal.hideLoading();
                                    }
                                    me.saving = false;
                                });
                            }
                        });
                    }
                }
            });
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /*取消禁言*/
        me.cancelTalk = function(id){
            var record = dataMap[id];
            if(record) {
                alertUtil.alert({
                    html: '即将取消管理员禁言<span class="ml-1 text-danger">' + record.liveNo + ' - ' + (record.playerNickname || '') + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        websocketService.request('007013', {id: record.id}, function(command){
                            if(command.success){
                                baseTable.ajax.reload();
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
        };
        /*取消禁言*/
        me.updateRow_talk = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.liveNo + '-' + (record.playerNickname || '');
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit_talk').find('input[name],select[name],textarea[name]').val('').trigger('change');
                $('#${menuId}_rowModal_talk').modal('show');
            }
        };
        /*保存修改*/
        me.saveData_talk = function(){
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit_talk').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!amountUtil.testInt(submitData.talkDisableTime) && submitData.talkDisableTime != 0) {
                toastr.warning('禁言时长（分钟）必须是正整数', '温馨提示');
                return false;
            }
            submitData.talkDisableTime = submitData.talkDisableTime * 60 * 1000;
            submitData.id = me.currentTpl.id;
            me.saving = true;
            websocketService.request('007012', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_rowModal_talk').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        /*禁用*/
        me.updateRow_player = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.liveNo + '-' + (record.playerNickname || '');
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit_player').find('input[name],select[name],textarea[name]').val('').trigger('change');
                $('#${menuId}_rowModal_player').modal('show');
            }
        };
        /*保存禁用*/
        me.saveData_player = function(){
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit_player').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!submitData.playerStatusMark) {
                toastr.warning('请输入禁播原因', '温馨提示');
                return false;
            }
            submitData.playerStatus = 4;
            submitData.id = me.currentTpl.userInfo.id;
            me.saving = true;
            websocketService.request('010005', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_formEdit_player').find('input[name],select[name],textarea[name]').val('').trigger('change');
                    $('#${menuId}_rowModal_player').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        /*虚拟在线倍数*/
        me.updateRow_virtualNum = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = record;
                me.currentTpl._title = record.liveNo + '-' + (record.playerNickname || '');
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit_virtualNum').find('input[name],select[name],textarea[name]').val('').trigger('change');
                var _value = record['virtualNum'] || 1;
                $('#${menuId}_formEdit_virtualNum').find('input[name="virtualNum"]').val(_value).trigger('change');
                $('#${menuId}_rowModal_virtualNum').modal('show');
            }
        };
        /*保存修改*/
        me.saveData_virtualNum = function(){
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit_virtualNum').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!amountUtil.testInt(submitData.virtualNum)) {
                toastr.warning('虚拟在线倍数必须是正整数', '温馨提示');
                return false;
            }
            submitData.virtualNum = +(submitData.virtualNum);
            submitData.id = me.currentTpl.id;
            me.saving = true;
            websocketService.request('007014', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_rowModal_virtualNum').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        /*启用*/
        me.enablePlayer = function(id){
            var record = dataMap[id];
            if(record) {
                alertUtil.alert({
                    html: '即将变更主播状态为正常：<span class="ml-1 text-danger">' + record.liveNo + ' - ' + (record.playerNickname || '') + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {
                            id: record.userInfo.id,
                            playerStatus: 1,
                            playerStatusMark: ''
                        };
                        websocketService.request('010005', submitData, function(command){
                            if(command.success){
                                baseTable.ajax.reload();
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
        };
        /*保存修改*/
        me.saveData_recommend = function(){
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit_recommend').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(!amountUtil.testInt(submitData.recommendVal)) {
                toastr.warning('推荐值必须是正整数', '温馨提示');
                return false;
            }
            submitData.recommendVal = +(submitData.recommendVal);
            if(submitData.recommendVal > 1000) {
                toastr.warning('推荐值必须小于1000', '温馨提示');
                return false;
            }
            submitData.id = me.currentTpl.id;
            submitData.recommend = 1;
            me.saving = true;
            websocketService.request('007014', submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload();
                    me.currentTpl = null;
                    $('#${menuId}_rowModal_recommend').modal('hide');
                } else {
                    me.currentTpl._errorMsg = command.message || '';
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
            });
        };
        me.showPlayer = function(id) {
            var record = dataMap[id];
            if(record) {
                if(me.loading) return;
                me.currentTpl = record;
                me.currentTpl._title = record.liveNo + '-' + (record.playerNickname || '');
                me.currentTpl._errorMsg = null;
                me.loading = true;
                websocketService.request('007015', {id: record.id}, function(command){
                    if(command.success){
                        var result = command.data || {};
                        var player = cyberplayer("${menuId}_playercontainer").setup({
                            width: 680,
                            height: 448,
                            file: result.playUrl, /* <—rtmp直播地址*/
                            autostart: true,
                            stretching: "uniform",
                            volume: 100,
                            controls: true,
                            rtmp: {
                                reconnecttime: 5, /* rtmp直播的重连次数*/
                                bufferlength: 1 /* 缓冲多少秒之后开始播放 默认1秒*/
                            },
                            ak: "64c473253c3445adaef415479380d34a" /* 公有云平台注册即可获得accessKey*/
                        });
                        $('#${menuId}_rowModal_live').modal('show').on('hide.bs.modal', function(){
                            player.remove();
                        });
                    } else {
                        me.currentTpl._errorMsg = command.message || '';
                        toastr.error(command.message || '提交异常', '系统提示');
                    }
                    me.loading = false;
                });
                
            }
        };
        /* 排序 */
        function loadOrderTypeDic() {
            var strHtml = '';
            for(var key in orderTypeDic) {
                strHtml +='<option value="' + key + '">' + orderTypeDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="orderType"]').html(strHtml).trigger('change');
        };
        /* 禁言时间 */
        function loadTalkDisableTimeDic() {
            var strHtml = '';
            for(var key in talkDisableTimeDic) {
                strHtml +='<option value="' + key + '">' + talkDisableTimeDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="talkDisableTime"]').html(strHtml).trigger('change');
        };
        /* 禁言状态 */
        function loadTalkDic() {
            var strHtml = '<option value="">-- 请选择 --</option>';
            for(var key in talkDic) {
                strHtml +='<option value="' + key + '">' + talkDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="talk"]').html(strHtml).trigger('change');
        };
        /*初始化页面*/
        function initPage() {
            loadOrderTypeDic();
            loadTalkDisableTimeDic();
            loadTalkDic();
            /*datatableUtil.datepicker();*/
            initTable();
        }
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>