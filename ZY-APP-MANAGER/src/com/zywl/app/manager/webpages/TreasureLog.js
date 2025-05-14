<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.defaultTpl = {};
        let dataMap = {};
        let baseTable = null;
        $scope.searchModel = {userId:'',userName:'',userNo:'',startDate:'', endDate:''};
        let statusDic = {'-1':'全部', '0':'封禁', '1': '通过', '2': '申请'};
        let statusColorDic = {'0': 'text-gray', '1': 'text-success', '2': 'text-danger'};
        let capitalType = {'1':'铜钱', '2':'灵石', '3': '仙晶', '4': '余额'};
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
                    {data: "capitalType", title: '货币', render: function(data, type, row, setting){
                            return capitalType[data] || '-';
                        }},
                    {data: "amount", title: '金额', render: datatableUtil.renderNormal},
                    {data: "balanceBefore", title: '修改前', render: datatableUtil.renderNormal},
                    {data: "balanceAfter", title: '修改后', render: datatableUtil.renderNormal},
                    {data: "sourceType", title: '来源', render: datatableUtil.renderNormal},
                    {data: "occupyBalanceBefore", title: '占用金额修改后', render: datatableUtil.renderNormal},
                    {data: "occupyBalanceAfter", title: '占用金额修改后', render: datatableUtil.renderNormal},
                    {data: "mark", title: '备注', render: datatableUtil.renderNormal},
                    {data: "createTime", title: '创建时间', render: datatableUtil.renderTime},
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
            websocketService.request('021030', searchData, function(command){
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
        me.updateRow = function(id,status){
            let _record = dataMap[id];
            let str = "审核通过：";
            if(parseInt(status) == 0) {
                str = "审核拒绝：";
            }
            if(_record) {
                alertUtil.alert({
                    html: str + '<span class="ml-1 text-success">' + _record.userId + '</span> ？',
                    icon: 'warning',
                    confirm: function() {
                        if(me.saving) return;
                        me.saving = true;
                        var submitData = {userId: _record.userId, status:status};
                        websocketService.request('021021', submitData, function(command){
                            if(command.success){
                                baseTable.ajax.reload(null, false);
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