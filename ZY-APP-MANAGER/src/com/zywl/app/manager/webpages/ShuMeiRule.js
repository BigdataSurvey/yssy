<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.defaultTpl = {};
        let dataMap = {};
        let baseTable = null;
        let statusDic = {'0':'未启用', '1': '启用'};
        let statusColorDic = {'0': 'text-gray', '1': 'text-success'};
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
                        {data: "models", title: '模型代码', render: datatableUtil.renderNormal},
                        {data: "status", title: '启用状态', render: function(data, type, row, setting){
                            return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                        }},
                        {data: "name", title: '名称', render: datatableUtil.renderNormal},
                        {data: "remark", title: '备注', render: datatableUtil.renderNormal},
                        {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                            let str = parseInt(row.status) == 1 ? "禁用" : "启用";
                            let _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>'+str+'</button>';
                            return _opearHtml;
                        }},
                        {data: "context", title: '内容', render: datatableUtil.renderNormal},
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
            websocketService.request('021150', searchData, function(command){
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
        me.updateRow = function(id){
            if(me.loading) return;
            me.loading = true;

            var record = dataMap[id];
            if (!record) {
                return;
            }
            let reqData = {
                id: record.id,
                status: record.status
            };
            websocketService.request('021151', reqData, function(command){
                if(command.success){
                    baseTable.ajax.reload(null, false);
                } else {
                    toastr.error(command.message || '查询异常', '系统提示');
                }
                toastr.success('更改成功', '系统提示');
                me.loading = false;
                $scope.$apply()
            });
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
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>