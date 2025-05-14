<@compress single_line=true>
if(!window.CtrlFn_${menuId}){
    window.CtrlFn_${menuId} = function($scope, $rootScope, $filter, $interval,$timeout, $compile, $templateCache, datatableUtil, alertUtil, amountUtil, storageService, websocketService, toastr){
        let me = this;
        me.defaultTpl = {};
        let dataMap = {};
        let baseTable = null;
        let statusDic = {'0':'不显示', '1': '显示'};
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
                    {data: "goodNo", title: '靓号号码', render: datatableUtil.renderNormal},
                    {data: "price", title: '价格', render: datatableUtil.renderMoney},
                    {data: "number", title: '数量', render: function(data, type, row, setting){
                        return data;
                    }},
                    {data: "status", title: '显示状态', render: function(data, type, row, setting){
                        return '<span class="' + statusColorDic[data] + '">' + (statusDic[data] || '-') + '</span>';
                    }},
                    {data: "id", title: '操作', width: '160px', render: function(data, type, row, setting) {
                            var _opearHtml = '<button class="btn waves-effect waves-light btn-primary btn-sm mr-2" ng-click="${menuId}.updateRow(\'' + row.id + '\')"><i class="ti-slice"></i>修改</button>';
                            return _opearHtml;
                        }},
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
            websocketService.request('021100', searchData, function(command){
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
        me.createRow = function(id) {
            me.currentTpl = {};
            me.currentTpl._title = '添加靓号';
            me.currentTpl._errorMsg = null;
            me.currentTpl.$type = 'add';
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
            $('#${menuId}_rowModal').modal('show');
        };
        me.saveData = function () {
            if(me.saving) return;
            var submitData = {};
            $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').each(function(i, r) {
                var _name = $(r).attr('name');
                var _value = $(r).val() || null;
                submitData[_name] = _value == null ? null : $.trim(_value);
            });
            if(me.currentTpl.$type == "add") {
                if (!submitData.goodNo) {
                    toastr.warning('请输入靓号', '温馨提示');
                    return false;
                }
                if (!submitData.price || parseFloat(submitData.price) == NaN) {
                    toastr.warning('请输入正确的价格', '温馨提示');
                    return false;
                }
            }else {
                submitData.id = me.currentTpl.id;
            }
            me.saving = true;
            let codeStr = me.currentTpl.$type == 'add' ? '021101' : '021102';
            websocketService.request(codeStr, submitData, function(command){
                if(command.success){
                    baseTable.ajax.reload(null, false);
                    $('#${menuId}_rowModal').modal('hide');
                    toastr.success("提交成功");
                } else {
                    toastr.error(command.message || '提交异常', '系统提示');
                }
                me.saving = false;
                $scope.$apply();
            });
        };
        me.updateRow = function(id){
            var record = dataMap[id];
            if(record) {
                me.currentTpl = {};
                me.currentTpl.id = id;
                me.currentTpl._title = '修改靓号';
                me.currentTpl._errorMsg = null;
                me.currentTpl.$type = 'edit';
                $('#${menuId}_formEdit').find('input[name],select[name],textarea[name]').val('').trigger('change');
                for(var key in record) {
                    $('#${menuId}_formEdit').find('input[name="' + key + '"],select[name="' + key + '"],textarea[name="' + key + '"]').val(record[key]).trigger('change');
                }
                $('#${menuId}_rowModal').modal('show');
            }
        };
        function initPage() {
            initTable();
        };
        initPage();
    };
    controllerProvider.register('Controller-${menuId}', window.CtrlFn_${menuId});
}
</@compress>