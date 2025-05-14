<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, storageService, websocketService, toastr){
        var me = this;
        var dataMap = {};
        var baseTable = null;
        var baseTable_detail = null;
        /*初始化表格*/
        function initTable() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bPaginate: false,
                bLengthChange: false,
                rowId: 'key',
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting) {
                        return setting.row + 1;
                    }},
                    {data: "name", title: '名称', render: function(data, type, row, setting){
                        return '<button class="btn waves-effect waves-light btn-link btn-sm mr-2" ng-click="${menuId}.loadData_detail(\'' + row.id + '\')">' + data + '</button>';
                    }},
                    {data: "value", title: '次数', render: datatableUtil.renderPrice}
                    ],
                    drawCallback: function(){
                        $compile($('#${menuId}_baseTable'))($scope);
                    },
                    ajax: function (data, callback, settings) {
                        loadData(data, callback, settings)
                    }
            });
        };
        /*初始化表格*/
        function initTalbe_detail() {
            baseTable_detail = datatableUtil.init('#${menuId}_baseTable_detail', {
                bPaginate: false,
                bLengthChange: false,
                columns: [
                    {data: "id", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "userNo", title: '用户账号', render: datatableUtil.renderNormal},
                    {data: "time", title: '记录时间', render: datatableUtil.renderTime}
                    ],
                    drawCallback: function(){
                        
                    },
                    ajax: function (data, callback, settings) {
                        loadData_detail(data, callback, settings);
                    }
            });
        };
        /*请求数据*/
        function loadData(data, callback, settings) {
            if(me.loading) return;
            me.loading = true;
            websocketService.request('010006', null, function(command){
                if(command.success){
                    var result = command.data || {};
                    var dataList = [];
                    for(var key in result) {
                        var record = {id:key, name: key, value: Object.keys(result[key]).length, data: result[key]};
                        dataList.push(record);
                        dataMap[record.id] = record;
                    }
                    dataList.sort(datatableUtil.compare('value'));
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
        function loadData_detail(data, callback, settings) {
            var dataList = [];
            var record = dataMap[me.currentId];
            if(record && record.data) {
                for(var key in record.data) {
                    var r = {id: key, userNo: key, time: record.data[key]};
                    dataList.push(r);
                }
            }
            dataList.sort(datatableUtil.compare('time'));
            var resultData = {
                draw: data.draw,
                recordsTotal: dataList.length,
                recordsFiltered: dataList.length,
                data: dataList
            };
            callback(resultData);
        };
        /*刷新*/
        me.loadData = function() {
            baseTable.ajax.reload();
        };
        /*加载明细*/
        me.loadData_detail = function(id) {
            me.currentId = id;
            var record = dataMap[me.currentId];
            if(record) {
                me.currentTpl = {_title: record.name};
            }
            baseTable_detail.ajax.reload();
            $('#${menuId}_detailModal').modal('show');
        };
        /*初始化界面*/
        function initPage() {
            initTable();
            initTalbe_detail();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>