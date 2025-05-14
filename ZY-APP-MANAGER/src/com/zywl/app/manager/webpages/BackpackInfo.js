<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, $stateParams, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.defaultTpl = {};
        let dataMap = {};
        let baseTable = null;
        $scope.searchModel = {userId:'',userName:'',userNo:'',startDate:'', endDate:''};
        me.queryParam = $stateParams.params;
        /*初始化表格*/
        function initTable() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                scrollCollapse: true,
                bLengthChange: false,
                columns: [
                    {data: "idx", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "id", title: 'ID', render: datatableUtil.renderNormal, visible:false},
                    {data: "userId", title: '用户平台ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '用户昵称', render: datatableUtil.renderNormal},
                    {data: "itemId", title: '道具ID', render: datatableUtil.renderNormal},
                    {data: "itemNumber", title: '道具数量', render: datatableUtil.renderNormal},
                    {data: "updateTime", title: '更新时间', render: datatableUtil.renderTime},
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
        function loadData(data, callback, settings){
            if(me.loading) return;
            me.loading = true;

            let searchData = {
                page: (settings._iDisplayStart / settings._iDisplayLength) + 1,
                limit: settings._iDisplayLength
            };
            $('#${menuId}_search').find('input[name],select[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            if(me.queryParam > 0) {
                searchData['userId'] = me.queryParam;
                me.queryParam = 0;
            }
            websocketService.request('021041', searchData, function(command){
                if(command.success){
                    let result = command.data || {};
                    let dataList = result.list || [];
                    for(let i = 0; i < dataList.length; i++) {
                        dataMap[dataList[i].id] = dataList[i];
                    }
                    let resultData = {
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
                $scope.$apply()
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
        me.search = function () {
            baseTable.ajax.reload();
        };
        function initPage() {
            initTable();
            var endDate = new Date();
            $('#${menuId}_content input[id$="-end-time"]').datepicker('setDate', endDate);
            endDate.setMonth(endDate.getMonth() - 1);
            $('#${menuId}_content input[id$="-start-time"]').datepicker('setDate', endDate);
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>