<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.defaultTpl = {};
        let dataMap = {};
        let baseTable = null;
        $scope.searchModel = {userId:'',userName:'',userNo:'',startDate:'', endDate:''};
        let roomDic = {'0':'太古', '1': '大荒', '2': '蓬莱', '3': '极阴', '4': '极寒'};
        let statusColorDic = {'0': 'text-gray', '1': 'text-success', '2': 'text-danger'};
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
                    {data: "userId", title: '用户ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '用户昵称', render: datatableUtil.renderNormal},
                    {data: "periodsNum", title: '期数', render: datatableUtil.renderNormal},
                    {data: "betInfo", title: '选择房间', render: function(data, type, row, setting){
                            return roomDic[data] || '-';
                        }},
                    {data: "betAmount", title: '数量', render: datatableUtil.renderNormal},
                    {data: "profit", title: '收益', render: datatableUtil.renderNormal},
                    {data: "lotteryResult", title: '胜利房间', render: function(data, type, row, setting){
                            return roomDic[data] || '-';
                        }},
                    {data: "winOrLose", title: '结果', render: function(data, type, row, setting){
                        return parseInt(data) == 1 ? "胜利" : "失败";
                        }},
                    {data: "createTime", title: '创建时间', render: datatableUtil.renderTime},
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
            websocketService.request('021130', searchData, function(command){
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