<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var dataMap_family = {};
        var statusDic = {'0':'未审核', '1': '等待推送', '2': '推送至微信', '3': '提现成功', '4': '提现失败'};
        var statusColorDic = {'0': 'text-gray', '1': 'text-success', '2': 'text-danger', '3': 'text-success', '4': 'text-danger'};
        var baseTable = null;
        var baseTable_family = null;
        me.currentTab = 1;
        me.switchTab = function(index){
            me.currentTab = index || 2;
            if(index == 1 && Object.keys(dataMap).length == 0){
                me.loadData();
            } else if (index == 2 && Object.keys(dataMap_family).length == 0){
                me.loadData();
            }
        };
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "userId", title: '用户平台ID', render: datatableUtil.renderNormal},
                    {data: "userNo", title: '用户ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '用户昵称', render: datatableUtil.renderNormal},
                    {data: "realName", title: '真实姓名', render: datatableUtil.renderNormal},
                    {data: "openId", title: 'openID', render: datatableUtil.renderNormal},
                    {data: "orderNo", title: '订单号码', render: datatableUtil.renderNormal},
                    /*{data: "batchOrderNo", title: '微信批号', render: datatableUtil.renderNormal},*/
                    {data: "status", title: '状态', render: function(data, type, row, setting){
                        return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                    }},
                    {data: "amount", title: '提现金额', render: datatableUtil.renderMoney},
                    {data: "receivedAmount", title: '收到金额', render: datatableUtil.renderMoney},
                    {data: "fee", title: '手续费', render: datatableUtil.renderMoney},
                    {data: "createTime", title: '申请时间', render: datatableUtil.renderTime},
                    {data: "updateTime", title: '审核时间', render: datatableUtil.renderTime},
                    {data: "remark", title: '原因', render: datatableUtil.renderNormal}
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
                type: 1
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                if(_name == 'status') {
                    if(_value && _value.length > 0) {
                        _value = _value.join(',');
                    } else {
                        _value = '-1';
                    }
                }
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            websocketService.request('021111', searchData, function(command){
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
            $compile($('#${menuId}_baseTable'))($scope);
        }
        /*刷新*/
        me.loadData = function() {
            if(me.currentTab == 1) {
                baseTable.ajax.reload(null, false);
            } else if(me.currentTab == 2){
                baseTable_family.ajax.reload();
            }
        };
        /* 状态 */
        function loadStatusDic() {
            var strHtml = '<option value="">--请选择--</option>';
            for(var key in statusDic) {
                strHtml +='<option value="' + key + '">' + statusDic[key] + '</option>';
            }
            $('#${menuId}_content select[name="status"]').html(strHtml).trigger('change');
        };
        function initPage() {
            loadStatusDic();
            /*日期查询*/
            datatableUtil.datepicker(true);
            var endDate = new Date();
            $('#${menuId}_content input[id$="-create-end-time"]').datepicker('setDate', endDate);
            endDate.setMonth(endDate.getMonth() - 1);
            $('#${menuId}_content input[id$="-create-start-time"]').datepicker('setDate', endDate);
            initTalbe();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>