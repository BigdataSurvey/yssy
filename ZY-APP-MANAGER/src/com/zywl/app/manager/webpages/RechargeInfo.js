<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        let dataMap = {};
        let statusDic = {'0':'未支付', '1': '支付成功', '2': '支付失败', '3': '支付超时'};
        let statusColorDic = {'0': 'text-gray', '1': 'text-success', '2': 'text-danger', '3': 'text-success', '4': 'text-danger'};
        let baseTable = null;
        me.switchTab = function(index){
            me.loadData();
        };
        /*初始化表格*/
        function initTalbe() {
            baseTable = datatableUtil.init('#${menuId}_baseTable', {
                bLengthChange: false,
                columns: [
                    {data: "idx", title: '#', render: function(data, type, row, setting){
                        return setting.row + 1;
                    }},
                    {data: "id", title: 'ID', render: datatableUtil.renderNormal,visible:false},
                    {data: "userId", title: '用户ID', render: datatableUtil.renderNormal},
                    {data: "userName", title: '用户昵称', render: datatableUtil.renderNormal},
                    {data: "orderNo", title: '订单号', render: datatableUtil.renderNormal},
                    {data: "prepayId", title: 'prepayId', render: datatableUtil.renderNormal},
                    {data: "productId", title: 'productId', render: datatableUtil.renderNormal},
                    {data: "price", title: '价格', render: datatableUtil.renderNormal},
                    {data: "status", title: '状态', render: function(data, type, row, setting){
                        return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                    }},
                    {data: "createTime", title: '申请时间', render: datatableUtil.renderTime},
                    {data: "expireTime", title: '过期时间', render: datatableUtil.renderTime}
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

                searchData[_name] = _value == null ? null : $.trim(_value);
            });
            websocketService.request('021113', searchData, function(command){
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
            baseTable.ajax.reload(null, false);
        };
        me.search = function () {
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