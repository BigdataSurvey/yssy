<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var statusDic = {'0': '待支付', '1,2': '已支付', '3': '已失效'};
        var statusColorDic = {'0': 'text-muted', '1,2': 'text-success', '3': 'text-gray'};
        var playerStatusDic = {'0': '非主播', '1': '正常', '2': '审核中', '3': '拒绝', '4': '禁用', '5': '重新审核'};
        var channelDic = {};
        var baseTable = null;
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "amount", title: '订单金额', render: datatableUtil.renderMoney},
                    {data: "realAmount", title: '实际支付金额', render: datatableUtil.renderMoney},
                    {data: "goldAmount", title: '兑换金币', render: function(data, type, row, setting){
                        return data == null ? '-' : parseInt(data);
                    }},
                    {data: "status", title: '状态', render: function(data, type, row, setting){
                        if(data == 1 || data == 2) data = '1,2';
                        return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                    }},
                    {data: "userInfo", title: '手机号', render: function(data, type, row, setting){
                        return data == null ? '-' : (data.phone || '-');
                    }},
                    {data: "userInfo", title: '昵称', render: function(data, type, row, setting){
                        return data == null ? '-' : (data.nickname || '-');
                    }},
                    {data: "userInfo", title: '主播状态', render: function(data, type, row, setting){
                        return data == null ? '-' : (playerStatusDic[data.playerStatus] || '-');
                    }},
                    {data: "userInfo", title: '观众等级', render: function(data, type, row, setting){
                        return data == null ? '-' : (data.viewerLevel || '-');
                    }},
                    {data: "channelName", title: '通道名称', render: datatableUtil.renderNormal},
                    {data: "orderNo", title: '订单号', render: datatableUtil.renderNormal},
                    {data: "channelOrderNo", title: '通道订单号', render: datatableUtil.renderNormal},
                    {data: "createTime", title: '创建时间', render: datatableUtil.renderTime},
                    {data: "payTime", title: '支付时间', render: datatableUtil.renderTime},
                    {data: "finishTime", title: '完成时间', render: datatableUtil.renderTime},
                    {data: "expiredTime", title: '过期时间', render: function(data, type, row, setting){
                        var _html = null;
                        if(data && row.status != 2) {
                            _html = $filter('date')(data, 'yyyy-MM-dd hh:mm:ss');
                        }
                        return _html || '-';
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
                if(_name == 'status') {
                    if(_value && _value.length > 0) {
                        _value = _value.join(',');
                    } else {
                        _value = null;
                    }
                }
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            websocketService.request('028001', searchData, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.list || [];
                    me.totalAmount = amountUtil.formatCurrency(result.totalAmount, 2);
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
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /* 状态 */
        function loadStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in statusDic) {
                strHtml +='<option value="' + key + '">' + statusDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="status"]').html(strHtml).trigger('change');
        };
        /* 通道列表 */
        function loadChannelDic() {
            websocketService.request('027001', null, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = result.channelList || [];
                    var strHtml = '<option value="">--请选择--</option>';
                    for(var i = 0; i < dataList.length; i++) {
                        var _record = dataList[i];
                        channelDic[_record.id] = _record; 
                        strHtml +='<option value="' + _record.id + '">' + _record.name + '</option>';
                    }
                    $('#${menuId}_content select[name="channelId"]').html(strHtml).trigger('change');
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                $scope.$apply();
            });
        };
        function initPage() {
            loadStatusDic();
            loadChannelDic();
            /*日期查询*/
            datatableUtil.datepicker(true);
            /*
            var endDate = new Date();
            $('#${menuId}_content input[id$="-end-time"]').datepicker('setDate', endDate);
            endDate.setMonth(endDate.getMonth() - 1);
            $('#${menuId}_content input[id$="-start-time"]').datepicker('setDate', endDate);
            */
            initTalbe();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>